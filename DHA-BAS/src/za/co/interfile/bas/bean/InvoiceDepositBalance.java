package za.co.interfile.bas.bean;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Theuns Cloete
 */
public class InvoiceDepositBalance {
    private final Logger logger = LoggerFactory.getLogger(InvoiceDepositBalance.class);
    private final Recon recon;
    private final Map<Long, Long> invoiceTransactions;
    private final Map<Long, Long> depositTransactions;

    public InvoiceDepositBalance(Recon recon, Map<Long, Long> invoiceTransactions, Map<Long, Long> depositTransactions) {
        this.recon = recon;
        this.invoiceTransactions = invoiceTransactions;
        this.depositTransactions = depositTransactions;
    }

    public long getReconID() {
        return this.recon.getReconID();
    }

    public String getReconDate() {
        return this.recon.getReconDate();
    }

    public long getFacility() {
        return this.recon.getFacilityID();
    }

    public Set<Long> getDayEnds() {
        return this.recon.getDayEnds();
    }

    public Set<Long> getDeposits() {
        return this.recon.getDeposits();
    }

    public Set<Long> getInvoiceTransactions() {
        return this.invoiceTransactions.keySet();
    }

    public Set<Long> getDepositTransactions() {
        return this.depositTransactions.keySet();
    }

    public boolean balances() {
        long invoicesTotal = this.calculateTotal(this.invoiceTransactions);
        long depositsTotal = this.calculateTotal(this.depositTransactions);

        if (invoicesTotal == depositsTotal) {
            logger.debug("Invoice and deposit totals balance [invoicesTotal=" + invoicesTotal + ",depositsTotal=" + depositsTotal + "] " + recon);
            return true;
        }
        logger.warn("Invoice and deposit totals DO NOT balance [invoicesTotal=" + invoicesTotal + ",depositsTotal=" + depositsTotal + "]" + recon);
        return false;
    }

    public long getInvoicesTotal() {
        return this.calculateTotal(this.invoiceTransactions);
    }

    public long getDepositsTotal() {
        return this.calculateTotal(this.depositTransactions);
    }

    private long calculateTotal(Map<?, Long> map) {
        long total = 0L;

        for (long value : map.values()) {
            total += value;
        }
        return total;
    }
}
