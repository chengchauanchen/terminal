package cn.vsx.vc.jump.command;

import android.content.Context;
import android.util.Log;

public class FactoryCommand {

    Context context;
    private static FactoryCommand instance;

    public static FactoryCommand getInstance(Context context) {
        if (instance == null) {
            instance = new FactoryCommand(context);
        }
        return instance;
    }


    private FactoryCommand(Context context) {
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
            case 6:
                jumpCommand = new VoipCall(context);
                break;
            case 7:
                jumpCommand = new CreateTemoGroup(context);
                Log.e("FactoryCommand", "jumpCommand:" + jumpCommand);
                break;
            case 10:
                jumpCommand = new PushVideoLive(context);
                break;
            default:
                jumpCommand = null;
        }
        return jumpCommand;
    }

}
