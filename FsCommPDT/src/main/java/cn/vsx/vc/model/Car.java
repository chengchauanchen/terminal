package cn.vsx.vc.model;

/**
 * 东湖 车
 */
public class Car {
//        "id":98,
//        "createdTime":1567699200000,
//        "updateTime":1567699200000,
//        "vehicleOccupant":"吹笛派出所",
//        "uniqueNo":"鄂A7616警",
//        "vehicleType":"TERMINAL_CAR"

    private int id;
    private String vehicleOccupant;//部门
    private String vehicleType;//类型

    private String uniqueNo;//车牌
    private Long createdTime;
    private Long updateTime;

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

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
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

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
