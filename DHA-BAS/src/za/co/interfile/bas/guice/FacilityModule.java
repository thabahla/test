package za.co.interfile.bas.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import za.co.interfile.bas.util.Closeables;

/**
 * @author Theuns Cloete
 */
public class FacilityModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(Properties.class).annotatedWith(Names.named("facilities")).toInstance(this.loadFacilities());
    }

    private Properties loadFacilities() {
        InputStream facilityResource = null;

        try {
            Properties properties = new Properties();

            facilityResource = ClassLoader.getSystemResourceAsStream("facility.properties");
            properties.load(facilityResource);
            return properties;
        }
        catch (IOException ioe) {
            throw new RuntimeException("Failed to load facilities from resource", ioe);
        }
        finally {
            Closeables.close(facilityResource);
        }
    }
}
