package za.co.interfile.bas.report.csv;

/**
 * @author Theuns Cloete
 */
public class ProcessedHeader implements ReportItem {
    private static final String HEADER = "RESPONSEFILENAME,BASFILEID,STATUS,REASON,SITE,DEPOSITDATE,DEPOSITORREFERENCE,AMOUNT,BASFILENAME";

    @Override
    public String toString() {
        return new StringBuilder()
            .append("The following entries have been processed and we were able to determine a status (outcome or result)")
            .append(System.getProperty("line.separator"))
            .append(HEADER)
            .toString();
    }
}
