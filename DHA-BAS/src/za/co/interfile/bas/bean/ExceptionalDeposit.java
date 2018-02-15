package za.co.interfile.bas.bean;

import com.google.common.base.Objects;
import java.util.Date;
/**
 * @author Theuns Cloete
 */
public class ExceptionalDeposit {
    private final Date depositDate;
    private final String depositOrReference;
    private final long depositTransactionID;
    private final long dayEndID;
    private final int depositStatusID;
    private final long exceptionAmount;
    private final long depositGrandTotal;

    public ExceptionalDeposit(Date depositDate, String depositOrReference, long depositTransactionID, long dayEndID, int depositStatusID, long exceptionAmount, long depositGrandTotal) {
        this.depositDate = depositDate;
        this.depositOrReference = depositOrReference;
        this.depositTransactionID = depositTransactionID;
        this.dayEndID = dayEndID;
        this.depositStatusID = depositStatusID;
        this.exceptionAmount = exceptionAmount;
        this.depositGrandTotal = depositGrandTotal;
    }

    public long getDepositTransactionID() {
        return this.depositTransactionID;
    }

    public boolean isConfirmedDeposit() {
        return this.depositStatusID == 2;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(ExceptionalDeposit.class)
            .add("depositDate", this.depositDate)
            .add("depositOrReference", this.depositOrReference)
            .add("depositTransaction", this.depositTransactionID)
            .add("dayEnd", this.dayEndID)
            .add("depositStatus", this.depositStatusID)
            .add("exceptionAmount", this.exceptionAmount)
            .add("depositGrantTotal", this.depositGrandTotal)
            .toString();
    }
}
