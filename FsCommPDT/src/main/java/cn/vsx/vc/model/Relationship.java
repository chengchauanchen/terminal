package cn.vsx.vc.model;


/**
 * 东湖安保--绑定装备
 */
public class Relationship {

    private int id;
    /**
     * 设备编号
     */
    private String equipmentNo;
    /**
     * 设备类型
     */
    private String equipmentType;
    /**
     * 警务通唯一标识
     */
    private String uniqueNo;

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
}
