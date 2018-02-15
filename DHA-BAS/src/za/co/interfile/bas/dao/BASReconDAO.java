package za.co.interfile.bas.dao;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.*;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.driver.OracleSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.interfile.bas.bean.*;
import za.co.interfile.bas.exception.BASTransportException;
import za.co.interfile.bas.util.Closeables;
import za.co.interfile.bas.util.Worker;

public class BASReconDAO {
    private static final int DEF_FETCH_SIZE = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(BASReconDAO.class);

    private final Provider<Connection> connectionProvider;
    private final BASIDDAO idDAO;

    @Inject
    public BASReconDAO(Provider<Connection> connectionProvider, BASIDDAO idDAO) {
        this.connectionProvider = connectionProvider;
        this.idDAO = idDAO;
    }

    public List<TcBankDeposits> getDeposits(String status) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<TcBankDeposits> bankDeposits = new ArrayList<TcBankDeposits>();

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select *"
            + " from TCBANKDEPOSITS d"
            + " inner join LTDEPOSITSTATUSES l on (d.FKDEPOSITSTATUSID = l.PKDEPOSITSTATUSID)"
            + " where l.DEPOSITSTATUS = ?"
            + " order by d.PFKDEPOSITTRANSACTIONID");
            statement.setString(1, status);
            resultSet = statement.executeQuery();
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            while (resultSet.next()) {
                TcBankDeposits tcBD = new TcBankDeposits();

                // FIXME How do we know the columns are retrieved in the order we expect them? The SQL query selects *
                tcBD.setPfkDepositTransactionId(resultSet.getInt(1));
                tcBD.setFkDepositStatusId(resultSet.getInt(2));
                tcBD.setFkEntityDepositorId(resultSet.getInt(3));
                tcBD.setDepositorReference(resultSet.getString(4));
                tcBD.setDepositDateTime(resultSet.getDate(5).toString());
                tcBD.setFkBankAccountId(resultSet.getInt(6));
                tcBD.setSubTotalCash(resultSet.getDouble(7));
                tcBD.setSubTotalCheque(resultSet.getDouble(8));
                tcBD.setSubTotalMoneyOrder(resultSet.getDouble(9));
                tcBD.setSubTotalOther(resultSet.getDouble(10));
                tcBD.setSubTotalPostalOrder(resultSet.getDouble(11));
                tcBD.setSubTotalTravCheques(resultSet.getDouble(12));
                tcBD.setGrandTotal(resultSet.getDouble(13));
                tcBD.setFkEditedByUserId(resultSet.getInt(14));
                tcBD.setEditedDateTime(resultSet.getDate(15).toString());
                tcBD.setFkDepositTypeId(resultSet.getInt(16));
                bankDeposits.add(tcBD);
            }
        }
        catch (Exception e) {
            LOGGER.error("Failure retrieving bank deposits; returning what could be retrieved", e);
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return bankDeposits;
    }

    public static Set<Long> getDayEnds(Connection connection, long pkID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement("select bank.FKDAYENDID"
                + " from TCBANKDEPOSITS bank, JOINBANKDEPBASFILE j, TCBASINTERFACEFILES bas"
                + " where j.PFKDEPOSITTRANSACTIONID = bank.PFKDEPOSITTRANSACTIONID"
                + " and bas.PKID = j.PFKBASFILEID"
                + " and bas.PKID = ?");
            statement.setLong(1, pkID);
            resultSet = statement.executeQuery();
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            Set<Long> dayEnds = Sets.newHashSet();

            while (resultSet.next()) {
                dayEnds.add(resultSet.getLong("FKDAYENDID"));
            }
            return dayEnds;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
        }
    }

    public List<ConfirmedDeposit> getConfirmedDeposits(Set<Long> deposits) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
        	StringBuffer sql = new StringBuffer("SELECT bd.PFKDEPOSITTRANSACTIONID, bd.FKDEPOSITSTATUSID, bd.BT_CASH, bd.BT_CHEQUES, bd.BT_POSTALORDERS, bd.FkEditedByUserId, ica.FkFacilityId ");
        	sql.append("from TCBANKDEPOSITS bd ");
        	sql.append("inner join irecashierassignment ica ON ica.fkcashieridassignee = bd.FkEditedByUserId ");
        	sql.append("where PFKDEPOSITTRANSACTIONID in (").append(Joiner.on(", ").join(deposits)).append(")");
        	
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql.toString());
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            List<ConfirmedDeposit> confirmedDeposits = new ArrayList<ConfirmedDeposit>();
            List<Long> unconfirmedDeposits = new ArrayList<Long>();

            while (resultSet.next()) {
                long depositID = resultSet.getLong(1);

                if (resultSet.getInt("FKDEPOSITSTATUSID") == 2) { // CONFIRMED
                    ConfirmedDeposit confirmedDeposit = new ConfirmedDeposit();
                    confirmedDeposit.setFacilityId(resultSet.getLong("FkFacilityId"));
                    confirmedDeposit.setDepositID(depositID);
                    confirmedDeposit.setCashTotal(resultSet.getDouble("BT_CASH"));
                    confirmedDeposit.setChequeTotal(resultSet.getDouble("BT_CHEQUES"));
                    confirmedDeposit.setPostalOrderTotal(resultSet.getDouble("BT_POSTALORDERS"));
                    confirmedDeposits.add(confirmedDeposit);
                }
                else {
                    unconfirmedDeposits.add(depositID);
                }
            }

            if (!unconfirmedDeposits.isEmpty()) {
                LOGGER.warn("Transaction IDs of not 'CONFIRMED' deposits " + unconfirmedDeposits);
            }

            // TODO Alternatively, implement the ConfirmedDeposit.toString() method
            LOGGER.info("Transaction IDs of 'CONFIRMED' deposits {}", confirmedDeposits);
            return confirmedDeposits;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    // UNUSED BASInfoDAO.getConfirmedDeposits(long DAYENDID)
    public List<ConfirmedDeposit> getConfirmedDeposits(long dayEndID) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PFKDEPOSITTRANSACTIONID, FKDEPOSITSTATUSID, BT_CASH, BT_CHEQUES,"
                + "     BT_POSTALORDERS"
                + " from TCBANKDEPOSITS"
                + " where FKDAYENDID = ?");
            statement.setLong(1, dayEndID);
            resultSet = statement.executeQuery();
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            List<ConfirmedDeposit> confirmedDeposits = new ArrayList<ConfirmedDeposit>();
            List<Long> unconfirmedDeposits = new ArrayList<Long>();

            while (resultSet.next()) {
                long depositID = resultSet.getLong(1);

                if (resultSet.getInt(2) == 2) {   // CONFIRMED
                    ConfirmedDeposit confirmedDeposit = new ConfirmedDeposit();

                    confirmedDeposit.setDepositID(depositID);
                    confirmedDeposit.setCashTotal(resultSet.getDouble(3));
                    confirmedDeposit.setChequeTotal(resultSet.getDouble(4));
                    confirmedDeposit.setPostalOrderTotal(resultSet.getDouble(5));
                    confirmedDeposits.add(confirmedDeposit);
                }
                else {
                    unconfirmedDeposits.add(depositID);
                }
            }

            if (!unconfirmedDeposits.isEmpty()) {
                LOGGER.warn("Transaction IDs of not 'CONFIRMED' deposits [dayEndID={}] {}", dayEndID, unconfirmedDeposits);
                throw new Exception("Transaction IDs not 'CONFIRMED'");
            }

            LOGGER.info("Transaction IDs of 'CONFIRMED' deposits {}", confirmedDeposits);
            return confirmedDeposits;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    // FIXME More hectic SQL! Can we simplify this?
    public List<TcPaymentTransactions> getPayments(String depositorReference) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<TcPaymentTransactions> payments = new ArrayList<TcPaymentTransactions>();

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select lc.PAYMENTCHANNEL, po.PODATE, po.POSTALORDERNUMBER, ch.CHEQUENUMBER,"
                + "     ch.CHEQUEDATE, ch.DRAWERNAME, p.PAYMENTAMOUNT, p.PFKPAYMENTTRANSACTIONID, p.FKPAYMENTCHANNELID,"
                + "     p.PAYMENTREFERENCE, p.FKEDITEDBYUSERID, p.EDITEDDATETIME, p.FKPAYMENTSYSTEMID, p.FKSHIFTID,"
                + "     p.FKPAYMENTMETHODID, p.FKPAYMENTSTATUSID, p.CASHTENDERED, p.CASHCHANGE, p.FKCASHIERDEPOSITID,"
                + "     d.FKFACILITYENTITYID, lt.PAYMENTMETHOD, bd.PFKDEPOSITTRANSACTIONID, bd.DEPOSITORREFERENCE,"
                + "     bd.DEPOSITDATETIME, cc.CARDNUMBER, tt.ISREVERSAL, tt.ISREVERSED"
                + " from (((((((TCPAYMENTTRANSACTIONS p inner join LTPAYMENTMETHODS lt on (p.FKPAYMENTMETHODID = lt.PKPAYMENTMETHODID)"
                + "            ) inner join IRECASHIERDEPOSITS cd on (cd.PKCASHIERDEPOSITID = p.FKCASHIERDEPOSITID)"
                + "           ) inner join IREDAYENDS d on (d.PKDAYENDID = cd.FKDAYENDID)"
                + "          ) inner join TCBANKDEPOSITS bd on (bd.PFKDEPOSITTRANSACTIONID = d.FKBANKDEPOSITTXID)"
                + "         ) inner join LTPAYMENTCHANNELS lc on (p.FKPAYMENTCHANNELID = lc.PKCHANNELID)"
                + "        ) inner join TCTRANSACTIONS tt on (tt.PKTRANSACTIONID = p.PFKPAYMENTTRANSACTIONID)"
                + "       ) left outer join TCPOSTALORDERS po on (po.FKPAYMENTTRANSACTIONID = p.PFKPAYMENTTRANSACTIONID)"
                + "      ) left outer join TCCHEQUE ch on (ch.FKPAYMENTTRANSACTIONID = p.PFKPAYMENTTRANSACTIONID)"
                + "     left outer join TCCARDPAYMENT cc on (cc.FKPAYMENTTRANSACTIONID = p.PFKPAYMENTTRANSACTIONID)"
                + " where bd.DEPOSITORREFERENCE = ?");
            statement.setString(1, depositorReference);
            resultSet = statement.executeQuery();
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            while (resultSet.next()) {
                TcPaymentTransactions paymentTransactions = new TcPaymentTransactions();

                paymentTransactions.setChequeNumber(resultSet.getString(4));
                paymentTransactions.setChequeDate(resultSet.getString(5));
                paymentTransactions.setPaymentAmount(resultSet.getDouble(7));
                paymentTransactions.setPfkPaymentTransactionId(resultSet.getInt(8));
                paymentTransactions.setFkPaymentChannelId(resultSet.getInt(9));
                paymentTransactions.setPaymentReference(resultSet.getString(10));
                paymentTransactions.setFkEditedByUserId(resultSet.getInt(11));
                paymentTransactions.setEditedDateTime(resultSet.getString(12));
                paymentTransactions.setFkPaymentSystemId(resultSet.getInt(13));
                paymentTransactions.setFkShiftId(resultSet.getInt(14));
                paymentTransactions.setFkPaymentMethodId(resultSet.getInt(15));
                paymentTransactions.setFkPaymentStatusId(resultSet.getInt(16));
                paymentTransactions.setCashTendered(resultSet.getDouble(17));
                paymentTransactions.setCashChange(resultSet.getDouble(18));
                paymentTransactions.setFacilityId(resultSet.getInt(20));
                paymentTransactions.setPaymentMethod(resultSet.getString(21));
                paymentTransactions.setCcCardNumber(resultSet.getString(25));
                // FIXME The credit card number is unset!?
                LOGGER.warn("We are setting the credit card number to null?");
                paymentTransactions.setCcCardNumber(null);
                paymentTransactions.setIsReversal(resultSet.getInt(26));
                paymentTransactions.setIsReversed(resultSet.getInt(27));
                payments.add(paymentTransactions);
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return payments;
    }



    // FIXME This method is insane. It is tooooooo long. Need to simplify.
    public boolean getPaymentsSummarized4(Set<Long> invoiceTransactions, Map<String, List<LineItem>> receiptLineItemsHmap, List<ConfirmedDeposit> depositList, Map<String, List<String>> deposit_receiptList_Hmap, int depositAmount) throws Exception {
        LOGGER.debug("Getting payments summarized [depositAmount=" + depositAmount + "]");
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        ResultSet result5 = null;
        Date depositDate = null;
        LineItem lineItem1 = null;
        LineItem lineItem2 = null;
        LineItem lineItem3 = null;
        LineItem tempLineItem = null;
        long receiptNumber = 0L;
        double receiptAmount = 0.0D;
        double cumulativeLineItemAmount = 0.0D;
        double paymentMethodTotal = 0.0D;
        double surplus = 0.0D;

        String depositId = "";

        List<String> receiptList = null;

        try {
            connection = this.connectionProvider.get();
            Statement statement3 = connection.createStatement();
            Statement statement4 = connection.createStatement();
            Statement statement5 = connection.createStatement();

            boolean ind_Deficit = false;

            Map<String, Double> cashDeposits = new HashMap<String, Double>();
            // FIXME Why are we only interested in the total cash deposits, i.e. payment method = 1?
            double cashDepositsTotal = BASReconDAO.getCashDepositsTotal(cashDeposits, depositList);
            Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // FIXME Why do we need a scroll sensitive and concurrent updateable result set? ^^^^
            ResultSet result2 = statement2.executeQuery("select it.ITEMCODE, sum(li.AMOUNT) AS AMOUNT, item.ACCOUNTNUMBER as ITEMACC,"
                + "     resp.ACCOUNTNUMBER AS RESPACC, obj.ACCOUNTNUMBER AS OBJACC, fund.ACCOUNTNUMBER AS FUNDACC, proj.ACCOUNTNUMBER AS PROJACC, ass.ACCOUNTNUMBER AS ASSACC,"
                + "     reg.ACCOUNTNUMBER AS REGACC, infr.AccountNumber AS INFRACC, f.PFKENTITYID as FacilityId "
                + " from IREDAYENDS de, IRECASHIERDEPOSITS cd, IRESHIFTS sh, TCFACILITY f, TCSCOAREGION reg,"
                + "     TCSCOARESPONSIBILITIES resp, TCPAYMENTTRANSACTIONS p, TCLINEITEMPAYMENTS li, TCTRANSACTIONS tt,"
                + "     LTPAYMENTMETHODS met, TCINVOICELINEITEMS inv, TCITEMTYPES it, TCSCOASEGMENTALLOCATIONS alloc,"
                + "     TCSCOAASSETS ass, TCSCOAFUNDS fund, TCSCOAITEMS item, TCSCOAOBJECTIVES obj, TCSCOAPROJECTS proj, tcscoainfrastructure infr "
                + " where alloc.FKSCOAPROJECTID = proj.PKSCOAPROJECTID"
                + " and alloc.FKSCOAOBJECTIVEID = obj.PKSCOAOBJECTIVEID"
                + " and alloc.FKSCOAITEMID = item.PKSCOAITEMID"
                + " and alloc.FKSCOAFUNDID = fund.PKSCOAFUNDID"
                + " and alloc.FKSCOAASSETID = ass.PKSCOAASSETID"
                + " and alloc.FkScoaInfrastructureId = infr.PkScoaInfrastructureId "
                + " and it.FKSCOAALLOCATIONID = alloc.PKSCOAALLOCATIONID"
                + " and inv.FKITEMTYPEID = it.PKITEMTYPEID"
                + " and li.FKLINEITEMID = inv.PKINVOICELINEITEMID"
                + " and tt.ISREVERSED = 0"
                + " and tt.ISREVERSAL = 0"
                + " and p.FKPAYMENTMETHODID = met.PKPAYMENTMETHODID"
                + " and p.PFKPAYMENTTRANSACTIONID = tt.PKTRANSACTIONID"
                + " and li.AMOUNT > 0"
                + " and p.PFKPAYMENTTRANSACTIONID = li.FKPAYMENTTRANSACTIONID"
                + " and p.FKPAYMENTMETHODID = 1"
                + " and f.FKSCOAREGIONID = reg.PKSCOAREGIONID"
                + " and f.FKSCOARESPONSIBILITYCODE = resp.PKSCOARESPONSIBILITYID"
                + " and sh.FKENTITYFACILITYID = f.PFKENTITYID"
                + " and cd.FKSHIFTID = sh.PKSHIFTID"
                + " and cd.PKCASHIERDEPOSITID = p.FKCASHIERDEPOSITID"
                + " and de.PKDAYENDID = cd.FKDAYENDID"
                + " and li.FKPAYMENTTRANSACTIONID in (" + Joiner.on(',').join(invoiceTransactions)
                + " )"
                + " group by it.ITEMCODE, item.ACCOUNTNUMBER, resp.ACCOUNTNUMBER, obj.ACCOUNTNUMBER, fund.ACCOUNTNUMBER,"
                + "     proj.ACCOUNTNUMBER, ass.ACCOUNTNUMBER, reg.ACCOUNTNUMBER, infr.AccountNumber, f.PfkEntityId");
           
            Iterator<String> depositIterator = cashDeposits.keySet().iterator();

            if (depositIterator.hasNext()) {
                LOGGER.debug("1: depositIterator.hasNext()");
                depositId = depositIterator.next();
                paymentMethodTotal = cashDeposits.get(depositId).doubleValue();
                LOGGER.debug("1: Getting from cash deposits [depositID=" + depositId + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                LOGGER.debug("1: Constructing new receipt list");
                receiptList = new ArrayList<String>();

                ResultSet result4 = statement5.executeQuery("select DEPOSITDATETIME from TCBANKDEPOSITS where PFKDEPOSITTRANSACTIONID = " + depositId);
                result4.next();
                depositDate = result4.getDate("DEPOSITDATETIME");
            }

            List<LineItem> lineItemList = new ArrayList<LineItem>();
            receiptAmount = 0.0D;
            cumulativeLineItemAmount = 0.0D;

            while (result2.next()) {
                LineItem lineItem = new LineItem();

                // FIXME We are selecting a lot of account numbers, but setting names, receipt amounts, responsibilities, objectives, funds, projects, etc.?
                lineItem.setItemName(result2.getString("ITEMCODE"));
                lineItem.setReceiptAmount(result2.getString("AMOUNT"));
                lineItem.setItem(result2.getString("ITEMACC"));
                lineItem.setResponsibility(result2.getString("RESPACC"));
                lineItem.setObjective(result2.getString("OBJACC"));
                lineItem.setFund(result2.getString("FUNDACC"));
                lineItem.setProject(result2.getString("PROJACC"));
                lineItem.setAsset(result2.getString("ASSACC"));
                lineItem.setRegion(result2.getString("REGACC"));
                lineItem.setInfrastructure(result2.getString("INFRACC"));
                lineItem.setReceiptDate(depositDate);
                lineItem.setPaymentMethod("C");
                lineItem.setFacilityId(result2.getLong("FacilityId"));
                LOGGER.debug("1: Processing line item [" + lineItem + "]");

                receiptAmount += result2.getDouble(2);
                cumulativeLineItemAmount += result2.getDouble(2);
                lineItemList.add(lineItem);

                if ((lineItemList.size() == 50) || (cumulativeLineItemAmount >= paymentMethodTotal)) {
                    LOGGER.debug("1: lineItemList.size() == 50 || cumulativeLineItemAmount >= paymentMethodTotal");
                    if (cumulativeLineItemAmount >= paymentMethodTotal) {
                        LOGGER.debug("1: cumulativeLineItemAmount >= paymentMethodTotal");
                        if (cumulativeLineItemAmount > paymentMethodTotal) {
                            LOGGER.debug("1: cumulativeLineItemAmount > paymentMethodTotal");
                            double difference = cumulativeLineItemAmount - paymentMethodTotal;

                            LOGGER.debug("1: difference=" + difference);
                            lineItem1 = lineItemList.remove(lineItemList.size() - 1);
                            LOGGER.debug("1: Removing last LineItem storing as lineItem1 [" + lineItem1 + "]");
                            lineItem1.setReceiptAmount(String.valueOf(Double.valueOf(lineItem1.getReceiptAmount()).doubleValue() - difference));
                            LOGGER.debug("1: Setting lineItem1.receiptAmount to " + lineItem1.getReceiptAmount());
                            lineItemList.add(lineItem1);

                            lineItem2 = new LineItem();
                            lineItem2.setItemName(lineItem1.getItemName());
                            lineItem2.setReceiptAmount(String.valueOf(difference));
                            lineItem2.setItem(lineItem1.getItem());
                            lineItem2.setResponsibility(lineItem1.getResponsibility());
                            lineItem2.setObjective(lineItem1.getObjective());
                            lineItem2.setFund(lineItem1.getFund());
                            lineItem2.setProject(lineItem1.getProject());
                            lineItem2.setAsset(lineItem1.getAsset());
                            lineItem2.setRegion(lineItem1.getRegion());
                            lineItem2.setInfrastructure(lineItem1.getInfrastructure());
                            lineItem2.setReceiptDate(depositDate);
                            lineItem2.setPaymentMethod("C");
                            lineItem2.setFacilityId(lineItemList.get(0).getFacilityId());
                            LOGGER.debug("1: Constructed lineItem2 [" + lineItem2 + "]");
                        }

                        if (paymentMethodTotal > 0.0D || receiptAmount > 0.0D) {
                            LOGGER.debug("1: paymentMethodTotal > 0.0D || receiptAmount > 0.0D");
                            PreparedStatement prepStat = connection.prepareStatement("insert into"
                                + " BASRECEIPTS(RECEIPTAMOUNT, LINEITEMCOUNT, PAYMENTMETHODID, REFERENCE, DEPOSITTRANSACTIONID,"
                                + "     LOCATIONLINK, REGIONINDICATOR, FACILITYID, EDITEDDATETIME)"
                                + " VALUES(?,?,?,?,?,?,?,?,?)");
                            double choiceReceiptAmount = 0.0;

                            if (cumulativeLineItemAmount > paymentMethodTotal) {
                                LOGGER.debug("1: Setting receiptAmount to paymentMethodTotal(" + paymentMethodTotal + "), because cumulativeLineItemAmount > paymentMethodTotal");
                                choiceReceiptAmount = paymentMethodTotal;
                            }
                            else {
                                LOGGER.debug("1: Setting receiptAmount to receiptAmount(" + receiptAmount + "), because cumulativeLineItemAmount <= paymentMethodTotal");
                                choiceReceiptAmount = receiptAmount;
                            }
                            prepStat.setDouble(1, choiceReceiptAmount);
                            prepStat.setInt(2, lineItemList.size());
                            prepStat.setInt(3, 1);
                            prepStat.setString(4, depositId);
                            prepStat.setString(5, depositId);
                            prepStat.setInt(6, 423);
                            prepStat.setString(7, "DHA");
                            prepStat.setLong(8, lineItemList.get(0).getFacilityId());
                            prepStat.setString(9, Worker.getDateTime());
                            LOGGER.debug("1: Inserting into BASRECEIPTS [receiptAmount=" + choiceReceiptAmount + ",lineItemCount=" + lineItemList.size() + ",paymentMethodID=1*,reference=" + depositId + ",depositTransactionID=" + depositId + ",locationLink=423*,regionIndicator=DHA*,facilityID=39*,editedDateTime=" + Worker.getDateTime() + "]");
                            prepStat.executeUpdate();

                            ResultSet result3 = statement3.executeQuery("select max(RECEIPTNUMBER) from BASRECEIPTS");
                            result3.next();
                            receiptNumber = result3.getLong(1);
                            LOGGER.debug("1: max(RECEIPTNUMBER)=" + receiptNumber);
                            LOGGER.debug("1: Putting receiptNumber -> lineItemList {" + receiptNumber + " -> " + lineItemList + "}");
                            receiptLineItemsHmap.put(String.valueOf(receiptNumber), lineItemList);
                            LOGGER.debug("1: Adding receipt to list [receiptNumber=" + receiptNumber + "]");
                            receiptList.add(String.valueOf(receiptNumber));
                        }

                        if (cumulativeLineItemAmount > paymentMethodTotal) {
                            LOGGER.debug("1: cumulativeLineItemAmount > paymentMethodTotal");
                            LOGGER.debug("1: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                            deposit_receiptList_Hmap.put(depositId, receiptList);
                            lineItemList = new ArrayList<LineItem>();
                            lineItemList.add(lineItem2);
                            LOGGER.debug("1: Constructed new lineItemList and added [" + lineItem2 + "]");
                            receiptAmount = Double.valueOf(lineItem2.getReceiptAmount());
                            cumulativeLineItemAmount = Double.valueOf(lineItem2.getReceiptAmount());
                            paymentMethodTotal = 0.0D;
                            LOGGER.debug("1: Setting [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                        }
                        else if (cumulativeLineItemAmount == paymentMethodTotal) {
                            LOGGER.debug("1: cumulativeLineItemAmount == paymentMethodTotal");
                            LOGGER.debug("1: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                            deposit_receiptList_Hmap.put(depositId, receiptList);
                            lineItemList = new ArrayList<LineItem>();
                            LOGGER.debug("1: Constructed new lineItemList");
                            receiptAmount = 0.0D;
                            cumulativeLineItemAmount = 0.0D;
                            paymentMethodTotal = 0.0D;
                            LOGGER.debug("1: Setting [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                        }
                        else if (lineItemList.size() == 50) {
                            LOGGER.debug("1: lineItemList.size() == 50");
                            lineItemList = new ArrayList<LineItem>();
                            LOGGER.debug("1: Constructed new lineItemList");
                            receiptAmount = 0.0D;
                            cumulativeLineItemAmount = 0.0D;
                            LOGGER.debug("1: Setting [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal stays " + paymentMethodTotal + "]");
                        }

                        if (ind_Deficit && !depositIterator.hasNext()) {
                            LOGGER.debug("1: Breaking, because (ind_Deficit && !depositIterator.hasNext())");
                            break;
                        }

                        if (depositIterator.hasNext()) {
                            LOGGER.debug("2: depositIterator.hasNext()");
                            depositId = depositIterator.next();
                            paymentMethodTotal = cashDeposits.get(depositId).doubleValue();
                            LOGGER.debug("2: Getting from cash deposits [depositID=" + depositId + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                            LOGGER.debug("2: Constructing new receipt list");
                            receiptList = new ArrayList<String>();

                            ResultSet result4 = statement5.executeQuery("select DEPOSITDATETIME from TCBANKDEPOSITS where PFKDEPOSITTRANSACTIONID = " + depositId);
                            result4.next();
                            depositDate = result4.getDate("DEPOSITDATETIME");
                        }
                        else if ((cumulativeLineItemAmount != paymentMethodTotal) && (paymentMethodTotal == 0.0D)) {
                            throw new Exception("Encountered a surplus, but was unable to retrieve details for the surplus line item from the database");
                        }

                        if (!lineItemList.isEmpty()) {
                            LOGGER.debug("lineItemList is not empty");
                            PreparedStatement prepStat1 = null;
                            LineItem lineItemTemp = (LineItem) lineItemList.get(0);

                            LOGGER.debug("Getting the first entry from lineItemList [lineItemTemp=" + lineItemTemp + "]");
                            if (lineItemTemp != null) {
                                LOGGER.debug("lineItemTemp != null");

                                do {
                                    if (cumulativeLineItemAmount >= paymentMethodTotal) {
                                        if (cumulativeLineItemAmount > paymentMethodTotal) {
                                            double difference = cumulativeLineItemAmount - paymentMethodTotal;

                                            lineItem1 = (LineItem) lineItemList.get(lineItemList.size() - 1);
                                            LOGGER.debug("2: Removing last LineItem storing as lineItem1 [" + lineItem1 + "]");
                                            lineItem1.setReceiptAmount(String.valueOf(Double.valueOf(Double.valueOf(lineItem1.getReceiptAmount()) - difference)));
                                            LOGGER.debug("2: Setting lineItem1.receiptAmount to " + lineItem1.getReceiptAmount());
                                            lineItemList.add(lineItem1);

                                            lineItem2 = new LineItem();
                                            lineItem2.setItemName(lineItem1.getItemName());
                                            lineItem2.setReceiptAmount(String.valueOf(difference));
                                            lineItem2.setItem(lineItem1.getItem());
                                            lineItem2.setResponsibility(lineItem1.getResponsibility());
                                            lineItem2.setObjective(lineItem1.getObjective());
                                            lineItem2.setFund(lineItem1.getFund());
                                            lineItem2.setProject(lineItem1.getProject());
                                            lineItem2.setAsset(lineItem1.getAsset());
                                            lineItem2.setRegion(lineItem1.getRegion());
                                            lineItem2.setInfrastructure(lineItem1.getInfrastructure());
                                            lineItem2.setReceiptDate(depositDate);
                                            lineItem2.setPaymentMethod("C");
                                            lineItem2.setFacilityId(lineItemList.get(0).getFacilityId());
                                            LOGGER.debug("2: Constructed lineItem2 [" + lineItem2 + "]");
                                        }

                                        if (paymentMethodTotal > 0.0D || receiptAmount > 0.0D) {
                                            LOGGER.debug("2: paymentMethodTotal > 0.0D || receiptAmount > 0.0D");
                                            prepStat1 = connection.prepareStatement("insert into"
                                                + " BASRECEIPTS(RECEIPTAMOUNT, LINEITEMCOUNT, PAYMENTMETHODID, REFERENCE,"
                                                + "     DEPOSITTRANSACTIONID, LOCATIONLINK, REGIONINDICATOR, FACILITYID, EDITEDDATETIME)"
                                                + " VALUES(?,?,?,?,?,?,?,?,?)");
                                            double choiceReceiptAmount = 0.0;

                                            if (cumulativeLineItemAmount > paymentMethodTotal) {
                                                LOGGER.debug("2: Setting receiptAmount to paymentMethodTotal(" + paymentMethodTotal + "), because cumulativeLineItemAmount > paymentMethodTotal");
                                                choiceReceiptAmount = paymentMethodTotal;
                                            }
                                            else {
                                                LOGGER.debug("2: Setting receiptAmount to receiptAmount(" + receiptAmount + "), because cumulativeLineItemAmount <= paymentMethodTotal");
                                                choiceReceiptAmount = receiptAmount;
                                            }

                                            
                                            prepStat1.setDouble(1, choiceReceiptAmount);
                                            prepStat1.setInt(2, lineItemList.size());
                                            prepStat1.setInt(3, 1);
                                            prepStat1.setString(4, depositId);
                                            prepStat1.setString(5, depositId);
                                            prepStat1.setInt(6, 423);
                                            prepStat1.setString(7, "DHA");
                                            prepStat1.setLong(8, lineItemTemp.getFacilityId());
                                            prepStat1.setString(9, Worker.getDateTime());
                                            LOGGER.debug("2: Inserting into BASRECEIPTS [receiptAmount=" + choiceReceiptAmount + ",lineItemCount=" + lineItemList.size() + ",paymentMethodID=1*,reference=" + depositId + ",depositTransactionID=" + depositId + ",locationLink=423*,regionIndicator=DHA*,facilityID="+lineItemTemp.getFacilityId()+",editedDateTime=" + Worker.getDateTime() + "]");
                                            prepStat1.executeUpdate();

                                            ResultSet result3 = statement3.executeQuery("select max(RECEIPTNUMBER) from BASRECEIPTS");
                                            result3.next();
                                            receiptNumber = result3.getLong(1);
                                            result3.close();
                                            LOGGER.debug("2: max(RECEIPTNUMBER)=" + receiptNumber);
                                            LOGGER.debug("2: Putting receiptNumber -> lineItemList {" + receiptNumber + " -> " + lineItemList + "}");
                                            receiptLineItemsHmap.put(String.valueOf(receiptNumber), lineItemList);
                                            LOGGER.debug("2: Adding receipt to list [receiptNumber=" + receiptNumber + "]");
                                            receiptList.add(String.valueOf(receiptNumber));
                                        }

                                        if (cumulativeLineItemAmount > paymentMethodTotal) {
                                            LOGGER.debug("2: cumulativeLineItemAmount > paymentMethodTotal");
                                            LOGGER.debug("2: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                                            deposit_receiptList_Hmap.put(depositId, receiptList);
                                            lineItemList = new ArrayList<LineItem>();
                                            lineItemList.add(lineItem2);
                                            LOGGER.debug("2: Constructed new lineItemList and added [" + lineItem2 + "]");
                                            receiptAmount = Double.valueOf(lineItem2.getReceiptAmount());
                                            cumulativeLineItemAmount = Double.valueOf(lineItem2.getReceiptAmount());
                                            paymentMethodTotal = 0.0D;
                                            LOGGER.debug("2: Setting [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                                        }
                                        else {
                                            if (cumulativeLineItemAmount == paymentMethodTotal) {
                                                LOGGER.debug("2: cumulativeLineItemAmount == paymentMethodTotal");
                                                LOGGER.debug("2: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                                                deposit_receiptList_Hmap.put(depositId, receiptList);

                                                receiptAmount = 0.0D;
                                                cumulativeLineItemAmount = 0.0D;
                                                paymentMethodTotal = 0.0D;
                                                LOGGER.debug("2: Setting and breaking [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                                                break;
                                            }

                                            if (lineItemList.size() == 50) {
                                                LOGGER.debug("2: lineItemList.size() == 50");
                                                lineItemList = new ArrayList<LineItem>();
                                                LOGGER.debug("2: Constructed new lineItemList");
                                                receiptAmount = 0.0D;
                                                cumulativeLineItemAmount = 0.0D;
                                                LOGGER.debug("2: Setting [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal stays " + paymentMethodTotal + "]");
                                            }
                                        }

                                        if (ind_Deficit && !depositIterator.hasNext()) {
                                            LOGGER.debug("2: Breaking because (ind_Deficit && !depositIterator.hasNext())");
                                            break;
                                        }

                                        if (paymentMethodTotal == 0.0D) {
                                            LOGGER.warn("paymentMethodTotal == 0.0D");
                                        }

                                        if (depositIterator.hasNext()) {
                                            LOGGER.debug("3: depositIterator.hasNext()");
                                            depositId = depositIterator.next();
                                            paymentMethodTotal = cashDeposits.get(depositId).doubleValue();
                                            LOGGER.debug("3: Getting from cash deposits [depositID=" + depositId + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                                            LOGGER.debug("3: Constructing new receipt list");
                                            receiptList = new ArrayList<String>();
                                            ResultSet result4 = statement5.executeQuery("select DEPOSITDATETIME from TCBANKDEPOSITS where PFKDEPOSITTRANSACTIONID = " + depositId);
                                            result4.next();
                                            depositDate = result4.getDate("DEPOSITDATETIME");
                                            result4.close();
                                        }
                                        else if ((cumulativeLineItemAmount != paymentMethodTotal) && (paymentMethodTotal == 0.0D)) {
                                            throw new Exception("Encountered a surplus, but was unable to retrieve details for the surplus line item from the database");
                                        }

                                        prepStat1.close();
                                    }

                                    lineItemTemp = lineItemList.get(0);
                                    LOGGER.debug("Getting the first entry from lineItemList [lineItemTemp=" + lineItemTemp + "]");

                                    if (lineItemTemp == null) {
                                        LOGGER.debug("Breaking, because lineItemTemp == null");
                                        break;
                                    }
                                    LOGGER.debug("Still Double.valueOf(lineItemTemp.getReceiptAmount()) >= paymentMethodTotal ? " + (Double.valueOf(lineItemTemp.getReceiptAmount()) >= paymentMethodTotal));
                                }
                                while (Double.valueOf(lineItemTemp.getReceiptAmount()) >= paymentMethodTotal);
                            }
                        }
                    
                    
                    } else {
                    	LineItem lineItemTemp = new LineItem();
                    
                    	if (!lineItemList.isEmpty()) {
                            lineItemTemp = (LineItem) lineItemList.get(0);
                    	}
                    	
                        if (receiptAmount > 0.0D) {
                            LOGGER.debug("3: receiptAmount > 0.0");

                            PreparedStatement prepStat = connection.prepareStatement("insert into"
                                + " BASRECEIPTS(RECEIPTAMOUNT, LINEITEMCOUNT, PAYMENTMETHODID, REFERENCE, DEPOSITTRANSACTIONID, LOCATIONLINK, REGIONINDICATOR, FACILITYID, EDITEDDATETIME)"
                                + " values(?,?,?,?,?,?,?,?,?)");
                            prepStat.setDouble(1, receiptAmount);
                            prepStat.setInt(2, lineItemList.size());
                            prepStat.setInt(3, 1);
                            prepStat.setString(4, depositId);
                            prepStat.setString(5, depositId);
                            prepStat.setInt(6, 423);
                            prepStat.setString(7, "DHA");
                            prepStat.setLong(8, lineItemTemp.getFacilityId());
                            prepStat.setString(9, Worker.getDateTime());
                            LOGGER.debug("3: Inserting into BASRECEIPTS [receiptAmount=" + receiptAmount + ",lineItemCount=" + lineItemList.size() + ",paymentMethodID=1*,reference=" + depositId + ",depositTransactionID=" + depositId + ",locationLink=423*,regionIndicator=DHA*,facilityID=39*,editedDateTime=" + Worker.getDateTime() + "]");
                            prepStat.executeUpdate();

                            ResultSet result4 = statement4.executeQuery("select max(RECEIPTNUMBER) from BASRECEIPTS");
                            result4.next();
                            receiptNumber = result4.getLong(1);
                            LOGGER.debug("3: max(RECEIPTNUMBER)=" + receiptNumber);
                            LOGGER.debug("3: Putting receiptNumber -> lineItemList {" + receiptNumber + " -> " + lineItemList + "}");
                            receiptLineItemsHmap.put(String.valueOf(receiptNumber), lineItemList);
                            LOGGER.debug("3: Adding receipt to list [receiptNumber=" + receiptNumber + "]");
                            receiptList.add(String.valueOf(receiptNumber));

                            lineItemList = new ArrayList<LineItem>();
                            LOGGER.debug("3.a: Constructed new lineItemList");
                            receiptAmount = 0.0D;
                            LOGGER.debug("3.a: Setting receiptAmount=0.0");
                        }
                        lineItemList = new ArrayList<LineItem>();
                        LOGGER.debug("3.b: Constructed new lineItemList");
                        receiptAmount = 0.0D;
                        LOGGER.debug("3.b: Setting receiptAmount=0.0");
                    }
                }

                if ((cumulativeLineItemAmount < paymentMethodTotal) && result2.isLast()) {
                    LOGGER.debug("4: (cumulativeLineItemAmount < paymentMethodTotal) && result2.isLast()");
                    surplus = paymentMethodTotal - cumulativeLineItemAmount;

                    LOGGER.debug("4: Executing surplus query");
                    
                    StringBuffer sql = new StringBuffer("select item.ACCOUNTNUMBER as Item, obj.ACCOUNTNUMBER as Objective, fund.ACCOUNTNUMBER as Fund, proj.ACCOUNTNUMBER as Project, ass.ACCOUNTNUMBER as Asset, resp.AccountNumber as Responsibility, infr.AccountNumber as Infrastructure ");
                    sql.append("from TCITEMTYPES it, TCSCOASEGMENTALLOCATIONS alloc, TCSCOAASSETS ass, TCSCOAFUNDS fund, TCSCOAITEMS item, TCSCOAOBJECTIVES obj, TCSCOAPROJECTS proj, TcScoaResponsibilities resp, TcFacility f, TcScoaInfrastructure infr   ");
                    sql.append("where alloc.FKSCOAPROJECTID = proj.PKSCOAPROJECTID "); 
                    sql.append("and alloc.FKSCOAOBJECTIVEID = obj.PKSCOAOBJECTIVEID  ");
                    sql.append("and alloc.FKSCOAITEMID = item.PKSCOAITEMID  ");
                    sql.append("and alloc.FKSCOAFUNDID = fund.PKSCOAFUNDID  ");
                    sql.append("and alloc.FKSCOAASSETID = ass.PKSCOAASSETID  ");
                    sql.append("and alloc.FKSCOAInfrastructureId = infr.PKSCOAInfrastructureID ");
                    sql.append("and it.FKSCOAALLOCATIONID = alloc.PKSCOAALLOCATIONID "); 
            		sql.append("and resp.PKSCOARESPONSIBILITYID = f.FkScoaResponsibilityCode "); 
            		sql.append("and f.PfkEntityId = ").append(lineItemList.get(0).getFacilityId()).append(" ");
            		sql.append("and it.ITEMTYPENAME = 'Sub Cashier suplus Cash-up' ");
                     
                     
                    result5 = statement5.executeQuery(sql.toString());
                  
                    
                    
                    if (result5.next()) {
                        lineItem = new LineItem();
                        lineItem.setItemName("Sub Cashier surplus Cash-up");
                        double temp = Double.valueOf(surplus);

                        if (temp < 0.0D) {
                            LOGGER.warn("4: Making negative amount positive! [surplus=" + surplus + "]");
                            temp *= -1.0D;
                        }
                  
                        lineItem.setReceiptAmount(String.valueOf(temp));
                        lineItem.setItem(result5.getString("Item"));
                        lineItem.setResponsibility(result5.getString("Responsibility"));
                        lineItem.setObjective(result5.getString("Objective"));
                        lineItem.setFund(result5.getString("Fund"));
                        lineItem.setProject(result5.getString("Project"));
                        lineItem.setAsset(result5.getString("Asset"));
                        lineItem.setInfrastructure(result5.getString("Infrastructure"));
                        lineItem.setRegion("5423");
                        lineItem.setReceiptDate(depositDate);
                        lineItem.setPaymentMethod("C");
                        
                        LOGGER.debug("4: Constructed lineItem [" + lineItem + "]");

                        receiptAmount += cashDepositsTotal - depositAmount;
                        LOGGER.debug("4: Adding difference(" + (cashDepositsTotal - depositAmount) + ") of cashDepositsTotal(" + cashDepositsTotal + ") and depositAmount(" + depositAmount + ") to receiptAmount(" + receiptAmount + ")");
                        cumulativeLineItemAmount += cashDepositsTotal - depositAmount;
                        LOGGER.debug("4: Adding difference(" + (cashDepositsTotal - depositAmount) + ") of cashDepositsTotal(" + cashDepositsTotal + ") and depositAmount(" + depositAmount + ") to cumulativeLineItemAmount(" + cumulativeLineItemAmount + ")");
                        lineItemList.add(lineItem);

                        if (paymentMethodTotal > 0.0D || receiptAmount > 0.0D) {
                            LOGGER.debug("4: paymentMethodTotal > 0.0 || receiptAmount > 0.0");

                            PreparedStatement prepStat1 = connection.prepareStatement("insert into"
                                + " BASRECEIPTS(RECEIPTAMOUNT, LINEITEMCOUNT, PAYMENTMETHODID, REFERENCE, DEPOSITTRANSACTIONID, LOCATIONLINK, REGIONINDICATOR, FACILITYID, EDITEDDATETIME)"
                                + " values(?,?,?,?,?,?,?,?,?)");
                            double choiceReceiptAmount = 0.0;

                            if (cumulativeLineItemAmount > paymentMethodTotal) {
                                LOGGER.debug("4: Setting receiptAmount to paymentMethodTotal(" + paymentMethodTotal + "), because cumulativeLineItemAmount > paymentMethodTotal");
                                choiceReceiptAmount = paymentMethodTotal;
                            }
                            else {
                                LOGGER.debug("4: Setting receiptAmount to receiptAmount(" + receiptAmount + "), because cumulativeLineItemAmount <= paymentMethodTotal");
                                choiceReceiptAmount = receiptAmount;
                            }
                            prepStat1.setDouble(1, choiceReceiptAmount);
                            prepStat1.setInt(2, lineItemList.size());
                            prepStat1.setInt(3, 1);
                            prepStat1.setString(4, depositId);
                            prepStat1.setString(5, depositId);
                            prepStat1.setInt(6, 423);
                            prepStat1.setString(7, "DHA");
                            prepStat1.setLong(8, lineItemList.get(0).getFacilityId());
                            prepStat1.setString(9, Worker.getDateTime());
                            LOGGER.debug("4: Inserting into BASRECEIPTS [receiptAmount=" + choiceReceiptAmount + ",lineItemCount=" + lineItemList.size() + ",paymentMethodID=1*,reference=" + depositId + ",depositTransactionID=" + depositId + ",locationLink=423*,regionIndicator=DHA*,facilityID=39*,editedDateTime=" + Worker.getDateTime() + "]");
                            prepStat1.executeUpdate();

                            ResultSet result3 = statement3.executeQuery("select max(RECEIPTNUMBER) from BASRECEIPTS");
                            result3.next();
                            receiptNumber = result3.getLong(1);
                            result3.close();
                            LOGGER.debug("4: max(RECEIPTNUMBER)=" + receiptNumber);
                            LOGGER.debug("4: Putting receiptNumber -> lineItemList {" + receiptNumber + " -> " + lineItemList + "}");
                            receiptLineItemsHmap.put(String.valueOf(receiptNumber), lineItemList);
                            LOGGER.debug("4: Adding receipt to list [receiptNumber=" + receiptNumber + "]");
                            receiptList.add(String.valueOf(receiptNumber));
                        }

                        LOGGER.debug("4: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                        deposit_receiptList_Hmap.put(depositId, receiptList);
                        lineItemList = new ArrayList<LineItem>();
                        LOGGER.debug("4: Constructed new lineItemList");
                        receiptAmount = 0.0D;
                        cumulativeLineItemAmount = 0.0D;
                        paymentMethodTotal = 0.0D;
                        LOGGER.debug("4: Setting [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                    }
                    else {
                        throw new Exception("Encountered a surplus in day end, but was unable to retrieve details for the surplus line item from the database");
                    }
                }

            }

            if (depositIterator.hasNext()) {
                LOGGER.debug("5: depositIterator.hasNext()");
                depositId = depositIterator.next();
                paymentMethodTotal = cashDeposits.get(depositId);
                LOGGER.debug("5: Getting from cash deposits [depositID=" + depositId + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                LOGGER.debug("5: Constructing new receipt list");
                receiptList = new ArrayList<String>();
                ResultSet result4 = statement5.executeQuery("select DEPOSITDATETIME from TCBANKDEPOSITS where PFKDEPOSITTRANSACTIONID = " + depositId);
                result4.next();
                depositDate = result4.getDate("DEPOSITDATETIME");
                result4.close();

                if (paymentMethodTotal > cumulativeLineItemAmount) {
                    LOGGER.debug("5: paymentMethodTotal > cumulativeLineItemAmount");
                    surplus = paymentMethodTotal - cumulativeLineItemAmount;

                    LOGGER.debug("5: Executing query with spelling mistake/typo");
                    
                    StringBuffer sql = new StringBuffer("select item.ACCOUNTNUMBER as Item, obj.ACCOUNTNUMBER as Objective, fund.ACCOUNTNUMBER as Fund, proj.ACCOUNTNUMBER as Project, ass.ACCOUNTNUMBER as Asset, resp.AccountNumber as Responsibility, infr.AccountNumber as Infrastructure ");
                    sql.append("from TCITEMTYPES it, TCSCOASEGMENTALLOCATIONS alloc, TCSCOAASSETS ass, TCSCOAFUNDS fund, TCSCOAITEMS item, TCSCOAOBJECTIVES obj, TCSCOAPROJECTS proj, TcScoaResponsibilities resp, TcFacility f, TcScoaInfrastructure infr   ");
                    sql.append("where alloc.FKSCOAPROJECTID = proj.PKSCOAPROJECTID "); 
                    sql.append("and alloc.FKSCOAOBJECTIVEID = obj.PKSCOAOBJECTIVEID  ");
                    sql.append("and alloc.FKSCOAITEMID = item.PKSCOAITEMID  ");
                    sql.append("and alloc.FKSCOAFUNDID = fund.PKSCOAFUNDID  ");
                    sql.append("and alloc.FKSCOAASSETID = ass.PKSCOAASSETID  ");
                    sql.append("and alloc.FKSCOAInfrastructureId = infr.PKSCOAInfrastructureID ");
                    sql.append("and it.FKSCOAALLOCATIONID = alloc.PKSCOAALLOCATIONID "); 
            		sql.append("and resp.PKSCOARESPONSIBILITYID = f.FkScoaResponsibilityCode "); 
            		sql.append("and f.PfkEntityId = ").append(lineItemList.get(0).getFacilityId()).append(" ");
            		sql.append("and it.ITEMTYPENAME = 'Sub Cashier suplus Cash-up' ");
            		
                    result5 = statement5.executeQuery(sql.toString());
                  

                    if (result5.next()) {
                        LOGGER.debug("5: Doubtful whether this will happen due to spelling mistake/typo");
                        LineItem lineItem = new LineItem();
                        lineItem.setItemName("Sub Cashier surplus Cash-up");
                        Double temp = Double.valueOf(surplus);
                        if (temp.doubleValue() < 0.0D) {
                            LOGGER.warn("Making negative amount positive! [surplus=" + surplus + "]");
                            temp = Double.valueOf(temp.doubleValue() * -1.0D);
                        }
                        lineItem.setReceiptAmount(temp.toString());
                        lineItem.setItem(result5.getString("Item"));
                        lineItem.setResponsibility("Responsibility");
                        lineItem.setObjective(result5.getString("Objective"));
                        lineItem.setFund(result5.getString("Fund"));
                        lineItem.setProject(result5.getString("Project"));
                        lineItem.setAsset(result5.getString("Asset"));
                        lineItem.setInfrastructure(result5.getString("Infrastructure"));
                        lineItem.setRegion("5423");
                        lineItem.setReceiptDate(depositDate);
                        lineItem.setPaymentMethod("C");
                        LOGGER.debug("5: Constructed lineItem [" + lineItem + "]");

                        receiptAmount += cashDepositsTotal - depositAmount;
                        LOGGER.debug("5: Adding difference(" + (cashDepositsTotal - depositAmount) + ") of cashDepositsTotal(" + cashDepositsTotal + ") and depositAmount(" + depositAmount + ") to receiptAmount(" + receiptAmount + ")");
                        cumulativeLineItemAmount += cashDepositsTotal - depositAmount;
                        LOGGER.debug("5: Adding difference(" + (cashDepositsTotal - depositAmount) + ") of cashDepositsTotal(" + cashDepositsTotal + ") and depositAmount(" + depositAmount + ") to cumulativeLineItemAmount(" + cumulativeLineItemAmount + ")");
                        lineItemList.add(lineItem);

                        if (paymentMethodTotal > 0.0D || receiptAmount > 0.0D) {
                            LOGGER.debug("paymentMethodTotal > 0.0 || receiptAmount > 0.0");

                            PreparedStatement prepStat1 = connection.prepareStatement("insert into"
                                + " BASRECEIPTS(RECEIPTAMOUNT, LINEITEMCOUNT, PAYMENTMETHODID, REFERENCE, DEPOSITTRANSACTIONID,"
                                + "     LOCATIONLINK, REGIONINDICATOR, FACILITYID, EDITEDDATETIME)"
                                + " values(?,?,?,?,?,?,?,?,?)");
                            double choiceReceiptAmount = 0.0;

                            if (cumulativeLineItemAmount > paymentMethodTotal) {
                                LOGGER.debug("5: Setting receiptAmount to paymentMethodTotal(" + paymentMethodTotal + "), because cumulativeLineItemAmount > paymentMethodTotal");
                                choiceReceiptAmount = paymentMethodTotal;
                            }
                            else {
                                LOGGER.debug("5: Setting receiptAmount to receiptAmount(" + receiptAmount + "), because cumulativeLineItemAmount <= paymentMethodTotal");
                                choiceReceiptAmount = receiptAmount;
                            }

                            prepStat1.setDouble(1, choiceReceiptAmount);
                            prepStat1.setInt(2, lineItemList.size());
                            prepStat1.setInt(3, 1);
                            prepStat1.setString(4, depositId);
                            prepStat1.setString(5, depositId);
                            prepStat1.setInt(6, 423);
                            prepStat1.setString(7, "DHA");
                            prepStat1.setLong(8, lineItemList.get(0).getFacilityId());
                            prepStat1.setString(9, Worker.getDateTime());
                            LOGGER.debug("5: Inserting into BASRECEIPTS [receiptAmount=" + choiceReceiptAmount + ",lineItemCount=" + lineItemList.size() + ",paymentMethodID=1*,reference=" + depositId + ",depositTransactionID=" + depositId + ",locationLink=423*,regionIndicator=DHA*,facilityID=39*,editedDateTime=" + Worker.getDateTime() + "]");
                            prepStat1.executeUpdate();

                            ResultSet result3 = statement3.executeQuery("select max(RECEIPTNUMBER) from BASRECEIPTS");
                            result3.next();
                            receiptNumber = result3.getLong(1);
                            result3.close();
                            LOGGER.debug("5: max(RECEIPTNUMBER)=" + receiptNumber);
                            LOGGER.debug("5: Putting receiptNumber -> lineItemList {" + receiptNumber + " -> " + lineItemList + "}");
                            receiptLineItemsHmap.put(String.valueOf(receiptNumber), lineItemList);
                            LOGGER.debug("5: Adding receipt to list [receiptNumber=" + receiptNumber + "]");
                            receiptList.add(String.valueOf(receiptNumber));
                        }

                        LOGGER.debug("5: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                        deposit_receiptList_Hmap.put(depositId, receiptList);
                        lineItemList = new ArrayList<LineItem>();
                        LOGGER.debug("5: Constructed new lineItemList");
                        receiptAmount = 0.0D;
                        cumulativeLineItemAmount = 0.0D;
                        paymentMethodTotal = 0.0D;
                        LOGGER.debug("5: Setting [receiptAmount=" + receiptAmount + ",cumulativeLineItemAmount=" + cumulativeLineItemAmount + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                    }
                    else {
                        throw new Exception("Encountered a surplus in day end, but was unable to retrieve details for the surplus line item from the database");
                    }
                }
            }

            if (!lineItemList.isEmpty() && paymentMethodTotal > 0.0D && !ind_Deficit) {
                LOGGER.debug("6: !lineItemList.isEmpty() && paymentMethodTotal > 0.0D && !ind_Deficit");
                LOGGER.debug("6: Setting receiptAmount of lineItem2 to " + paymentMethodTotal);
                lineItem2.setReceiptAmount(String.valueOf(paymentMethodTotal));
                tempLineItem = new LineItem();
                tempLineItem.setItemName(lineItem2.getItemName());
                tempLineItem.setReceiptAmount(lineItem2.getReceiptAmount());
                tempLineItem.setItem(lineItem2.getItem());
                tempLineItem.setResponsibility(lineItem2.getResponsibility());
                tempLineItem.setObjective(lineItem2.getObjective());
                tempLineItem.setFund(lineItem2.getFund());
                tempLineItem.setProject(lineItem2.getProject());
                tempLineItem.setAsset(lineItem2.getAsset());
                tempLineItem.setRegion(lineItem2.getRegion());
                tempLineItem.setInfrastructure(lineItem2.getInfrastructure());
                tempLineItem.setReceiptDate(depositDate);
                tempLineItem.setPaymentMethod("C");
                LOGGER.debug("6: Constructed templineItem [" + tempLineItem + "]");

                PreparedStatement prepStat = connection.prepareStatement("insert into"
                    + " BASRECEIPTS(RECEIPTAMOUNT, LINEITEMCOUNT, PAYMENTMETHODID, REFERENCE, DEPOSITTRANSACTIONID, LOCATIONLINK, REGIONINDICATOR, FACILITYID, EDITEDDATETIME)"
                    + " values(?,?,?,?,?,?,?,?,?)");
                prepStat.setDouble(1, paymentMethodTotal);
                prepStat.setInt(2, lineItemList.size());
                prepStat.setInt(3, 1);
                prepStat.setString(4, depositId);
                prepStat.setString(5, depositId);
                prepStat.setInt(6, 423);
                prepStat.setString(7, "DHA");
                prepStat.setLong(8, lineItemList.get(0).getFacilityId());
                prepStat.setString(9, Worker.getDateTime());
                LOGGER.debug("6: Inserting into BASRECEIPTS [receiptAmount=" + paymentMethodTotal + ",lineItemCount=" + lineItemList.size() + ",paymentMethodID=1*,reference=" + depositId + ",depositTransactionID=" + depositId + ",locationLink=423*,regionIndicator=DHA*,facilityID=39*,editedDateTime=" + Worker.getDateTime() + "]");
                prepStat.executeUpdate();

                ResultSet result4 = statement4.executeQuery("select max(RECEIPTNUMBER) from BASRECEIPTS");
                result4.next();
                receiptNumber = result4.getLong(1);
                LOGGER.debug("6: max(RECEIPTNUMBER)=" + receiptNumber);
                LOGGER.debug("6: Putting receiptNumber -> lineItemList {" + receiptNumber + " -> " + lineItemList + "}");
                receiptLineItemsHmap.put(String.valueOf(receiptNumber), lineItemList);
                LOGGER.debug("6: Adding receipt to list [receiptNumber=" + receiptNumber + "]");
                receiptList.add(String.valueOf(receiptNumber));
                LOGGER.debug("6: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                deposit_receiptList_Hmap.put(depositId, receiptList);

                while (depositIterator.hasNext()) {
                    LOGGER.debug("7: depositIterator.hasNext()");
                    depositId = depositIterator.next();
                    paymentMethodTotal = cashDeposits.get(depositId).doubleValue();
                    LOGGER.debug("7: Getting from cash deposits [depositID=" + depositId + ",paymentMethodTotal=" + paymentMethodTotal + "]");
                    LOGGER.debug("7: Constructing new receipt list");
                    receiptList = new ArrayList<String>();

                    lineItemList = new ArrayList<LineItem>();
                    lineItem3 = new LineItem();
                    lineItem3.setItemName(tempLineItem.getItemName());
                    lineItem3.setReceiptAmount(Double.valueOf(paymentMethodTotal).toString());
                    lineItem3.setItem(tempLineItem.getItem());
                    lineItem3.setResponsibility(tempLineItem.getResponsibility());
                    lineItem3.setObjective(tempLineItem.getObjective());
                    lineItem3.setFund(tempLineItem.getFund());
                    lineItem3.setProject(tempLineItem.getProject());
                    lineItem3.setAsset(tempLineItem.getAsset());
                    lineItem3.setRegion(tempLineItem.getRegion());
                    lineItem3.setInfrastructure(tempLineItem.getInfrastructure());
                    lineItem3.setReceiptDate(depositDate);
                    lineItem3.setPaymentMethod("C");
                    LOGGER.debug("7: Constructed lineItem3 [" + lineItem3 + "]");
                    lineItemList.add(lineItem3);

                    result4 = connection.createStatement().executeQuery("select DEPOSITDATETIME from TCBANKDEPOSITS where PFKDEPOSITTRANSACTIONID = " + depositId);
                    result4.next();
                    depositDate = result4.getDate("DEPOSITDATETIME");

                    prepStat = connection.prepareStatement("insert into"
                        + " BASRECEIPTS(RECEIPTAMOUNT, LINEITEMCOUNT, PAYMENTMETHODID, REFERENCE, DEPOSITTRANSACTIONID,"
                        + "     LOCATIONLINK, REGIONINDICATOR, FACILITYID, EDITEDDATETIME)"
                        + " values(?,?,?,?,?,?,?,?,?)");
                    prepStat.setDouble(1, paymentMethodTotal);
                    prepStat.setInt(2, lineItemList.size());
                    prepStat.setInt(3, 1);
                    prepStat.setString(4, depositId);
                    prepStat.setString(5, depositId);
                    prepStat.setInt(6, 423);
                    prepStat.setString(7, "DHA");
                    prepStat.setLong(8, lineItemList.get(0).getFacilityId());
                    prepStat.setString(9, Worker.getDateTime());
                    LOGGER.debug("7: Inserting into BASRECEIPTS [receiptAmount=" + paymentMethodTotal + ",lineItemCount=" + lineItemList.size() + ",paymentMethodID=1*,reference=" + depositId + ",depositTransactionID=" + depositId + ",locationLink=423*,regionIndicator=DHA*,facilityID=39*,editedDateTime=" + Worker.getDateTime() + "]");
                    prepStat.executeUpdate();

                    result4 = statement4.executeQuery("select max(RECEIPTNUMBER) from BASRECEIPTS");
                    result4.next();
                    receiptNumber = result4.getLong(1);
                    LOGGER.debug("7: max(RECEIPTNUMBER)=" + receiptNumber);
                    LOGGER.debug("7: Putting receiptNumber -> lineItemList {" + receiptNumber + " -> " + lineItemList + "}");
                    receiptLineItemsHmap.put(String.valueOf(receiptNumber), lineItemList);
                    LOGGER.debug("7: Adding receipt to list [receiptNumber=" + receiptNumber + "]");
                    receiptList.add(String.valueOf(receiptNumber));
                    LOGGER.debug("7: Putting depositID -> receiptList {" + depositId + " -> " + receiptList + "}");
                    deposit_receiptList_Hmap.put(depositId, receiptList);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Exception during BASInfoDAO.getPaymentsSummarized4()", e);
            return false;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return true;
    }

    
   
    
    public List<TcLineItemPayment> getLineItemPayment(int paymentTransactionID) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<TcLineItemPayment> lineItemPayments = new ArrayList<TcLineItemPayment>();

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select * from TCLINEITEMPAYMENTS t where FKPAYMENTTRANSACTIONID = ?");
            statement.setInt(1, paymentTransactionID);
            resultSet = statement.executeQuery();
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            while (resultSet.next()) {
                TcLineItemPayment lineItemPayment = new TcLineItemPayment();

                // FIXME How do we know the correct columns are referenced? The SQL query selects *
                lineItemPayment.setPkLineItemPaymentId(resultSet.getInt(1));
                lineItemPayment.setAmount(resultSet.getDouble(2));
                lineItemPayment.setFkEntityLastEditedById(resultSet.getInt(3));
                lineItemPayment.setEditedDateTime(resultSet.getDate(4).toString());
                lineItemPayment.setFkPaymentTransactionId(resultSet.getInt(5));
                lineItemPayment.setFkLineItemId(resultSet.getInt(6));
                lineItemPayments.add(lineItemPayment);
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return lineItemPayments;
    }

   

    // UNUSED BASInfoDAO.getAllPayments(int dayEndID)
    // FIXME Parameter dayEndID is not used
    public List<TcPaymentTransactions> getAllPayments(int dayEndID) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<TcPaymentTransactions> payments = new ArrayList<TcPaymentTransactions>();

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select p.PAYMENTAMOUNT, p.PFKPAYMENTTRANSACTIONID"
                + " from TCTRANSACTIONS t cross join TCBANKDEPOSITS tb cross join ("
                + "     IRECASHIERDEPOSITS cd inner join IREDAYENDS de on (cd.FKDAYENDID = de.PKDAYENDID)"
                + " ) inner join TCPAYMENTTRANSACTIONS p on (cd.PKCASHIERDEPOSITID = p.FKCASHIERDEPOSITID)"
                + " where de.PKDAYENDID = ("
                + "     select max(de.PKDAYENDID)"
                + "     from IREDAYENDS de)"
                + "     group by p.PFKPAYMENTTRANSACTIONID");
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            while (resultSet.next()) {
                TcPaymentTransactions paymentTransactions = new TcPaymentTransactions();

                // FIXME Why Math.abs?
                paymentTransactions.setPaymentAmount(Math.abs(resultSet.getInt(1)));
                paymentTransactions.setPfkPaymentTransactionId(resultSet.getInt(2));
                payments.add(paymentTransactions);
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return payments;
    }

    public TCBASInterfaceFile mergeBASFile(long now, int basFileStatus, String fileName, String backUpURL) throws SQLException {
        Connection connection = null;
        CallableStatement statement = null;

        try {
            Timestamp nowTime = new java.sql.Timestamp(now);

            connection = this.connectionProvider.get();
            statement = connection.prepareCall("begin MERGE_BAS_FILE(?, ?, ?, ?, ?, ?, ?); end;");
            statement.registerOutParameter(1, OracleTypes.TIMESTAMP);
            statement.registerOutParameter(7, OracleTypes.NUMBER);
            statement.setTimestamp(1, nowTime);
            statement.setInt(2, basFileStatus);
            statement.setTimestamp(3, nowTime);
            statement.setString(4, fileName);
            statement.setString(5, backUpURL);
            statement.setString(6, "None");

            LOGGER.debug("Calling MERGE_BAS_FILE [basFileStatus={},fileName={}]", basFileStatus, fileName);

            statement.execute();

            if (statement.getObject(7) != null) {
                TCBASInterfaceFile basInterfaceFile = new TCBASInterfaceFile(statement.getLong(7), statement.getTimestamp(1), basFileStatus, nowTime, fileName, backUpURL, "None");

                LOGGER.debug("Merged {}", basInterfaceFile);
                return basInterfaceFile;
            }
            LOGGER.debug("Failure calling MERGE_BAS_FILE [basFileStatus={},fileName={}]", basFileStatus, fileName);
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return null;
    }

    // UNUSED BASInfoDAO.getDepositId()
    public List<TcBankDeposits> getDepositID() throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<TcBankDeposits> depositsList = new ArrayList<TcBankDeposits>();

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select PFKDEPOSITTRANSACTIONID as DEPOSITID"
                + " from TCBANKDEPOSITS"
                + " where FKDEPOSITSTATUSID = 2");  // CONFIRMED
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            while (resultSet.next()) {
                TcBankDeposits deposits = new TcBankDeposits();

                deposits.setPfkDepositTransactionId(resultSet.getInt(1));
                depositsList.add(deposits);
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return depositsList;
    }

    // UNUSED BASInfoDAO.checkConfirmedDayEnds()
    public boolean checkConfirmedDayEnds() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select PKDAYENDID from IREDAYENDS where FKDAYENDSTATUSID = 3"); // CONFIRMED

            if (resultSet.next()) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.warn("Failure checking for confirmed day ends", e);
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return false;
    }

 


    public void updateDeposits(Set<Long> processedDayEnds, Set<Long> processedDeposits, String newStatus) throws Exception {
        Connection connection = null;
        PreparedStatement dayEndsStatement = null;
        PreparedStatement depositsStatement = null;

        try {
            java.sql.Date currentDateTime = new java.sql.Date(System.currentTimeMillis());

            connection = this.connectionProvider.get();
            dayEndsStatement = connection.prepareStatement("update IREDAYENDS"
                + " set FKDAYENDSTATUSID = 6, EDITEDDATETIME = ?"
                + " where PKDAYENDID in (" + Joiner.on(',').join(processedDayEnds) + ")");
            dayEndsStatement.setDate(1, currentDateTime);

            int updated = dayEndsStatement.executeUpdate();

            if (updated == 0) {
                LOGGER.error("Day end status not updated [dayEnds=" + processedDayEnds + "]");
                throw new BASTransportException("An error occured updating the day end status");
            }

            LOGGER.info("Day end status updated successfully");

            depositsStatement = connection.prepareStatement("update TCBANKDEPOSITS"
                + " set FKDEPOSITSTATUSID = ?, EDITEDDATETIME = ?"
                + " where PFKDEPOSITTRANSACTIONID in (" + Joiner.on(',').join(processedDeposits) + ")");
            depositsStatement.setInt(1, this.idDAO.getDepositStatusId(newStatus));
            depositsStatement.setDate(2, currentDateTime);

            updated = depositsStatement.executeUpdate();

            if (updated == 0) {
                LOGGER.error("Deposit status not updated [dayEnds=" + processedDayEnds + "]");
                throw new BASTransportException("An error occured updating the deposit status");
            }

            LOGGER.info("Bank deposit status updated successfully");
        }
        finally {
            Closeables.close(depositsStatement);
            Closeables.close(dayEndsStatement);
            Closeables.close(connection);
        }
    }

 

    public List<TcBankDeposits> getDepositsGrandTotal(String depositorReference) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select sum(GRANDTOTAL) as TOTAL, sum(SUBTOTALCASH) as CASH,"
                + "     sum(SUBTOTALMONEYORDER) as MONEYORDER, sum(SUBTOTALCHEQUE) as CHEQUE,"
                + "     sum(SUBTOTALPOSTALORDER) as POSTALORDER, sum(SUBTOTALTRAVCHEQUES) as TRAVELCHEQUES,"
                + "     sum(SUBTOTALOTHER) as OTHER"
                + " from TCBANKDEPOSITS d inner join LTDEPOSITSTATUSES s on (d.FKDEPOSITSTATUSID = s.PKDEPOSITSTATUSID)"
                + " where d.DEPOSITORREFERENCE = ?");
            statement.setString(1, depositorReference);
            resultSet = statement.executeQuery();
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            List<TcBankDeposits> deposits = new ArrayList<TcBankDeposits>();

            while (resultSet.next()) {
                TcBankDeposits depositTotals = new TcBankDeposits();
                double sqlTotal = resultSet.getDouble("TOTAL");

                depositTotals.setSubTotalCash(resultSet.getDouble("CASH"));
                depositTotals.setSubTotalMoneyOrder(resultSet.getDouble("MONEYORDER"));
                depositTotals.setSubTotalCheque(resultSet.getDouble("CHEQUE"));
                depositTotals.setSubTotalPostalOrder(resultSet.getDouble("POSTALORDER"));
                depositTotals.setSubTotalTravCheques(resultSet.getDouble("TRAVELCHEQUES"));
                depositTotals.setSubTotalOther(resultSet.getDouble("OTHER"));

                // FIXME using grand total as calculated by Java and ignoring cash total!!!???
                // FIXME probably differs from grand total as calculated by SQL
                // FIXME save value as variable to log what is really going on
                double javaTotal = depositTotals.getSubTotalCheque() + depositTotals.getSubTotalMoneyOrder() + depositTotals.getSubTotalOther() + depositTotals.getSubTotalPostalOrder() + depositTotals.getSubTotalTravCheques();
                depositTotals.setGrandTotal(javaTotal);
                deposits.add(depositTotals);
                LOGGER.debug("Grand totals [differ=" + (Double.compare(sqlTotal, javaTotal) != 0) + ",javaTotal=" + javaTotal + ",sqlTotal=" + sqlTotal + "]");
            }
            return deposits;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public BasSubFooter getSubFooterTotals(String depositorReference) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            // Select the DepositorReference, because we GROUP BY it
            statement = connection.prepareStatement("SELECT SUM(pay.PaymentAmount) AS Amount, dep.DepositorReference, COUNT(pay.PfkPaymentTransactionId) AS Line"
                + " FROM ("
                + "     (iredayends d INNER JOIN tcbankdeposits dep ON (d.FkBankDepositTxId = dep.PfkDepositTransactionId))"
                + "         INNER JOIN irecashierdeposits cdep ON (cdep.FkDayEndId = d.PkDayEndId)"
                + " ) INNER JOIN tcpaymenttransactions pay ON (pay.FkCashierDepositId = cdep.PkCashierDepositId)"
                + " WHERE (dep.DepositorReference = ?)"
                + " GROUP BY dep.DepositorReference");
            statement.setString(1, depositorReference);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                BasSubFooter footer = new BasSubFooter();

                footer.setAmount(resultSet.getDouble("Amount"));
                footer.setLine(resultSet.getInt("Line"));
                footer.setReference(depositorReference);
                return footer;
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return null;
    }

    public void deleteBasEntry(int basID) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("delete from TCBASINTERFACEFILES where PKID = ?");
            statement.setInt(1, basID);

            int updated = statement.executeUpdate();

            LOGGER.debug("Deleted " + updated + " BAS entries from TCBASINTERFACEFILES");
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public static Map<String, Integer> getBASStatuses(Connection connection, Collection<String> filenames) throws SQLException {
        Map<String, Integer> basStatuses = Maps.newHashMap();
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select FILENAME, FKINTERFACEFILESTATUSID"
                + " from TCBASINTERFACEFILES"
                + " where FILENAME in (" + Joiner.on(',').join(Collections2.transform(filenames, new Function<String, String>() {
                    public String apply(String input) {
                        return String.format("'%s'", input);
                    }
                }))
                + ")");

            while (resultSet.next()) {
                basStatuses.put(resultSet.getString("FILENAME"), resultSet.getInt("FKINTERFACEFILESTATUSID"));
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
        }
        return basStatuses;
    }

    public static void updateBASStatus(Connection connection, long pkID, int basStatus) throws SQLException {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("update TCBASINTERFACEFILES"
                + " set FKINTERFACEFILESTATUSID = ?, EDITEDDATETIME = ?"
                + " where PKID = ?");
            statement.setInt(1, basStatus);
            statement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            statement.setLong(3, pkID);

            if (statement.executeUpdate() == 0) {
                LOGGER.warn("Attempt to update BAS file statuses resulted in none being updated [pkID={},basStatus={}]", pkID, basStatus);
            }
        }
        finally {
            Closeables.close(statement);
        }
    }

    public static void updateBASStatus(Connection connection, String fileName, int basStatus) throws SQLException {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("update TCBASINTERFACEFILES"
                + " set FKINTERFACEFILESTATUSID = ?, EDITEDDATETIME = ?"
                + " where FILENAME = ?");
            statement.setInt(1, basStatus);
            statement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            statement.setString(3, fileName);

            if (statement.executeUpdate() == 0) {
                LOGGER.warn("Attempt to update BAS file statuses resulted in none being updated [fileName={},basStatus={}]", fileName, basStatus);
            }
        }
        finally {
            Closeables.close(statement);
        }
    }

    public static void updateDayEndStatus(Connection connection, Set<Long> dayEnds, int dayEndStatus) throws SQLException {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("update IREDAYENDS"
                + " set FKDAYENDSTATUSID = ?"
                + " where PKDAYENDID in (" + Joiner.on(',').join(dayEnds) + ")");
            statement.setInt(1, dayEndStatus);

            if (statement.executeUpdate() == 0) {
                LOGGER.warn("Attempt to update day end status resulted in none being updated [dayEnds={},dayEndStatus={}]", dayEnds, dayEndStatus);
            }
        }
        finally {
            Closeables.close(statement);
        }
    }

    public void joinDepositsWithBASFile(Set<Long> processedDeposits, String basFileName) throws Exception {
        Connection connection = null;
        PreparedStatement selectStatement = null;
        PreparedStatement mergeStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            selectStatement = connection.prepareStatement("select PKID from TCBASINTERFACEFILES where FILENAME = ?");
            selectStatement.setString(1, basFileName);
            resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                long basFileID = resultSet.getLong("PKID");

                mergeStatement = connection.prepareStatement("insert into JOINBANKDEPBASFILE (PFKBASFILEID, PFKDEPOSITTRANSACTIONID) values (?, ?)");

                for (long processedDeposit : processedDeposits) {
                    mergeStatement.setLong(1, basFileID);
                    mergeStatement.setLong(2, processedDeposit);

                    try {
                        int updated = mergeStatement.executeUpdate();
                        LOGGER.debug("Joined bank deposit and BAS file name [basFileID={},processedDeposit={},rowsUpdated={}]", new Object[] {basFileID, processedDeposit, updated});
                    }
                    catch (SQLException sqle) {
                        if (sqle.getErrorCode() != 1) {   // ORA-00001
                            throw sqle;
                        }
                        LOGGER.debug("Bank deposit and BAS file name already joined [basFileID={},processedDeposit={}]", basFileID, processedDeposit);
                    }
                }
            }
            else {
                LOGGER.warn("File not found; bank deposits not joined [basFileName={},deposits={}]", basFileName, processedDeposits);
            }
        }
        finally {
            Closeables.close(mergeStatement);
            Closeables.close(resultSet);
            Closeables.close(selectStatement);
            Closeables.close(connection);
        }
    }

    // Extracted from another method
    public static double getCashDepositsTotal(Map<String, Double> depositPaymentMethodTotal, List<ConfirmedDeposit> depositList) {
        double cashDepositsTotal = 0.0D;

        for (ConfirmedDeposit confirmedDepsit : depositList) {
            double cashTotal = confirmedDepsit.getCashTotal();

            if (cashTotal != 0.0D) {
                depositPaymentMethodTotal.put(confirmedDepsit.toString(), cashTotal);
                cashDepositsTotal += cashTotal;
            }
        }
        return cashDepositsTotal;
    }

    // Extracted from another method
    public static double getPostalOrderDepositsTotal(Map<String, Double> depositPaymentMethodTotal, List<ConfirmedDeposit> depositList) {
        double postalOrderDepositsTotal = 0.0D;

        for (ConfirmedDeposit confirmedDepsit : depositList) {
            double postalOrderTotal = confirmedDepsit.getPostalOrderTotal();

            if (postalOrderTotal != 0.0D) {
                depositPaymentMethodTotal.put(confirmedDepsit.toString(), postalOrderTotal);
                postalOrderDepositsTotal += postalOrderTotal;
            }
        }
        return postalOrderDepositsTotal;
    }

    // Extracted from another method
    public static double getChequeDepositsTotal(Map<String, Double> depositPaymentMethodTotal, List<ConfirmedDeposit> depositList) {
        double chequeDepositsTotal = 0.0D;

        for (ConfirmedDeposit confirmedDepsit : depositList) {
            double chequeTotal = confirmedDepsit.getChequeTotal();

            if (chequeTotal != 0.0D) {
                depositPaymentMethodTotal.put(confirmedDepsit.toString(), chequeTotal);
                chequeDepositsTotal += chequeTotal;
            }
        }
        return chequeDepositsTotal;
    }

    public List<Recon> getConfirmedReconsSince(String previousYearEnd) throws Exception {
        Connection connection = null;
        PreparedStatement reconsStatement = null;
        ResultSet reconsResultSet = null;

        try {
            connection = this.connectionProvider.get();
            reconsStatement = connection.prepareStatement("select recon.RECONDATE, recon.IRERECON_ID"
                + " from IRERECON recon, TCBANKDEPOSITS bank, IREDAYENDS dayend"
                + " where RECONDATE > ?"
                + " and (STATUS = 'CONFIRMED' OR STATUS = 'BAS UNBALANCED')"
                + " and recon.IRERECON_ID = bank.RECONID"
                + " and dayend.PKDAYENDID = bank.FKDAYENDID"
                + " group by recon.RECONDATE, recon.IRERECON_ID"
                + " order by recon.RECONDATE, recon.IRERECON_ID");
            reconsStatement.setString(1, previousYearEnd);
            LOGGER.debug("Retrieving confirmed recons since " + previousYearEnd);
            reconsResultSet = reconsStatement.executeQuery();
            reconsResultSet.setFetchSize(DEF_FETCH_SIZE);

            List<Recon> confirmedRecons = new ArrayList<Recon>();

            while (reconsResultSet.next()) {
                String reconDate = reconsResultSet.getString("RECONDATE");
                long reconID = reconsResultSet.getLong("IRERECON_ID");

                LOGGER.debug("Found IRE recon [reconID=" + reconID + ",reconDate=" + reconDate + "]");
                confirmedRecons.addAll(this.getReconsWithConfirmedDeposits(connection, reconDate, reconID));
            }

            if (confirmedRecons.isEmpty()) {
                LOGGER.warn("No 'CONFIRMED' IRE recons found!");
            }
            return confirmedRecons;
        }
        finally {
            Closeables.close(reconsResultSet);
            Closeables.close(reconsStatement);
            Closeables.close(connection);
        }
    }

    private List<Recon> getReconsWithConfirmedDeposits(Connection connection, String reconDate, long reconID) throws SQLException {
        PreparedStatement depositsStatement = null;
        ResultSet depositsResultSet = null;

        try {
            depositsStatement = connection.prepareStatement("select bank.PFKDEPOSITTRANSACTIONID, bank.FKDAYENDID,"
                + "     bank.FKDEPOSITSTATUSID, bank.DEPOSITORREFERENCE, ent.DISPLAYNAME"
                + " from TCBANKDEPOSITS bank, ENTITIES ent, IREDAYENDS dayend, IRERECON recon"
                + " where dayend.PKDAYENDID = bank.FKDAYENDID"
                + "     and dayend.FKFACILITYENTITYID = ent.PKENTITYID"
                + "     and recon.IRERECON_ID = bank.RECONID"
                + "     and bank.RECONID = ?");
            depositsStatement.setLong(1, reconID);
            depositsResultSet = depositsStatement.executeQuery();
            depositsResultSet.setFetchSize(DEF_FETCH_SIZE);

            List<Recon> recons = new ArrayList<Recon>();
            Set<Long> confirmedDeposits = new HashSet<Long>();
            Set<Long> dayEnds = new HashSet<Long>();

            while (depositsResultSet.next()) {
                long depositTransaction = depositsResultSet.getLong("PFKDEPOSITTRANSACTIONID");
                long dayEndID = depositsResultSet.getLong("FKDAYENDID");
                int depositStatusID = depositsResultSet.getInt("FKDEPOSITSTATUSID");
                String depositOrReference = depositsResultSet.getString("DEPOSITORREFERENCE");
                String facility = depositsResultSet.getString("DISPLAYNAME");

                LOGGER.debug("Found bank deposit [reconID=" + reconID + ",reconDate=" + reconDate + ",depositTransactionID=" + depositTransaction + ",dayEndID=" + dayEndID + ",depositStatusID=" + depositStatusID + ",depositOrReference=" + depositOrReference + ",facility=" + facility + "]");

                if (depositStatusID == 2) {    // CONFIRMED
                    LOGGER.info("Bank deposit 'CONFIRMED' [reconID=" + reconID + ",reconDate=" + reconDate + ",depositTransactionID=" + depositTransaction + ",dayEndID=" + dayEndID + ",depositOrReference=" + depositOrReference + ",facility=" + facility + "]");
                    confirmedDeposits.add(depositTransaction);
                    dayEnds.add(dayEndID);
                }
                else if(depositStatusID == 4) { // BAS CREATED
                    LOGGER.info("Bank deposit already 'BAS CREATED' [reconID=" + reconID + ",reconDate=" + reconDate + ",depositTransactionID=" + depositTransaction + ",dayEndID=" + dayEndID + ",depositOrReference=" + depositOrReference + ",facility=" + facility + "]");
                    this.updateReconStatus(connection, reconID, "BAS CREATED");
                }
                else {
                    LOGGER.warn("Bank deposit not 'CONFIRMED' [reconID=" + reconID + ",reconDate=" + reconDate + ",depositTransactionID=" + depositTransaction + ",dayEndID=" + dayEndID + ",depositOrReference=" + depositOrReference + ",facility=" + facility + "]");
                }
            }

            if (!confirmedDeposits.isEmpty()) {
                long facilityID = BASReconDAO.retrieveFacilityID(connection, dayEnds);
                Recon recon = new Recon(reconID, reconDate, confirmedDeposits, dayEnds, facilityID);

                recons.add(recon);
                LOGGER.debug("Need to summarise " + recon);
            }
            else {
                LOGGER.info("No bank deposits associated with IRE recon [reconID=" + reconID + ",reconDate=" + reconDate + "]");
            }
            return recons;
        }
        finally {
            Closeables.close(depositsResultSet);
            Closeables.close(depositsStatement);
        }
    }

    public static long retrieveFacilityID(Connection connection, Set<Long> dayEnds) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select distinct FKFACILITYENTITYID"
                + " from IREDAYENDS"
                + " where PKDAYENDID in (" + Joiner.on(", ").join(dayEnds) + ")");

            if (resultSet.next()) {
                long facilityID = resultSet.getLong(1);

                LOGGER.debug("Found facility [dayEnds=" + dayEnds + ",facilityID=" + facilityID + "]");

                if (resultSet.next()) {
                    LOGGER.warn("Found more than one facility ID for dayEnds " + dayEnds + "; expected only one");
                }
                return facilityID;
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
        }
        LOGGER.warn("Did not find facility ID for dayEnds " + dayEnds);
        return 0L;
    }

    public void updateReconStatus(Connection connection, long reconID, String status) {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("update IRERECON set STATUS = ? where IRERECON_ID = ?");
            statement.setString(1, status);
            statement.setLong(2, reconID);
            LOGGER.debug("Updating recon status [reconID={},status={}]", reconID, status);

            int updated = statement.executeUpdate();

            if (updated != 1) {
                // TODO Should we rollback?
                LOGGER.warn("Expected a single row to be updated, but " + updated + " rows were updated instead");
            }
        }
        catch (Exception e) {
            LOGGER.error("Failure updating recon status", e);
        }
        finally {
            Closeables.close(statement);
        }
    }

    public void updateReconStatus(long reconID, String status) {
        Connection connection = null;

        try {
            connection = this.connectionProvider.get();
            this.updateReconStatus(connection, reconID, status);
        }
        catch (Exception e) {
            LOGGER.error("Failure updating recon status", e);
        }
        finally {
            Closeables.close(connection);
        }
    }

    public InvoiceDepositBalance reconBalances(Recon recon) throws Exception {
        LOGGER.info("Calculating whether invoices and deposits balance [" + recon + "]");

        Connection connection = null;

        try {
            connection = this.connectionProvider.get();

            Map<Long, Long> invoiceTransactions = BASReconDAO.retrieveInvoiceTransactions(connection, recon.getReconDate(), recon.getFacilityID());
            Map<Long, Long> depositTransactions = BASReconDAO.retrieveDepositTransactions(connection, recon.getDeposits());

            return new InvoiceDepositBalance(recon, invoiceTransactions, depositTransactions);
        }
        catch (Exception e) {
            LOGGER.error("Failure calculating whether invoices and deposits balance", e);
            throw e;
        }
        finally {
            Closeables.close(connection);
        }
    }

    public static Map<Long, Long> retrieveInvoiceTransactions(Connection connection, String reconDate, long facilityID) throws SQLException {
        PreparedStatement pstmt1 = null, pstmt2 = null;
        ResultSet rs1 = null, rs2 = null;

        try {
            Multimap<Long, ShiftTransaction> shifts = HashMultimap.create();
            
            //Done this way to cater for reversals. There is no invoice created for a reversal, therefore the totals will be incorrect if the invoice ismanualpayment is retrieved in the first sql statement
            
            pstmt1 = connection.prepareStatement("select distinct(pay.PFKPAYMENTTRANSACTIONID), dep.FKSHIFTID, pay.PAYMENTAMOUNT, pay.PAYMENTREFERENCE " +
            		"from IRECASHIERDEPOSITS dep, TCPAYMENTTRANSACTIONS pay, IRESHIFTS shift " +
            		"where shift.FKENTITYFACILITYID = ? " +
            		"and to_char(dep.SUBMITTEDDATETIME, 'YYYY-MM-DD') = ? " + 
            		"and dep.FKSHIFTID = pay.FKSHIFTID " +
            		"and pay.FKSHIFTID = shift.PKShiftId " +
            		"order by pay.PAYMENTAMOUNT desc");
            
            
            pstmt2 = connection.prepareStatement("select inv.ISMANUALPAYMENT " +
					"from IRECASHIERDEPOSITS dep, TCPAYMENTTRANSACTIONS pay, TCINVOICES inv " +
					"where inv.FKFACILITYENTITYID = ? " +
					"and to_char(dep.SUBMITTEDDATETIME, 'YYYY-MM-DD') = ? " + 
					"and dep.FKSHIFTID = pay.FKSHIFTID " +
					"and pay.PFKPAYMENTTRANSACTIONID = inv.PFKINVOICETRANSACTIONID " +
					"order by pay.PAYMENTAMOUNT desc");
            
            
            pstmt1.setLong(1, facilityID);
            pstmt1.setString(2, reconDate);
            
            pstmt2.setLong(1, facilityID);
            pstmt2.setString(2, reconDate);
            
            LOGGER.debug("Retrieving invoice transactions [reconDate=" + reconDate +",facilityID=" + facilityID + "]");
            
            rs1 = pstmt1.executeQuery();
            rs1.setFetchSize(DEF_FETCH_SIZE);

            rs2 = pstmt2.executeQuery();
            
            
            while (rs1.next()) {
                long shiftID = rs1.getLong("FKSHIFTID");
                long invoiceTransaction = rs1.getLong("PFKPAYMENTTRANSACTIONID");
                long amountPaid = rs1.getLong("PAYMENTAMOUNT");
                boolean manualPayment = false;
                
                if (amountPaid > 0) {
                	try {
                		manualPayment = rs2.getBoolean("ISMANUALPAYMENT");
                	} catch (Exception e) {
                		//ignore - rs2 may not have the same number of records as rs1
                	}
                }

                shifts.put(shiftID, new ShiftTransaction(invoiceTransaction, manualPayment, amountPaid));
            }
            
            BASReconDAO.validateShifts(shifts, reconDate, facilityID);
            return BASReconDAO.transformShifts(shifts);
        }
        finally {
            Closeables.close(rs1);
            Closeables.close(rs2);
            Closeables.close(pstmt1);
        }
    }

    public static void validateShifts(Multimap<Long, ShiftTransaction> shifts, String reconDate, long facilityID) {
        for (long shiftID : shifts.keySet()) {
            if (!BASReconDAO.isValidShift(shifts.get(shiftID))) {
                LOGGER.warn("Invalid shift contains both normal and manual payments [reconDate=" + reconDate + ",facilityID=" + facilityID + ",shiftID=" + shiftID + "]");
            }
        }
    }

    public static boolean isValidShift(Collection<ShiftTransaction> shiftTransactions) {
        boolean containsNormalPayments = false;
        boolean containsManualPayments = false;

        for (ShiftTransaction shiftTransaction : shiftTransactions) {
            if (shiftTransaction.isManualPayment()) {
                containsManualPayments = true;
            }
            else {
                containsNormalPayments = true;
            }
        }
        // if there are no transactions in the shift then it is valid
        return shiftTransactions.isEmpty() || (containsNormalPayments != containsManualPayments);
    }

    public static Map<Long, Long> transformShifts(Multimap<Long, ShiftTransaction> shifts) {
        Map<Long, Long> invoiceTransactions = Maps.newHashMap();

        for (ShiftTransaction shiftTransaction : shifts.values()) {
            invoiceTransactions.put(shiftTransaction.getInvoiceTransaction(), shiftTransaction.getAmountPaid());
        }
        return invoiceTransactions;
    }

    public static Map<Long, Long> retrieveDepositTransactions(Connection connection, Set<Long> deposits) throws SQLException {
        Statement depositsStatement = null;
        ResultSet depositsResultSet = null;
        Map<Long, Long> depositTransactions = new HashMap<Long, Long>();

        try {
            LOGGER.debug("Retrieving deposit transactions " + deposits);
            depositsStatement = connection.createStatement();
            depositsResultSet = depositsStatement.executeQuery("select PFKDEPOSITTRANSACTIONID, GRANDTOTAL"
                + " from TCBANKDEPOSITS"
                + " where PFKDEPOSITTRANSACTIONID in (" + Joiner.on(", ").join(deposits) + ")");
            
            depositsResultSet.setFetchSize(DEF_FETCH_SIZE);

            
            while(depositsResultSet.next()) {
            	long depositTransaction = depositsResultSet.getLong(1);
            	long grandTotal = depositsResultSet.getLong(2);

                depositTransactions.put(depositTransaction, grandTotal);
            }
        }
        finally {
            Closeables.close(depositsResultSet);
            Closeables.close(depositsStatement);
        }
        return depositTransactions;
    }

    public long findManualPaymentsBackdated(long facilityID, String reconDate) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select sum(AMOUNTPAID)"
                + " from TCINVOICES"
                + " where FKFACILITYENTITYID = ?"
                + " and ISMANUALPAYMENT = 1"
                + " and MANUALPAYMENTDATE = ?"
                + " and (to_char(GENERATIONDATE, 'YYYY-MM-DD') > MANUALPAYMENTDATE"
                + "     or to_char(EDITEDDATETIME, 'YYYY-MM-DD') > MANUALPAYMENTDATE)");
            statement.setLong(1, facilityID);
            statement.setString(2, reconDate);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0L;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public long findManualPaymentsDifferentDated(long facilityID, String reconDate) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select sum(AMOUNTPAID)"
                + " from TCINVOICES"
                + " where FKFACILITYENTITYID = ?"
                + " and ISMANUALPAYMENT = 1"
                + " and (to_char(GENERATIONDATE, 'YYYY-MM-DD') = ? or to_char(EDITEDDATETIME, 'YYYY-MM-DD') = ?)"
                + " and (to_char(GENERATIONDATE, 'YYYY-MM-DD') > MANUALPAYMENTDATE"
                + "     or to_char(EDITEDDATETIME, 'YYYY-MM-DD') > MANUALPAYMENTDATE)");
            statement.setLong(1, facilityID);
            statement.setString(2, reconDate);
            statement.setString(3, reconDate);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0L;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }
    
    
    public String getNameForFacility(long facilityId) throws Exception {
    	 Connection connection = null;
         PreparedStatement statement = null;
         ResultSet resultSet = null;

         try {
             connection = this.connectionProvider.get();
             statement = connection.prepareStatement("select displayname from entities where pkentityid = ?");
             statement.setLong(1, facilityId);
           
             resultSet = statement.executeQuery();

             if (resultSet.next()) {
                 return resultSet.getString("displayname");
             }
             
             return "";
         }
         finally {
             Closeables.close(resultSet);
             Closeables.close(statement);
             Closeables.close(connection);
         }
    
    }
    
    
}
