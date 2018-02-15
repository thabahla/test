package za.co.interfile.bas.dao;

import za.co.interfile.bas.util.Closeables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.interfile.bas.bean.Recon;
import za.co.interfile.bas.bean.ShiftTransaction;

/**
 * @author Theuns Cloete
 */
public class BASAuditDAO {
    private static final int DEF_FETCH_SIZE = 1000;

    private final Logger logger = LoggerFactory.getLogger(BASAuditDAO.class);
    private final Provider<Connection> connectionProvider;

    @Inject
    public BASAuditDAO(Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public boolean exceptionReportExists() throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select count(TABLE_NAME) from USER_TABLES where TABLE_NAME = 'EXCEPTIONREPORT'");

            if (resultSet.next()) {
                return resultSet.getInt(1) == 1;
            }
            return false;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public void createExceptionReport() throws Exception {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();

            statement.executeUpdate("create table EXCEPTIONREPORT ("
                + " UNKNOWN varchar2(8),"
                + " DEPOSITORREFERENCE varchar2(100) not null,"
                + " AMOUNT number(10,2) not null,"
                + " TREASURYTX varchar2(32),"
                + " DEPOSITDATE varchar2(10) not null,"
                + " REASON varchar2(128),"
                + " BANK varchar2(8),"
                + " OFFICE varchar2(128),"
                + " AUD_RECONID number(10,0),"
                + " AUD_RECON_STATUS varchar2(16),"
                + "	AUD_FACILITY varchar2(128),"
                + "	AUD_DEP_STATUS number(4),"
                + "	AUD_INV_SHIFTS number(1),"
                + "	AUD_TX_TOTALS number(10),"
                + "	AUD_DEP_TOTALS number(10),"
                + "	AUD_M_PAY_BACKD number(10),"
                + "	AUD_M_PAY_DIFFD number(10))");

            this.logger.info("Created EXCEPTIONREPORT table; data needs to be imported now (yes I'm looking at you)");
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public void countExceptions() throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select COUNT(*)"
                + " from EXCEPTIONREPORT");

            if (resultSet.next()) {
                logger.info("There are " + resultSet.getInt(1) + " bank exceptions to consider");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public void countDepositorReferenceMatches() throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select count(ex.DEPOSITORREFERENCE), count(distinct dep.RECONID),"
                + " sum(ex.AMOUNT), sum(dep.GRANDTOTAL)"
                + " from EXCEPTIONREPORT ex, TCBANKDEPOSITS dep"
                + " where ex.DEPOSITORREFERENCE = dep.DEPOSITORREFERENCE");

            if (resultSet.next()) {
                int depositCount = resultSet.getInt(1);
                int reconCount = resultSet.getInt(2);
                long exceptionTotal = resultSet.getLong(3);
                long depositTotal = resultSet.getLong(4);

                logger.info("There are " + depositCount + " deposits that correspond in EXCEPTIONREPORT and TCBANKDEPOSITS with totals of " + exceptionTotal + " and " + depositTotal + " respectively");
                logger.info("These deposits belong to " + reconCount + " recons in TCBANKDEPOSITS");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public Set<Recon> retrieveExceptionalConfirmedRecons() throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select recon.RECONDATE, recon.IRERECON_ID, recon.STATUS, ex.DEPOSITORREFERENCE,"
                + "     ex.AUD_DEP_STATUS, ex.AUD_INV_SHIFTS, AUD_TX_TOTALS, AUD_DEP_TOTALS, AUD_M_PAY_BACKD, AUD_M_PAY_DIFFD"
                + " from IRERECON recon, TCBANKDEPOSITS bank, IREDAYENDS dayend, EXCEPTIONREPORT ex"
                + " where ex.DEPOSITORREFERENCE = bank.DEPOSITORREFERENCE"
                + " and recon.IRERECON_ID = bank.RECONID"
                + " and dayend.PKDAYENDID = bank.FKDAYENDID"
                + " group by recon.RECONDATE, recon.IRERECON_ID, recon.STATUS, ex.DEPOSITORREFERENCE, ex.AUD_DEP_STATUS,"
                + "     ex.AUD_INV_SHIFTS, AUD_TX_TOTALS, AUD_DEP_TOTALS, AUD_M_PAY_BACKD, AUD_M_PAY_DIFFD"
                + " order by recon.RECONDATE, recon.IRERECON_ID");
            resultSet.setFetchSize(DEF_FETCH_SIZE);

            Set<Recon> exceptionalRecons = Sets.newHashSet();

            while (resultSet.next()) {
                String reconDate = resultSet.getString("RECONDATE");
                long reconID = resultSet.getLong("IRERECON_ID");
                String reconStatus = resultSet.getString("STATUS");
                String depositorReference = resultSet.getString("DEPOSITORREFERENCE");

                this.updateReconDetails(depositorReference, reconID, reconStatus);

                if (resultSet.getObject("AUD_DEP_STATUS") == null || resultSet.getObject("AUD_INV_SHIFTS") == null
                        || resultSet.getObject("AUD_TX_TOTALS") == null || resultSet.getObject("AUD_DEP_TOTALS") == null
                        || resultSet.getObject("AUD_M_PAY_BACKD") == null || resultSet.getObject("AUD_M_PAY_DIFFD") == null) {
                    logger.debug("Found IRE recon [reconID=" + reconID + ",reconDate=" + reconDate + "]");
                    exceptionalRecons.addAll(this.getReconsWithConfirmedDeposits(connection, reconID, reconDate));
                }
                else {
                    logger.debug("Already processed IRE recon [reconID=" + reconID + ",reconDate=" + reconDate + "]");
                }
            }
            logger.info("Retrieved " + exceptionalRecons.size() + " exceptional recons");
            return exceptionalRecons;
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    private void updateReconDetails(String depositorReference, long reconID, String reconStatus) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("update EXCEPTIONREPORT"
                + " set AUD_RECONID = ?, AUD_RECON_STATUS = ?"
                + " where DEPOSITORREFERENCE = ?");
            statement.setLong(1, reconID);
            statement.setString(2, reconStatus);
            statement.setString(3, depositorReference);

            int updated = statement.executeUpdate();

            logger.debug("Updated " + updated + " rows in EXCEPTIONREPORT [depositorReference=" + depositorReference + ",reconID=" + reconID + ",reconStatus=" + reconStatus + "]");
        }
        catch (SQLException sqle) {
            logger.error("Unable to update EXCEPTIONREPORT table [depositorReference=" + depositorReference + ",reconID=" + reconID + ",reconStatus=" + reconStatus + "]", sqle);
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    private List<Recon> getReconsWithConfirmedDeposits(Connection connection, long reconID, String reconDate) throws SQLException {
        PreparedStatement depositsStatement = null;
        ResultSet depositsResultSet = null;

        try {
            depositsStatement = connection.prepareStatement("select bank.PFKDEPOSITTRANSACTIONID, bank.FKDAYENDID,"
                + "     bank.FKDEPOSITSTATUSID, bank.DEPOSITORREFERENCE, ent.DISPLAYNAME"
                + " from TCBANKDEPOSITS bank, ENTITIES ent, IREDAYENDS dayend, IRERECON recon"
                + " where dayend.PKDAYENDID = bank.FKDAYENDID"
                + "     and dayend.FKFACILITYENTITYID = ent.PKENTITYID"
                + "     and recon.IRERECON_ID = bank.RECONID"
                + "     and bank.RECONID = ?");
            depositsStatement.setLong(1, reconID);
            depositsResultSet = depositsStatement.executeQuery();
            depositsResultSet.setFetchSize(DEF_FETCH_SIZE);

            List<Recon> recons = new ArrayList<Recon>();
            Set<Long> confirmedDeposits = new HashSet<Long>();
            Set<Long> dayEnds = new HashSet<Long>();

            while (depositsResultSet.next()) {
                long depositTransaction = depositsResultSet.getLong("PFKDEPOSITTRANSACTIONID");
                long dayEndID = depositsResultSet.getLong("FKDAYENDID");
                int depositStatusID = depositsResultSet.getInt("FKDEPOSITSTATUSID");
                String depositorReference = depositsResultSet.getString("DEPOSITORREFERENCE");
                String facility = depositsResultSet.getString("DISPLAYNAME");

                logger.debug("Found bank deposit [reconID=" + reconID + ",reconDate=" + reconDate + ",depositTransactionID=" + depositTransaction + ",dayEndID=" + dayEndID + ",depositStatusID=" + depositStatusID + ",depositorReference=" + depositorReference + ",facility=" + facility + "]");
                this.updateDepositDetails(depositorReference, facility, depositStatusID);

                // 1 = CREATED
                // 2 = CONFIRMED
                // 3 = RECONCILED
                // 4 = BAS CREATED
                if (depositStatusID > 1) {
                    confirmedDeposits.add(depositTransaction);
                    dayEnds.add(dayEndID);
                }
            }

            if (!confirmedDeposits.isEmpty()) {
                long facilityID = BASReconDAO.retrieveFacilityID(connection, dayEnds);
                Recon recon = new Recon(reconID, reconDate, confirmedDeposits, dayEnds, facilityID);

                recons.add(recon);
                logger.debug("Need to summarise " + recon);
            }
            else {
                logger.info("No bank deposits associated with IRE recon [reconID=" + reconID + ",reconDate=" + reconDate + "]");
            }
            return recons;
        }
        finally {
            Closeables.close(depositsResultSet);
            Closeables.close(depositsStatement);
        }
    }

    public void updateDepositDetails(String depositorReference, String facility, int depositStatus) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("update EXCEPTIONREPORT"
                + " set AUD_FACILITY = ?, AUD_DEP_STATUS = ?"
                + " where DEPOSITORREFERENCE = ?");
            statement.setString(1, facility);
            statement.setInt(2, depositStatus);
            statement.setString(3, depositorReference);

            int updated = statement.executeUpdate();

            logger.debug("Updated " + updated + " rows in EXCEPTIONREPORT [depositorReference=" + depositorReference + ",depositStatus=" + depositStatus + "]");
        }
        catch (SQLException sqle) {
            logger.error("Unable to update EXCEPTIONREPORT table [depositorReference=" + depositorReference + ",depositStatus=" + depositStatus + "]", sqle);
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public Map<Long, Long> retrieveInvoiceTransactions(Connection connection, Recon recon) throws SQLException {
        PreparedStatement invoicesStatement = null;
        ResultSet invoicesResultSet = null;

        try {
            Multimap<Long, ShiftTransaction> shifts = HashMultimap.create();

            invoicesStatement = connection.prepareStatement("select dep.FKSHIFTID, pay.PFKPAYMENTTRANSACTIONID, inv.AMOUNTPAID, inv.ISMANUALPAYMENT"
                + " from IRECASHIERDEPOSITS dep, TCPAYMENTTRANSACTIONS pay, TCINVOICES inv"
                + " where inv.FKFACILITYENTITYID = ?"
                + " and to_char(dep.SUBMITTEDDATETIME, 'YYYY-MM-DD') = ?"
                + " and dep.FKSHIFTID = pay.FKSHIFTID"
                + " and pay.PFKPAYMENTTRANSACTIONID = inv.PFKINVOICETRANSACTIONID"
                + " order by dep.FKSHIFTID, pay.PFKPAYMENTTRANSACTIONID");

            invoicesStatement.setLong(1, recon.getFacilityID());
            invoicesStatement.setString(2, recon.getReconDate());
            this.logger.debug("Retrieving invoice transactions " + recon);
            invoicesResultSet = invoicesStatement.executeQuery();
            invoicesResultSet.setFetchSize(DEF_FETCH_SIZE);

            while (invoicesResultSet.next()) {
                long shiftID = invoicesResultSet.getLong("FKSHIFTID");
                long invoiceTransaction = invoicesResultSet.getLong("PFKPAYMENTTRANSACTIONID");
                long amountPaid = invoicesResultSet.getLong("AMOUNTPAID");
                boolean manualPayment = invoicesResultSet.getBoolean("ISMANUALPAYMENT");

                shifts.put(shiftID, new ShiftTransaction(invoiceTransaction, manualPayment, amountPaid));
            }
            this.validateShifts(shifts, recon);
            return BASReconDAO.transformShifts(shifts);
        }
        finally {
            Closeables.close(invoicesResultSet);
            Closeables.close(invoicesStatement);
        }
    }

    public void validateShifts(Multimap<Long, ShiftTransaction> shifts, Recon recon) {
        boolean containsInvalidShifts = false;

        for (long shiftID : shifts.keySet()) {
            if (!BASReconDAO.isValidShift(shifts.get(shiftID))) {
                this.logger.warn("Invalid shift contains both normal and manual payments [shiftID=" + shiftID +"] " + recon);
                containsInvalidShifts = true;
            }
        }
        this.updateInvalidShifts(recon.getReconID(), containsInvalidShifts);
    }

    public void updateInvalidShifts(long reconID, boolean invalidShifts) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("update EXCEPTIONREPORT"
                + " set AUD_INV_SHIFTS = ?"
                + " where AUD_RECONID = ? and AUD_DEP_STATUS > 1");
            statement.setBoolean(1, invalidShifts);
            statement.setLong(2, reconID);

            int updated = statement.executeUpdate();

            logger.debug("Updated " + updated + " rows in EXCEPTIONREPORT [reconID=" + reconID + ",invalidShifts=" + invalidShifts + "]");
        }
        catch (SQLException sqle) {
            logger.error("Unable to update EXCEPTIONREPORT table [reconID=" + reconID + ",invalidShifts=" + invalidShifts + "]", sqle);
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public void updateInvoicesDepositsBalances(long reconID, long invoicesTotal, long depositsTotal) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("update EXCEPTIONREPORT"
                + " set AUD_TX_TOTALS = ?, AUD_DEP_TOTALS = ?"
                + " where AUD_RECONID = ? and AUD_DEP_STATUS > 1");
            statement.setLong(1, invoicesTotal);
            statement.setLong(2, depositsTotal);
            statement.setLong(3, reconID);

            int updated = statement.executeUpdate();

            logger.debug("Updated " + updated + " rows in EXCEPTIONREPORT [reconID=" + reconID + ",invoicesTotal=" + invoicesTotal + ",depositsTotal=" + depositsTotal + "]");
        }
        catch (SQLException sqle) {
            logger.error("Unable to update EXCEPTIONREPORT table [reconID=" + reconID + ",invoicesTotal=" + invoicesTotal + ",depositsTotal=" + depositsTotal + "]", sqle);
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public void updateManualPaymentsBackdated(long reconID, long manualPaymentsBackdated) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("update EXCEPTIONREPORT"
                + " set AUD_M_PAY_BACKD = ?"
                + " where AUD_RECONID = ? and AUD_DEP_STATUS > 1");
            statement.setLong(1, manualPaymentsBackdated);
            statement.setLong(2, reconID);

            int updated = statement.executeUpdate();

            logger.debug("Updated " + updated + " rows in EXCEPTIONREPORT [reconID=" + reconID + ",manualPaymentsBackdated=" + manualPaymentsBackdated + "]");
        }
        catch (SQLException sqle) {
            logger.error("Unable to update EXCEPTIONREPORT table [reconID=" + reconID + ",manualPaymentsBackdated=" + manualPaymentsBackdated + "]", sqle);
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }

    public void updateManualPaymentsDifferentDated(long reconID, long manualPaymentsDifferentDated) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("update EXCEPTIONREPORT"
                + " set AUD_M_PAY_DIFFD = ?"
                + " where AUD_RECONID = ? and AUD_DEP_STATUS > 1");
            statement.setLong(1, manualPaymentsDifferentDated);
            statement.setLong(2, reconID);

            int updated = statement.executeUpdate();

            logger.debug("Updated " + updated + " rows in EXCEPTIONREPORT [reconID=" + reconID + ",manualPaymentsDifferentDated=" + manualPaymentsDifferentDated + "]");
        }
        catch (SQLException sqle) {
            logger.error("Unable to update EXCEPTIONREPORT table [reconID=" + reconID + ",manualPaymentsDifferentDated=" + manualPaymentsDifferentDated + "]", sqle);
        }
        finally {
            Closeables.close(statement);
            Closeables.close(connection);
        }
    }
}
