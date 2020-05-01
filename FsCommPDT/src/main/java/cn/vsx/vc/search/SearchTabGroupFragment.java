package cn.vsx.vc.search;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.allen.library.observer.CommonObserver;

import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllGroupHandler;
import cn.vsx.vc.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.manager.search.SearchUtil;

/**
 * 通讯录 搜索
 */
public class SearchTabGroupFragment extends BaseSearchFragment {

    private RecyclerView group_recyclerView;

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
        searchAdapter = new SearchAdapter(getContext(), datas);
        group_recyclerView.setAdapter(searchAdapter);
    }


    @Override
    public void initData() {
        getDbAllGroup();
    }

    @Override
    public void initListener() {
        TerminalFactory.getSDK().registReceiveHandler(receiveGetAllGroupHandler);
        /*---------------------------*/
        registReceiveHandler();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllGroupHandler);

        /*---------------------------*/
        unregistReceiveHandler();
    }

    //所有组
    private ReceiveGetAllGroupHandler receiveGetAllGroupHandler = new ReceiveGetAllGroupHandler() {
        @Override
        public void handler(List<Group> groups) {
            logger.info("SearchTabFragment获取组数据:" + groups);
            getDbAllGroup();
        }
    };

    /******************************数据处理*********************************/


    /**
     * 获取本地数据库 组数据
     */
    private void getDbAllGroup() {
        SearchUtil.getDbAllGroup()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<List<GroupSearchBean>>() {
                    @Override
                    protected String setTag() {
                        return "";
                    }

                    @Override
                    protected void onError(String errorMsg) {
                        logger.error("getTotalCountPolice----请求报错:" + errorMsg);
                    }

                    @Override
                    protected void onSuccess(List<GroupSearchBean> allRowSize) {
                        logger.info("获取本地数据库 组数据"+allRowSize);
                        datas.clear();
                        datas.addAll(allRowSize);
                        searchAdapter.notifyDataSetChanged();
                    }
                });
    }


}
