package ptt.terminalsdk.bean;

import java.io.Serializable;
import java.util.List;

public class NfcBaseBean implements Serializable {
    private static final long serialVersionUID = 5362503103452034617L;
    private List<Integer> code;
    private NfcDataBean data;

    public List<Integer> getCode() {
        return code;
    }

    public void setCode(List<Integer> code) {
        this.code = code;
    }

    public NfcDataBean getData() {
        return data;
    }

    public void setData(NfcDataBean data) {
        this.data = data;
    }
}
