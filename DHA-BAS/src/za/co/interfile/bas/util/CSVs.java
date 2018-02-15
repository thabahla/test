package za.co.interfile.bas.util;

/**
 * @author Theuns Cloete
 */
public final class CSVs {
    private CSVs() {
    }

    public static String clean(Object object) {
        if (object == null) {
            return "";
        }

        String content = object.toString().trim();

        return content.contains(",") ? "\"" + content + "\"" : content;
    }
}
