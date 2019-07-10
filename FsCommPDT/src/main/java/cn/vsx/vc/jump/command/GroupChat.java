package cn.vsx.vc.jump.command;

import android.content.Context;
import android.text.TextUtils;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.constant.ParamKey;
import cn.vsx.vc.jump.utils.MemberUtil;
import cn.vsx.vc.utils.ToastUtil;

/**
 * 组会话
 */
public class GroupChat extends BaseCommand {

    public GroupChat(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.GroupChat;
    }


    @Override
    protected void jumpPage(SendBean sendBean) {
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
        Group group =  DataUtil.getTempGroupByGroupName(groupName);
        if(group==null){
            ToastUtil.showToast(context,"未找到当前组");
            return;
        }
        GroupCallNewsActivity.startCurrentActivity(context, group.getNo(), group.getName(), 0, "", true);
    }
}
