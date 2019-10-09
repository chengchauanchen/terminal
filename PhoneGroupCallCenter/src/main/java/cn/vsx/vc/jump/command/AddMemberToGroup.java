package cn.vsx.vc.jump.command;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.utils.AppKeyUtils;
import cn.vsx.vc.jump.utils.MemberUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 添加成员到临时组
 */
public class AddMemberToGroup extends BaseCommand implements IJumpCommand {

    public AddMemberToGroup(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.AddMemberToGroup;
    }

    @Override
    public void jumpPage(SendBean sendBean) {
        String businessId = sendBean.getGroupNo();
        List<String> memberNos = sendBean.getNumberList();
        List<Integer> members = getMembers(memberNos);
        getMemberToNo(members, businessId, TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 获取 警号 int型
     * @param memberNos
     * @return
     */
    private List<Integer> getMembers(List<String> memberNos){
        List<Integer> nos = new ArrayList<>();
        for (String member : memberNos){
            nos.add(MemberUtil.strToInt(member));
        }
        return nos;
    }


    /**
     * 获取成员 UniqueNo
     *
     * @param memberNos
     * @param type
     */
    private void getMemberToNo(List<Integer> memberNos, String businessId, int type) {
        List<Long> uniqueNoList = new ArrayList<>();

        TerminalFactory.getSDK().getThreadPool().execute(() -> {
//            List<Account> accounts = DataUtil.getAccountsByMemberNos(memberNos);
            List<Account> accounts = new ArrayList<>();
            for (int memberNo : memberNos){
                Account account = DataUtil.getAccountByMemberNo(memberNo, true);
                accounts.add(account);
            }
            for (Account account : accounts){
                if (account != null && !account.getMembers().isEmpty()){
                    List<Member> members = account.getMembers();
                    for (Member member : members) {
                        if (type == member.getType()) {
                            uniqueNoList.add(member.getUniqueNo());
                            break;
                        }
                    }
                }
            }

            Group group = getTempGroupNoToBusinessId(businessId);
            if (group != null && uniqueNoList.size() > 0) {
                addMember(group.getNo(), uniqueNoList);
            }else{
                AppKeyUtils.setAppKey(null);
            }
        });
    }


    /**
     * 通过businessId获取组GroupNo
     */
    private Group getTempGroupNoToBusinessId(String BusinessId) {
        Group group = DataUtil.getTempGroupByGroupName(BusinessId);
        if (group == null) {
            //为空，主动请求一次
            TerminalFactory.getSDK().getConfigManager().updateAllGroupInfo(false);
            Group group2 = DataUtil.getTempGroupByGroupName(BusinessId);
            if (group2 == null) {
                ToastUtil.showToast(context, "未找到当前组,请重试");
                return null;
            }
            return group2;
        } else {
            return group;
        }
    }

    private void addMember(int tempGroupNo, List<Long> uniqueNoList) {
        int requestMemberNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        long requestUniqueNo = TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
        MyTerminalFactory.getSDK().getTempGroupManager().addMemberToTempGroup(tempGroupNo, requestMemberNo, requestUniqueNo, uniqueNoList);
    }
}
