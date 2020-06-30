package ptt.terminalsdk.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class NfcPerformBean implements Serializable {
    private static final long serialVersionUID = 5362503103452034617L;
    private LinkedHashMap<Integer,Boolean> codeState;
    private NfcDataBean data;

    public LinkedHashMap<Integer, Boolean> getCodeState() {
        return codeState;
    }

    public void setCodeState(LinkedHashMap<Integer, Boolean> codeState) {
        this.codeState = codeState;
    }

    public NfcDataBean getData() {
        return data;
    }

    public void setData(NfcDataBean data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return "NfcPerformBean{" +
                "codeState=" + codeState +
                ", data=" + data +
                '}';
    }
}
