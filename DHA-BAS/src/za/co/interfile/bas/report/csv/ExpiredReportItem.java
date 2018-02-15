package za.co.interfile.bas.report.csv;

import java.sql.Timestamp;
import za.co.interfile.bas.util.CSVs;

/**
 * @author Theuns Cloete
 */
public class ExpiredReportItem implements ReportItem {
    private final Long basFileID;
    private final Timestamp dateCreated;
    private final String filename;
    private final String status;
    private final Timestamp editedDate;

    public ExpiredReportItem(Long basFileID, Timestamp dateCreated, String filename, String status, Timestamp editedDate) {
        this.basFileID = basFileID;
        this.dateCreated = dateCreated;
        this.filename = filename;
        this.status = status;
        this.editedDate = editedDate;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append(CSVs.clean(this.basFileID)).append(',')
            .append(CSVs.clean(this.dateCreated)).append(',')
            .append(CSVs.clean(this.filename)).append(',')
            .append(CSVs.clean(this.status)).append(',')
            .append(CSVs.clean(this.editedDate))
            .toString();
    }
}
