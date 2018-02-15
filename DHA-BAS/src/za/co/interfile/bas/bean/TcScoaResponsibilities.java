package za.co.interfile.bas.bean;

public class TcScoaResponsibilities {

    private int pkScoaResponsibilityId;
    private String responsibilityName;
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

    public int getPkScoaResponsibilityId() {
        return this.pkScoaResponsibilityId;
    }

    public void setPkScoaResponsibilityId(int pkScoaResponsibilityId) {
        this.pkScoaResponsibilityId = pkScoaResponsibilityId;
    }

    public String getResponsibilityName() {
        return this.responsibilityName;
    }

    public void setResponsibilityName(String responsibilityName) {
        this.responsibilityName = responsibilityName;
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
        return "[ pkScoaResponsibilityId = " + this.pkScoaResponsibilityId + " "
                + " responsibilityName =" + this.responsibilityName + " "
                + " editedDateTime = " + this.editedDateTime + " "
                + " fkEntityLastEditedById = " + this.fkEntityLastEditedById + " "
                + " accountNumber = " + this.accountNumber + " ]";
    }
}
