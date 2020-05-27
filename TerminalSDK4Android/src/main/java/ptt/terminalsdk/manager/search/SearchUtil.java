package ptt.terminalsdk.manager.search;

import android.text.TextUtils;
import android.util.Log;

import com.pinyinsearch.model.PinyinSearchUnit;
import com.pinyinsearch.util.T9Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.bean.SearchTitleBean;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverUpdateTopContactsHandler;

public class SearchUtil {

    public static Observable<List<Object>> searchDbAllData(String curCharacter) {
        return Observable.zip(SearchUtil.getDbAllGroup(), SearchUtil.getDbAllAccount(), new BiFunction<List<GroupSearchBean>, List<MemberSearchBean>, Map<String, Object>>() {
            @Override
            public Map<String, Object> apply(List<GroupSearchBean> groupSearchBeans, List<MemberSearchBean> memberSearchBeans) throws Exception {
                Map<String, Object> map = new HashMap();
                if (groupSearchBeans == null || groupSearchBeans.size() == 0) {
                    Log.i("SearchUtil", "更新通讯录组数据 没有缓存正在网络同步");
                }
                if (memberSearchBeans == null || memberSearchBeans.size() == 0) {
                    Log.i("SearchUtil", "更新通讯录人数据 没有缓存正在网络同步");
                }
                map.put("group", groupSearchBeans);
                map.put("Member", memberSearchBeans);
                return map;
            }
        }).flatMap(new Function<Map<String, Object>, ObservableSource<List<Object>>>() {
            @Override
            public ObservableSource<List<Object>> apply(Map<String, Object> map) throws Exception {
                List<GroupSearchBean> group = (List<GroupSearchBean>) map.get("group");
                List<MemberSearchBean> members = (List<MemberSearchBean>) map.get("Member");
                return searcAll(curCharacter, group, members);
            }
        });
    }


    public static Observable<List<Object>> searcAll(String curCharacter, List<GroupSearchBean> groupDatas, List<MemberSearchBean> memberDatas) {
        return Observable.zip(SearchUtil.searchObservable(curCharacter, groupDatas),
                SearchUtil.searchMemberObservable(curCharacter, memberDatas),
                (groupSearchBeans, memberSearchBeans) -> {
                    List<Object> datas = new ArrayList<>();
                    if (groupSearchBeans != null && groupSearchBeans.size() > 0) {
                        SearchTitleBean titleBean = new SearchTitleBean("组");
                        datas.add(titleBean);
                        datas.addAll(groupSearchBeans);
                    }

                    if (memberSearchBeans != null && memberSearchBeans.size() > 0) {
                        SearchTitleBean titleBean2 = new SearchTitleBean("工作人员");
                        datas.add(titleBean2);
                        datas.addAll(memberSearchBeans);
                    }
                    return datas;
                });
    }


    /*----------------人-----------------------*/

    public static Observable<Boolean> syncAllData() {
        return Observable.zip(getGroupsAllObservable(), getDeptAllDataObservable(), new BiFunction<List<Group>, List<Account>, Boolean>() {
            @Override
            public Boolean apply(List<Group> groups, List<Account> accounts) throws Exception {
                if (groups != null && accounts != null) {
                    Log.i("SearchUtil", "更新通讯录组数据+人数据 网络同步完成:"+groups.size()+","+accounts.size());
                    return true;
                } else {
                    Log.i("SearchUtil", "更新通讯录组数据+人数据 网络同步失败");
                    return false;
                }
            }
        });
    }

