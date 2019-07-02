package cn.vsx.vc.jump.command;

import android.content.Context;

public class FactoryCommand {

    Context context;

    public FactoryCommand(Context context) {
        this.context = context;
    }

    public IJumpCommand getJumpCommand(int commandType) {
        IJumpCommand jumpCommand;

        switch (commandType) {
            case 1:
                jumpCommand = new GroupChat(context);
                break;
            case 2:
                jumpCommand = new IndividualCall(context);
                break;
            case 3:
                jumpCommand = new OtherLive(context);
                break;
            case 4:
                jumpCommand = new PersonChat(context);
                break;
            case 5:
                jumpCommand = new SelfLive(context);
                break;
            default:
                jumpCommand = null;
        }
        return jumpCommand;
    }

}
