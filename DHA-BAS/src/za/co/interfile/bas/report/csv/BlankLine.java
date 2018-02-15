package za.co.interfile.bas.report.csv;

/**
 * @author Theuns Cloete
 */
public class BlankLine implements ReportItem {
    private static final String BLANK = "";

    @Override
    public String toString() {
        return BLANK;
    }
}
