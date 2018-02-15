package za.co.interfile.bas.bean;

public class TcScoaAsset {

    private int pkScoaAssertId;
    private String assetName;
    private String editedDateTime;
    private int fkEntityLastEditedById;
    private String accountNumber;
    private short isDeleted;

    public int getPkScoaAssertId() {
        return this.pkScoaAssertId;
    }

    public void setPkScoaAssertId(int pkScoaAssertId) {
        this.pkScoaAssertId = pkScoaAssertId;
    }

    public String getAssetName() {
        return this.assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
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
        return " [ pkScoaAssertId = " + this.pkScoaAssertId
                + " assertName = " + this.assetName
                + " editedDateTime = " + this.editedDateTime
                + " fkEntityLastEditedById = " + this.fkEntityLastEditedById
                + " accountNumber = " + this.accountNumber
                + " isDeleted = " + this.isDeleted + " ]";
    }
}
