package za.co.interfile.bas.util;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Closeables {
    private static final Logger LOGGER = LoggerFactory.getLogger(Closeables.class);

    private Closeables() {
    }

    /**
     * Helper method to close connections. If the connection is a pooled connection then it will be returned to the connection
     * pool.
     */
    public static void close(Connection connection) {
        if (connection != null) {
            try {
                LOGGER.trace("Returning DB connection to pool");
                connection.close();
            }
            catch (SQLException sqle) {
                LOGGER.warn("Failure returning/closing connection", sqle);
            }
        }
    }

    /**
     * Helper method to close statements.
     */
    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException sqle) {
                LOGGER.warn("Failure closing statement", sqle);
            }
        }
    }

    /**
     * Helper method to close result sets.
     */
    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            }
            catch (SQLException sqle) {
                LOGGER.warn("Failure closing result set", sqle);
            }
        }
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException ioe) {
                LOGGER.warn("Failure closing Closeable", ioe);
            }
        }
    }

    public static void close(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                LOGGER.trace("Closing FTP session");
                ftpClient.logout();
                ftpClient.disconnect();
            }
            catch (IOException ioe) {
                LOGGER.error("Failure closing FTP session", ioe);
            }
        }
    }
}
