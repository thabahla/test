package za.co.interfile.bas;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.interfile.bas.audit.BASAuditor;
import za.co.interfile.bas.build.BASBuilder;
import za.co.interfile.bas.guice.ConfigurationModule;
import za.co.interfile.bas.guice.DatabaseModule;
import za.co.interfile.bas.guice.FTPModule;
import za.co.interfile.bas.guice.FacilityModule;
import za.co.interfile.bas.report.BASReporter;
import za.co.interfile.bas.upload.BASUploader;

/**
 * @author Theuns Cloete
 */
public class BasicAccountingSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAccountingSystem.class);

    public static void main(String[] args) {
        try {
            final TaskType taskType = TaskType.valueOf(System.getProperty("task", TaskType.BUILD.toString()));
        	 //final TaskType taskType = TaskType.valueOf(System.getProperty("task", TaskType.AUDIT.toString()));
            final Task task = BasicAccountingSystem.spawn(taskType);

            if (task != null) {
                Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
                    @Override
                    public void run() {
                        task.requestShutdown();
                        LOGGER.info("Shutdown requested; waiting for {} to finish", taskType);
                    }
                });

                task.run();
            }
        }
        catch (IllegalArgumentException iae) {
            LOGGER.info("usage: java -Ddir.work=[WORK_DIR] -Dconfig=[CONFIG] -Dtask=[TASK] -jar <JAR>");
            LOGGER.info("where:");
            LOGGER.info("\t[WORK_DIR] = absolute path to the working directory where all files will be generated etc.");
            LOGGER.info("\t  [CONFIG] = absolute path to a custom properties file");
            LOGGER.info("\t    [TASK] = one of {} (default: {})", TaskType.values(), TaskType.BUILD);
            LOGGER.info("\t     <JAR> = absolute path to the jar file (if you are reading this, then the JAR file is being used)");
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static Task spawn(TaskType taskType) {
        Injector injector = Guice.createInjector(new ConfigurationModule(System.getProperty("config")), new DatabaseModule(), new FTPModule(), new FacilityModule());

        switch (taskType) {
            case AUDIT: return injector.getInstance(BASAuditor.class);      // new
            case BUILD: return injector.getInstance(BASBuilder.class);      // old, refactored
            case UPLOAD: return injector.getInstance(BASUploader.class);    // new
            case REPORT: return injector.getInstance(BASReporter.class);    // new
            default: return null;
        }
    }
}
