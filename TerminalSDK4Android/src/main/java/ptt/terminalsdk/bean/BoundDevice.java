package ptt.terminalsdk.bean;

public class BoundDevice {

    /**
     * "id":"175",
     *         "equipmentNo":"72049322",
     *         "equipmentType":"TERMINAL_PDT",
     *         "uniqueNo":"357792661622489088",
     *         "bindStates":"BIND",
     *         "phoneNumber":null,
     *         "terminalType":"TERMINAL_PHONE",
     *         "userName":"荣耀pad",
     *         "department":"治安管理支队",
     *         "accountNo":"10000437",
     *         "createTime":null,
     *         "updateTime":null,
     *         "expirationTime":"1575550050984"
     */

    private String id;
    private String equipmentNo;
    private String equipmentType;
    private String uniqueNo;
    private String bindStates;
    private String phoneNumber;
    private String terminalType;
    private String userName;
    private String department;
    private String accountNo;
    private String createTime;
    private String updateTime;
    private String expirationTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getUniqueNo() {
        return uniqueNo;
    }

    public void setUniqueNo(String uniqueNo) {
        this.uniqueNo = uniqueNo;
    }

    public String getBindStates() {
        return bindStates;
    }

    public void setBindStates(String bindStates) {
        this.bindStates = bindStates;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public String toString() {
        return "BoundDevice{" +
                "id='" + id + '\'' +
                ", equipmentNo='" + equipmentNo + '\'' +
                ", equipmentType='" + equipmentType + '\'' +
                ", uniqueNo='" + uniqueNo + '\'' +
                ", bindStates='" + bindStates + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", terminalType='" + terminalType + '\'' +
                ", userName='" + userName + '\'' +
                ", department='" + department + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", expirationTime='" + expirationTime + '\'' +
                '}';
    }
}
