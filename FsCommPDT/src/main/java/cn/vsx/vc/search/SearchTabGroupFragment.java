package cn.vsx.vc.search;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupViewHandler;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverSearchGroupDataCompleteHandler;

/**
 * 通讯录 搜索
 */
public class SearchTabGroupFragment extends BaseSearchFragment {

    private RecyclerView group_recyclerView;
    private Handler mHandler = new Handler();
    @Override
    public int getContentViewId() {
        return R.layout.fragment_search_group;
    }

    @Override
    public void initView() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        group_recyclerView = mRootView.findViewById(R.id.group_recyclerView);
        group_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(getContext(), datas,false);
        group_recyclerView.setAdapter(searchAdapter);
    }


    @Override
    public void initData() {
        List<GroupSearchBean> list = MyTerminalFactory.getSDK().getSearchDataManager().getGroupSreachDatas();
        if(list.size()>0){
            datas.clear();
            datas.addAll(list);
            if (searchAdapter!=null) {
                searchAdapter.notifyDataSetChanged();
            }
        }else{
            MyTerminalFactory.getSDK().getSearchDataManager().getDbAllGroup();
        }
    }

    @Override
    public void initListener() {
        TerminalFactory.getSDK().registReceiveHandler(receiverSearchGroupDataCompleteHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupViewHandler);

        /*---------------------------*/
        registReceiveHandler();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiverSearchGroupDataCompleteHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupViewHandler);

        /*---------------------------*/
        unregistReceiveHandler();
    }
    //刷新UI
    private ReceiverSearchGroupDataCompleteHandler receiverSearchGroupDataCompleteHandler = new ReceiverSearchGroupDataCompleteHandler() {
        @Override
        public void handler() {
            datas.clear();
            datas.addAll(MyTerminalFactory.getSDK().getSearchDataManager().getGroupSreachDatas());
            mHandler.post(() -> {
                if(searchAdapter!=null){
                    searchAdapter.notifyDataSetChanged();
                }
            });

        }
    };

    /**
     * 收到強制转组
     */
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            if (!forceSwitchGroup) {
                return;
            }
            MyTerminalFactory.getSDK().getSearchDataManager().getDbAllGroup();
        }
    };

    /**
     * 取消和监听通知
     */
    private ReceiveSetMonitorGroupViewHandler receiveSetMonitorGroupViewHandler = new ReceiveSetMonitorGroupViewHandler() {
        @Override
        public void handler() {
            if(searchAdapter !=null){
                mHandler.post(()->{
                    searchAdapter.notifyDataSetChanged();
                });
            }
        }
    };

}
