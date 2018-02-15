package za.co.interfile.bas.bean;

public class EntityPerson {

    private String birthDate;
    private String editedDateTime;
    private String firstName;
    private int fkEntityLastEditedById;
    private int fkEntityTitleId;
    private int fkMaritalStatus;
    private int gender;
    private String initials;
    private int isCashier;
    private int isUser;
    private String lastName;
    private String maidenName;
    private int pfkEntityId;
    private String preferedName;

    public String getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getEditedDateTime() {
        return this.editedDateTime;
    }

    public void setEditedDateTime(String editedDateTime) {
        this.editedDateTime = editedDateTime;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getFkEntityLastEditedById() {
        return this.fkEntityLastEditedById;
    }

    public void setFkEntityLastEditedById(int fkEntityLastEditedById) {
        this.fkEntityLastEditedById = fkEntityLastEditedById;
    }

    public int getFkEntityTitleId() {
        return this.fkEntityTitleId;
    }

    public void setFkEntityTitleId(int fkEntityTitleId) {
        this.fkEntityTitleId = fkEntityTitleId;
    }

    public int getFkMaritalStatus() {
        return this.fkMaritalStatus;
    }

    public void setFkMaritalStatus(int fkMaritalStatus) {
        this.fkMaritalStatus = fkMaritalStatus;
    }

    public int getGender() {
        return this.gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getInitials() {
        return this.initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public int getCashier() {
        return this.isCashier;
    }

    public void setCashier(int isCashier) {
        this.isCashier = isCashier;
    }

    public int isUser() {
        return this.isUser;
    }

    public void setUser(int isUser) {
        this.isUser = isUser;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return this.maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public int getPfkEntityId() {
        return this.pfkEntityId;
    }

    public void setPfkEntityId(int pfkEntityId) {
        this.pfkEntityId = pfkEntityId;
    }

    public String getPreferedName() {
        return this.preferedName;
    }

    public void setPreferedName(String preferedName) {
        this.preferedName = preferedName;
    }
}
