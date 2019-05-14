package cn.vsx.vc.activity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MessageListAdapter;
import ptt.terminalsdk.context.MyTerminalFactory;

public class HistoryCombatGroupActivity extends BaseActivity implements View.OnClickListener{



    ListView help_combat_list;

    ImageView iv_return;

    TextView tv_title;

    TextView tv_close_combat;

    private MessageListAdapter mMessageListAdapter;
    private ArrayList<TerminalMessage> messageList = new ArrayList<>();
    private List<TerminalMessage> terminalMessageData = new ArrayList<>();
    private Handler mHandler = new Handler();

    @Override
    public int getLayoutResId(){
        return R.layout.activity_combat_group;
    }

    @Override
    public void initView(){
        help_combat_list = (ListView) findViewById(R.id.help_combat_list);
        iv_return = (ImageView) findViewById(R.id.iv_return);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_close_combat = (TextView) findViewById(R.id.tv_close_combat);
        tv_close_combat.setVisibility(View.GONE);
        tv_title.setText(getResources().getString(R.string.tv_close_combat));
    }

    @Override
    public void initListener(){
        iv_return.setOnClickListener(this);
    }

    @Override
    public void initData(){
        loadMessages();
        mMessageListAdapter = new MessageListAdapter(this, messageList, false, true);
        help_combat_list.setAdapter(mMessageListAdapter);
    }

    @Override
    public void doOtherDestroy(){
    }

    private void loadMessages(){
        synchronized(this){
            clearData();
            List<TerminalMessage> messageList = TerminalFactory.getSDK().getTerminalMessageManager().getHistoryCombatMessageList();
            addData(messageList);
        }
    }
    private void clearData(){
        synchronized(this){
            messageList.clear();
        }
    }
    private void addData(List<TerminalMessage> terminalMessages){
        synchronized(this){
            messageList.addAll(terminalMessages);
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.iv_return:
                finish();
                break;
        }
    }

    private void saveMessagesToSql(){
        synchronized(this){
            MyTerminalFactory.getSDK().getTerminalMessageManager().updateHistoryCombatMessageList(messageList);
        }
    }

    /**
     * 对聊天列表排序
     */
    private void sortMessageList(){
        synchronized(HistoryCombatGroupActivity.this){
            if(!messageList.isEmpty()){

                //再按照时间来排序
                Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
                //再保存到数据库
                saveMessagesToSql();
                if(mMessageListAdapter !=null){
                    Log.e("NewsFragment", "messageList:" + messageList);
                    mMessageListAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
