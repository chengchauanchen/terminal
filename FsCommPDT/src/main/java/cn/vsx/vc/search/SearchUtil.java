package cn.vsx.vc.search;

import android.text.TextUtils;
import android.util.Log;

import com.pinyinsearch.model.PinyinSearchUnit;
import com.pinyinsearch.util.T9Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
                return TerminalFactory.getSDK().getConfigManager().getGroupsAllSync(false);
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
                return TerminalFactory.getSDK().getConfigManager().getDeptAllDataSync(false);
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
            Log.e("SearchUtil", "成员数据.size:" + search.size());
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
            Log.e("SearchUtil", "成员数据.size:" + search.size());
            return search;
        }).subscribeOn(Schedulers.io());
    }


    /*----------------组-----------------------*/

    /**
     * 获取本地数据库 组数据
     *
     * @return
     */
    public static Observable<List<GroupSearchBean>> getDbAllGroup() {
        return Observable.fromCallable(() -> {
            List<GroupSearchBean> search = TerminalFactory.getSDK().getSQLiteDBManager().getAllGroup(new ArrayList<>(),0);
            Log.e("SearchUtil", "组数据.size:" + search.size());
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
            Log.e("SearchUtil", "组数据.size:" + search.size());
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

}
