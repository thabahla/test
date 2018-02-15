package za.co.interfile.bas.bean;

public class TcItemType {

    private String itemTypeName;
    private String startValidityPeriod;
    private String endValidityPeriod;
    private int fkEntityLastEditedById;
    private String editedDateTime;
    private String description;
    private double itemBasePrice;
    private String itemCode;
    private int fkInvoiceTypeId;
    private int scoaAllocationId;
    private int pkItemTypeId;
    private String invoiceTypeName;
    private int scoaAssetID;
    private int scoaObjectiveId;
    private int scoaProjectID;
    private int scoafundID;
    private int scoaItemID;
    private String scoaAsset;
    private String scoaObjective;
    private String scoaProject;
    private String scoaFund;
    private String scoaItem;
    private int isVariablePrice;

    public int getPkItemTypeId() {
        return this.pkItemTypeId;
    }

    public void setPkItemTypeId(int pkItemTypeId) {
        this.pkItemTypeId = pkItemTypeId;
    }

    public String getInvoiceTypeName() {
        return this.invoiceTypeName;
    }

    public void setInvoiceTypeName(String invoiceTypeName) {
        this.invoiceTypeName = invoiceTypeName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String editedDateTime) {
        this.editedDateTime = editedDateTime;
    }

    public String getEndValidityPeriod() {
        return this.endValidityPeriod;
    }

    public void setEndValidityPeriod(String endValidityPeriod) {
        this.endValidityPeriod = endValidityPeriod;
    }

    public int getFkEntityLastEditedById() {
        return this.fkEntityLastEditedById;
    }

    public void setFkEntityLastEditedById(int fkEntityLastEditedById) {
        this.fkEntityLastEditedById = fkEntityLastEditedById;
    }

    public int getFkInvoiceTypeId() {
        return this.fkInvoiceTypeId;
    }

    public void setFkInvoiceTypeId(int fkInvoiceTypeId) {
        this.fkInvoiceTypeId = fkInvoiceTypeId;
    }

    public double getItemBasePrice() {
        return this.itemBasePrice;
    }

    public void setItemBasePrice(double itemBasePrice) {
        this.itemBasePrice = itemBasePrice;
    }

    public String getItemCode() {
        return this.itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemTypeName() {
        return this.itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public int getScoaAllocationId() {
        return this.scoaAllocationId;
    }

    public void setScoaAllocationId(int scoaAllocationId) {
        this.scoaAllocationId = scoaAllocationId;
    }

    public String getStartValidityPeriod() {
        return this.startValidityPeriod;
    }

    public void setStartValidityPeriod(String startValidityPeriod) {
        this.startValidityPeriod = startValidityPeriod;
    }

    public int getScoaAssetID() {
        return this.scoaAssetID;
    }

    public void setScoaAssetID(int scoaAssetID) {
        this.scoaAssetID = scoaAssetID;
    }

    public int getScoaObjectiveId() {
        return this.scoaObjectiveId;
    }

    public void setScoaObjectiveId(int scoaObjectiveId) {
        this.scoaObjectiveId = scoaObjectiveId;
    }

    public int getScoaProjectID() {
        return this.scoaProjectID;
    }

    public void setScoaProjectID(int scoaProjectID) {
        this.scoaProjectID = scoaProjectID;
    }

    public int getScoafundID() {
        return this.scoafundID;
    }

    public void setScoafundID(int scoafundID) {
        this.scoafundID = scoafundID;
    }

    public int getScoaItemID() {
        return this.scoaItemID;
    }

    public void setScoaItemID(int scoaItemID) {
        this.scoaItemID = scoaItemID;
    }

    public String toString() {
        return "[ itemTypeName =" + this.itemTypeName + " startValidityPeriod= " + this.startValidityPeriod + " endValidityPeriod = "
                + this.endValidityPeriod + " fkEntityLastEditedById = " + this.fkEntityLastEditedById + " editedDateTime = " + this.editedDateTime
                + " description = " + this.description + " itemBasePrice = " + this.itemBasePrice + " itemCode = " + this.itemCode + " fkInvoiceTypeId = "
                + this.fkInvoiceTypeId + " scoaAllocationId = " + this.scoaAllocationId + " pkItemTypeId = " + this.pkItemTypeId
                + " invoiceTypeName = " + this.invoiceTypeName + " ,scoaAssetID= " + this.scoaAssetID + ", scoaObjectiveId =" + this.scoaObjectiveId
                + " , scoaProjectID =  " + this.scoaProjectID + " ,scoafundID = " + this.scoafundID + " , scoaItemID = " + this.scoaItemID
                + "isVariablePrice = " + this.isVariablePrice + " ]";
    }

    public int getIsVariablePrice() {
        return this.isVariablePrice;
    }

    public void setIsVariablePrice(int isVariablePrice) {
        this.isVariablePrice = isVariablePrice;
    }

    public String getScoaAsset() {
        return this.scoaAsset;
    }

    public void setScoaAsset(String scoaAsset) {
        this.scoaAsset = scoaAsset;
    }

    public String getScoaObjective() {
        return this.scoaObjective;
    }

    public void setScoaObjective(String scoaObjective) {
        this.scoaObjective = scoaObjective;
    }

    public String getScoaProject() {
        return this.scoaProject;
    }

    public void setScoaProject(String scoaProject) {
        this.scoaProject = scoaProject;
    }

    public String getScoaFund() {
        return this.scoaFund;
    }

    public void setScoaFund(String scoaFund) {
        this.scoaFund = scoaFund;
    }

    public String getScoaItem() {
        return this.scoaItem;
    }

    public void setScoaItem(String scoaItem) {
        this.scoaItem = scoaItem;
    }
}
