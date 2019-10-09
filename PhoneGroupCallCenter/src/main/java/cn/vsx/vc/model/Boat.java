package cn.vsx.vc.model;

/**
 * 船
 */
public class Boat {
//        "id":80,
//        "createdTime":1567699200000,
//        "updateTime":1567699200000,
//        "vehicleOccupant":"东湖分局",
//        "uniqueNo":"15米的巡逻反恐挺",
//        "vehicleType":"TERMINAL_PATROL"

    private int id;
    private Long createdTime;
    private Long updateTime;
    private String vehicleOccupant;//部门
    private String uniqueNo;//车牌
    private String vehicleType;//类型

    private boolean isCheck = false;

    public String getVehicleOccupant() {
        return vehicleOccupant;
    }

    public void setVehicleOccupant(String vehicleOccupant) {
        this.vehicleOccupant = vehicleOccupant;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUniqueNo() {
        return uniqueNo;
    }

    public void setUniqueNo(String uniqueNo) {
        this.uniqueNo = uniqueNo;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
