package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.ChooseDevicesAdapter;
import cn.vsx.vc.adapter.MergeTransmitListAdapter;
import cn.vsx.vc.model.CatalogBean;

public class ChooseDevicesDialog extends Dialog {

    private TextView textTitle;
    private RecyclerView rvList;

    private List<Member> list;
    private ChooseDevicesAdapter adapter;
    private ChooseDevicesAdapter.ItemClickListener mItemClickListener;

    private int type  = 1;
    public static final int TYPE_CALL_PRIVATE = 1;//个呼
    public static final int TYPE_LIVE = 2;//请求图像
    public static final int TYPE_CALL_PHONE = 3;//打电话
    public ChooseDevicesDialog(Context context,int type, List<Member> list, ChooseDevicesAdapter.ItemClickListener mItemClickListener) {
        super(context);
        this.type = type;
        this.list = list;
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
        adapter = new ChooseDevicesAdapter(this.getContext(), list, mItemClickListener);
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
     * @param type
     * @return
     */
    private String getDialogTitle(int type){
        String title = "";
        switch (type){
            case TYPE_CALL_PRIVATE:
                title = getContext().getString(R.string.choose_devices_to_private_call);
                break;
            case TYPE_LIVE:
                title = getContext().getString(R.string.choose_devices_to_request_live);
                break;
            case TYPE_CALL_PHONE:
                title = getContext().getString(R.string.choose_devices_to_call);
                break;
                default:
                    title = getContext().getString(R.string.choose_devices_to_private_call);
                    break;
        }
        return title;
    }

}
