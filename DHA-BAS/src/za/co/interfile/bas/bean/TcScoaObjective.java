package za.co.interfile.bas.bean;

public class TcScoaObjective {

    private String objectiveName;
    private int scoaObjectiveId;
    private String editedDateTime;
    private int fkEntityLastEditedById;
    private String accountNumber;
    private short isDeleted;

    public short getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(short isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public void setObjectiveName(String objectiveName) {
        this.objectiveName = objectiveName;
    }

    public int getScoaObjectiveId() {
        return this.scoaObjectiveId;
    }

    public void setScoaObjectiveId(int scoaObjectiveId) {
        this.scoaObjectiveId = scoaObjectiveId;
    }

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String editedDateTime) {
        this.editedDateTime = editedDateTime;
    }

    public int getFkEntityLastEditedById() {
        return this.fkEntityLastEditedById;
    }

    public void setFkEntityLastEditedById(int fkEntityLastEditedById) {
        this.fkEntityLastEditedById = fkEntityLastEditedById;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String toString() {
        return " [objectiveName = " + this.objectiveName + " ]\n"
                + " [scoaObjectiveId = " + this.scoaObjectiveId + " ]\n"
                + " [editedDateTime = " + this.editedDateTime + " ]\n"
                + " [fkEntityLastEditedById = " + this.fkEntityLastEditedById + " ]\n"
                + "[accountNumber = " + this.accountNumber + " ]\n";
    }
}
