package cn.vsx.vc.jump.command;

import android.content.Context;

public class BaseCommand {
    private Context context;

    public BaseCommand(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
