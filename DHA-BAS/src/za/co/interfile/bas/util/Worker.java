package za.co.interfile.bas.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME This is clearly a helper class. Needs to be final with private constructor and all methods should be static.
 * @author Theuns Cloete (not initially, just the guy who added these comments - so far)
 */
public class Worker {
    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);
    private static final DateFormat DATE_FORMAT_SLASH = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final DateFormat DATE_FORMAT_MINUS = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static String getDateTime() {
        return DATE_FORMAT_SLASH.format(new Date());
    }

    public static Timestamp stringToTimstamp(String dateString) {
        try {
            return new Timestamp(DATE_FORMAT_SLASH.parse(dateString).getTime());
        }
        catch (ParseException e) {
            LOGGER.warn("Failure parsing date [{}]", dateString);
        }
        return null;
    }

    public static String yyyymmddToddmmyyy(String yyyymmdd) {
        return new StringBuilder()
            .append(yyyymmdd.substring(6, 8))
            .append(yyyymmdd.substring(4, 6))
            .append(yyyymmdd.substring(0, 4))
            .toString();
    }
}
