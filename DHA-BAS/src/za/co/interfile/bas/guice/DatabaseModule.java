package za.co.interfile.bas.guice;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.mchange.v2.c3p0.DataSources;
import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Theuns Cloete
 */
public class DatabaseModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(DataSource.class).toProvider(DataSourceProvider.class).asEagerSingleton();
        binder.bind(Connection.class).toProvider(ConnectionProvider.class);
    }

    private static class DataSourceProvider implements Provider<DataSource> {
        private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
        private final Logger logger = LoggerFactory.getLogger(DataSourceProvider.class);
        private final String url;
        private final String username;
        private final String password;

        @Inject
        private DataSourceProvider(@Named("db.jdbc.url") String url, @Named("db.username") String username, @Named("db.password") String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        @Override
        public DataSource get() {
            try {
                this.logger.trace("Loading Oracle JDBC driver");
                Class.forName(ORACLE_DRIVER);
                this.logger.trace("Creating unpooled data source");
                DataSource unpooled = DataSources.unpooledDataSource(this.url, this.username, this.password);
                this.logger.trace("Creating pooled data source");
                return DataSources.pooledDataSource(unpooled);
            }
            catch (Exception e) {
                throw new RuntimeException("Unable to create data source", e);
            }
        }

    }

    private static class ConnectionProvider implements Provider<Connection> {
        private final Logger logger = LoggerFactory.getLogger(ConnectionProvider.class);
        private final DataSource pooled;

        @Inject
        private ConnectionProvider(DataSource pooled) {
            this.pooled = pooled;
        }

        @Override
        public Connection get() {
            try {
                this.logger.trace("Retrieving connection from pool");
                return this.pooled.getConnection();
            }
            catch (Exception e) {
                throw new RuntimeException("Unable to retrieve pooled connection", e);
            }
        }
    }
}