    /**
     * 网络同步组数据
     *
     * @return
     */
    public static Observable<List<Group>> getGroupsAllObservable() {
        return Observable.fromCallable(new Callable<List<Group>>() {
            @Override
            public List<Group> call() throws Exception {
                Log.i("SearchUtil", "更新通讯录组数据 没有缓存正在网络同步");
                return TerminalFactory.getSDK().getConfigManager().getGroupsAllSync(true);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 网络同步人数据
     *
     * @return
     */
    public static Observable<List<Account>> getDeptAllDataObservable() {
        return Observable.fromCallable(new Callable<List<Account>>() {
            @Override
            public List<Account> call() throws Exception {
                Log.i("SearchUtil", "更新通讯录人数据 没有缓存正在网络同步");
                return TerminalFactory.getSDK().getConfigManager().getDeptAllDataSync(true);
            }
        }).subscribeOn(Schedulers.io());
    }


    public static Observable<List<GroupSearchBean>> getListenedGroup(){
        return Observable.fromCallable(()-> TerminalFactory.getSDK().getConfigManager().getMonitorSearchGroup());
    }

    /**
     * 获取监听组
     * @return
     */
    public static Observable<List<GroupSearchBean>> getMonitorGroupList(){
        return Observable.fromCallable(()-> TerminalFactory.getSDK().getConfigManager().getMonitorGroupList());
    }

    /**
     * 组 拼音搜索Observable
     *
     * @param keyword
     * @param accounts
     * @return
     */
    public static Observable<List<MemberSearchBean>> searchMemberObservable(String keyword, List<MemberSearchBean> accounts) {
        return Observable.fromCallable(() -> {
            List<MemberSearchBean> search = SearchUtil.searchMember(keyword, accounts);
            Log.e("SearchUtil", "搜索成员数据search.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }


    /**
     * 组 拼音搜索
     *
     * @param keyword
     * @param memberSearchs
     * @return
     */
    public static List<MemberSearchBean> searchMember(String keyword, List<MemberSearchBean> memberSearchs) {
        List<MemberSearchBean> mQwertySearchGroups = new ArrayList<>();
        if (TextUtils.isEmpty(keyword)) {
            return mQwertySearchGroups;
        }
        for (MemberSearchBean memberSearchBean : memberSearchs) {
            PinyinSearchUnit labelPinyinSearchUnit = memberSearchBean.getLabelPinyinSearchUnit();
            boolean match = T9Util.match(labelPinyinSearchUnit, keyword);
            if (true == match) {// search by LabelPinyinUnits;
                MemberSearchBean member = memberSearchBean;
                member.setSearchByType(MemberSearchBean.SearchByType.SearchByLabel);
                member.setMatchKeywords(labelPinyinSearchUnit.getMatchKeyword().toString());
                member.setMatchStartIndex((member.getName() + member.getNo()).indexOf(member.getMatchKeywords().toString()));
                member.setMatchLength(member.getMatchKeywords().length());
                mQwertySearchGroups.add(member);
                continue;
            }
        }
        return mQwertySearchGroups;
    }

    /**
     * 获取本地数据库 人数据
     *
     * @return
     */
    public static Observable<List<MemberSearchBean>> getDbAllAccount() {
        return Observable.fromCallable(() -> {
            List<MemberSearchBean> search = TerminalFactory.getSDK().getSQLiteDBManager().getAllAccount(new ArrayList<>(),0);
            Log.e("SearchUtil", "getDbAllAccount-成员数据.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取本地数据库 人数据
     *
     * @return
     */
    public static Observable<List<MemberSearchBean>> getAllAccountFirst() {
        return Observable.fromCallable(() -> {
            List<MemberSearchBean> search = TerminalFactory.getSDK().getSQLiteDBManager().getAllAccountFirst();
            Log.e("SearchUtil", "getDbAllAccount-成员数据.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }


    /*----------------组-----------------------*/

    /**
     * 获取当前组的信息
     * @return
     */
    public static GroupSearchBean getCurrentGroupInfo() {
        int groupNo = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0);
        GroupSearchBean bean = null;
        //判断内存中是否有组的数据
        int size = MyTerminalFactory.getSDK().getSearchDataManager().getGroupSreachDatas().size();
        Log.e("SearchUtil", "getCurrentGroupInfo-size:" + size);
        if(size>0){
            //有数据就从内存中取
            bean = MyTerminalFactory.getSDK().getSearchDataManager().getSearchGroupByNo(groupNo);
        }
        Log.e("SearchUtil", "getCurrentGroupInfo-getSearchGroupByNo:" + bean);
        if(size<=0 || isNeedGetGroupInfo(bean)){
            //如果内存中没有，就从数据库中取
            bean = TerminalFactory.getSDK().getSQLiteDBManager().getGroupByNo(groupNo);
        }
        Log.e("SearchUtil", "getCurrentGroupInfo-getGroupByNo:" + bean);
        //如果数据库中没有，就从服务器获取
//        if(isNeedGetGroupInfo(bean)){
//            bean = TerminalFactory.getSDK().getDataManager().getGroupSearchBeanByNoWithNoThread(groupNo);
//        }
//        Log.e("SearchUtil", "getCurrentGroupInfo-getGroupSearchBeanByNoWithNoThread:" + bean);
        //如果服务器获取失败，就自己new一个名字和编号一样的GroupSearchBean
        if(bean == null){
            bean =  DataUtil.newGroupSearchBeanByNo(groupNo);
        }
        Log.e("SearchUtil", "getCurrentGroupInfo-newGroupSearchBeanByNo:" + bean);
        return bean;
    }

    public static boolean isNeedGetGroupInfo(GroupSearchBean bean){
        boolean result = false;
        if(bean == null){
            result = true;
            return result;
        }
        if(TextUtils.equals(String.valueOf(bean.no),bean.name)){
            result = true;
        }
        return result;
    }

    /**
     * 获取本地数据库 根据组编号获取组信息
     *
     * @return
     */
    public static Observable<GroupSearchBean> getGroupByGroupNo(int groupNo) {
        return Observable.fromCallable(() -> {
            GroupSearchBean search = TerminalFactory.getSDK().getSQLiteDBManager().getGroupByNo(groupNo);
            return search;
        }).subscribeOn(Schedulers.io());
    }
    /**
     * 获取本地数据库 组数据
     *
     * @return
     */
    public static Observable<List<GroupSearchBean>> getDbAllGroup() {
        return Observable.fromCallable(() -> {
            List<GroupSearchBean> search = TerminalFactory.getSDK().getSQLiteDBManager().getAllGroup(new ArrayList<>(),0);
            Log.e("SearchUtil", "getDbAllGroup-组数据.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取本地数据库 组数据
     *
     * @return
     */
    public static Observable<List<GroupSearchBean>> getAllGroupFirst() {
        return Observable.fromCallable(() -> {
            List<GroupSearchBean> search = TerminalFactory.getSDK().getSQLiteDBManager().getAllGroupFirst();
            Log.e("SearchUtil", "getAllGroupFirst-组数据.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }


    /**
     * 组 拼音搜索Observable
     *
     * @param keyword
     * @param groupDatas
     * @return
     */
    public static Observable<List<GroupSearchBean>> searchObservable(String keyword, List<GroupSearchBean> groupDatas) {

        return Observable.fromCallable(() -> {
            List<GroupSearchBean> search = SearchUtil.searchGroup(keyword, groupDatas);
            Log.e("SearchUtil", "搜索组数据search.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 人员 拼音搜索Observable
     *
     * @param keyword
     * @return
     */
    public static List<MemberSearchBean> searchMemberByKey(String keyword) {
        List<MemberSearchBean> accounts = MyTerminalFactory.getSDK().getSearchDataManager().getAccountSreachDatas();
        List<MemberSearchBean> search = SearchUtil.searchMember(keyword, accounts);
        List<MemberSearchBean> result = DataUtil.filterNoMemberFromAccount(search);
        Log.e("SearchUtil", "搜索成员数据search.size:" + search.size()+"-filter:"+result.size());
        return result;
    }

    /**
     * 组 拼音搜索Observable
     *
     * @param keyword
     * @return
     */
    public static List<GroupSearchBean> searchGroupByKey(String keyword) {
        List<GroupSearchBean> groups = MyTerminalFactory.getSDK().getSearchDataManager().getGroupSreachDatas();
        List<GroupSearchBean> search = SearchUtil.searchGroup(keyword, groups);
        Log.e("SearchUtil", "搜索组数据search.size:" + search.size());
        return search;
    }

    /**
     * 组 拼音搜索
     *
     * @param keyword
     * @param groupSearchs
     * @return
     */
    public static List<GroupSearchBean> searchGroup(String keyword, List<GroupSearchBean> groupSearchs) {
        List<GroupSearchBean> mQwertySearchGroups = new ArrayList<>();

        if (TextUtils.isEmpty(keyword)) {
            return mQwertySearchGroups;
        }
        for (GroupSearchBean groupSearchBean : groupSearchs) {
            PinyinSearchUnit labelPinyinSearchUnit = groupSearchBean.getLabelPinyinSearchUnit();
            boolean match = T9Util.match(labelPinyinSearchUnit, keyword);
            if (true == match) {// search by LabelPinyinUnits;
                GroupSearchBean group = groupSearchBean;
                group.setSearchByType(GroupSearchBean.SearchByType.SearchByLabel);
                group.setMatchKeywords(labelPinyinSearchUnit.getMatchKeyword().toString());
                group.setMatchStartIndex(group.getName().indexOf(group.getMatchKeywords().toString()));
                group.setMatchLength(group.getMatchKeywords().length());
                mQwertySearchGroups.add(group);
                continue;
            }
        }
        return mQwertySearchGroups;
    }

    /**
     * 将一个list均分成n个list,主要通过偏移量来实现的
     *
     * @param source
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remaider = source.size() % n; //(先计算出余数)
        int number = source.size() / n; //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 获取常用联系人 前5位
     *
     * @return
     */
    public static Observable<List<MemberSearchBean>> getTop5ContactsAccount() {
        return Observable.fromCallable(() -> {
            List<MemberSearchBean> search = TerminalFactory.getSDK().getSQLiteDBManager().getTop5ContactsAccount();
            Log.e("SearchUtil", "获取常用联系人.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 常用联系人 应用打 Tag
     *
     * @param accountNo
     */
    public static void setUpdateUseTimeTag(int accountNo) {
        TerminalFactory.getSDK().getSQLiteDBManager().updateAccountUseTime(accountNo);
        //发送 常用联系人更新通知
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdateTopContactsHandler.class);
    }

}
