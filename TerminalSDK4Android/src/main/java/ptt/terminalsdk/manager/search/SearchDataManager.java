package ptt.terminalsdk.manager.search;

import com.allen.library.observer.CommonObserver;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllAccountHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllGroupHandler;
import io.reactivex.schedulers.Schedulers;
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
  }

  public void stop(){
    TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllGroupHandler);
    TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllAccountHandler);
  }

  //所有组
  private ReceiveGetAllGroupHandler receiveGetAllGroupHandler = new ReceiveGetAllGroupHandler() {
    @Override
    public void handler(List<Group> groups) {
      logger.info("SearchTabFragment获取组数据:" + groups);
      getDbAllGroup();
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
   * 获取本地数据库 组数据
   */
  public synchronized void getDbAllGroup() {
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

    public void addGroupSreachDatas(GroupSearchBean bean){
      if(bean!=null&& !groupDatas.contains(bean)){
          groupDatas.add(bean);
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
