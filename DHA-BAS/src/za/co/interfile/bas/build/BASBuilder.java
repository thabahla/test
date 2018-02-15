package za.co.interfile.bas.build;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.interfile.bas.Task;
import za.co.interfile.bas.bean.*;
import za.co.interfile.bas.dao.BASReconDAO;
import za.co.interfile.bas.exception.BASTransportException;
import za.co.interfile.bas.util.Closeables;

public class BASBuilder implements Task {
    private static final DateFormat BAS_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");
    private static final DateFormat BAS_TIME_FORMAT = new SimpleDateFormat("HHmmss");

    private final Logger logger = LoggerFactory.getLogger(BASBuilder.class);
    private final Provider<Connection> connectionProvider;
    private final BASReconDAO reconDAO;
    private final Properties facilities;
    private final String lastYearEnd;
    private final String workDirectory;
    private final String outputDirectory;

    private int lineCount;
    private int receiptTotal;
    private Double hashTotals = new Double(0.0);
    private Iterator<String> depositIterator;
    Set<Long> processedDeposits;
    private String basFileName = "";
    int facilityNumber = 0;

    @Inject
    public BASBuilder(Provider<Connection> connectionProvider, BASReconDAO reconDAO,
        @Named("facilities") Properties facilities,
        @Named("build.last.year.end") String lastYearEnd,
        @Named("dir.work") String workDirectory,
        @Named("dir.out") String outputDirectory) {
        this.connectionProvider = connectionProvider;
        this.reconDAO = reconDAO;
        this.facilities = facilities;
        this.lastYearEnd = lastYearEnd;
        this.workDirectory = workDirectory;
        this.outputDirectory = outputDirectory;
    }

