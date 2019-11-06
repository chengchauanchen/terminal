package cn.vsx.vc.model;


import java.io.Serializable;
import java.util.Objects;

/**
 * Created by XX on 2018/4/11.
 */

public class SetLivingStopTimeBean implements Serializable {
    private static final long serialVersionUID = -2104287198102010844L;
    private int time;
    private boolean checked;

    public SetLivingStopTimeBean() {
    }

    public SetLivingStopTimeBean(int time, boolean checked) {
        this.time = time;
        this.checked = checked;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetLivingStopTimeBean bean = (SetLivingStopTimeBean) o;
        return time == bean.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }
}
