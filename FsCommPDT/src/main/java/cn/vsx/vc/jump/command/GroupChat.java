package cn.vsx.vc.jump.command;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.utils.MemberUtil;
import cn.vsx.vc.utils.ToastUtil;

/**
 * 组会话
 */
public class GroupChat extends BaseCommand {

    private String groupName;

    public GroupChat(Context context) {
        super(context);

    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.GroupChat;
    }


    @Override
    protected void jumpPage(SendBean sendBean) {
        Log.e("JumpService", "GroupChat--jumpPage");
        int groupNo = MemberUtil.strToInt(sendBean.getGroupNo());
        String groupName = sendBean.getGroupName();
        if(TextUtils.isEmpty(groupName)){
            jumpGroupChatActivity(getContext(), groupNo);
        }else{
            jumpGroupChatActivityForName(getContext(),groupName);
        }
    }

    /**
     * 跳转到组会话
     *
     * @param groupNo
     */
    public void jumpGroupChatActivity(Context context, int groupNo) {
        //如果groupNo为空，则默认进入前期组
        if(groupNo==0){
            groupNo =  TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0);
        }
        Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
        GroupCallNewsActivity.startCurrentActivity(context, groupNo, group.getName(), 0, "", true);
    }

    public void jumpGroupChatActivityForName(Context context, String groupName) {
        Log.e("JumpService", "GroupChat--jumpGroupChatActivityForName");

        this.groupName = groupName;
        Group group =  DataUtil.getTempGroupByGroupName(groupName);
        if(group==null){
            //为空，主动请求一次
            TerminalFactory.getSDK().getConfigManager().updateAllGroupInfo(false);
            Group group2 =  DataUtil.getTempGroupByGroupName(groupName);
            if(group2==null){
                ToastUtil.showToast(context,"未找到当前组,请重试");
            }else{
                GroupCallNewsActivity.startCurrentActivity(context, group2.getNo(), group2.getName(), 0, "", true);
            }
            return;
        }
        GroupCallNewsActivity.startCurrentActivity(context, group.getNo(), group.getName(), 0, "", true);
    }
}
