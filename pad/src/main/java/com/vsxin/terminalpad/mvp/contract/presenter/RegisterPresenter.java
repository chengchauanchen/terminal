package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.view.IRegisterView;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 注册登录
 */
public class RegisterPresenter extends BasePresenter<IRegisterView> {

    public RegisterPresenter(Context mContext) {
        super(mContext);
    }


    /**
     * 注册用户
     * @param userName 姓名
     * @param invitationCode 邀请码
     */
    public void registerUser(String userName,String invitationCode) {
        if (TextUtils.isEmpty(userName)) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_please_input_invitation_code));
            return;
        } else if (!DataUtil.isLegalOrg(invitationCode) || invitationCode.length() != 6) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_please_input_invitation_code_by_six_number));
            return;
        } else if (TextUtils.isEmpty(userName)) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_please_input_name));
            return;
        } else if (!DataUtil.isLegalName(userName) || userName.length() > 12 || userName.length() < 2) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_please_input_correct_name));
            return;
        }
        String registIP = TerminalFactory.getSDK().getAuthManagerTwo().getTempIp();
        String registPort = TerminalFactory.getSDK().getAuthManagerTwo().getTempPort();
        if (TextUtils.isEmpty(registIP) || TextUtils.isEmpty(registPort)) {
            ToastUtil.showToast(R.string.text_please_select_unit);
        } else {
            getView().changeProgressMsg(getContext().getString(R.string.text_registing));
            TerminalFactory.getSDK().getAuthManagerTwo().regist(userName, invitationCode);
        }
    }

}
