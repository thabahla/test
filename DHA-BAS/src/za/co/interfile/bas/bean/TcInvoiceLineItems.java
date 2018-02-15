package za.co.interfile.bas.bean;

import java.util.List;

public class TcInvoiceLineItems {

    private int pkInvoiceLineItemId;
    private int fkInvoiceId;
    private String itemCode;
    private int fkItemTypeId;
    private String lineItemDate;
    private double lineItemSubTotal;
    private int lineItemQuantity;
    private double lineItemUnitPrice;
    private int fkLineItemStatusId;
    private String description;
    private double amountPaid;
    private double outstanding;
    private int fkEntityLastEditedBy;
    private String editedDateTime;
    private int synchronised;
    private int fkPaymentId;
    private List<ScoaAllocations> allocationsList;
    private long tpInvoiceLineItemId;

    public int getSynchronised() {
        return this.synchronised;
    }

    public void setSynchronised(int synchronised) {
        this.synchronised = synchronised;
    }

    public int getFkInvoiceId() {
        return this.fkInvoiceId;
    }

    public void setFkInvoiceId(int fkInvoiceId) {
        this.fkInvoiceId = fkInvoiceId;
    }

    public int getFkItemTypeId() {
        return this.fkItemTypeId;
    }

    public void setFkItemTypeId(int fkItemTypeId) {
        this.fkItemTypeId = fkItemTypeId;
    }

    public int getFkLineItemStatusId() {
        return this.fkLineItemStatusId;
    }

    public void setFkLineItemStatusId(int fkLineItemStatusId) {
        this.fkLineItemStatusId = fkLineItemStatusId;
    }

    public String getLineItemDate() {
        return this.lineItemDate;
    }

    public void setLineItemDate(String lineItemDate) {
        this.lineItemDate = lineItemDate;
    }

    public int getLineItemQuantity() {
        return this.lineItemQuantity;
    }

    public void setLineItemQuantity(int lineItemQuantity) {
        this.lineItemQuantity = lineItemQuantity;
    }

    public double getLineItemSubTotal() {
        return this.lineItemSubTotal;
    }

    public void setLineItemSubTotal(double lineItemSubTotal) {
        this.lineItemSubTotal = lineItemSubTotal;
    }

    public double getLineItemUnitPrice() {
        return this.lineItemUnitPrice;
    }

    public void setLineItemUnitPrice(double lineItemUnitPrice) {
        this.lineItemUnitPrice = lineItemUnitPrice;
    }

    public int getPkInvoiceLineItemId() {
        return this.pkInvoiceLineItemId;
    }

    public void setPkInvoiceLineItemId(int pkInvoiceLineItemId) {
        this.pkInvoiceLineItemId = pkInvoiceLineItemId;
    }

    public String getItemCode() {
        return this.itemCode;
    }

    public void setItemCode(String iteCode) {
        this.itemCode = iteCode;
    }

    public double getAmountPaid() {
        return this.amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getOutstanding() {
        return this.outstanding;
    }

    public void setOutstanding(double outstanding) {
        this.outstanding = outstanding;
    }

    public int getFkEntityLastEditedBy() {
        return this.fkEntityLastEditedBy;
    }

    public void setFkEntityLastEditedBy(int fkEntityLastEditedBy) {
        this.fkEntityLastEditedBy = fkEntityLastEditedBy;
    }

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String editedDateTime) {
        this.editedDateTime = editedDateTime;
    }

    public String toString() {
        return "[ pkInvoiceLineItemId=" + this.pkInvoiceLineItemId + " itemCode=" + this.itemCode + " lineItemQuantity="
                + this.lineItemQuantity + " lineItemUnitPrice=" + this.lineItemUnitPrice + " lineItemSubTotal=" + this.lineItemSubTotal
                + " fkInvoiceId=" + this.fkInvoiceId + " fkItemTypeId=" + this.fkItemTypeId + " lineItemDate=" + this.lineItemDate
                + " fkLineItemStatusId=" + this.fkLineItemStatusId + " description=" + this.description + " amountPaid=" + this.amountPaid
                + " outstanding=" + this.outstanding + " fkEntityLastEditedBy=" + this.fkEntityLastEditedBy + "  editedDateTime="
                + this.editedDateTime + " fkPaymentId=" + this.fkPaymentId + " synchronized=" + this.synchronised + "]";
    }

    public int getFkPaymentId() {
        return this.fkPaymentId;
    }

    public void setFkPaymentId(int fkPaymentId) {
        this.fkPaymentId = fkPaymentId;
    }

    public List<ScoaAllocations> getAllocationsList() {
        return this.allocationsList;
    }

    public void setAllocationsList(List<ScoaAllocations> allocationsList) {
        this.allocationsList = allocationsList;
    }

    public long getTpInvoiceLineItemId() {
        return this.tpInvoiceLineItemId;
    }

    public void setTpInvoiceLineItemId(long tpInvoiceLineItemId) {
        this.tpInvoiceLineItemId = tpInvoiceLineItemId;
    }
}
