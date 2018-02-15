package za.co.interfile.bas.report.csv;

import java.sql.Timestamp;
import za.co.interfile.bas.util.CSVs;

/**
 * @author Theuns Cloete
 */
public class ProcessedReportItem implements ReportItem {
    private final String responseFilename;
    private final Long basFileID;
    private final String status;
    private final String reason;
    private final String site;
    private final Timestamp depositDate;
    private final Long depositorReference;
    private final Long amount;
    private final String basFilename;

    public ProcessedReportItem(String responseFilename, Long basFileID, String status, String reason, String site, Timestamp depositDate, Long depositorReference, Long amount, String basFilename) {
        this.responseFilename = responseFilename;
        this.basFileID = basFileID;
        this.status = status;
        this.reason = reason;
        this.site = site;
        this.depositDate = depositDate;
        this.depositorReference = depositorReference;
        this.amount = amount;
        this.basFilename = basFilename;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append(CSVs.clean(this.responseFilename)).append(',')
            .append(CSVs.clean(this.basFileID)).append(',')
            .append(CSVs.clean(this.status)).append(',')
            .append(CSVs.clean(this.reason)).append(',')
            .append(CSVs.clean(this.site)).append(',')
            .append(CSVs.clean(this.depositDate)).append(',')
            .append(CSVs.clean(this.depositorReference)).append(',')
            .append(CSVs.clean(this.amount)).append(',')
            .append(CSVs.clean(this.basFilename))
            .toString();
    }
}
