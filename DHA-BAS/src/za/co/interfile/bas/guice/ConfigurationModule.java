package za.co.interfile.bas.guice;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import za.co.interfile.bas.util.Closeables;

/**
 * @author Theuns Cloete
 */
public class ConfigurationModule implements Module {
    private static final String DEF_PROPERTIES = "dha.properties";
    private final String customProperties;

    public ConfigurationModule(String customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public void configure(Binder binder) {
        Names.bindProperties(binder, this.loadProperties());
        binder.bindConstant().annotatedWith(Names.named("dir.work")).to(System.getProperty("dir.work", "."));
        binder.bind(Properties.class).annotatedWith(Names.named("smtp.properties")).toProvider(SMTPPropertiesProvider.class).asEagerSingleton();
    }

    private Properties loadProperties() {
        Properties properties = new Properties();

        this.loadProperties(properties, ClassLoader.getSystemResourceAsStream(DEF_PROPERTIES));

        if (!Strings.isNullOrEmpty(this.customProperties)) {
            try {
                this.loadProperties(properties, new FileInputStream(customProperties));
            }
            catch (FileNotFoundException fnfe) {
                throw new RuntimeException(fnfe);
            }
        }
        return properties;
    }

    private void loadProperties(Properties properties, InputStream resource) {
        try {
            properties.load(resource);
        }
        catch (IOException ioe) {
            throw new RuntimeException("Failure loading configuration from properties resource", ioe);
        }
        finally {
            Closeables.close(resource);
        }
    }

    private InternetAddress[] splitAddresses(final String commaSeparated) {
        return Lists.transform(Arrays.asList(commaSeparated.split(",")), new Function<String, InternetAddress>() {
            public InternetAddress apply(String input) {
                try {
                    return new InternetAddress(input);
                }
                catch (AddressException ae) {
                    throw new RuntimeException("Invalid comma-separated list of e-mail addresses: " + commaSeparated, ae);
                }
            }
        }).toArray(new InternetAddress[0]);
    }

    @Singleton
    @Provides()
    @Named("addresses.reply.to")
    public InternetAddress[] provideReplyToAddresses(@Named("mail.reply.to") String replyToAddresses) {
        return this.splitAddresses(replyToAddresses);
    }

    @Singleton
    @Provides
    @Named("addresses.treasury")
    public InternetAddress[] provideTreasuryAddresses(@Named("mail.treasury") String treasuryAddresses) {
        return this.splitAddresses(treasuryAddresses);
    }

    @Singleton
    @Provides
    @Named("addresses.home.affairs")
    public InternetAddress[] provideHomeAffairsAddresses(@Named("mail.home.affairs") String homeAffairsAddresses) {
        return this.splitAddresses(homeAffairsAddresses);
    }

    @Singleton
    @Provides
    @Named("addresses.interfile")
    public InternetAddress[] provideInterfileAddresses(@Named("mail.interfile") String interfileAddresses) {
        return this.splitAddresses(interfileAddresses);
    }

    private static class SMTPPropertiesProvider implements Provider<Properties> {
        private final String host;
        private final int port;

        @Inject
        private SMTPPropertiesProvider(@Named("mail.smtp.host") String host, @Named("mail.smtp.port") int port) {
            this.host = host;
            this.port = port;
        }

        public Properties get() {
            Properties properties = new Properties();

            properties.put("mail.smtp.host", this.host);
            properties.put("mail.smtp.port", this.port);
            return properties;
        }
    }
}
