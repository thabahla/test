package za.co.interfile.bas.bean;

import com.google.common.base.Objects;
import java.util.Date;

public class LineItem {
    private String itemName;
    private String receiptNumber;
    private String receiptAmount;
    private Date receiptDate;
    private String item;
    private String responsibility;
    private String objective;
    private String fund;
    private String project;
    private String asset;
    private String region;
    private String infrastructure;
    private String paymentMethod;
    private String ccExpiryDate = "00000000";
    private String ccType = "";
    private String chequeDate = "00000000";
    private String passportNumber = "";
    private String lastName;
    private long facilityId;
    
    

    public String getInfrastructure() {
		return infrastructure;
	}

	public void setInfrastructure(String infrastructure) {
		this.infrastructure = infrastructure;
	}

	public long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}

	public String getReceiptNumber() {
        return this.receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getReceiptAmount() {
        return this.receiptAmount;
    }

    public void setReceiptAmount(String receiptAmount) {
        this.receiptAmount = receiptAmount;
    }

    public Date getReceiptDate() {
        return this.receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getItem() {
        return this.item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getResponsibility() {
        return this.responsibility;
    }

    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }

    public String getObjective() {
        return this.objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getFund() {
        return this.fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getAsset() {
        return this.asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCcExpiryDate() {
        return this.ccExpiryDate;
    }

    public void setCcExpiryDate(String ccExpiryDate) {
        this.ccExpiryDate = ccExpiryDate;
    }

    public String getCcType() {
        return this.ccType;
    }

    public void setCcType(String ccType) {
        this.ccType = ccType;
    }

    public String getChequeDate() {
        return this.chequeDate;
    }

    public void setChequeDate(String chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getPassportNumber() {
        return this.passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(LineItem.class)
            .add("itemName", this.itemName)
            .add("receiptNumber", this.receiptNumber)
            .add("receiptAmount", this.receiptAmount)
            .add("receiptDate", this.receiptDate)
            .add("item", this.item)
            .add("responsibility", this.responsibility)
            .add("objective", this.objective)
            .add("fund", this.fund)
            .add("project", this.project)
            .add("asset", this.asset)
            .add("region", this.region)
            .add("paymentMethod", this.paymentMethod)
            .add("ccExpiryDate", this.ccExpiryDate)
            .add("ccType", this.ccType)
            .add("chequeDate", this.chequeDate)
            .add("passportNumber", this.passportNumber)
            .add("lastName", this.lastName)
            .toString();
    }
}
