package za.co.interfile.bas.guice;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Theuns Cloete
 */
public class FTPModule implements Module {

    public void configure(Binder binder) {
        binder.bind(FTPClient.class).toProvider(FTPClientProvider.class);
    }

    private static class FTPClientProvider implements Provider<FTPClient> {
        private final Logger logger = LoggerFactory.getLogger(FTPClientProvider.class);
        private final String host;
        private final int port;
        private final String username;
        private final String password;

        @Inject
        public FTPClientProvider(@Named("ftp.host") String host, @Named("ftp.port") int port, @Named("ftp.username") String username, @Named("ftp.password") String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        private String connectionString() {
            return String.format("ftp://%s@%s:%d", this.username, this.host, this.port);
        }

        public FTPClient get() {
            try {
                FTPClient ftpClient = new FTPClient();

                this.logger.debug("Opening connection to {}", this.connectionString());
                ftpClient.connect(this.host, this.port);
                this.logger.debug("Logging into {}", this.connectionString());
                ftpClient.login(this.username, this.password);

                int replyCode = ftpClient.getReplyCode();

                if (FTPReply.isPositiveCompletion(replyCode)) {
                    this.logger.debug("Entering local PASSIVE mode");
                    ftpClient.enterLocalPassiveMode();
                    replyCode = ftpClient.getReplyCode();

                    if (FTPReply.isPositiveCompletion(replyCode)) {
                        return ftpClient;
                    }
                    else {
                        this.logger.error("Failure entering local PASSIVE mode [{}]", ftpClient.getReplyString());
                    }
                }
                else {
                    this.logger.error("Failure logging into {} [{}]", this.connectionString(), ftpClient.getReplyString());
                }
            }
            catch (Exception e) {
                this.logger.error("Failure opening FTP session to " + this.connectionString(), e);
            }
            return null;
        }
    }
}
