package za.co.interfile.bas.bean;

public class TcPostalOrder {

    private double amount;
    private String date;
    private String editedDateTime;
    private int fkEditedByUserId;
    private int fkPaymentTransactionId;
    private int pkPostalOrderId;
    private String postalOrderNumber;
    private int fkPaymentId;

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public int getFkPaymentTransactionId() {
        return this.fkPaymentTransactionId;
    }

    public void setFkPaymentTransactionId(int fkPaymentTransactionId) {
        this.fkPaymentTransactionId = fkPaymentTransactionId;
    }

    public int getPkPostalOrderId() {
        return this.pkPostalOrderId;
    }

    public void setPkPostalOrderId(int pkPostalOrderId) {
        this.pkPostalOrderId = pkPostalOrderId;
    }

    public String getPostalOrderNumber() {
        return this.postalOrderNumber;
    }

    public void setPostalOrderNumber(String postalOrderNumber) {
        this.postalOrderNumber = postalOrderNumber;
    }

    public String toString() {
        return " [amount=" + this.amount + " ,date=" + this.date + " ,editedDateTime=" + this.editedDateTime
                + " ,fkEditedByUserId=" + this.fkEditedByUserId + " ,fkPaymentTransactionId=" + this.fkPaymentTransactionId
                + " ,pkPostalOrderId=" + this.pkPostalOrderId + " ,postalOrderNumber=" + this.postalOrderNumber
                + ", fkPaymentId =" + this.fkPaymentId + "]";
    }

    public int getFkPaymentId() {
        return this.fkPaymentId;
    }

    public void setFkPaymentId(int fkPaymentId) {
        this.fkPaymentId = fkPaymentId;
    }
}
