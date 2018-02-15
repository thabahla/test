package za.co.interfile.bas.report;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.interfile.bas.Task;
import za.co.interfile.bas.dao.BASReconDAO;
import za.co.interfile.bas.notify.BASNotifier;
import za.co.interfile.bas.report.csv.*;
import za.co.interfile.bas.util.Closeables;
import za.co.interfile.bas.util.FTPClients;

public class BASReporter implements Task {
    private static final Pattern RESPONSE_FILENAME_REGEX = Pattern.compile("^BICRIRE\\d+\\.TXT$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESPONSE_CONTENT_REGEX = Pattern.compile("CRIRE\\p{Space}+"    // first line
        + "\\d{14}BAS\\d{2}(\\d{10})\\p{Space}+\\p{Cntrl}+"                                     // serial number
        + "(?:\\d+ - (\\p{Print}+)\\p{Space}+\\p{Cntrl}+)?"                                     // reason (only for ABORT "parts")
        + "TRAILER\\d+(\\w+)\\p{Space}+\\p{Cntrl}+");                                           // SUCCESS / ABORT
    private static final DateFormat CSV_FILENAME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final int DEF_FETCH_SIZE = 1000;

    private final Logger logger = LoggerFactory.getLogger(BASReporter.class);
    private final String outbox;
    private final String workDirectory;
    private final String outputDirectory;
    private final String sentDirectory;
    private final String processedDirectory;
    private final String reportedDirectory;
    private final int expiryDays;
    private final Provider<Connection> connectionProvider;
    private final Provider<FTPClient> ftpClientProvider;
    private final BASNotifier basNotifier;

    @Inject
    public BASReporter(@Named("ftp.outbox") String outbox,
        @Named("dir.work") String workDirectory,
        @Named("dir.out") String outputDirectory,
        @Named("dir.sent") String sentDirectory,
        @Named("dir.processed") String processedDirectory,
        @Named("dir.reported") String reportedDirectory,
        @Named("report.expiry.days") int expiryDays,
        Provider<Connection> connectionProvider,
        Provider<FTPClient> ftpClientProvider,
        BASNotifier basNotifier) {
            Preconditions.checkArgument(expiryDays > 0, "'report.expiry.days' must be greater than 0");
            this.ftpClientProvider = ftpClientProvider;
            this.outbox = outbox;
            this.workDirectory = workDirectory;
            this.outputDirectory = outputDirectory;
            this.sentDirectory = sentDirectory;
            this.processedDirectory = processedDirectory;
            this.reportedDirectory = reportedDirectory;
            this.expiryDays = expiryDays;
            this.connectionProvider = connectionProvider;
            this.basNotifier = basNotifier;
    }

    public void run() {
        this.logger.info("Running");

        File directory = new File(this.workDirectory + File.separatorChar + this.processedDirectory);

        if (directory.isDirectory()) {
            Collection<ReportItem> report = Lists.newArrayList();
            FTPClient ftpClient = null;

            try {
                ftpClient = this.ftpClientProvider.get();

                if (FTPClients.changeWorkingDirectory(ftpClient, this.outbox)) {
                    Collection<File> downloaded = this.download(ftpClient, directory);

                    report.add(new ProcessedHeader());
                    report.addAll(this.processResponseFiles(downloaded));
                    report.add(new BlankLine());
                }
            }
            finally {
                Closeables.close(ftpClient);
            }

            report.add(new ExpiredHeader(this.expiryDays));
            report.addAll(this.findExpiredFiles());
            this.sendCSVReport(report);
            this.logger.debug("Response files resulted in report with {} lines", report.size());
        }
        else {
            this.logger.warn("{} is not a directory and it should be", directory.getAbsolutePath());
        }
        this.logger.info("Done");
    }

    public void requestShutdown() {
    }

