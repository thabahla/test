package za.co.interfile.bas.bean;

import com.google.common.base.Objects;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;

public class TCBASInterfaceFile {
    private static final DecimalFormat SERIAL_NUMBER_FORMAT = new DecimalFormat("0000000000");

    private final long pkID;
    private final Timestamp dateCreated;
    private final int fkInterfaceFileStatusID;
    private final Timestamp editedDateTime;
    private final String fileName;
    private final String backUpURL;
    private final String creationReason;

    public TCBASInterfaceFile(long pkID, Timestamp dateCreated, int fkInterfaceFileStatusID, Timestamp editedDateTime, String fileName, String backUpURL, String creationReason) {
        this.pkID = pkID;
        this.dateCreated = dateCreated;
        this.fkInterfaceFileStatusID = fkInterfaceFileStatusID;
        this.editedDateTime = editedDateTime;
        this.fileName = fileName;
        this.backUpURL = backUpURL;
        this.creationReason = creationReason;
    }

    public long getPKID() {
        return this.pkID;
    }

    public Date getDateCreated() {
        return this.dateCreated;
    }

    public int getFKInterfaceFileStatusID() {
        return this.fkInterfaceFileStatusID;
    }

    public Date getEditedDateTime() {
        return this.editedDateTime;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getBackUpURL() {
        return this.backUpURL;
    }

    public String getCreationReason() {
        return this.creationReason;
    }

    public String getSerialNumber() {
        return SERIAL_NUMBER_FORMAT.format(this.pkID);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(TCBASInterfaceFile.class)
            .add("pkID", this.pkID)
            .add("dateCreated", this.dateCreated)
            .add("basFileStatus", this.fkInterfaceFileStatusID)
            .add("editedDate", this.editedDateTime)
            .add("fileName", this.fileName)
            .toString();
    }
}
