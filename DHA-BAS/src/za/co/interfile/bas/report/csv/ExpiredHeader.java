package za.co.interfile.bas.report.csv;

/**
 * @author Theuns Cloete
 */
public class ExpiredHeader implements ReportItem {
    private static final String HEADER = "BASFILEID,DATECREATED,FILENAME,STATUS,EDITEDDATETIME";

    private final int expiryDays;

    public ExpiredHeader(int expiryDays) {
        this.expiryDays = expiryDays;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append(String.format("The following entries have expired as we have not yet received a response (outcome or result) for them after %d days. They will be resent for processing.", this.expiryDays))
            .append(System.getProperty("line.separator"))
            .append(HEADER)
            .toString();
    }
}
