package za.co.interfile.bas.bean;

public class TcScoaItem {

    private String itemName;
    private int scoaItemId;
    private int fkEntityLastEditedById;
    private String editedDateTime;
    private String accountNumber;
    private short isDeleted;

    public short getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(short isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getScoaItemId() {
        return this.scoaItemId;
    }

    public void setScoaItemId(int scoaItemId) {
        this.scoaItemId = scoaItemId;
    }

    public int getFkEntityLastEditedById() {
        return this.fkEntityLastEditedById;
    }

    public void setFkEntityLastEditedById(int fkEntityLastEditedById) {
        this.fkEntityLastEditedById = fkEntityLastEditedById;
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

    public String toString() {
        return "[ Item Name = " + this.itemName + " ]\n"
                + "[ Scoa Item Id = " + this.scoaItemId + " ]\n"
                + "[ Entity LastEdited By Id = " + this.fkEntityLastEditedById + " ]\n"
                + "[ Edited Date Time = " + this.editedDateTime + "]\n"
                + "[ Account Number = " + this.accountNumber + " ]\n";
    }
}
