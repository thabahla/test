package za.co.interfile.bas.upload;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.interfile.bas.Task;
import za.co.interfile.bas.dao.BASReconDAO;
import za.co.interfile.bas.notify.BASNotifier;
import za.co.interfile.bas.util.Closeables;
import za.co.interfile.bas.util.FTPClients;

public class BASUploader implements Task {
    private static final Pattern BAS_FILENAME_REGEX = Pattern.compile("^BO\\p{Graph}+\\.txt$", Pattern.CASE_INSENSITIVE);

    private final Logger logger = LoggerFactory.getLogger(BASUploader.class);
    private final String inbox;
    private final String workDirectory;
    private final String outputDirectory;
    private final String sentDirectory;
    private final int batchSize;
    private final long sleepMinutes;
    private volatile boolean running = false;
    private final Provider<Connection> connectionProvider;
    private final Provider<FTPClient> ftpClientProvider;
    private final BASNotifier basNotifier;

    @Inject
    public BASUploader(@Named("ftp.inbox") String inbox,
        @Named("dir.work") String workDirectory,
        @Named("dir.out") String outputDirectory,
        @Named("dir.sent") String sentDirectory,
        @Named("upload.batch.size") int batchSize,
        @Named("upload.sleep.minutes") long sleepMinutes,
        Provider<Connection> connectionProvider,
        Provider<FTPClient> ftpClientProvider,
        BASNotifier basNotifier) {
            Preconditions.checkArgument(batchSize > 0, "'upload.batch.size' must be positive");
            Preconditions.checkArgument(sleepMinutes > 0 && sleepMinutes < 60, "'upload.sleep.minutes' not in range [1,59]");

            this.inbox = inbox;
            this.workDirectory = workDirectory;
            this.outputDirectory = outputDirectory;
            this.sentDirectory = sentDirectory;
            this.batchSize = batchSize;
            this.sleepMinutes = sleepMinutes;
            this.connectionProvider = connectionProvider;
            this.ftpClientProvider = ftpClientProvider;
            this.basNotifier = basNotifier;
    }

    public void run() {
        this.running = true;

        long iteration = 0L;

        while (this.running) {
            this.logger.info("Attempting to run iteration " + iteration);

            Collection<File> basFiles = this.getBatch();

            if (basFiles.size() < this.batchSize) {
                this.logger.debug("Only {} BAS files ready to be uploaded; waiting for more", basFiles.size());
            }
            else {
                FTPClient ftpClient = null;

                try {
                    ftpClient = this.ftpClientProvider.get();

                    if (FTPClients.changeWorkingDirectory(ftpClient, this.inbox)) {
                        if (this.isWaitingForTreasury(ftpClient)) {
                            this.logger.debug("{} BAS files ready to be uploaded, but waiting for Treasury to process previously uploaded files", basFiles.size());
                        }
                        else {
                            this.logger.debug("Running iteration {} with {} files", iteration++, basFiles.size());

                            Collection<String> uploadedFilenames = this.upload(ftpClient, basFiles);

                            this.basNotifier.notifyTreasury(uploadedFilenames);
                            this.updateBASStatuses(uploadedFilenames);
                        }
                    }
                }
                finally {
                    Closeables.close(ftpClient);
                }
            }

            if (this.running) {
                this.sleepAWhile();
            }
        }
        this.logger.info("Done");
    }

    public void requestShutdown() {
        this.running = false;
    }

    /**
     * Returns a batch of {@linkplain BATCH_SIZE} or fewer files.
     */
    private Collection<File> getBatch() {
        final File directory = new File(this.workDirectory + File.separatorChar + this.outputDirectory);

        if (directory.isDirectory()) {
            String[] files = directory.list(new FilenameFilter() {
                private int count = 0;

                public boolean accept(File dir, String name) {
                    return BAS_FILENAME_REGEX.matcher(name).matches() && this.count++ < batchSize;
                }
            });

            return Collections2.transform(Arrays.asList(files), new Function<String, File>() {
                public File apply(String input) {
                    return new File(directory, input);
                }
            });
        }
        return Collections.emptyList();
    }

    /**
     * @return true when Treasury still has files to process; false otherwise
     */
    private boolean isWaitingForTreasury(FTPClient ftpClient) {
        this.logger.debug("Counting previously uploaded files");

        try {
            String[] previouslyUploadedFiles = ftpClient.listNames();

            if (previouslyUploadedFiles.length > 0) {
                // ignore files that we did not upload (filenames don't match)
                for (String filename : previouslyUploadedFiles) {
                    if (BAS_FILENAME_REGEX.matcher(filename).matches()) {
                        return true;
                    }
                }
            }
        }
        catch (IOException ioe) {
            this.logger.error("Failure counting previously uploaded files", ioe);
        }
        return false;
    }

    private Collection<String> upload(FTPClient ftpClient, Collection<File> basFiles) {
        Collection<String> uploaded = Lists.newArrayList();

        this.logger.info("Uploading {} BAS files", basFiles.size());

        for (File basFile : basFiles) {
            InputStream is = null;

            try {
                is = new BufferedInputStream(new FileInputStream(basFile));

                if (FTPClients.setFileTypeToBinary(ftpClient)) {
                    this.logger.debug("Uploading file [{}]", basFile.getName());

                    if (ftpClient.storeFile(basFile.getName(), is)) {
                        Closeables.close(is);   // for Windows: cannot move (delete) a file if it is still "open"
                        this.moveToSentDirectory(basFile);
                        uploaded.add(basFile.getName());
                    }
                    else {
                        this.logger.error("Failure uploading file [{}]", basFile.getName());
                    }
                }
            }
            catch (IOException ioe) {
                this.logger.error("Failure uploading file", ioe);
            }
            finally {
                Closeables.close(is);
            }
        }
        this.logger.info("Uploaded {} BAS files", uploaded.size());
        return uploaded;
    }

    private boolean moveToSentDirectory(File from) {
        File to = new File(this.workDirectory + File.separatorChar + this.sentDirectory + File.separatorChar + from.getName());

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

    private void sleepAWhile() {
        try {
            this.logger.debug("Sleeping for {} minutes", this.sleepMinutes);
            TimeUnit.MINUTES.sleep(this.sleepMinutes);
        }
        catch (InterruptedException ie) {
            this.logger.warn("Sleep interrupted", ie);
        }
    }

    private void updateBASStatuses(Collection<String> uploadedFilenames) {
        Connection connection = null;

        try {
            connection = this.connectionProvider.get();

            // TODO: we are doing batch updates, the connection shouldn't be auto-commit
            for (Map.Entry<String, Integer> entry : BASReconDAO.getBASStatuses(connection, uploadedFilenames).entrySet()) {
                String filename = entry.getKey();
                int status = entry.getValue();

                if (status == 1) {  // Created
                    BASReconDAO.updateBASStatus(connection, filename, 2);   // Sent
                }
                else {
                    BASReconDAO.updateBASStatus(connection, filename, 4);   // Resent
                }
            }
        }
        catch (SQLException sqle) {
            this.logger.error("Failure updated BAS statuses", sqle);
        }
        finally {
            Closeables.close(connection);
        }
    }
}
