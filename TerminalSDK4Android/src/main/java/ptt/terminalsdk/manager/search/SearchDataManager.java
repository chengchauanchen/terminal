package ptt.terminalsdk.manager.search;

import android.text.TextUtils;

import com.allen.library.observer.CommonObserver;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllAccountHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverSearchGroupDataCompleteHandler;

/**
 * @author martian on 2020/4/7.
 */
public class SearchDataManager {

  private Logger logger = Logger.getLogger(getClass());
  public static final String TAG = "search-";
  private List<GroupSearchBean> groupDatas = new ArrayList<>();
  private List<MemberSearchBean> memberDatas = new ArrayList<>();

  public void start(){
    TerminalFactory.getSDK().registReceiveHandler(receiveGetAllGroupHandler);
    TerminalFactory.getSDK().registReceiveHandler(receiveGetAllAccountHandler);
    TerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
    TerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
  }

  public void stop(){
    TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllGroupHandler);
    TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllAccountHandler);
    TerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
    TerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
  }

  //所有组
  private ReceiveGetAllGroupHandler receiveGetAllGroupHandler = new ReceiveGetAllGroupHandler() {
    @Override
    public void handler(List<Group> groups) {
      logger.info("SearchTabFragment获取组数据:" + groups);
      getDbAllGroup(true);
    }
  };

  //所有人
  ReceiveGetAllAccountHandler receiveGetAllAccountHandler = new ReceiveGetAllAccountHandler() {
    @Override
    public void handler(List<Account> accounts) {
      logger.info("SearchTabFragment获取人数据:" + accounts);
      getDbAllAccount();
    }
  };

    /**
     * 获取到临时组数据，更新本地的数据库
     */
    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = new ReceivegUpdateGroupHandler(){
        @Override
        public void handler(int depId, String depName, List<Department> departments, List<Group> groups){
            //更新临时组的数据,比对数据库中的数据，清除或者添加临时组数据
            if (depId == -1 && groups!=null) {
                TerminalFactory.getSDK().getThreadPool().execute(() -> {
                    List<GroupSearchBean> tempList=new ArrayList<GroupSearchBean>(groupDatas);
                    if(tempList.isEmpty()){
                        tempList.addAll(TerminalFactory.getSDK().getSQLiteDBManager().getAllGroup(new ArrayList<>(),0));
                    }
                    //删除已经解散的（groups中没有的临时组）
                    Iterator<GroupSearchBean> iterator = tempList.iterator();
                    while (iterator.hasNext()){
                        GroupSearchBean bean = iterator.next();
                        if(bean!=null && TextUtils.equals(GroupType.TEMPORARY.toString(),bean.getGroupType())
                                && !groups.contains(bean)){
                            iterator.remove();
                        }
                    }
                    //添加有的（本地没有的临时组，可能是离线状态下，拉进的临时组）
                    for (Group bean: groups) {
                       if(bean!=null && !tempList.contains(new GroupSearchBean(bean.getNo()))){
                           GroupSearchBean groupSearchBean = DataUtil.groupToGroupSearchBean(bean);
                           if(groupSearchBean!=null){
                               tempList.add(groupSearchBean);
                           }
                       }
                    }
                    //更新数据库，更新内存，更新UI
                    TerminalFactory.getSDK().getSQLiteDBManager().updateAllGroup(new ArrayList<Group>(tempList),true);
                    groupDatas.clear();
                    groupDatas.addAll(tempList);
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiverSearchGroupDataCompleteHandler.class);
                });
            }
        }
    };

    /**
     * 网络状态
     */
    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
        @Override
        public void handler(boolean connected){
            if(connected){
                //用于更新临时组数据
                MyTerminalFactory.getSDK().getConfigManager().updateGroup(-1, "临时组");
            }
        }
    };

  /**
   * 获取本地数据库 组数据
   */
  public synchronized void getDbAllGroup(boolean isUpdateTempGroup) {
    SearchUtil.getDbAllGroup()
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(new CommonObserver<List<GroupSearchBean>>() {
          @Override
          protected String setTag() {
            return "";
          }

          @Override
          protected void onError(String errorMsg) {
            logger.error(TAG+"getDbAllGroup----请求报错:" + errorMsg);
          }

          @Override
          protected void onSuccess(List<GroupSearchBean> allRowSize) {
//              LogUtil.printLongContentDebug(TAG+"allGroup:",allRowSize.toString());
//            logger.info(TAG+"getDbAllGroup----onSuccess:" +allRowSize);
              groupDatas.clear();
              groupDatas.addAll(allRowSize);
              TerminalFactory.getSDK().notifyReceiveHandler(ReceiverSearchGroupDataCompleteHandler.class);
              if(isUpdateTempGroup){
                  TerminalFactory.getSDK().getThreadPool().execute(() -> {
                      //用于更新临时组数据
                      MyTerminalFactory.getSDK().getConfigManager().updateGroup(-1, "临时组");
                  });
              }
          }
        });
  }


  /**
   * 获取本地数据库 人数据
   */
  public synchronized  void getDbAllAccount() {
    SearchUtil.getDbAllAccount()
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(new CommonObserver<List<MemberSearchBean>>() {
          @Override
          protected String setTag() {
            return "";
          }

          @Override
          protected void onError(String errorMsg) {
            logger.error(TAG+"getDbAllAccount----请求报错:" + errorMsg);
          }

          @Override
          protected void onSuccess(List<MemberSearchBean> allRowSize) {
//          logger.info(TAG+"getDbAllAccount----onSuccess:"+allRowSize);
//            LogUtil.printLongContentDebug(TAG+"allAccount:",allRowSize.toString());
            memberDatas.clear();
            memberDatas.addAll(allRowSize);
          }
        });
  }

  public List<GroupSearchBean> getGroupSreachDatas(){
    return  groupDatas;
  }

    /**
     * 添加组到集合中
     * @param bean
     */
    public void addGroupSreachDatas(GroupSearchBean bean){
      if(bean!=null&& !groupDatas.contains(bean)){
          groupDatas.add(bean);
          //更新UI
          TerminalFactory.getSDK().notifyReceiveHandler(ReceiverSearchGroupDataCompleteHandler.class);
      }
    }

  public List<MemberSearchBean>  getAccountSreachDatas(){
    return  memberDatas;
  }

  public GroupSearchBean getSearchGroupByNo(int groupNo){
        GroupSearchBean result = null;
        for(GroupSearchBean groupSearchBean : groupDatas){
            if(groupSearchBean.getNo() == groupNo){
                result = groupSearchBean;
                break;
            }
        }
        if (result == null) {
            result = new GroupSearchBean();
            result.id = groupNo;
            result.no = groupNo;
            result.name = groupNo+"";
            result.setGroupType(GroupType.BROADBAND.toString());
            result.setResponseGroupType(ResponseGroupType.RESPONSE_FALSE.toString());
        }
        return result;
    }
}
