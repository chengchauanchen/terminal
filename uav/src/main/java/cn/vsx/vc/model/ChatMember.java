package cn.vsx.vc.model;

/**
 * Created by gt358 on 2017/9/15.
 */

public class ChatMember {

    private int id;
    private String name;
    private boolean isGroup;

    public ChatMember(int id, String name, boolean isGroup) {
        this.id = id;
        this.name = name;
        this.isGroup = isGroup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
