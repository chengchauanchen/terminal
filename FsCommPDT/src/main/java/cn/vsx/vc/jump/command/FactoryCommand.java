package cn.vsx.vc.jump.command;

import android.content.Context;

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
            case 0:
                jumpCommand = new SendVsxSDKProcess(context);//绿之云询问我当前的状态
                break;
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
                break;
            case 8:
                jumpCommand = new ChangeGroup(context);
            case 10:
                jumpCommand = new PushVideoLive(context);
                break;
            case 11:
                jumpCommand = new AddMemberToGroup(context);
                break;
            default:
                jumpCommand = null;
        }
        return jumpCommand;
    }

}
