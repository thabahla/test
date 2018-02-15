package za.co.interfile.bas.bean;

import java.util.List;

public class TcPaymentTransactions {

    private int pfkPaymentTransactionId;
    private double paymentAmount;
    private int fkPaymentChannelId;
    private String paymentReference;
    private int fkEditedByUserId;
    private String editedDateTime;
    private int fkPaymentSystemId;
    private int fkShiftId;
    private String shiftStatus;
    private int shiftNumber;
    private int fkPaymentMethodId;
    private int fkPaymentStatusId;
    private double cashTendered;
    private double cashChange;
    private String channel;
    private String method;
    private String facility;
    private String cashier;
    private int cashierId;
    private int facilityId;
    private int receiptNumber;
    private String paymentSystem;
    private String paymentStatus;
    private int customerId;
    private String customerName;
    private String accountNumber;
    private double accountBalance;
    private int accountId;
    private long invoiceId;
    private String invoiceNumber;
    private String invoiceDate;
    private String invoiceStatus;
    private String invoiceGenerationDate;
    private int isReversal;
    private int isReversed;
    private int fkReversedTransactionId;
    private List<TcInvoiceLineItems> lineItems;
    private String paymentMethod;
    private int fkCashierDepositId;
    private TcCheque cheque;
    private TcPostalOrder postalOrder;
    private String ccExpiryDate;
    private String chequeDate;
    private String chequeNumber;
    private String ccCardNumber;

    public TcPostalOrder getPostalOrder() {
        return this.postalOrder;
    }

    public void setPostalOrder(TcPostalOrder postalOrder) {
        this.postalOrder = postalOrder;
    }

    public double getCashChange() {
        return this.cashChange;
    }

    public void setCashChange(double cashChange) {
        this.cashChange = cashChange;
    }

    public double getCashTendered() {
        return this.cashTendered;
    }

