package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MessageListAdapter;
import ptt.terminalsdk.context.MyTerminalFactory;

public class CombatGroupActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener{


    @Bind(R.id.help_combat_list)
    ListView help_combat_list;
    @Bind(R.id.iv_return)
    ImageView iv_return;
    @Bind(R.id.tv_title)
    TextView tv_title;
    @Bind(R.id.tv_close_combat)
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
        tv_title.setText(getResources().getString(R.string.tv_combat_group));
        tv_close_combat.setVisibility(View.VISIBLE);
    }

    @Override
    public void initListener(){
        tv_close_combat.setOnClickListener(this);
        iv_return.setOnClickListener(this);
        help_combat_list.setOnItemClickListener(this);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
    }

    @Override
    public void initData(){
        loadMessages();
        sortMessageList();
        mMessageListAdapter = new MessageListAdapter(this, messageList, false, false);
        help_combat_list.setAdapter(mMessageListAdapter);
    }

    @Override
    public void doOtherDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
    }

    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler(){
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType){
            if (TempGroupType.TO_HELP_COMBAT.toString().equals(tempGroupType)) {
                if(!isAdd){
                    //如果消息列表里有合成作战组，去掉再刷新
                    Iterator<TerminalMessage> iterator = messageList.iterator();
                    while(iterator.hasNext()){
                        TerminalMessage next = iterator.next();
                        if(next.messageToId == tempGroupNo){
                            iterator.remove();
                        }
                    }
                    mHandler.post(()-> mMessageListAdapter.notifyDataSetChanged());
                }
            }
        }
    };

    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        synchronized(CombatGroupActivity.this){
            //Newsfragment已经处理了消息，只需重新从数据库取数据
            if (TerminalMessageUtil.isGroupMeaage(terminalMessage) && GroupUtils.isCombatGroup(terminalMessage.messageToId)){
                mHandler.postDelayed(()->{
                    loadMessages();
                    sortMessageList();
                    mMessageListAdapter.notifyDataSetChanged();
                },1000);
            }
        }
    };

    private void loadMessages(){
        synchronized(this){
            clearData();
            List<TerminalMessage> messageList = TerminalFactory.getSDK().getTerminalMessageManager().getCombatMessageList();
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
            case R.id.tv_close_combat:
                //去合成作战组历史界面
                Intent intent = new Intent(this,HistoryCombatGroupActivity.class);
                startActivity(intent);
            break;
            case R.id.iv_return:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        TerminalMessage terminalMessage = messageList.get(position);
        if(terminalMessage.unReadCount!=0){
            terminalMessage.unReadCount = 0;
            saveMessagesToSql();
        }

        Intent intent = new Intent(this, GroupCallNewsActivity.class);
        intent.putExtra("isGroup", TerminalMessageUtil.isGroupMeaage(terminalMessage));
        intent.putExtra("userId", TerminalMessageUtil.getNo(terminalMessage));//组id
        intent.putExtra("userName", TerminalMessageUtil.getTitleName(terminalMessage));
        startActivity(intent);
    }

    private void saveMessagesToSql(){
        synchronized(this){
            MyTerminalFactory.getSDK().getTerminalMessageManager().updateCombatMessageList(messageList);
        }
    }

    /**
     * 对聊天列表排序
     */
    private void sortMessageList(){
        synchronized(CombatGroupActivity.this){
            if(!messageList.isEmpty()){

                //再按照时间来排序
                Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
                //再保存到数据库
                saveMessagesToSql();
                if(mMessageListAdapter !=null){
                    Log.e("CombatGroupActivity", "messageList:" + messageList);
                    mMessageListAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
