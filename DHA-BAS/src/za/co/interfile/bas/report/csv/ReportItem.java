package za.co.interfile.bas.report.csv;

import za.co.interfile.bas.util.CSVs;
/**
 * @author Theuns Cloete
 */
public interface ReportItem {
    /**
     * Retrieve this entry's Comma Separated representation.
     * @return a comma separated String with all the data of this object. Null values should be converted to blank values and
     * values containing commands should be enclosed in quotation marks, i.e. "this value contains, a comma"
     * @see {@linkplain CSVs#clean(java.lang.Object)}
     */
    @Override
    String toString();
}