    public void setCashTendered(double cashTendered) {
        this.cashTendered = cashTendered;
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

    public int getFkPaymentChannelId() {
        return this.fkPaymentChannelId;
    }

    public void setFkPaymentChannelId(int fkPaymentChannel) {
        this.fkPaymentChannelId = fkPaymentChannel;
    }

    public int getFkPaymentMethodId() {
        return this.fkPaymentMethodId;
    }

    public void setFkPaymentMethodId(int fkPaymentMethodId) {
        this.fkPaymentMethodId = fkPaymentMethodId;
    }

    public int getFkPaymentStatusId() {
        return this.fkPaymentStatusId;
    }

    public void setFkPaymentStatusId(int fkPaymentStatusId) {
        this.fkPaymentStatusId = fkPaymentStatusId;
    }

    public int getFkPaymentSystemId() {
        return this.fkPaymentSystemId;
    }

    public void setFkPaymentSystemId(int fkPaymentSystemId) {
        this.fkPaymentSystemId = fkPaymentSystemId;
    }

    public int getFkShiftId() {
        return this.fkShiftId;
    }

    public void setFkShiftId(int fkShiftId) {
        this.fkShiftId = fkShiftId;
    }

    public double getPaymentAmount() {
        return this.paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentReference() {
        return this.paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public int getPfkPaymentTransactionId() {
        return this.pfkPaymentTransactionId;
    }

    public void setPfkPaymentTransactionId(int pfkPaymentTransactionId) {
        this.pfkPaymentTransactionId = pfkPaymentTransactionId;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getFacility() {
        return this.facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getCashier() {
        return this.cashier;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public int getCashierId() {
        return this.cashierId;
    }

    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    public int getFacilityId() {
        return this.facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public int getReceiptNumber() {
        return this.receiptNumber;
    }

    public void setReceiptNumber(int receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getPaymentSystem() {
        return this.paymentSystem;
    }

    public void setPaymentSystem(String paymentSystem) {
        this.paymentSystem = paymentSystem;
    }

    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceDate() {
        return this.invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceStatus() {
        return this.invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getInvoiceGenerationDate() {
        return this.invoiceGenerationDate;
    }

    public void setInvoiceGenerationDate(String invoiceGenerationDate) {
        this.invoiceGenerationDate = invoiceGenerationDate;
    }

    public int getIsReversal() {
        return this.isReversal;
    }

    public void setIsReversal(int isReversal) {
        this.isReversal = isReversal;
    }

    public String getShiftStatus() {
        return this.shiftStatus;
    }

    public void setShiftStatus(String shiftStatus) {
        this.shiftStatus = shiftStatus;
    }

    public int getShiftNumber() {
        return this.shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getIsReversed() {
        return this.isReversed;
    }

    public void setIsReversed(int isReversed) {
        this.isReversed = isReversed;
    }

    public int getFkReversedTransactionId() {
        return this.fkReversedTransactionId;
    }

    public void setFkReversedTransactionId(int fkReversedTransactionId) {
        this.fkReversedTransactionId = fkReversedTransactionId;
    }

    public List<TcInvoiceLineItems> getLineItems() {
        return this.lineItems;
    }

    public void setLineItems(List<TcInvoiceLineItems> lineItems) {
        this.lineItems = lineItems;
    }

    public double getAccountBalance() {
        return this.accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getFkCashierDepositId() {
        return this.fkCashierDepositId;
    }

    public void setFkCashierDepositId(int fkCashierDepositId) {
        this.fkCashierDepositId = fkCashierDepositId;
    }

    public String toString() {
        return " [ pfkPaymentTransactionId=" + this.pfkPaymentTransactionId
                + " paymentAmount=" + this.paymentAmount
                + " fkPaymentChannelId=" + this.fkPaymentChannelId
                + " paymentReference=" + this.paymentReference
                + " fkEditedByUserId=" + this.fkEditedByUserId
                + " editedDateTime=" + this.editedDateTime
                + " fkPaymentSystemId=" + this.fkPaymentSystemId
                + " fkShiftId=" + this.fkShiftId
                + " shiftStatus=" + this.shiftStatus
                + " shiftNumber=" + this.shiftNumber
                + " fkPaymentMethodId=" + this.fkPaymentMethodId
                + " fkPaymentStatusId=" + this.fkPaymentStatusId
                + " cashTendered=" + this.cashTendered
                + " cashChange=" + this.cashChange
                + " channel=" + this.channel
                + " method=" + this.method
                + " facility=" + this.facility
                + " cashier=" + this.cashier
                + " cashierId=" + this.cashierId
                + " facilityId=" + this.facilityId
                + " receiptNumber=" + this.receiptNumber
                + " paymentSystem=" + this.paymentSystem
                + " paymentStatus=" + this.paymentStatus
                + " customerId=" + this.customerId
                + " customerName=" + this.customerName
                + " accountNumber=" + this.accountNumber
                + " accountBalance=" + this.accountBalance
                + " accountId=" + this.accountId
                + " invoiceNumber=" + this.invoiceNumber
                + " invoiceDate=" + this.invoiceDate
                + " invoiceStatus=" + this.invoiceStatus
                + " invoiceGenerationDate=" + this.invoiceGenerationDate
                + " isReversal=" + this.isReversal
                + " isReversed=" + this.isReversed
                + " fkReversedTransactionId=" + this.fkReversedTransactionId
                + " lineItems=" + this.lineItems
                + " paymentMethod=" + this.paymentMethod
                + " fkCashierDepositId=" + this.fkCashierDepositId + "]";
    }

    public long getInvoiceId() {
        return this.invoiceId;
    }

    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public TcCheque getCheque() {
        return this.cheque;
    }

    public void setCheque(TcCheque cheque) {
        this.cheque = cheque;
    }

    public String getCcExpiryDate() {
        return this.ccExpiryDate;
    }

    public void setCcExpiryDate(String ccExpiryDate) {
        this.ccExpiryDate = ccExpiryDate;
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

    public String getCcCardNumber() {
        return this.ccCardNumber;
    }

    public void setCcCardNumber(String ccCardNumber) {
        this.ccCardNumber = ccCardNumber;
    }
}
