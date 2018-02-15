package za.co.interfile.bas.bean;

public class TcScoaRegion {

    private int pkScoaRegionId;
    private String regionName;
    private String editedDateTime;
    private int fkEntityLastEditedById;
    private String accountNumber;
    private short isDeleted;

    public int getPkScoaRegionId() {
        return this.pkScoaRegionId;
    }

    public void setPkScoaRegionId(int pkScoaRegionId) {
        this.pkScoaRegionId = pkScoaRegionId;
    }

    public String getRegionName() {
        return this.regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
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

    public short getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(short isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String toString() {
        return " [ pkScoaRegionId = " + this.pkScoaRegionId
                + " regionName = " + this.regionName
                + " editedDateTime = " + this.editedDateTime
                + " fkEntityLastEditedById = " + this.fkEntityLastEditedById
                + " accountNumber = " + this.accountNumber
                + " isDeleted = " + this.isDeleted + " ]";
    }
}
