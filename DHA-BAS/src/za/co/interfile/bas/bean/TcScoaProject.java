package za.co.interfile.bas.bean;

public class TcScoaProject {

    private String projectName;
    private int scoaProjectId;
    private String editedDateTime;
    private String accountNumber;
    private int fkEntityLastEditedById;
    private short isDeleted;

    public short getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(short isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getScoaProjectId() {
        return this.scoaProjectId;
    }

    public void setScoaProjectId(int scoaProjectId) {
        this.scoaProjectId = scoaProjectId;
    }

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String editedDateTime) {
        this.editedDateTime = editedDateTime;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getFkEntityLastEditedById() {
        return this.fkEntityLastEditedById;
    }

    public void setFkEntityLastEditedById(int fkEntityLastEditedById) {
        this.fkEntityLastEditedById = fkEntityLastEditedById;
    }

    public String toString() {
        return " [ projectName = " + this.projectName + " "
                + "  scoaProjectId = " + this.scoaProjectId + " "
                + "  editedDateTime = " + this.editedDateTime + " "
                + "  AccountNumber = " + this.accountNumber + "  "
                + "  fkEntityLastEditedById = " + this.fkEntityLastEditedById + " ]";
    }
}
