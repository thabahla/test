package za.co.interfile.bas.util;

import java.io.IOException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Theuns Cloete
 */
public final class FTPClients {
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPClients.class);

    private FTPClients() {
    }

    public static boolean changeWorkingDirectory(FTPClient ftpClient, String workingDirectory) {
        try {
            LOGGER.trace("Changing FTP working directory [{}]", workingDirectory);

            if (ftpClient.changeWorkingDirectory(workingDirectory)) {
                return true;
            }
            else {
                LOGGER.error("Failure changing FTP working directory [{}]", workingDirectory);
            }
        }
        catch (IOException ioe) {
            LOGGER.error("Failure changing FTP working directory", ioe);
        }
        return false;
    }

    public static boolean setFileTypeToBinary(FTPClient ftpClient) {
        LOGGER.trace("Setting file type to BINARY");

        try {
            if (ftpClient.setFileType(FTP.BINARY_FILE_TYPE)) {
                return true;
            }
            LOGGER.error("Failure setting file type to BINARY");
        }
        catch (IOException ioe) {
            LOGGER.error("Failure setting file type to BINARY", ioe);
        }
        return false;
    }
}
