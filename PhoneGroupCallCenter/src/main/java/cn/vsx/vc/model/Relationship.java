package cn.vsx.vc.model;


/**
 * 东湖安保--绑定装备
 */
public class Relationship {

    private int id;
    /**
     * 警号
     */
    private String accountNo;//1

    /**
     * 绑定 警务通唯一标识， 布控球编号
     */
    private String uniqueNo;//2

    /**
     * 被绑定设备编号
     */
    private String equipmentNo;//3
    /**
     * 被绑定设备类型（除了警务通以外的其他设备）
     * 目前限制车，船，执法记录仪，手台【与警务通绑定】
     */
    private String equipmentType;//4


    /**
     * 绑定 设备类型（除载具以外的其他设备）
     * 目前限制警务通，布控球，车台
     * 警务通绑定【车，船，执法记录仪，手台】
     * 布控球绑定【车船】
     * 车台绑定【车船】
     */
    private String terminalType;

    private String bindStates;

    private String userName;//5

    private String phoneNumber;//6

    private String department;//7


  public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getBindStates() {
        return bindStates;
    }

    public void setBindStates(String bindStates) {
        this.bindStates = bindStates;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
