package za.co.interfile.bas.bean;

import java.util.List;

public class TcBankDeposits {

    private String depositDateTime;
    private String depositorReference;
    private String editedDateTime;
    private int fkBankAccountId;
    private int fkDepositStatusId;
    private int fkDepositTypeId;
    private int fkEditedByUserId;
    private int fkEntityDepositorId;
    private double grandTotal;
    private int pfkDepositTransactionId;
    private double subTotalCash;
    private double subTotalCheque;
    private double subTotalMoneyOrder;
    private double subTotalOther;
    private double subTotalPostalOrder;
    private double subTotalTravCheques;
    private String depositorName;
    private String depositType;
    private String depositStatus;
    private List<TcPaymentTransactions> paymentsList;

    public String getDepositDateTime() {
        return this.depositDateTime;
    }

    public void setDepositDateTime(String depositDateTime) {
        this.depositDateTime = depositDateTime;
    }

    public String getDepositorReference() {
        return this.depositorReference;
    }

    public void setDepositorReference(String depositorReference) {
        this.depositorReference = depositorReference;
    }

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String editedDateTime) {
        this.editedDateTime = editedDateTime;
    }

    public int getFkBankAccountId() {
        return this.fkBankAccountId;
    }

    public void setFkBankAccountId(int fkBankAccountId) {
        this.fkBankAccountId = fkBankAccountId;
    }

    public int getFkDepositStatusId() {
        return this.fkDepositStatusId;
    }

    public void setFkDepositStatusId(int fkDepositStatusId) {
        this.fkDepositStatusId = fkDepositStatusId;
    }

    public int getFkDepositTypeId() {
        return this.fkDepositTypeId;
    }

    public void setFkDepositTypeId(int fkDepositTypeId) {
        this.fkDepositTypeId = fkDepositTypeId;
    }

    public int getFkEditedByUserId() {
        return this.fkEditedByUserId;
    }

    public void setFkEditedByUserId(int fkEditedByUserId) {
        this.fkEditedByUserId = fkEditedByUserId;
    }

    public int getFkEntityDepositorId() {
        return this.fkEntityDepositorId;
    }

    public void setFkEntityDepositorId(int fkEntityDepositorId) {
        this.fkEntityDepositorId = fkEntityDepositorId;
    }

    public double getGrandTotal() {
        return this.grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public int getPfkDepositTransactionId() {
        return this.pfkDepositTransactionId;
    }

    public void setPfkDepositTransactionId(int pfkDepositTransactionId) {
        this.pfkDepositTransactionId = pfkDepositTransactionId;
    }

    public double getSubTotalCash() {
        return this.subTotalCash;
    }

    public void setSubTotalCash(double subTotalCash) {
        this.subTotalCash = subTotalCash;
    }

    public double getSubTotalCheque() {
        return this.subTotalCheque;
    }

    public void setSubTotalCheque(double subTotalCheque) {
        this.subTotalCheque = subTotalCheque;
    }

    public double getSubTotalMoneyOrder() {
        return this.subTotalMoneyOrder;
    }

    public void setSubTotalMoneyOrder(double subTotalMoneyOrder) {
        this.subTotalMoneyOrder = subTotalMoneyOrder;
    }

    public double getSubTotalOther() {
        return this.subTotalOther;
    }

    public void setSubTotalOther(double subTotalOther) {
        this.subTotalOther = subTotalOther;
    }

    public double getSubTotalPostalOrder() {
        return this.subTotalPostalOrder;
    }

    public void setSubTotalPostalOrder(double subTotalPostalOrder) {
        this.subTotalPostalOrder = subTotalPostalOrder;
    }

    public double getSubTotalTravCheques() {
        return this.subTotalTravCheques;
    }

    public void setSubTotalTravCheques(double subTotalTravCheques) {
        this.subTotalTravCheques = subTotalTravCheques;
    }

    public String getDepositorName() {
        return this.depositorName;
    }

    public void setDepositorName(String depositorName) {
        this.depositorName = depositorName;
    }

    public String getDepositType() {
        return this.depositType;
    }

    public void setDepositType(String depositType) {
        this.depositType = depositType;
    }

    public List<TcPaymentTransactions> getPaymentsList() {
        return this.paymentsList;
    }

    public void setPaymentsList(List<TcPaymentTransactions> paymentsList) {
        this.paymentsList = paymentsList;
    }

    public String getDepositStatus() {
        return this.depositStatus;
    }

    public void setDepositStatus(String depositStatus) {
        this.depositStatus = depositStatus;
    }
}
