package za.co.interfile.bas.audit;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.interfile.bas.Task;
import za.co.interfile.bas.bean.ExceptionalDeposit;
import za.co.interfile.bas.bean.InvoiceDepositBalance;
import za.co.interfile.bas.bean.Recon;
import za.co.interfile.bas.dao.BASAuditDAO;
import za.co.interfile.bas.dao.BASReconDAO;
import za.co.interfile.bas.util.Closeables;

/**
 * @author Theuns Cloete
 */
public class BASAuditor implements Task {
    private final Logger logger = LoggerFactory.getLogger(BASAuditor.class);
    private final Provider<Connection> connectionProvider;
    private final BASAuditDAO auditDAO;
    private final BASReconDAO reconDAO;

    @Inject
    public BASAuditor(Provider<Connection> connectionProvider, BASAuditDAO auditDAO, BASReconDAO reconDAO) {
        this.connectionProvider = connectionProvider;
        this.auditDAO = auditDAO;
        this.reconDAO = reconDAO;
    }

    public void run() {
        this.logger.info("Running");
        Connection connection = null;

        try {
            if (!this.auditDAO.exceptionReportExists()) {
                this.logger.info("EXCEPTIONREPORT table does not exist; creating");
                this.auditDAO.createExceptionReport();
            }

            this.auditDAO.countExceptions();
            this.auditDAO.countDepositorReferenceMatches();
            connection = this.connectionProvider.get();

            Set<Recon> exceptionalRecons = this.auditDAO.retrieveExceptionalConfirmedRecons();

            for (Recon recon : exceptionalRecons) {
                Map<Long, Long> invoiceTransactions = this.auditDAO.retrieveInvoiceTransactions(connection, recon);
                Map<Long, Long> depositTransactions = BASReconDAO.retrieveDepositTransactions(connection, recon.getDeposits());
                InvoiceDepositBalance invoiceDepositBalance = new InvoiceDepositBalance(recon, invoiceTransactions, depositTransactions);

                this.auditDAO.updateInvoicesDepositsBalances(recon.getReconID(), invoiceDepositBalance.getInvoicesTotal(), invoiceDepositBalance.getDepositsTotal());
                invoiceDepositBalance.balances();

                long manualPaymentsBackdated = this.reconDAO.findManualPaymentsBackdated(recon.getFacilityID(), recon.getReconDate());
                this.auditDAO.updateManualPaymentsBackdated(recon.getReconID(), manualPaymentsBackdated);

                long manualPaymentsDifferentDated = this.reconDAO.findManualPaymentsDifferentDated(recon.getFacilityID(), recon.getReconDate());
                this.auditDAO.updateManualPaymentsDifferentDated(recon.getReconID(), manualPaymentsDifferentDated);
            }
        }
        catch (Exception e) {
            this.logger.error("Audit failure", e);
        }
        finally {
            Closeables.close(connection);
        }
        this.logger.info("Done");
    }

    public void requestShutdown() {
        System.exit(0);
    }

    public static Set<Long> transformExceptionalDeposits(Collection<ExceptionalDeposit> exceptionalDeposits) {
        Set<Long> confirmedDeposits = Sets.newHashSet();

        for (ExceptionalDeposit deposit : exceptionalDeposits) {
            if (deposit.isConfirmedDeposit()) {
                confirmedDeposits.add(deposit.getDepositTransactionID());
            }
        }
        return confirmedDeposits;
    }
}
