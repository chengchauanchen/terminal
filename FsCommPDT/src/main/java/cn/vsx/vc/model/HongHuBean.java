package cn.vsx.vc.model;

import java.util.List;

public class HongHuBean {
    private String deptName;//部门
    private List<Car> car;//车
    private List<Boat> ship;//船

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public List<Car> getCar() {
        return car;
    }

    public void setCar(List<Car> car) {
        this.car = car;
    }

    public List<Boat> getShip() {
        return ship;
    }

    public void setShip(List<Boat> ship) {
        this.ship = ship;
    }
}
