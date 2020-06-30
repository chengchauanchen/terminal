package ptt.terminalsdk.bean;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum NfcBusinessVideoType implements Serializable{
	CARRIAGE_INSPECTION("车厢巡视", 1),
	TRAIN_ARRIVAL_INSPECTION("列车终到检查", 2);

	private String value;
	private int code;
	private static Map<Integer, NfcBusinessVideoType> code2UpdateType = new HashMap<>();

	private NfcBusinessVideoType(String value, int code) {
		this.setValue(value);
		this.setCode(code);
	}

	static {
		for (NfcBusinessVideoType playType : EnumSet.allOf(NfcBusinessVideoType.class)) {
			// Yes, use some appropriate locale in production code :)
			code2UpdateType.put(playType.getCode(), playType);
		}
	}

	public static NfcBusinessVideoType getInstanceByCode(int code) {
		return code2UpdateType.get(code);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
