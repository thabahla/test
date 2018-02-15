package za.co.interfile.bas.bean;

public class ConfirmedDeposit {
    private long depositID;
    private double cashTotal;
    private double chequeTotal;
    private double postalOrderTotal;
    private long facilityId;

    
    
    public long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}

	public long getDepositID() {
        return this.depositID;
    }

    public void setDepositID(long depositID) {
        this.depositID = depositID;
    }

    public double getCashTotal() {
        return this.cashTotal;
    }

    public void setCashTotal(double cashTotal) {
        this.cashTotal = cashTotal;
    }

    public double getChequeTotal() {
        return this.chequeTotal;
    }

    public void setChequeTotal(double chequeTotal) {
        this.chequeTotal = chequeTotal;
    }

    public double getPostalOrderTotal() {
        return this.postalOrderTotal;
    }

    public void setPostalOrderTotal(double postalOrderTotal) {
        this.postalOrderTotal = postalOrderTotal;
    }

    @Override
    public String toString() {
        return String.valueOf(this.depositID);
    }
}
