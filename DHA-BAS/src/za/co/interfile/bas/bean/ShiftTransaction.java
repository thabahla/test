package za.co.interfile.bas.bean;

/**
 * @author Theuns Cloete
 */
public class ShiftTransaction {
    private final long invoiceTransaction;
    private final boolean manualPayment;
    private final long amountPaid;

    public ShiftTransaction(long invoiceTransaction, boolean manualPayment, long amountPaid) {
        this.invoiceTransaction = invoiceTransaction;
        this.manualPayment = manualPayment;
        this.amountPaid = amountPaid;
    }

    public long getInvoiceTransaction() {
        return this.invoiceTransaction;
    }

    public boolean isManualPayment() {
        return this.manualPayment;
    }

    public long getAmountPaid() {
        return this.amountPaid;
    }
}
