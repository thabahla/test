package za.co.interfile.bas.bean;

public class TcCheque {

    private double amount;
    private String chequeDate;
    private String chequeNumber;
    private String drawerName;
    private String editedDateTime;
    private int fkEditedByUserId;
    private int fkInstitutionEntityId;
    private int fkPaymentTransactionId;
    private int pkChequeId;
    private int fkEntityLastEditedById;
    private int fkPaymentId;
    private String finInstitution;

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getChequeDate() {
        return this.chequeDate;
    }

    public void setChequeDate(String chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getChequeNumber() {
        return this.chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getDrawerName() {
        return this.drawerName;
    }

    public void setDrawerName(String drawerName) {
        this.drawerName = drawerName;
    }

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String editedDateTime) {
        this.editedDateTime = editedDateTime;
    }

    public int getFkEditedByUserId() {
        return this.fkEditedByUserId;
    }

    public void setFkEditedByUserId(int fkEditedByUserId) {
        this.fkEditedByUserId = fkEditedByUserId;
    }

    public int getFkInstitutionEntityId() {
        return this.fkInstitutionEntityId;
    }

    public void setFkInstitutionEntityId(int fkInstitutionEntityId) {
        this.fkInstitutionEntityId = fkInstitutionEntityId;
    }

    public int getFkPaymentTransactionId() {
        return this.fkPaymentTransactionId;
    }

    public void setFkPaymentTransactionId(int fkPaymentTransactionId) {
        this.fkPaymentTransactionId = fkPaymentTransactionId;
    }

    public int getPkChequeId() {
        return this.pkChequeId;
    }

    public void setPkChequeId(int pkChequeId) {
        this.pkChequeId = pkChequeId;
    }

    public int getFkEntityLastEditedById() {
        return this.fkEntityLastEditedById;
    }

    public void setFkEntityLastEditedById(int fkEntityLastEditedById) {
        this.fkEntityLastEditedById = fkEntityLastEditedById;
    }

    public String toString() {
        return " [ amount = " + this.amount + ", chequeDate =" + this.chequeDate + ", chequeNumber=" + this.chequeNumber
                + ", drawerName=" + this.drawerName + ", editedDateTime=" + this.editedDateTime + ", fkEditedByUserId="
                + this.fkEditedByUserId + ", fkInstitutionEntityId=" + this.fkInstitutionEntityId + " ,"
                + "fkPaymentTransactionId=" + this.fkPaymentTransactionId + " ,pkChequeId = " + this.pkChequeId
                + ",fkEntityLastEditedById= " + this.fkEntityLastEditedById + " ,fkPaymentId=" + this.fkPaymentId + "]";
    }

    public int getFkPaymentId() {
        return this.fkPaymentId;
    }

    public void setFkPaymentId(int fkPaymentId) {
        this.fkPaymentId = fkPaymentId;
    }

    public String getFinInstitution() {
        return this.finInstitution;
    }

    public void setFinInstitution(String finInstitution) {
        this.finInstitution = finInstitution;
    }
}