    public void run() {
        this.logger.info("Running");

        Connection connection = null;

        try {
            connection = this.connectionProvider.get();

            List<Recon> confirmedRecons = this.reconDAO.getConfirmedReconsSince(this.lastYearEnd);

            if (confirmedRecons.isEmpty()) {
                this.logger.info("There are either no 'CONFIRMED' IRE recons or they do not have 'CONFIRMED' deposits!");
            }
            else {
                this.logger.info("There are {} 'CONFIRMED' recons to process", confirmedRecons.size());

                for (Recon recon : confirmedRecons) {
                    this.logger.info("Summarising [{}]", recon);
                    
                    if (recon.getFacilityID() == 304897) {
                    	String test = "";
                    }
                    
                    InvoiceDepositBalance invoiceDepositBalance = this.reconDAO.reconBalances(recon);
                    
                    if (invoiceDepositBalance.balances()) {
                        this.logger.info("Generating file [{}]", recon);

                        String result = this.createBASFile(invoiceDepositBalance);

                        if (result == null) {
                            this.logger.error("BAS file not created [{}]", recon);
                            // TODO: Should we update the DB so that this recon is not processed again?
                            // TODO: Or do we hope the next attempt will go better?
                        }
                        else if (result.equalsIgnoreCase("SURPLUS")) {
                            this.logger.error("Surplus encountered; BAS file not created [{}]", recon);
                            this.reconDAO.updateReconStatus(connection, recon.getReconID(), "BAS SURPLUS");
                        }
                        else {
                            this.logger.info("BAS file created [{}]", recon);
                            this.reconDAO.updateReconStatus(connection, recon.getReconID(), "BAS CREATED");
                        }
                    }
                    else {
                        this.reconDAO.updateReconStatus(connection, recon.getReconID(), "BAS UNBALANCED");
                    }
                    this.validateManualPaymentDates(recon);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error in BAS batch run", e);
        }
        finally {
            Closeables.close(connection);
        }
        logger.info("BAS batch run completed without major errors");
    }

    public void requestShutdown() {
    }

    private void validateManualPaymentDates(Recon recon) throws SQLException {
        this.logger.debug("Validating manual payment dates");

        long backdated = this.reconDAO.findManualPaymentsBackdated(recon.getFacilityID(), recon.getReconDate());

        if (backdated > 0L) {
            this.logger.warn("Backdated manual payments detected [amount={}] {}", backdated, recon);
        }

        long diffDated = this.reconDAO.findManualPaymentsDifferentDated(recon.getFacilityID(), recon.getReconDate());

        if (diffDated > 0L) {
            this.logger.warn("Different dated manual payments detected [amount={}] {}", diffDated, recon);
        }
    }

    public String createBASFile(InvoiceDepositBalance invoiceDepositBalance) throws Exception {
        Connection connection = null;

        try {
            connection = this.connectionProvider.get();

            String fileName = this.generateFileName(invoiceDepositBalance.getReconDate(), invoiceDepositBalance.getFacility());
            File basFile = new File(this.workDirectory + File.separatorChar + this.outputDirectory, fileName);
            logger.debug("FileName: " + fileName + "\nbasFile CanonicalPath: " + basFile.getCanonicalPath());
            TCBASInterfaceFile bas = this.reconDAO.mergeBASFile(System.currentTimeMillis(), 1, fileName, basFile.getCanonicalPath());

            if (bas != null) {
                this.basFileName = fileName;

                String basFileContent = this.assembleBASFile(connection, invoiceDepositBalance, bas.getSerialNumber(), false);

                if (basFileContent.equalsIgnoreCase("SURPLUS")) {
                    return basFileContent;
                }
                else if (!basFileContent.isEmpty()) {
                    try {
                        Files.createParentDirs(basFile);
                        File basFileTmp = new File(basFile.getCanonicalPath() + ".tmp");
                        this.logger.debug("Writing BAS content to file [{}]", basFileTmp.getName());
                        Files.write(basFileContent.getBytes(), basFileTmp);
                        this.logger.debug("Renaming BAS file [from={},to={}]", basFileTmp, basFile);
                        Files.move(basFileTmp.getCanonicalFile(), basFile);
                    }
                    catch (IOException ioe) {
                        this.logger.error("Failure during BAS file creation, writing or renaming", ioe);
                    }
                }
                return basFileContent;
            }
        }
        finally {
            Closeables.close(connection);
        }
        return null;
    }

    private String generateFileName(String reconDate, long facility) throws Exception {
        String fac = reconDAO.getNameForFacility(facility); 
        String date = reconDate.substring(2, reconDate.length()).replaceAll("-", "");

        return "BO" + fac + date + ".txt";
    }

    public String assembleBASFile(Connection connection, InvoiceDepositBalance invoiceDepositBalance, String serialNumber, boolean resend) throws Exception {
        String body = this.assembleInterfaceBody(invoiceDepositBalance);

        if (body.equalsIgnoreCase("SURPLUS")) {
            return body;
        }
        else if (!body.isEmpty()) {
            this.reconDAO.joinDepositsWithBASFile(this.processedDeposits, this.basFileName);
            this.reconDAO.updateDeposits(invoiceDepositBalance.getDayEnds(), this.processedDeposits, "BAS Created");
            return new StringBuilder(this.assembleInterfaceHeader(serialNumber, resend))
                .append(body)
                .append(this.assembleInterfaceTrailer())
                .toString();
        }
        return "";
    }

    private String assembleInterfaceHeader(String serialNumber, boolean resend) {
        Date now = new Date();
        StringBuilder header = new StringBuilder();

        header.append("CRIRE  ");
        header.append(BAS_DATE_FORMAT.format(now));
        header.append(BAS_TIME_FORMAT.format(now));
        header.append(serialNumber);

        if (resend) {
            header.append("  RESEND");
        }

        header.append(Strings.repeat(" ", 500 - header.length()));
        header.append("\r\n");
        return header.toString();
    }

    private String assembleInterfaceBody(InvoiceDepositBalance invoiceDepositBalance) throws BASTransportException {
        String reciepts = "";
        String reference = "";
        int facilityId = 0;
        String paymentType = "";

        Connection connection = null;
        PreparedStatement depositStatement = null;
        PreparedStatement receiptStatement = null;
        ResultSet depositDetails = null;
        ResultSet receiptDetails = null;

        HashMap<String, List<LineItem>> receiptLineItemsHmap = new HashMap<String, List<LineItem>>();
        Map<String, List<String>> deposit_receiptList_Hmap = new HashMap<String, List<String>>();

        String depositId = "";
        try {
            this.processedDeposits = Sets.newHashSet();

            int depositTotal = 0;

            List<ConfirmedDeposit> confirmedDeposits = this.reconDAO.getConfirmedDeposits(invoiceDepositBalance.getDeposits());

            if (confirmedDeposits.isEmpty()) {
                logger.warn("There are no 'CONFIRMED' deposits");
            }

            receiptLineItemsHmap = new HashMap<String, List<LineItem>>();
            deposit_receiptList_Hmap = new HashMap<String, List<String>>();
            logger.warn("What's the use? depositTotal is always 0.0!? (" + depositTotal + ")");
            boolean processed = this.reconDAO.getPaymentsSummarized4(invoiceDepositBalance.getInvoiceTransactions(), receiptLineItemsHmap, confirmedDeposits, deposit_receiptList_Hmap, depositTotal);
            logger.debug("this.basInfoDAO.getPaymentsSummarized4() done");

            if (processed) {
                this.depositIterator = deposit_receiptList_Hmap.keySet().iterator();
                connection = this.connectionProvider.get();

                while (this.depositIterator.hasNext()) {
                    depositId = this.depositIterator.next();
                    List<String> receiptList = deposit_receiptList_Hmap.get(depositId);

                    this.processedDeposits.add(Long.valueOf(depositId));
                    depositStatement = connection.prepareStatement("select EDITEDDATETIME, DEPOSITORREFERENCE from TCBANKDEPOSITS where PFKDEPOSITTRANSACTIONID = ?");
                    depositStatement.setInt(1, Integer.valueOf(depositId));
                    depositDetails = depositStatement.executeQuery();
                    depositDetails.next();
                    Date depositConfirmDate = depositDetails.getDate("EDITEDDATETIME");
                    String depositReference = depositDetails.getString(2);
                    for (int i = 0; i < receiptList.size(); i++) {
                        String receiptNumber = (String) receiptList.get(i);

                        receiptStatement = connection.prepareStatement("select RECEIPTAMOUNT, FACILITYID, LOCATIONLINK, REGIONINDICATOR"
                            + " from BASRECEIPTS"
                            + " where RECEIPTNUMBER = ?");
                        receiptStatement.setInt(1, Integer.valueOf(receiptNumber));
                        receiptDetails = receiptStatement.executeQuery();
                        receiptDetails.next();

                        this.receiptTotal += 1;

                        String receiptHeader = new String();
                        facilityId = receiptDetails.getInt(2);
                        this.facilityNumber = facilityId;
                        String lineNumber = "00";
                        receiptHeader = receiptHeader + lineNumber;
                        String tractionType = "CR001 ";
                        receiptHeader = receiptHeader + tractionType;
                        String depositNumber = padWithZeros(depositReference, 8);
                        receiptHeader = receiptHeader + depositNumber;

                        receiptHeader = receiptHeader + padWithZeros(receiptNumber, 8);
                        receiptHeader = receiptHeader + BAS_DATE_FORMAT.format(depositConfirmDate);

                        String locationLink = receiptDetails.getString(3);
                        receiptHeader = receiptHeader + locationLink;

                        String regionalIndicator = fillUpString(receiptDetails.getString(4), 6);
                        receiptHeader = receiptHeader + regionalIndicator;
                        reference = fillUpString(depositReference, 15);
                        receiptHeader = receiptHeader + reference;

                        String lastName = fillUpString("SUMMARIZED", 32);
                        String initials = fillUpString("S", 4);
                        receiptHeader = receiptHeader + initials;
                        receiptHeader = receiptHeader + lastName;

                        String addressLine1 = fillUpString("", 32);

                        receiptHeader = receiptHeader + addressLine1;

                        String addressLine2 = fillUpString("", 32);

                        receiptHeader = receiptHeader + addressLine2;

                        String addressLine3 = fillUpString("", 32);

                        receiptHeader = receiptHeader + addressLine3;

                        String addressLine4 = fillUpString("", 32);

                        receiptHeader = receiptHeader + addressLine4;

                        String postalCode = fillNumUpString("", 4);

                        receiptHeader = receiptHeader + postalCode;

                        String spaceFiller = "";

                        int receiptHeaderLength = receiptHeader.length();

                        for (int x = 0; x < 500 - receiptHeaderLength; x++) {
                            spaceFiller = spaceFiller + " ";
                        }
                        receiptHeader = receiptHeader + spaceFiller;

                        reciepts = reciepts + receiptHeader + "\r\n";

                        this.lineCount += 1;

                        List<LineItem> lineItemList = receiptLineItemsHmap.get(receiptNumber);

                        int count = 1;
                        for (LineItem tcLIP : lineItemList) {
                            if (Double.valueOf(tcLIP.getReceiptAmount()).doubleValue() != 0.0D) {
                                String lineItem = new String();

                                Integer temp = new Integer(count);

                                String lineItemNumber = padLineNumber(temp.toString(), 2);
                                count++;
                                lineItem = lineItem + lineItemNumber;

                                lineItem = lineItem + padWithZeros(receiptNumber, 8);

                                Double tempMoney = Double.valueOf(tcLIP.getReceiptAmount());
                                if (tempMoney.doubleValue() < 1.0D) {
                                    tempMoney = Double.valueOf(tempMoney.doubleValue() * -1.0D);
                                }
                                String receiptAmount = formatMoney(tempMoney.doubleValue());
                                lineItem = lineItem + receiptAmount;

                                this.hashTotals = Double.valueOf(this.hashTotals.doubleValue() + Math.abs(Double.valueOf(tcLIP.getReceiptAmount()).doubleValue()));

                                lineItem = lineItem + BAS_DATE_FORMAT.format(tcLIP.getReceiptDate());

                                String segment0 = padWithZeros(tcLIP.getItem(), 8);
                                String segment1 = padWithZeros(tcLIP.getResponsibility(), 8);
                                String segment2 = padWithZeros(tcLIP.getObjective(), 8);
                                String segment3 = padWithZeros(tcLIP.getFund(), 8);
                                String segment4 = padWithZeros(tcLIP.getProject(), 8);
                                String segment5 = padWithZeros(tcLIP.getAsset(), 8);
                                String segment6 = padWithZeros(tcLIP.getRegion(), 8);
                                String segment7 = padWithZeros(tcLIP.getInfrastructure(), 8);
                                String segment8 = "00000000";

                                lineItem = lineItem + segment0 + segment1 + segment2 + segment3 + segment4 + segment5 + segment6 + segment7 + segment8;

                                String description = tcLIP.getItemName();

                                if (description.length() >= 32) {
                                    description = description.substring(description.length() - 32);
                                }
                                else {
                                    description = fillUpString(description, 32);
                                }
                                lineItem = lineItem + description;

                                lineItem = lineItem + fillUpString(tcLIP.getPaymentMethod(), 6);

                                lineItem = lineItem + tcLIP.getCcExpiryDate();

                                lineItem = lineItem + tcLIP.getChequeDate();

                                lineItem = lineItem + fillNumUpString("", 8);

                                lineItem = lineItem + fillUpString("", 14);

                                String ccType = tcLIP.getCcType();
                                lineItem = lineItem + fillUpString(ccType, 6);

                                String passportNumber = tcLIP.getPassportNumber();
                                lineItem = lineItem + fillUpString(passportNumber, 15);

                                lineItem = lineItem + lastName;

                                String matchFieldCode1 = "";
                                lineItem = lineItem + fillNumUpString(matchFieldCode1, 10);
                                String matchFieldCode2 = "";
                                lineItem = lineItem + fillNumUpString(matchFieldCode2, 10);
                                String matchFieldCode3 = "";
                                lineItem = lineItem + fillNumUpString(matchFieldCode3, 10);
                                String matchFieldCode4 = "";
                                lineItem = lineItem + fillNumUpString(matchFieldCode4, 10);
                                String matchFieldCode5 = "";
                                lineItem = lineItem + fillNumUpString(matchFieldCode5, 10);

                                String matchFieldValue1 = "";
                                lineItem = lineItem + fillUpString(matchFieldValue1, 32);
                                String matchFieldValue2 = "";
                                lineItem = lineItem + fillUpString(matchFieldValue2, 32);
                                String matchFieldValue3 = "";
                                lineItem = lineItem + fillUpString(matchFieldValue3, 32);
                                String matchFieldValue4 = "";
                                lineItem = lineItem + fillUpString(matchFieldValue4, 32);
                                String matchFieldValue5 = "";
                                lineItem = lineItem + fillUpString(matchFieldValue5, 32);

                                String lineSpaceFiller = "";

                                for (int x = 0; x < 55; x++) {
                                    lineSpaceFiller = lineSpaceFiller + " ";
                                }

                                lineItem = lineItem + lineSpaceFiller;
                                reciepts = reciepts + lineItem + "\r\n";
                                
                                System.out.println("***TASH -> lineItem: " + lineItem);
                                this.lineCount += 1;
                            }
                        }
                    }
                    reciepts = reciepts + assembleSubFooterSummarized(connection, this.receiptTotal, facilityId, depositId, paymentType);
                    reciepts = reciepts + assembleDepositTotals(connection, depositId, depositReference);
                    this.receiptTotal = 0;
                }
            }
            else {
                reciepts = "SURPLUS";
            }

        }
        catch (Exception e) {
            logger.error("Failure assembling summarized receipts", e);
            throw new BASTransportException(e.getMessage());
        }
        finally {
            Closeables.close(receiptDetails);
            Closeables.close(depositDetails);
            Closeables.close(receiptStatement);
            Closeables.close(depositStatement);
            Closeables.close(connection);
        }
        return reciepts;
    }

    private String assembleSubFooterSummarized(Connection connection, int numberOfreceipts, int facilityId, String depositId, String paymentType) throws Exception {
        String subFooter = "";

        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            DecimalFormat decimalFormat = new DecimalFormat("0000000000000000");
            String lineNumber = "88";

            statement = connection.prepareStatement("select d.BASLOCATIONLINKCODE, bd.BT_GRANDTOTAL"
                + " from TCBANKDEPOSITS bd, IREDAYENDS de, TCFACILITY f, IREDEPARTMENTCONFIG d"
                + " where f.FKDEPARTMENTID = d.PKDEPARTMENTID"
                + " and de.FKFACILITYENTITYID = f.PFKENTITYID"
                + " and bd.FKDAYENDID = de.PKDAYENDID"
                + " and bd.PFKDEPOSITTRANSACTIONID = ?");
            statement.setInt(1, Integer.valueOf(depositId));
            resultSet = statement.executeQuery();
            resultSet.next();

            String installLocation = fillNumUpString(resultSet.getString(1), 3);
            String totalBatchAmount = decimalFormat.format(resultSet.getDouble(2) * 100.0D);
            this.hashTotals = Double.valueOf(this.hashTotals.doubleValue() + Double.valueOf(resultSet.getDouble(2)).doubleValue());

            String totalNumberOfReciepts = Integer.valueOf(numberOfreceipts).toString();

            String spaceFiller = "";
            for (int i = 0; i < 477; i++) {
                spaceFiller = spaceFiller + " ";
            }

            subFooter = lineNumber + installLocation + totalBatchAmount + padWithZeros(totalNumberOfReciepts, 2) + spaceFiller + "\r\n";
            this.lineCount += 1;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
        }
        return subFooter;
    }

    private String assembleDepositTotals(Connection connection, String depositId, String depositReference) throws Exception {
        String depositTotals = "";

        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            String depositNumber = "";
            String totalCashAmount = "";
            String totalChequeAmount = "";
            String totalTravelersChequeAmount = "";
            String totalPostalOrderAmount = "";
            String totalMoneyOrderAmount = "";
            String totalOtherAmount = "";
            String transactionType = "CR005 ";

            String lineNumber = "99";

            statement = connection.prepareStatement("select d.BASLOCATIONLINKCODE, bd.BT_CASH, bd.BT_CHEQUES, bd.BT_POSTALORDERS"
                + " from TCBANKDEPOSITS bd, IREDAYENDS de, TCFACILITY f, IREDEPARTMENTCONFIG d"
                + " where f.FKDEPARTMENTID = d.PKDEPARTMENTID"
                + " and de.FKFACILITYENTITYID = f.PFKENTITYID"
                + " and bd.FKDAYENDID = de.PKDAYENDID"
                + " and bd.PFKDEPOSITTRANSACTIONID = ?");
            statement.setInt(1, Integer.valueOf(depositId));
            resultSet = statement.executeQuery();
            resultSet.next();

            String installLocation = resultSet.getString(1);

            String valueFormat = "0000000000000000";

            DecimalFormat decimalFormat = new DecimalFormat(valueFormat);

            depositNumber = padWithZeros(depositReference, 8);
            totalCashAmount = decimalFormat.format(resultSet.getDouble(2) * 100.0D);
            totalChequeAmount = decimalFormat.format(resultSet.getDouble(3) * 100.0D);
            totalTravelersChequeAmount = decimalFormat.format(0.0D);
            totalPostalOrderAmount = decimalFormat.format(resultSet.getDouble(4) * 100.0D);
            totalMoneyOrderAmount = decimalFormat.format(0.0D);
            totalOtherAmount = decimalFormat.format(0.0D);

            this.hashTotals = Double.valueOf(this.hashTotals.doubleValue()
                + new Double(totalCashAmount).doubleValue() / 100.0D
                + new Double(totalChequeAmount).doubleValue() / 100.0D
                + new Double(totalTravelersChequeAmount).doubleValue() / 100.0D
                + new Double(totalPostalOrderAmount).doubleValue() / 100.0D
                + new Double(totalMoneyOrderAmount).doubleValue() / 100.0D
                + new Double(totalOtherAmount).doubleValue() / 100.0D);
            String spaceFiller = "";

            for (int i = 0; i < 385; i++) {
                spaceFiller = spaceFiller + " ";
            }

            depositTotals = lineNumber + installLocation + depositNumber + totalCashAmount + totalChequeAmount
                + totalTravelersChequeAmount + totalPostalOrderAmount + totalMoneyOrderAmount + totalOtherAmount + transactionType + spaceFiller + "\r\n";
            this.lineCount += 1;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
        }
        return depositTotals;
    }

