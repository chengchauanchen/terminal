package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.Bind;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdatePhoneMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.LiveContactsAdapter;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class DeleteTemporaryGroupMemberActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.create_temporary_select_member)
    LinearLayout create_temporary_select_member;
    @Bind(R.id.iv_create_temporary_selectmember_return)
    ImageView iv_create_temporary_selectmember_return;
    @Bind(R.id.btn_create_temporary_selectmember_start)
    Button btn_create_temporary_selectmember_start;
    @Bind(R.id.et_search_allcontacts)
    EditText et_search_allcontacts;
    @Bind(R.id.tv_checktext)
    TextView tv_checktext;
    @Bind(R.id.horizonMenu)
    HorizontalScrollView horizonMenu;
    @Bind(R.id.search_select)
    ImageView search_select;
    @Bind(R.id.ll_no_info)
    LinearLayout ll_no_info;
    @Bind(R.id.tv_no_user)
    TextView tv_no_user;
    @Bind(R.id.img_cencle)
    ImageView img_cencle;
    @Bind(R.id.txt_create_temporary_selectmember_title)
    TextView txt_create_temporary_selectmember_title;
    @Bind(R.id.lv_create_temporary_select_member_listview)
    ListView lv_create_temporary_select_member_listview;

    private List<Member> memberList = new ArrayList<>();
    private List<Member> selectItem = new ArrayList<>();
    private int screenWidth;
    private LiveContactsAdapter liveContactsAdapter;
    private Handler myHandler = new Handler();
    private int CREATE_TEMPORARY_GROUP_MEMBER=0;
    private int CURRENT_TEMPORARY_GROUP_MEMBER=1;
    private int type;
    public static DeleteTemporaryGroupMemberActivity instance = null;
    @Override
    public int getLayoutResId() {
        return R.layout.activity_delete_temporary_group_member;
    }

    @Override
    public void initView() {
        instance=this;
        type = getIntent().getIntExtra("type",-1);
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        if(type==CREATE_TEMPORARY_GROUP_MEMBER){
            txt_create_temporary_selectmember_title.setText("创建临时组");
        }else if(type==CURRENT_TEMPORARY_GROUP_MEMBER){
            txt_create_temporary_selectmember_title.setText("添加成员");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void initListener() {

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePhoneMemberHandler);

        //选择列表
        iv_create_temporary_selectmember_return.setOnClickListener(this);//返回
        btn_create_temporary_selectmember_start.setOnClickListener(this);//下一步
        et_search_allcontacts.addTextChangedListener(new EditChangeListener());
        et_search_allcontacts.setOnFocusChangeListener(new EditListener());
        liveContactsAdapter.setOnItemClickListener(new OnInvitaListViewItemClick());
        img_cencle.setOnClickListener(v -> et_search_allcontacts.setText(""));
    }

    @Override
    public void initData() {
        CopyOnWriteArrayList<Member> phoneContacts = MyTerminalFactory.getSDK().getSQLiteDBManager().getPhoneMember();
        logger.info(phoneContacts.toString());
        memberList.addAll(phoneContacts);
        Collections.sort(memberList);
        liveContactsAdapter = new LiveContactsAdapter(getApplicationContext(), memberList, true);
        lv_create_temporary_select_member_listview.setAdapter(liveContactsAdapter);
        liveContactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePhoneMemberHandler);

        selectItem.clear();
        memberList.clear();
        tv_checktext.setText("");
        et_search_allcontacts.setText("");
        btn_create_temporary_selectmember_start.setText("下一步");
        btn_create_temporary_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
    }

    private long lastSearchTime=0;
    private long currentTime=System.currentTimeMillis();
    @Override
    public void onClick(View v) {
        if( currentTime- lastSearchTime<1000){
            return;
        }
        currentTime=lastSearchTime;
        switch (v.getId()){
            case R.id.iv_create_temporary_selectmember_return://返回
                finish();
                break;
            case R.id.btn_create_temporary_selectmember_start://下一步
                if(type==CREATE_TEMPORARY_GROUP_MEMBER){
                    ArrayList<Integer> list=new ArrayList<>();
                    list.addAll(liveContactsAdapter.getPushMemberList());
                    Intent intent = new Intent(DeleteTemporaryGroupMemberActivity.this, CreateTemporaryGroupsActivity.class);
                    intent.putIntegerArrayListExtra("list",list);
                    startActivity(intent);
                }else if(type==CURRENT_TEMPORARY_GROUP_MEMBER){
                    ArrayList<Integer> list=new ArrayList<>();
                    list.addAll(liveContactsAdapter.getPushMemberList());
                    Intent intent = new Intent(DeleteTemporaryGroupMemberActivity.this, CreateTemporaryGroupsActivity.class);
                    intent.putIntegerArrayListExtra("list",list);
                    startActivity(intent);
                }else {
                    ToastUtil.showToast(DeleteTemporaryGroupMemberActivity.this,"跳转界面失败");
                }


                break;
        }
    }

    //更新警务通成员信息
    private ReceiveUpdatePhoneMemberHandler receiveUpdatePhoneMemberHandler = new ReceiveUpdatePhoneMemberHandler() {
        @Override
        public void handler(final List<Member> allMembers) {
            myHandler.post(() -> {
                logger.info("警务通列表成员-----memberList" + allMembers);
                memberList.clear();
                memberList.addAll(allMembers);
                liveContactsAdapter.addData(memberList);
            });
        }
    };

    private final class EditChangeListener implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }


        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (TextUtils.isEmpty(String.valueOf(s))) {
                lv_create_temporary_select_member_listview.setVisibility(View.VISIBLE);
                ll_no_info.setVisibility(View.GONE);
                liveContactsAdapter.setKeyWords(s.toString());
                img_cencle.setVisibility(View.GONE);
            } else {
                if (get(s.toString()).size() <= 0) {
                    lv_create_temporary_select_member_listview.setVisibility(View.GONE);
                    ll_no_info.setVisibility(View.VISIBLE);
                    tv_no_user.setText(et_search_allcontacts.getText().toString());
                } else {
                    lv_create_temporary_select_member_listview.setVisibility(View.VISIBLE);
                    ll_no_info.setVisibility(View.GONE);
                    liveContactsAdapter.setKeyWords(s.toString());
                }

                img_cencle.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private final class EditListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText et = (EditText) v;
            if (hasFocus) {
                search_select.setVisibility(View.VISIBLE);
            } else {
                search_select.setVisibility(View.VISIBLE);
            }
        }
    }


    private class OnInvitaListViewItemClick implements LiveContactsAdapter.OnItemClickListener {

        @Override
        public void onItemClick(List<Member>pushMembers ,Member liveMember,boolean isPush) {

            StringBuffer sb = new StringBuffer();
            if(isPush){
                for(Member m : pushMembers){
                    sb.append(m.getName()).append("  ");
                }
                if(!pushMembers.isEmpty()){
                    btn_create_temporary_selectmember_start.setText(String.format("开始(%s)",pushMembers.size()));
                    btn_create_temporary_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                }else{
                    btn_create_temporary_selectmember_start.setText("开始");
                    btn_create_temporary_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                }
            }else{

                if(null != liveMember){
                    sb.append(liveMember.getName());
                    btn_create_temporary_selectmember_start.setText(String.format("开始(%s)",1));
                    btn_create_temporary_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                }else{
                    btn_create_temporary_selectmember_start.setText("开始");
                    btn_create_temporary_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg_no);
                }
            }
            et_search_allcontacts.setText("");

            tv_checktext.setText(sb);
            //获取textview宽度
            TextPaint textPaint = new TextPaint();
            textPaint = tv_checktext.getPaint();
            float textPaintWidth = textPaint.measureText(sb.toString());
            if (textPaintWidth >= screenWidth - (screenWidth / 4)) {
                horizonMenu.setLayoutParams(new LinearLayout.LayoutParams(screenWidth - (screenWidth / 4), ViewGroup.LayoutParams.WRAP_CONTENT));
                logger.info("textView的宽度达到了屏幕的五分之四");
            } else {
                horizonMenu.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }


    private List<Member> get(String content) {
        List<Member> list = new ArrayList<>();
        if (memberList == null) {
            memberList = new ArrayList<>();
        } else {
            for (int i = 0; i < memberList.size(); i++) {

                if (String.valueOf(memberList.get(i).id).contains(content)) {
                    list.add(memberList.get(i));
                } else {
                    String name = memberList.get(i).getName();
                    if (!Util.isEmpty(name) && !Util.isEmpty(content) && name.toLowerCase().contains(content.toLowerCase())) {
                        list.add(memberList.get(i));
                    }
                }
            }
        }
        return list;
    }
}
