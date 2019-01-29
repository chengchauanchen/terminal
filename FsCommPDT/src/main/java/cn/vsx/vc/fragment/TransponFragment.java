package cn.vsx.vc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.TransponListAdapter;
import cn.vsx.vc.model.ChatMember;
import cn.vsx.vc.receiveHandle.ReceiverTransponHandler;
import cn.vsx.vc.utils.DialogUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/10/20.
 */

public class TransponFragment extends BaseFragment{
    @Bind(R.id.lv_chat_member)
    ListView lv_chat_member;

    private List<ChatMember> chatLists = new ArrayList<>();

    private TransponListAdapter transponListAdapter;
    private int userId;
    private int messageType;

    private FrameLayout fragmentContainer;

    public static TransponFragment getInstance (int userId, int messageType) {
        TransponFragment fragment = new TransponFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);
        bundle.putInt("messageType", messageType);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setFragmentContainer (FrameLayout fragmentContainer) {
        this.fragmentContainer = fragmentContainer;
    }

    @Override
    public int getContentViewId() {
        return R.layout.popup_transpon;
    }

    @Override
    public void initView() {
        userId = getArguments().getInt("userId");
        messageType = getArguments().getInt("messageType");
        transponListAdapter = new TransponListAdapter(chatLists, context);
        lv_chat_member.setAdapter(transponListAdapter);
    }

    @Override
    public void initListener() {
        lv_chat_member.setOnItemClickListener((parent, view, position, id) -> new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                return "确定转发消息?";
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public void doConfirmThings() {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverTransponHandler.class, chatLists.get(position));
                fragmentContainer.setVisibility(View.GONE);
                getFragmentManager().popBackStack();
            }

            @Override
            public void doCancelThings() {
            }
        }.showDialog());
    }

    @Override
    public void initData() {
        List<Group> groups = MyTerminalFactory.getSDK().getConfigManager().getAllGroups();
        //消息类型不为图像和个呼的时候
        if(messageType != MessageType.VIDEO_LIVE.getCode() && messageType != MessageType.PRIVATE_CALL.getCode()) {
                for(Group group : groups) {
                    if(group.id != userId) {
                        ChatMember chatMember = new ChatMember(group.id, group.name, true);
                        chatLists.add(chatMember);
                    }
                }
        }
        //消息类型不为组呼和录音的时候
        if(messageType != MessageType.GROUP_CALL.getCode() && messageType != MessageType.AUDIO.getCode()) {
            List<Member> members = MyTerminalFactory.getSDK().getConfigManager().getAllMembers();
            for (Member member : members) {
                if(member.id != userId) {
                    ChatMember chatMember = new ChatMember(member.id, member.getName(), false);
                    chatLists.add(chatMember);
                }

            }
        }
        transponListAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.news_bar_return})
    public void onClick (View view) {
        switch (view.getId()) {
            case R.id.news_bar_return:
                fragmentContainer.setVisibility(View.GONE);
                getFragmentManager().popBackStack();
                break;
        }
    }
}
