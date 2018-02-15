package za.co.interfile.bas.dao;

import za.co.interfile.bas.util.Closeables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This looks a lot like a helper class. Should be final with private constructor and static methods.
 *
 * @author Theuns Cloete (not initially, just the one who added these comments)
 */
public class BASIDDAO {
    private final Logger logger = LoggerFactory.getLogger(BASIDDAO.class);
    private final Provider<Connection> connectionProvider;

    @Inject
    public BASIDDAO(Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    // UNUSED IdRetriever.getUserId(String username)
    public int getUserId(String username) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select FKPERSONENTITYID from IRELOGINS where PKUSERNAME = ?");
            statement.setString(1, username);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("FKPERSONENTITYID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getMethodId(String method)
    public int getMethodId(String method) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKPAYMENTMETHODID from LTPAYMENTMETHODS where PAYMENTMETHOD = ?");
            statement.setString(1, method);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKPAYMENTMETHODID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
   }

    // UNUSED IdRetriever.getSystemId(String billingSystem)
    public int getSystemId(String billingSystem) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKSYSTEMID from TCSYSTEMS where NAME = ?");
            statement.setString(1, billingSystem);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKSYSTEMID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getShiftId(int currentUserId, String editedDteTime)
    public int getShiftId(int currentUserId, String editedDteTime) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKSHIFTID"
                + " from IRESHIFTS"
                + " where FKENTITYWORKERID = ?"
                + " and CREATEDTIMEDATE = ?");
            statement.setInt(1, currentUserId);
            statement.setString(2, editedDteTime);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKSHIFTID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getInvoiceStatusId(String invoiceStatus)
    public int getInvoiceStatusId(String invoiceStatus) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKINVOICESTATUSID from LTINVOICESTATUSES where INVOICESTATUS = ?");
            statement.setString(1, invoiceStatus);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKINVOICESTATUSID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getInvoiceAccountId(String accountNumber)
    public int getInvoiceAccountId(String accountNumber) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKACCOUNTID from TCACCOUNTS where ACCOUNTNUMBER = ?");
            statement.setString(1, accountNumber);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKACCOUNTID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getInvoiceTypeId(String billingSystem)
    public int getInvoiceTypeId(String billingSystem) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            // FIXME Why do we always append " Invoice" to the billing system?
            statement = connection.prepareStatement("select PKINVOICETYPEID from TCINVOICETYPES where INVOICETYPENAME = ?");
            statement.setString(1, billingSystem + " Invoice");
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getInt("PKINVOICETYPEID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getInvoiceId(String time, int userId)
    // UNUSED paramter userId
    public int getInvoiceId(String time, int userId) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PFKINVOICETRANSACTIONID from TCINVOICES where EDITEDDATETIME = ?");
            statement.setString(1, time);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getInt("PFKINVOICETRANSACTIONID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getItemTypeId(String itemCode)
    public int getItemTypeId(String itemCode) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKITEMTYPEID from TCITEMTYPES where ITEMCODE = ?");
            statement.setString(1, itemCode);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKITEMTYPEID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getEntityId(int userId, String editedDateTime)
    public int getEntityId(int userId, String editedDateTime) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKENTITYID"
                + " from ENTITIES"
                + " where FKENTITYLASTEDITEDBYID = ?"
                + " and EDITEDDATETIME = ?");
            statement.setInt(1, userId);
            statement.setString(2, editedDateTime);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // XXX Replaced getting PKDAYPKENTITYIDENDSTATUSID with PKENTITYID
                return resultSet.getInt("PKENTITYID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getBankAcountId(String accountNumber)
    public int getBankAcountId(String accountNumber) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKBANKACCOUNTID from TCBANKACCOUNTS where ACCOUNTNUMBER = ?");
            statement.setString(1, accountNumber);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getInt("PKBANKACCOUNTID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getDepositTypeId(String depositType)
    public int getDepositTypeId(String depositType) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKDEPOSITTYPEID from LTDEPOSITTYPES where DEPOSITTYPE = ?");
            statement.setString(1, depositType);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKDEPOSITTYPEID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getBankAccountTypeId(String accountType)
    public int getBankAccountTypeId(String accountType) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKBANKACCOUNTTYPEID from LTBANKACCTYPES where BANKACCOUNTTYPE = ?");
            statement.setString(1, accountType);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("PKBANKACCOUNTTYPEID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getDepartmentId(String department)
    public int getDepartmentId(String department) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKDEPARTMENTID from IREDEPARTMENTCONFIG where NAME = ?");
            statement.setString(1, department);
            resultSet = statement.executeQuery();

            // XXX Change while to if
            if (resultSet.next()) {
                return resultSet.getInt("PKDEPARTMENTID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    public int getDepositStatusId(String status) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKDEPOSITSTATUSID from LTDEPOSITSTATUSES where DEPOSITSTATUS = ?");
            statement.setString(1, status);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getInt("PKDEPOSITSTATUSID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getUserAccessLevelId(String level)
    public int getUserAccessLevelId(String level) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKUSERACCESSLEVELID from LTUSERACCESSLEVEL where ACCESSLEVEL = ?");
            statement.setString(1, level);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getInt("PKUSERACCESSLEVELID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getCustAccountTypeId(String accountType)
    public int getCustAccountTypeId(String accountType) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKCUSTACCOUNTTYPEID from LTCUSTOMERACCOUNTTYPES where CUSTACCOUNTTYPE = ?");
            statement.setString(1, accountType);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getInt("PKCUSTACCOUNTTYPEID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getUserAccountNumber(String userName)
    public String getUserAccountNumber(String userName) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            // FIXME This statement seems to select unnecessary columns
            statement = connection.prepareStatement("select acc.ACCOUNTNUMBER, login.PKUSERNAME, acc.FKENTITYACCHOLDERID,"
                + "     acc.PKACCOUNTID"
                + " from TCACCOUNTS acc inner join IRELOGINS login on (acc.FKENTITYACCHOLDERID = login.FKPERSONENTITYID)"
                + " where login.PKUSERNAME = ?");
            statement.setString(1, userName);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getString("acc.ACCOUNTNUMBER");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return "";
    }

    // UNUSED IdRetriever.getChannelId(String channel)
    public int getChannelId(String channel) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select PKCHANNELID, PAYMENTCHANNEL from LTPAYMENTCHANNELS where PAYMENTCHANNEL = ?");
            statement.setString(1, channel);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getInt("PKCHANNELID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0;
    }

    // UNUSED IdRetriever.getTPEntityId(long entityId)
    public String getTPEntityId(long entityId) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select TPENTITYID from ENTITIES where PKENTITYID = ?");
            statement.setLong(1, entityId);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getString("TPENTITYID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return "";
    }

    // UNUSED IdRetriever.getTPAccountId(String accountNumber)
    public long getTPAccountId(String accountNumber) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select TPACCOUNTID from TCACCOUNTS where ACCOUNTNUMBER = ?");
            statement.setString(1, accountNumber);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getLong("TPACCOUNTID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0L;
    }

    // UNUSED IdRetriever.getBASId(String referenceNumber)
    public long getBASId(String referenceNumber) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select bas.PKID"
                + " from TCBASINTERFACEFILES bas inner join JOINBANKDEPBASFILE depbas on ("
                + "     bas.PKID = depbas.PFKBASFILEID) inner join TCBANKDEPOSITS dep on ("
                + "     depbas.PFKDEPOSITTRANSACTIONID = dep.PFKDEPOSITTRANSACTIONID)"
                + " where dep.DEPOSITORREFERENCE = ?");
            statement.setString(1, referenceNumber);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                return resultSet.getLong("bas.PKID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0L;
    }

    // UNUSED IdRetriever.getDayEndId(String serialNumber)
    public long getDayEndId(String serialNumber) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.get();
            statement = connection.prepareStatement("select de.PKDAYENDID, jdb.PFKBASFILEID, jdb.PFKDEPOSITTRANSACTIONID, bas.SERIALNUMBER"
                + " from (IREDAYENDS de left outer join JOINBANKDEPBASFILE jdb on ("
                + "     de.FKBANKDEPOSITTXID = jdb.PFKDEPOSITTRANSACTIONID)) inner join TCBASINTERFACEFILES bas on ("
                + "     bas.PKID = jdb.PFKBASFILEID)"
                + " where bas.SERIALNUMBER = ?");
            statement.setString(1, serialNumber);
            resultSet = statement.executeQuery();

            // XXX Changed while to if
            if (resultSet.next()) {
                // XXX Changed getting PKID to PKDAYENDID
                return resultSet.getLong("de.PKDAYENDID");
            }
        }
        finally {
            Closeables.close(resultSet);
            Closeables.close(statement);
            Closeables.close(connection);
        }
        // XXX Idicates not found?
        return 0L;
    }
}
