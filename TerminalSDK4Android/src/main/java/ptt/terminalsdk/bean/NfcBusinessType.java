package ptt.terminalsdk.bean;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum NfcBusinessType implements Serializable{
	BIND("账号绑定", 1),
	BIND_WARNING("警情绑定", 2),
	VIDEO("录像", 3),
	VIDEO_PUSH("实时上报", 4);

	private static final long serialVersionUID = 2L;

	private String value;
	private int code;
	private static Map<Integer, NfcBusinessType> code2UpdateType = new HashMap<>();

	private NfcBusinessType(String value, int code) {
		this.setValue(value);
		this.setCode(code);
	}

	static {
		for (NfcBusinessType playType : EnumSet.allOf(NfcBusinessType.class)) {
			// Yes, use some appropriate locale in production code :)
			code2UpdateType.put(playType.getCode(), playType);
		}
	}

	public static NfcBusinessType getInstanceByCode(int code) {
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