    private String assembleInterfaceTrailer() {
        StringBuilder trailer = new StringBuilder();

        trailer.append("TRAILER");
        trailer.append(String.format("%08d", this.lineCount));
        trailer.append(String.format("%016d", Math.round(this.hashTotals.doubleValue() * 100)));
        trailer.append(Strings.repeat(" ", 469));

        this.lineCount = 0;
        this.hashTotals = Double.valueOf(0.0D);
        return trailer.toString();
    }

    private String formatMoney(double money) {
        Double temp = new Double(money);
        String start = temp.toString();
        int index = start.indexOf(".");

        if (start.substring(index + 1).length() > 2) {
            BigDecimal dec = new BigDecimal(temp.doubleValue());
            dec = dec.setScale(2, RoundingMode.HALF_UP);
            start = Double.valueOf(dec.doubleValue()).toString();
        }
        String cents = start.substring(index + 1, start.length());
        if (cents.length() == 1) {
            cents = cents + "0";
        }
        cents = padLineNumber(cents, 2);
        String rands = padLineNumber(start.substring(0, index), 14);
        return rands + cents;
    }

    /**
     * @depracated Use {@linkplain Strings#padEnd(java.lang.String, int, char)} instead
     */
    private String padLineNumber(String count, int i) {
        for (int j = count.length(); j < i; j++) {
            count = "0" + count;
        }
        return count;
    }

    /**
     * @depracated Use {@linkplain Strings#padEnd(java.lang.String, int, char)} instead
     */
    private String fillUpString(String old, int number) {
        if (old.length() > number) {
            old = old.substring(0, number);
        }

        for (int i = old.length(); i < number; i++) {
            old = old + " ";
        }
        return old;
    }

    /**
     * @depracated Use {@linkplain Strings#padEnd(java.lang.String, int, char)} instead
     */
    private String fillNumUpString(String old, int number) {
        if (old.length() > number) {
            old = old.substring(0, number);
        }

        for (int i = old.length(); i < number; i++) {
            old = old + "0";
        }
        return old;
    }

    /**
     * @depracated Use {@linkplain Strings#padEnd(java.lang.String, int, char)} instead
     */
    private String padWithZeros(String old, int number) {
        if (old.length() > number) {
            old = old.substring(0, number);
        }

        String pad = "";
        for (int i = old.length(); i < number; i++) {
            pad = pad + "0";
        }
        return pad + old;
    }
}