    private Collection<File> download(FTPClient ftpClient, File directory) {
        Collection<File> downloaded = Lists.newArrayList();

        this.logger.info("Downloading response files");

        try {
            for (String responseFilename : ftpClient.listNames()) {
                if (RESPONSE_FILENAME_REGEX.matcher(responseFilename).matches()) {
                    File tmp = new File(directory, responseFilename + ".tmp");
                    File download = new File(directory, responseFilename);
                    OutputStream os = null;

                    try {
                        Files.createParentDirs(tmp);
                        os = new BufferedOutputStream(new FileOutputStream(tmp));

                        if (FTPClients.setFileTypeToBinary(ftpClient)) {
                            this.logger.debug("Downloading file [{}]", responseFilename);

                            if (ftpClient.retrieveFile(responseFilename, os)) {
                                Closeables.close(os);   // for Windows: cannot rename/move (delete) a file if it is still "open"
                                Files.move(tmp, download);
                                downloaded.add(download);
                                this.logger.debug("Deleting file [{}]", responseFilename);

                                if (!ftpClient.deleteFile(responseFilename)) {
                                    this.logger.debug("Failure deleting file [{}]", responseFilename);
                                }
                            }
                            else {
                                this.logger.debug("Failure downloading file [{}]", responseFilename);
                            }
                        }
                    }
                    catch (IOException ioe) {
                        this.logger.error("Failure downloading file", ioe);
                    }
                    finally {
                        Closeables.close(os);
                    }
                }
            }
        }
        catch (IOException ioe) {
            this.logger.error("Failure listing response files to download", ioe);
        }
        this.logger.info("Downloaded {} response files", downloaded.size());
        return downloaded;
    }

    private Collection<ReportItem> processResponseFiles(Collection<File> downloaded) {
        this.logger.debug("Processing {} response files", downloaded.size());

        Collection<ReportItem> report = Lists.newArrayList();

        for (File responseFile : downloaded) {
            if (responseFile.isFile()) {
                report.addAll(this.processFile(responseFile));
            }
            else {
                this.logger.warn("Response file does not exist or it is not a file [{}]", responseFile);
            }
        }
        return report;
    }

    private Collection<ReportItem> processFile(File responseFile) {
        this.logger.debug("Processing response file [{}]", responseFile);

        Connection connection = null;
        Collection<ReportItem> report = Lists.newArrayList();

        try {
            String content = Files.toString(responseFile, Charsets.UTF_8);
            Matcher matcher = RESPONSE_CONTENT_REGEX.matcher(content);

            connection = this.connectionProvider.get();

            while (matcher.find()) {
                try {
                    long basFileID = Long.valueOf(matcher.group(1));
                    String reason = matcher.group(2);   // may be null
                    String status = matcher.group(3).trim();

                    report.add(this.getReportData(connection, responseFile.getName(), basFileID, status, reason));
                    this.updateBASInterfaceFiles(connection, basFileID, status);
                    this.updateDayEnds(connection, BASReconDAO.getDayEnds(connection, basFileID), status);
                }
                catch (NumberFormatException nfe) {
                    this.logger.error("Response file contains invalid serial number", nfe);
                }
            }
            this.moveToSubDirectory(responseFile, "Success");
        }
        catch (Exception e) {
            this.logger.error("Failure processing response file", e);
            this.moveToSubDirectory(responseFile, "Failure");
        }
        finally {
            Closeables.close(connection);
        }
        return report;
    }

    private boolean move(File from, File to) {
        try {
            this.logger.debug("Moving file [from={},to={}]", from, to);
            Files.createParentDirs(to);
            Files.move(from, to);
            return true;
        }
        catch (IOException ioe) {
            this.logger.error("Failure moving file [from=" + from + ",to=" + to + "]", ioe);
        }
        return false;
    }

    private boolean moveToSubDirectory(File from, String subDirectory) {
        File to = new File(this.workDirectory + File.separatorChar + this.reportedDirectory + File.separatorChar + subDirectory + File.separatorChar + from.getName());

        return this.move(from, to);
    }

    private boolean moveToCSVSentDirectory(File from) {
        File to = new File(this.workDirectory + File.separatorChar + this.reportedDirectory + File.separatorChar + "CSV" + File.separatorChar + "Sent" + File.separatorChar + from.getName());

        return this.move(from, to);
    }

    private void sendCSVReport(Collection<ReportItem> report) {
        File csvReport = this.generateCSVReport(report);

        if (csvReport != null && this.basNotifier.notifyHomeAffairs(csvReport)) {
            this.moveToCSVSentDirectory(csvReport);
        }
        else {
            this.logger.warn("Failure sending CSV report [{}]", csvReport);
        }
    }

    private File generateCSVReport(Collection<ReportItem> report) {
        this.logger.debug("Generating CSV report");

        File csv = new File(this.workDirectory + File.separatorChar + this.reportedDirectory + File.separatorChar + "CSV" + File.separatorChar + CSV_FILENAME_FORMAT.format(new Date()) + ".csv");
        BufferedWriter writer = null;

        try {
            Files.createParentDirs(csv);
            writer = Files.newWriter(csv, Charsets.UTF_8);
            Joiner.on(System.getProperty("line.separator")).appendTo(writer, report);
            writer.flush();
            Closeables.close(writer);
            return csv;
        }
        catch (IOException ioe) {
            this.logger.error("Failure generating CSV report", ioe);
        }
        finally {
            Closeables.close(writer);
        }
        return null;
    }

