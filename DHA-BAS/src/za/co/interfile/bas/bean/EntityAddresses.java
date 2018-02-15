package za.co.interfile.bas.bean;

public class EntityAddresses {

    private String editedDateTime;
    private int fkAddressTypeId;
    private int fkCityId;
    private int fkCountryId;
    private int fkEntityId;
    private int fkEntityLastEditedId;
    private int fkProvinceId;
    private int isDomicilium;
    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private int pkAddressId;
    private String postalCode;
    private String addressType;
    private int isPrimary;

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String edited_Date_Time) {
        this.editedDateTime = edited_Date_Time;
    }

    public int getFkAddressTypeId() {
        return this.fkAddressTypeId;
    }

    public void setFkAddressTypeId(int fkAddress_Type_Id) {
        this.fkAddressTypeId = fkAddress_Type_Id;
    }

    public int getFkCityId() {
        return this.fkCityId;
    }

    public void setFkCityId(int fkCity_Id) {
        this.fkCityId = fkCity_Id;
    }

    public int getFkCountryId() {
        return this.fkCountryId;
    }

    public void setFkCountryId(int fkCountry_Id) {
        this.fkCountryId = fkCountry_Id;
    }

    public int getFkEntityId() {
        return this.fkEntityId;
    }

    public void setFkEntityId(int fkEntity_Id) {
        this.fkEntityId = fkEntity_Id;
    }

    public int getFkEntityLastEditedId() {
        return this.fkEntityLastEditedId;
    }

    public void setFkEntityLastEditedId(int fkEntity_Last_Edited_Id) {
        this.fkEntityLastEditedId = fkEntity_Last_Edited_Id;
    }

    public int getFkProvinceId() {
        return this.fkProvinceId;
    }

    public void setFkProvinceId(int fkProvince_Id) {
        this.fkProvinceId = fkProvince_Id;
    }

    public int getDomicilium() {
        return this.isDomicilium;
    }

    public void setDomicilium(int isDomicilium) {
        this.isDomicilium = isDomicilium;
    }

    public String getLine1() {
        return this.line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return this.line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getLine3() {
        return this.line3;
    }

    public void setLine3(String line3) {
        this.line3 = line3;
    }

    public String getLine4() {
        return this.line4;
    }

    public void setLine4(String line4) {
        this.line4 = line4;
    }

    public int getPkAddressId() {
        return this.pkAddressId;
    }

    public void setPkAddressId(int pkAddress_Id) {
        this.pkAddressId = pkAddress_Id;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public int getIsDomicilium() {
        return this.isDomicilium;
    }

    public void setIsDomicilium(int isDomicilium) {
        this.isDomicilium = isDomicilium;
    }

    public String getAddressType() {
        return this.addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public int getIsPrimary() {
        return this.isPrimary;
    }

    public void setIsPrimary(int isPrimary) {
        this.isPrimary = isPrimary;
    }
}
