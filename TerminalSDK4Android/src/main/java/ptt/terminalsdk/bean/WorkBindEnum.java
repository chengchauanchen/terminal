package ptt.terminalsdk.bean;

/**
 * 工作帮定 类型
 */
public enum WorkBindEnum {
    /**
     * public static final String TERMINAL_BODYWARNCAMERA = "TERMINAL_BODY_WORN_CAMERA"; public static
     * final String TERMINAL_PDT = "TERMINAL_PDT"; public static final String TERMINAL_CAR =
     * "TERMINAL_CAR"; public static final String TERMINAL_CAR = "TERMINAL_SHOUKAO"; public static
     * final String TERMINAL_CAR = "TERMINAL_JINGUN"; public static final String TERMINAL_CAR =
     * "TERMINAL_WASI";
     */
    pdt("电台", "DEVICE_PDT_TERMINAL"),
    car("警车", "DEVICE_PDT_CAR"),
    zfy("执法仪", "DEVICE_BODY_WORN_CAMERA"),
    shoukao("手铐", "TERMINAL_SHOUKAO"),
    jingun("警棍", "TERMINAL_JINGUN"),
    wasi("催泪喷雾", "TERMINAL_WASI"),
    eppackage("防疫包", "TERMINAL_EPPACKAGE");

    private String name;
    private String type;

    WorkBindEnum(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