    private ReportItem getReportData(Connection connection, String responseFilename, long basFileID, String status, String reason) throws SQLException {
        this.logger.debug("Fetching report data [basFileID={},status={}]", basFileID, status);

        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement("select e.DISPLAYNAME, bank.DEPOSITDATETIME, bank.DEPOSITORREFERENCE,"
                + "     bank.GRANDTOTAL, bas.FILENAME"
                + " from ENTITIES e, TCBANKDEPOSITS bank, TCBASINTERFACEFILES bas, IREDAYENDS de, JOINBANKDEPBASFILE j"
                + " where bas.PKID = ?"
                + " and bas.PKID = j.PFKBASFILEID"
                + " and j.PFKDEPOSITTRANSACTIONID = bank.PFKDEPOSITTRANSACTIONID"
                + " and de.PKDAYENDID = bank.FKDAYENDID"
                + " and e.PKENTITYID = de.FKFACILITYENTITYID");
            statement.setLong(1, basFileID);
            resultSet = statement.executeQuery();
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            if (resultSet.next()) {
                return new ProcessedReportItem(responseFilename,
                    basFileID,
                    status,
                    reason,
                    resultSet.getString("DISPLAYNAME"),
                    resultSet.getTimestamp("DEPOSITDATETIME"),
                    resultSet.getLong("DEPOSITORREFERENCE"),
                    resultSet.getLong("GRANDTOTAL"),
                    resultSet.getString("FILENAME"));
            }
            return new ProcessedReportItem(responseFilename,
                basFileID,
                status,
                reason,
                null,
                null,
                null,
                null,
                null);
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
        }
    }

    private void updateBASInterfaceFiles(Connection connection, long pkID, String status) throws SQLException {
        if (status.equalsIgnoreCase("SUCCESS")) {
            BASReconDAO.updateBASStatus(connection, pkID, 5);  // Succeeded
        }
        else {
            BASReconDAO.updateBASStatus(connection, pkID, 3);  // Failed
        }
    }

    private void updateDayEnds(Connection connection, Set<Long> dayEnds, String status) throws SQLException {
        if (status.equalsIgnoreCase("SUCCESS")) {
            BASReconDAO.updateDayEndStatus(connection, dayEnds, 8); // BAS Successful
        }
        else {
            BASReconDAO.updateDayEndStatus(connection, dayEnds, 7); // BAS Failed
        }
    }

    private Collection<ReportItem> findExpiredFiles() {
        Collection<ReportItem> expired = Lists.newArrayList();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select t.PKID, t.DATECREATED, t.FILENAME, l.STATUS, t.EDITEDDATETIME"
                + " from TCBASINTERFACEFILES t, LTINTERFACEFILESTATUSES l"
                + " where t.EDITEDDATETIME < ?"
                + " and (t.FKINTERFACEFILESTATUSID = 2 or t.FKINTERFACEFILESTATUSID = 4)"   // Sent or Resent
                + " and l.PKINTERFACEFILESTATUSID = t.FKINTERFACEFILESTATUSID");
//            statement.setDate(1, new java.sql.Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(this.expiryDays)));
            statement.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis() - 1L));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String filename = resultSet.getString("FILENAME");

                if (this.copyToOutputDirectory(filename)) {
                    expired.add(new ExpiredReportItem(
                        resultSet.getLong("PKID"),
                        resultSet.getTimestamp("DATECREATED"),
                        filename,
                        resultSet.getString("STATUS"),
                        resultSet.getTimestamp("EDITEDDATETIME")));
                }
                else {
                    expired.add(new ExpiredReportItem(
                        resultSet.getLong("PKID"),
                        resultSet.getTimestamp("DATECREATED"),
                        filename,
                        resultSet.getString("STATUS") + ", but file not found for resending",
                        resultSet.getTimestamp("EDITEDDATETIME")));
                }
            }
        }
        catch (SQLException sqle) {
            this.logger.error("Failure retrieving data of expired files", sqle);
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        return expired;
    }

    private boolean copyToOutputDirectory(String filename) {
        File from = new File(this.workDirectory + File.separatorChar + this.sentDirectory, filename);
        File to = new File(this.workDirectory + File.separatorChar + this.outputDirectory, filename);

        try {
            this.logger.debug("Copying file [from={},to={}]", from, to);
            Files.createParentDirs(to);
            Files.copy(from, to);
            return true;
        }
        catch (IOException ioe) {
            this.logger.error("Failure copying file [from=" + from + ",to=" + to + "]", ioe);
        }
        return false;
    }
}
