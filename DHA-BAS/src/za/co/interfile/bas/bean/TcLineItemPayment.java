package za.co.interfile.bas.bean;

public class TcLineItemPayment {

    private int pkLineItemPaymentId;
    private double amount;
    private int fkEntityLastEditedById;
    private String editedDateTime;
    private int fkPaymentTransactionId;
    private int fkLineItemId;

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public int getFkLineItemId() {
        return this.fkLineItemId;
    }

    public void setFkLineItemId(int fkLineItemId) {
        this.fkLineItemId = fkLineItemId;
    }

    public int getFkPaymentTransactionId() {
        return this.fkPaymentTransactionId;
    }

    public void setFkPaymentTransactionId(int fkPaymentTransactionId) {
        this.fkPaymentTransactionId = fkPaymentTransactionId;
    }

    public int getPkLineItemPaymentId() {
        return this.pkLineItemPaymentId;
    }

    public void setPkLineItemPaymentId(int pkLineItemPaymentId) {
        this.pkLineItemPaymentId = pkLineItemPaymentId;
    }
}
