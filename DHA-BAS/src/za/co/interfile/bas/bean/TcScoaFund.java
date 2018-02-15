package za.co.interfile.bas.bean;

public class TcScoaFund {

    private String fundName;
    private int scoaFundId;
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

    public String getFundName() {
        return this.fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public int getScoaFundId() {
        return this.scoaFundId;
    }

    public void setScoaFundId(int scoaFundId) {
        this.scoaFundId = scoaFundId;
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
        return " [Fund Name = " + this.fundName + " ]\n"
                + " [Scoa Fund Id = " + this.scoaFundId + " ]\n"
                + " [editedDateTime " + this.editedDateTime + " ]\n"
                + " [fkEntityLastEditedById " + this.fkEntityLastEditedById + " ]\n"
                + " [accountNumber = " + this.accountNumber + " ]\n";
    }
}
