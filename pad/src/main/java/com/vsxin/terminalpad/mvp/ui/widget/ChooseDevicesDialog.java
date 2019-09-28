package com.vsxin.terminalpad.mvp.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.ui.adapter.ChooseDevicesAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import ptt.terminalsdk.tools.ToastUtil;

public class ChooseDevicesDialog extends Dialog {

    private TextView textTitle;
    private RecyclerView rvList;

    private Account account;
    private List<Member> list;
    private ChooseDevicesAdapter adapter;
    private ChooseDevicesAdapter.ItemClickListener mItemClickListener;

    private int type = 1;
    public static final int TYPE_CALL_PRIVATE = 1;//个呼
    public static final int TYPE_PULL_LIVE = 2;//请求图像
    public static final int TYPE_CALL_PHONE = 3;//打电话
    public static final int TYPE_PUSH_LIVE = 4;//上报图像

    public ChooseDevicesDialog(Context context, int type, Account account, ChooseDevicesAdapter.ItemClickListener mItemClickListener) {
        super(context, R.style.dialog);
        this.type = type;
        this.account = account;
        this.list = getList(account, type);
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose_devices);
        initView();
        init();
    }

    private void initView() {
        textTitle = findViewById(R.id.text_title);
        rvList = findViewById(R.id.rv_list);

        textTitle.setText(getDialogTitle(type));
        adapter = new ChooseDevicesAdapter(this.getContext(), this, list, mItemClickListener, (type == TYPE_CALL_PHONE));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvList.setLayoutManager(linearLayoutManager);
        rvList.setAdapter(adapter);
    }

    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.width = (int) (width * 0.9);
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
        window.setGravity(Gravity.RIGHT|Gravity.CENTER);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    /**
     * 获取标题
     *
     * @param type
     * @return
     */
    private String getDialogTitle(int type) {
        String title = "";
        switch (type) {
            case TYPE_CALL_PRIVATE:
                title = getContext().getString(R.string.choose_devices_to_private_call);
                break;
            case TYPE_PULL_LIVE:
                title = getContext().getString(R.string.choose_devices_to_request_live);
                break;
            case TYPE_CALL_PHONE:
                title = getContext().getString(R.string.choose_devices_to_call);
                break;
            case TYPE_PUSH_LIVE:
                title = getContext().getString(R.string.choose_devices_to_push_live);
                break;
            default:
                title = getContext().getString(R.string.choose_devices_to_private_call);
                break;
        }
        return title;
    }

    public void showDialog() {
        if (list != null && !list.isEmpty()) {
            if (list.size() == 1) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(this, list.get(0));
                }
            } else {
                show();
            }
        } else {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.no_use_devices));
        }
    }

    /**
     * 获取对应业务的数据集合
     *
     * @param account
     * @return
     */
    private List<Member> getList(Account account, int type) {
        List<Member> result = new ArrayList<>();
        if (account != null && account.getMembers() != null && !account.getMembers().isEmpty()) {
            switch (type) {
                //打电话
                case ChooseDevicesDialog.TYPE_CALL_PHONE:
                    result.addAll(getCallPhoneMemberList(account));
                    break;
                //个呼
                case ChooseDevicesDialog.TYPE_CALL_PRIVATE:
                    result.addAll(getCallPrivateMemberList(account));
                    break;
                //请求图像
                case ChooseDevicesDialog.TYPE_PULL_LIVE:
                    result.addAll(getPullLiveMemberList(account));
                    break;
                //请求图像
                case ChooseDevicesDialog.TYPE_PUSH_LIVE:
                    result.addAll(getPushLiveMemberList(account));
                    break;
            }
        }
        return DataUtil.removeMemberMyself(result);
    }

    /**
     * 获取可以打电话的设备信息
     *
     * @param account
     * @return
     */
    private List<Member> getCallPhoneMemberList(Account account) {
        List<Member> result = new ArrayList<>();
        for (Member member : account.getMembers()) {
            if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
                    member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode() &&
                    member.type != TerminalMemberType.TERMINAL_PDT.getCode()) {
                result.add(member);
                //因为电话号码都是一样的，获取到任意一个就跳出循环
                System.out.println("跳出循环");
                break;
            }
        }
//        for (Member member : account.getMembers()) {
//                if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
//                        member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode() &&
//                        member.type != TerminalMemberType.TERMINAL_PDT.getCode()) {
//                    result.add(member);
//                }
//            }
        //普通电话
        if (!TextUtils.isEmpty(account.getPhone())) {
            Member m = new Member();
            m.type = TerminalMemberType.TERMINAL_PHONE.getCode();
            m.setUniqueNo(0);
            m.setPhone(account.getPhone());
            result.add(m);
        }
        return result;
    }

    /**
     * 获取可以打个呼的设备信息
     *
     * @param account
     * @return
     */
    private List<Member> getCallPrivateMemberList(Account account) {
        List<Member> result = new ArrayList<>();
        for (Member member : account.getMembers()) {
            if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
                    member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()) {
                result.add(member);
            }
        }
        return result;
    }

    /**
     * 获取可以请求图像的设备信息
     *
     * @param account
     * @return
     */
    private List<Member> getPullLiveMemberList(Account account) {
        List<Member> result = new ArrayList<>();
        for (Member member : account.getMembers()) {
            if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
                    member.type != TerminalMemberType.TERMINAL_PDT.getCode() &&
                    member.type != TerminalMemberType.TERMINAL_PC.getCode()) {
                result.add(member);
            }
        }
        return result;
    }

    /**
     * 获取可以上报图像的设备信息
     *
     * @param account
     * @return
     */
    private List<Member> getPushLiveMemberList(Account account) {
        List<Member> result = new ArrayList<>();
        for (Member member : account.getMembers()) {
            if (member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode() &&
                    member.type != TerminalMemberType.TERMINAL_PDT.getCode()) {
                result.add(member);
            }
        }
        return result;
    }


}
