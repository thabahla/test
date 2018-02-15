package za.co.interfile.bas.bean;

public abstract class TcScoa {

    private String accountNumber;
    private int primaryKey;
    private String scoaName;

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(int primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getScoaName() {
        return this.scoaName;
    }

    public void setScoaName(String scoaName) {
        this.scoaName = scoaName;
    }

    public String toString() {
        return "[ accountNumber = " + this.accountNumber + ", primaryKey = " + this.primaryKey + ", scoaName = " + this.scoaName + " ]";
    }
}
