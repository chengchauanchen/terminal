package cn.vsx.vc.model;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PlayType implements Serializable{
	PLAY_AUDIO("录音", 1),
	PLAY_PRIVATE_CALL("个呼", 2),
	PLAY_GROUP_CALL("组呼", 3);

	private static final long serialVersionUID = 2L;

	private String value;
	private int code;
	private static Map<Integer, PlayType> code2playType = new HashMap<>();

	private PlayType(String value, int code) {
		this.setValue(value);
		this.setCode(code);
	}

	static {
		for (PlayType playType : EnumSet.allOf(PlayType.class)) {
			// Yes, use some appropriate locale in production code :)
			code2playType.put(playType.getCode(), playType);
		}
	}

	public static PlayType getInstanceByCode(int code) {
		return code2playType.get(code);
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
