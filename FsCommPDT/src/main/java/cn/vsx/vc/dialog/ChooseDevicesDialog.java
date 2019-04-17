package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.ChooseDevicesAdapter;
import cn.vsx.vc.utils.ToastUtil;

public class ChooseDevicesDialog extends Dialog {

    private TextView textTitle;
    private RecyclerView rvList;

    private List<Member> list;
    private ChooseDevicesAdapter adapter;
    private ChooseDevicesAdapter.ItemClickListener mItemClickListener;

    private int type = 1;
    public static final int TYPE_CALL_PRIVATE = 1;//个呼
    public static final int TYPE_PULL_LIVE = 2;//请求图像
    public static final int TYPE_CALL_PHONE = 3;//打电话
    public static final int TYPE_PUSH_LIVE = 4;//上报图像

    public ChooseDevicesDialog(Context context, int type, List<Member> list, ChooseDevicesAdapter.ItemClickListener mItemClickListener) {
        super(context);
        this.type = type;
        this.list = getList(list, type);
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
        adapter = new ChooseDevicesAdapter(this.getContext(), list, mItemClickListener, (type == TYPE_CALL_PHONE));
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
//        layoutParams.width= (int) (width*0.9);
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
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
            show();
        }else{
            ToastUtil.showToast(getContext(),getContext().getString(R.string.no_use_devices));
        }
    }

    /**
     * 获取对应业务的数据集合
     *
     * @param list
     * @return
     */
    private List<Member> getList(List<Member> list, int type) {
        List<Member> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            switch (type) {
                //打电话
                case ChooseDevicesDialog.TYPE_CALL_PHONE:
                    result.addAll(getCallPhoneMemberList(list));
                    break;
                //个呼
                case ChooseDevicesDialog.TYPE_CALL_PRIVATE:
                    result.addAll(getCallPrivateMemberList(list));
                    break;
                //请求图像
                case ChooseDevicesDialog.TYPE_PULL_LIVE:
                    result.addAll(getPullLiveMemberList(list));
                    break;
                //请求图像
                case ChooseDevicesDialog.TYPE_PUSH_LIVE:
                    result.addAll(getPushLiveMemberList(list));
                    break;
            }
        }
        return result;
    }

    /**
     * 获取可以打电话的设备信息
     *
     * @param list
     * @return
     */
    private List<Member> getCallPhoneMemberList(List<Member> list) {
        List<Member> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (Member member : list) {
                if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
                        member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode() &&
                        member.type != TerminalMemberType.TERMINAL_PDT.getCode()) {
                    result.add(member);
                }
            }
        }
        //普通电话
        Member m = new Member();
        m.type = TerminalMemberType.TERMINAL_PHONE.getCode();
        m.setUniqueNo(0);
        result.add(m);
        return result;
    }

    /**
     * 获取可以打个呼的设备信息
     *
     * @param list
     * @return
     */
    private List<Member> getCallPrivateMemberList(List<Member> list) {
        List<Member> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (Member member : list) {
                if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
                        member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()) {
                    result.add(member);
                }
            }
        }
        return result;
    }

    /**
     * 获取可以请求图像的设备信息
     *
     * @param list
     * @return
     */
    private List<Member> getPullLiveMemberList(List<Member> list) {
        List<Member> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (Member member : list) {
                if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
                        member.type != TerminalMemberType.TERMINAL_PDT.getCode()) {
                    result.add(member);
                }
            }
        }
        return result;
    }

    /**
     * 获取可以上报图像的设备信息
     *
     * @param list
     * @return
     */
    private List<Member> getPushLiveMemberList(List<Member> list) {
        List<Member> result = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (Member member : list) {
                if (member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode() &&
                        member.type != TerminalMemberType.TERMINAL_PDT.getCode()) {
                    result.add(member);
                }
            }
        }
        return result;
    }


}
