package ptt.terminalsdk.bean;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.List;

public class NfcBaseBean implements Serializable {
    private static final long serialVersionUID = 5362503103452034617L;
    private List<Integer> code;
    private JsonObject data;

    public List<Integer> getCode() {
        return code;
    }

    public void setCode(List<Integer> code) {
        this.code = code;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
