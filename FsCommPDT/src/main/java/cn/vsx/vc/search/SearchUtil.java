package cn.vsx.vc.search;

import android.text.TextUtils;
import android.util.Log;

import com.pinyinsearch.model.PinyinSearchUnit;
import com.pinyinsearch.util.PinyinUtil;
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
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SearchUtil {

    private void searchGroupZip(String keyword) {
        getDbAllGroup4()
                .map(groupMap -> {
                    List<Observable<List<GroupSearchBean>>> observables = new ArrayList<>();
                    for (String key : groupMap.keySet()) {//keySet获取map集合key的集合  然后在遍历key即可
                        List<GroupSearchBean> value = groupMap.get(key);//
                        observables.add(searchObservable(keyword,value));
                    }
                    return observables;
                });


//        Observable.zipIterable()
    }


    private Observable<Map<String, List<GroupSearchBean>>> getDbAllGroup4() {
        return SearchUtil.getDbAllGroup()
                .map(groupSearchBeans -> {
                    Map<String, List<GroupSearchBean>> stringListMap = splitList(groupSearchBeans, 4);
                    return stringListMap;
                });
    }

    private Observable<Map<String, List<MemberSearchBean>>> getAllAccount4() {
        return SearchUtil.getDbAllAccount()
                .map(memberSearchBeans -> {
                    Map<String, List<MemberSearchBean>> stringListMap = splitList(memberSearchBeans, 4);
                    return stringListMap;
                });
    }


    /*----------------人-----------------------*/


    /**
     * 获取本地数据库 人数据
     *
     * @return
     */
    public static Observable<List<MemberSearchBean>> getDbAllAccount() {
        return Observable.fromCallable(() -> TerminalFactory.getSDK().getSQLiteDBManager().getAllAccount());
    }

    public static Observable<List<GroupSearchBean>> getListenedGroup(){
        return Observable.fromCallable(()-> TerminalFactory.getSDK().getConfigManager().getMonitorSearchGroup());
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
            long sortStartTime = System.currentTimeMillis();
            List<MemberSearchBean> search = SearchUtil.searchMember(keyword, accounts);
            Log.e("SearchUtil", "SearchTabFragment搜索成功search.size:" + search.size());
            long sortEndTime = System.currentTimeMillis();
            Log.e("SearchUtil", "组搜索耗时:" + (sortEndTime - sortStartTime));
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


//    /**
//     * 将原始组数据 转化为 可以查询的bean
//     *
//     * @param accounts
//     * @return
//     */
//    public static List<MemberSearchBean> getMemberSearchData(List<Account> accounts) {
//        List<MemberSearchBean> memberSearchsNew = new ArrayList<>();
//        for (Account account : accounts) {
//            MemberSearchBean memberSearch = getMemberSearch(account);
//            memberSearch.getLabelPinyinSearchUnit().setBaseData(memberSearch.getName()+memberSearch.getNo());
//            PinyinUtil.parse(memberSearch.getLabelPinyinSearchUnit());
//            String sortKey = PinyinUtil.getSortKey(memberSearch.getLabelPinyinSearchUnit()).toUpperCase();
//            memberSearch.setSortKey(praseSortKey(sortKey));
//            memberSearchsNew.add(memberSearch);
//        }
//        return memberSearchsNew;
//    }

//    private static MemberSearchBean getMemberSearch(Account account){
//        if(null==account){
//            return null;
//        }
//        MemberSearchBean memberSearchBean = new MemberSearchBean();
//        memberSearchBean.setPrivateTelephone(account.getPrivateTelephone());
//        memberSearchBean.setChecked(account.isChecked());
//        memberSearchBean.setDepartment(account.getDepartment());
//        memberSearchBean.setDepartmentName(account.getDepartmentName());
//        memberSearchBean.setDeptId(account.getDeptId());
//        memberSearchBean.setId(account.getId());
//        memberSearchBean.setMembers(account.getMembers());
//        memberSearchBean.setName(account.getName());
//        memberSearchBean.setNo(account.getNo());
//        memberSearchBean.setPhone(account.getPhone());
//        memberSearchBean.setPhoneNumber(account.getPhoneNumber());
//        return memberSearchBean;
//    }






    /*----------------组-----------------------*/

    /**
     * 获取本地数据库 组数据
     *
     * @return
     */
    public static Observable<List<GroupSearchBean>> getDbAllGroup() {
        return Observable.fromCallable(() -> TerminalFactory.getSDK().getSQLiteDBManager().getAllGroup());
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
            long sortStartTime = System.currentTimeMillis();
            List<GroupSearchBean> search = SearchUtil.searchGroup(keyword, groupDatas);
            Log.e("SearchUtil", "SearchTabFragment搜索成功search.size:" + search.size());
            long sortEndTime = System.currentTimeMillis();
            Log.e("SearchUtil", "组搜索耗时:" + (sortEndTime - sortStartTime));
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
     * 将原始组数据 转化为 可以查询的bean
     *
     * @param groups
     * @return
     */
    public static List<GroupSearchBean> getGroupSearchData(List<Group> groups) {
        List<GroupSearchBean> groupSearchsNew = new ArrayList<>();
        for (Group group : groups) {
            GroupSearchBean groupSearchBean = getGroupSearch(group);
            groupSearchBean.getLabelPinyinSearchUnit().setBaseData(groupSearchBean.getName());
            PinyinUtil.parse(groupSearchBean.getLabelPinyinSearchUnit());
            String sortKey = PinyinUtil.getSortKey(groupSearchBean.getLabelPinyinSearchUnit()).toUpperCase();
            groupSearchBean.setSortKey(praseSortKey(sortKey));
            groupSearchsNew.add(groupSearchBean);
        }
        return groupSearchsNew;
    }


    private static GroupSearchBean getGroupSearch(Group groups) {
        if (null == groups) {
            return null;
        }
        GroupSearchBean groupSearchBean = new GroupSearchBean();
        groupSearchBean.setUniqueNoStr(groups.getUniqueNoStr());
        groupSearchBean.setUniqueNo(groups.getUniqueNo());
        groupSearchBean.setBusinessDisposeStatus(groups.businessDisposeStatus);
        groupSearchBean.setBusinessId(groups.getBusinessId());
        groupSearchBean.setChecked(groups.isChecked);
        groupSearchBean.setCreatedMemberName(groups.getCreatedMemberName());
        groupSearchBean.setCreatedMemberNo(groups.getCreatedMemberNo());
        groupSearchBean.setCreatedMemberUniqueNo(groups.getCreatedMemberUniqueNo());
        groupSearchBean.setDepartmentName(groups.getDepartmentName());
        groupSearchBean.setDeptId(groups.getDeptId());
        groupSearchBean.setGroupType(groups.getGroupType());
        groupSearchBean.setHighUser(groups.isHighUser());
        groupSearchBean.setId(groups.getId());
        groupSearchBean.setName(groups.getName());
        groupSearchBean.setNo(groups.getNo());
        groupSearchBean.setProcessingState(groups.getProcessingState());
        groupSearchBean.setResponseGroupType(groups.getResponseGroupType());
        groupSearchBean.setTempGroupType(groups.getTempGroupType());
        return groupSearchBean;
    }


    private static String praseSortKey(String sortKey) {
        if (null == sortKey || sortKey.length() <= 0) {
            return null;
        }
        if ((sortKey.charAt(0) >= 'a' && sortKey.charAt(0) <= 'z') || (sortKey.charAt(0) >= 'A' && sortKey.charAt(0) <= 'Z')) {
            return sortKey;
        }
        return String.valueOf(/*QuickAlphabeticBar.DEFAULT_INDEX_CHARACTER*/'#')
                + sortKey;
    }

    /**
     * @param num  分的份数
     * @param list 需要分的集合
     */
    public <T> Map<String, List<T>> splitList(List<T> list, Integer num) {
        int listSize = list.size(); //list 长度
        HashMap<String, List<T>> stringListHashMap = new HashMap<String, List<T>>(); //用户封装返回的多个list
        List<T> stringlist = new ArrayList<T>();
        ;         //用于承装每个等分list
        for (int i = 0; i < listSize; i++) {                        //for循环依次放入每个list中
            stringlist.add(list.get(i));                            //先将string对象放入list,以防止最后一个没有放入
            if (((i + 1) % num == 0) || (i + 1 == listSize)) {               //如果l+1 除以 要分的份数 为整除,或者是最后一份,为结束循环.那就算作一份list,
                stringListHashMap.put("stringList" + i, stringlist); //将这一份放入Map中.
                stringlist = new ArrayList<T>();                //新建一个list,用于继续存储对象
            }
        }
        return stringListHashMap;                                     //将map返回
    }
}
