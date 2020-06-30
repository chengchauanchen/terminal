package ptt.terminalsdk.bean;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.List;

public class NfcDataBean implements Serializable {
    private static final long serialVersionUID = -2567478351625252532L;
    private int no;
    private String uNo;
    private int gNo;
    private int state;
    private JSONObject tag;
    private List<String> vStr;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getuNo() {
        return uNo;
    }

    public void setuNo(String uNo) {
        this.uNo = uNo;
    }

    public int getgNo() {
        return gNo;
    }

    public void setgNo(int gNo) {
        this.gNo = gNo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public JSONObject getTag() {
        return tag;
    }

    public void setTag(JSONObject tag) {
        this.tag = tag;
    }

    public List<String> getvStr() {
        return vStr;
    }

    public void setvStr(List<String> vStr) {
        this.vStr = vStr;
    }

    @Override
    public String toString() {
        return "NfcDataBean{" +
                "no=" + no +
                ", uNo='" + uNo + '\'' +
                ", gNo=" + gNo +
                ", state=" + state +
                ", tag=" + tag +
                ", vStr=" + vStr +
                '}';
    }
}
