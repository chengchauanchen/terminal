package ptt.terminalsdk.manager;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.allen.library.manage.RxUrlManager;
import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.RequestDataType;
import cn.vsx.hamster.common.TerminalMemberStatusEnum;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.data.DataManager;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSearchMemberResultHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.BuildConfig;
import ptt.terminalsdk.bean.DepData;
import ptt.terminalsdk.bean.GroupBean;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.http.AppUrlConfig;
import ptt.terminalsdk.receiveHandler.ReceiveGetTerminalDeviceHandler;
import ptt.terminalsdk.receiveHandler.ReceiveUpdateDepGroupHandler;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/5
 * 描述：
 * 修订历史：
 */
public class MyDataManager extends DataManager{

    private Logger logger = Logger.getLogger(getClass());
    private static List<GroupBean> depAllGroup = new ArrayList<>();
    private boolean uavVoiceOpen = true;

    @Override
    protected void setUrl(JSONObject resultJsonObject){
        super.setUrl(resultJsonObject);
        // TODO: 2019/8/5 海康摄像头地址暂时写在build.gradle 里
        TerminalFactory.getSDK().putParam(Params.HIKVISION_CAMERADATA_URL, BuildConfig.HIKVISIONCAMERADATAURL);
        //认证成功之后配置BaseUrl
        AppUrlConfig.setFileServerUrl("http://" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP) + ":" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0));
        RxUrlManager.getInstance().setMultipleUrl(AppUrlConfig.getAllUrl());
    }

    /**
     * 更新当前组部门下的所有组
     */
    @Override
    public void updateDepAllGroup(){
        if(notNeedUpdateDepAllGroup()){
            return;
        }
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    //查询所在部门的组
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("type", RequestDataType.DEPARTMENTS_GROUPS_DATA.toString());
                    paramsMap.put("memberId", String.valueOf(TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L)));
                    paramsMap.put("id", String.valueOf(TerminalFactory.getSDK().getParam(Params.CURRENT_DEP_ID,0L)));

                    String serverIp = TerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                    int serverPort = TerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                    String url = "http://" + serverIp + ":" + serverPort + "/file/getDeptData";

                    String result =TerminalFactory.getSDK().getOkHttpManager().updateDepAllDataByOkhttp(false,url,paramsMap);
                    logger.info("请求当前组部门下的所有组:"+result);
                    if(!Util.isEmpty(result)){
                        DepData resultBean = new Gson().fromJson(result, DepData.class);
                        if(resultBean != null && resultBean.getMemberGroups() !=null){
                            List<GroupBean> groupList = resultBean.getMemberGroups().getGroupList();
                            depAllGroup.clear();
                            depAllGroup.addAll(groupList);
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateDepGroupHandler.class,groupList);
                        }else {
//                            logger.info("请求当前组部门下的所有组数据为null");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
//        ApiManager.getFileServerApi(60*60)
//                .getDeptData(paramsMap)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new CommonObserver<String>(){
//                    @Override
//                    protected void onError(String errorMsg){
//                        logger.info("请求当前组部门下的所有组失败---"+errorMsg);
//                    }
//
//                    @Override
//                    protected void onSuccess(String depData){
//                        logger.info("请求当前组部门下的所有组:"+depData);
//                        if(depData != null && depData.getMemberGroups() !=null){
//                            List<GroupBean> groupList = depData.getMemberGroups().getGroupList();
//                            depAllGroup.clear();
//                            depAllGroup.addAll(groupList);
//                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateDepGroupHandler.class,groupList);
//                        }else {
////                            logger.info("请求当前组部门下的所有组数据为null");
//                        }
//                    }
//                });

    }

    @Override
    public void getDeptDeviceList(int depId, List<String> type){
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    JSONObject requestJsonObject = new JSONObject();
                    requestJsonObject.put("id",String.valueOf(depId));
                    requestJsonObject.put("uniqueNo",String.valueOf(TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L)));
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.addAll(type);
                    requestJsonObject.put("type",jsonArray);
                    String tag = "/file/getDeptList";
                    String url = "http://" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP) + ":" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0) + tag;
                    String result =TerminalFactory.getSDK().getOkHttpManager().updateDepListByOkhttp(false,url,requestJsonObject.toJSONString(),tag);
                    logger.info("请求当前组部门下的设备列表:"+result);
                    if(!Util.isEmpty(result)){
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        if(jsonObject != null){
                            TerminalFactory.getSDK().getOkHttpManager().savePostByKey(tag,requestJsonObject.toJSONString(),result);
                            List<Department> departments = new ArrayList<>();
                            JSONArray deptList = jsonObject.getJSONArray("deptList");
                            for(int i = 0; i < deptList.size(); i++){
                                JSONObject dep = deptList.getJSONObject(i);
                                Department department = JSON.parseObject(dep.toJSONString(), Department.class);
                                departments.add(department);
                            }
                            List<Member> members = new ArrayList<>();
                            JSONArray terminals = jsonObject.getJSONArray("terminals");
                            for(int i = 0; i < terminals.size(); i++){
                                JSONObject terminal = terminals.getJSONObject(i);
                                Member member = new Member();
                                member.setId(terminal.getIntValue("id"));
                                JSONObject account = terminal.getJSONObject("account");
                                member.setNo(account.getIntValue("no"));
                                member.setName(account.getString("name"));
                                member.setPhone(account.getString("phoneNumber"));
                                member.setBind(account.containsKey("bind") && account.getBoolean("bind"));
                                JSONObject department = account.getJSONObject("department");
                                member.setDeptId(department.getIntValue("id"));
                                member.setDepartmentName(department.getString("name"));
                                member.setUniqueNo(terminal.getLongValue("uniqueNo"));
                                member.setType((TerminalMemberType.valueOf(terminal.getString("terminalMemberType")).getCode()));
                                member.setTerminalMemberType(terminal.getString("terminalMemberType"));
                                member.setStatus(TerminalMemberStatusEnum.valueOf(terminal.getString("terminalMemberStatus")).getCode());
                                member.setGb28181No(terminal.getString("gb28181No"));
                                //                            member.setTerminalMemberMode(terminal.getString("terminalMemberMode"));
                                members.add(member);
                            }
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetTerminalDeviceHandler.class, depId, type, departments, members);
                        }
                    }

//                    ApiManager.getFileServerApi(60*60)
//                            .getDeptList(requestJsonObject.toJSONString())
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new CommonObserver<String>(){
//                                @Override
//                                protected void onError(String errorMsg){
//                                    logger.error("请求设备列表失败--"+errorMsg);
//                                }
//
//                                @Override
//                                protected void onSuccess(String result){
//                                    JSONObject jsonObject = JSONObject.parseObject(result);
//                                    if(jsonObject != null){
//                                        List<Department> departments = new ArrayList<>();
//                                        JSONArray deptList = jsonObject.getJSONArray("deptList");
//                                        for(int i = 0; i < deptList.size(); i++){
//                                            JSONObject dep = deptList.getJSONObject(i);
//                                            Department department = JSON.parseObject(dep.toJSONString(), Department.class);
//                                            departments.add(department);
//                                        }
//                                        List<Member> members = new ArrayList<>();
//                                        JSONArray terminals = jsonObject.getJSONArray("terminals");
//                                        for(int i = 0; i < terminals.size(); i++){
//                                            JSONObject terminal = terminals.getJSONObject(i);
//                                            Member member = new Member();
//                                            member.setId(terminal.getIntValue("id"));
//                                            JSONObject account = terminal.getJSONObject("account");
//                                            member.setNo(account.getIntValue("no"));
//                                            member.setName(account.getString("name"));
//                                            member.setPhone(account.getString("phoneNumber"));
//                                            member.setBind(account.containsKey("bind") && account.getBoolean("bind"));
//                                            JSONObject department = account.getJSONObject("department");
//                                            member.setDeptId(department.getIntValue("id"));
//                                            member.setDepartmentName(department.getString("name"));
//                                            member.setUniqueNo(terminal.getLongValue("uniqueNo"));
//                                            member.setType((TerminalMemberType.valueOf(terminal.getString("terminalMemberType")).getCode()));
//                                            member.setTerminalMemberType(terminal.getString("terminalMemberType"));
//                                            member.setStatus(TerminalMemberStatusEnum.valueOf(terminal.getString("terminalMemberStatus")).getCode());
//                                            member.setGb28181No(terminal.getString("gb28181No"));
//                                            //                            member.setTerminalMemberMode(terminal.getString("terminalMemberMode"));
//                                            members.add(member);
//                                        }
//                                        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetTerminalDeviceHandler.class, depId, type, departments, members);
//                                    }
//                                }
//                            });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });



    }

    @Override
    public void findByDeptAndKeyList(int page,int pageSize,List<String> type, final String keywords){
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    JSONObject jsonRequestObject = new JSONObject();
                    jsonRequestObject.put("deptId",String.valueOf(TerminalFactory.getSDK().getParam(Params.CURRENT_DEP_ID,0L)));
                    JSONArray jsonRequestArray = new JSONArray();
                    jsonRequestArray.addAll(type);
                    jsonRequestObject.put("type",jsonRequestArray);
                    jsonRequestObject.put("key",keywords);
                    jsonRequestObject.put("page",page);
                    jsonRequestObject.put("addChild",true);
                    jsonRequestObject.put("pageSize",pageSize);
                    String tag = "/file/terminal/findByDeptAndKeyList";
                    String url = "http://" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP) + ":" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0) + tag;
                    String result =TerminalFactory.getSDK().getOkHttpManager().updateDepListByKeyByOkhttp(false,url,jsonRequestObject.toJSONString(),tag);
                    logger.info("请求当前组部门下的设备查询列表:"+result);
                    if(!Util.isEmpty(result)){
                        TerminalFactory.getSDK().getOkHttpManager().savePostByKey(tag,jsonRequestObject.toJSONString(),result);
                        JSONObject jsonResult = JSONObject.parseObject(result);
                        if(jsonResult!=null){
                            int number = jsonResult.getIntValue("number");
                            int totalPages = jsonResult.getIntValue("totalPages");
                            int size = jsonResult.getIntValue("totalElements");
                            JSONArray jsonArray = jsonResult.getJSONArray("content");
                            List<Member> members = new ArrayList<>();
                            for(int i = 0; i < jsonArray.size(); i++){
                                Member member = new Member();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                member.setId(Integer.valueOf(jsonObject.getString("id")));
                                member.setUniqueNo(Long.parseLong(jsonObject.getString("uniqueNo")));
                                member.setType(TerminalMemberType.valueOf(jsonObject.getString("terminalMemberType")).getCode());
                                member.setStatus(TerminalMemberStatusEnum.valueOf(jsonObject.getString("terminalMemberStatus")).getCode());
                                JSONObject account = jsonObject.getJSONObject("account");
                                member.setName(account.getString("name"));
                                member.setPhone(account.getString("phoneNumber"));
                                member.setNo(account.getIntValue("no"));
                                member.setBind(account.containsKey("bind")&&account.getBoolean("bind"));
                                JSONObject department = account.getJSONObject("department");
                                member.setDeptId(Integer.valueOf(department.getString("id")));
                                member.setDepartmentName(department.getString("name"));
                                member.setGb28181No(jsonObject.getString("gb28181No"));
                                member.setTerminalMemberType(jsonObject.getString("terminalMemberType"));
                                members.add(member);
                            }
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSearchMemberResultHandler.class,number,totalPages,size,members);
                        }
                    }

//                    ApiManager.getFileServerApi(60*60)
//                            .findByDeptAndKeyList(jsonRequestObject.toJSONString())
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new CommonObserver<String>(){
//                                @Override
//                                protected void onError(String errorMsg){
//                                    logger.error("搜索失败---"+errorMsg);
//                                }
//
//                                @Override
//                                protected void onSuccess(String result){
//                                    JSONObject jsonResult = JSONObject.parseObject(result);
//                                    int number = jsonResult.getIntValue("number");
//                                    int totalPages = jsonResult.getIntValue("totalPages");
//                                    int size = jsonResult.getIntValue("totalElements");
//                                    JSONArray jsonArray = jsonResult.getJSONArray("content");
//                                    List<Member> members = new ArrayList<>();
//                                    for(int i = 0; i < jsonArray.size(); i++){
//                                        Member member = new Member();
//                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                        member.setId(Integer.valueOf(jsonObject.getString("id")));
//                                        member.setUniqueNo(Long.parseLong(jsonObject.getString("uniqueNo")));
//                                        member.setType(TerminalMemberType.valueOf(jsonObject.getString("terminalMemberType")).getCode());
//                                        member.setStatus(TerminalMemberStatusEnum.valueOf(jsonObject.getString("terminalMemberStatus")).getCode());
//                                        JSONObject account = jsonObject.getJSONObject("account");
//                                        member.setName(account.getString("name"));
//                                        member.setPhone(account.getString("phoneNumber"));
//                                        member.setNo(account.getIntValue("no"));
//                                        member.setBind(account.containsKey("bind")&&account.getBoolean("bind"));
//                                        JSONObject department = account.getJSONObject("department");
//                                        member.setDeptId(Integer.valueOf(department.getString("id")));
//                                        member.setDepartmentName(department.getString("name"));
//                                        member.setGb28181No(jsonObject.getString("gb28181No"));
//                                        member.setTerminalMemberType(jsonObject.getString("terminalMemberType"));
//                                        members.add(member);
//                                    }
//                                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSearchMemberResultHandler.class,number,totalPages,size,members);
//                                }
//                            });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public static List<GroupBean> getDepAllGroup(){
        return depAllGroup;
    }

    /**
     * 在账号改变的时候，删除相关的数据,比如（sp中会存储之前的当前组信息，改变账号的时候需要删除）
     */
    @Override
    public void clearDataByAccountChanged(){
        logger.info("在账号改变的时候，删除相关的数据:");
        //删除之前账号的当前组
        TerminalFactory.getSDK().putParam(Params.CURRENT_GROUP_ID, 0);
    }

    @Override
    public boolean isUavVoiceOpen(){
        return uavVoiceOpen;
    }

    @Override
    public void setUavVoiceOpen(boolean uavVoiceOpen){
        this.uavVoiceOpen = uavVoiceOpen;
    }

    /**
     * 是否更新部门下所有组信息
     * @return
     */
    private boolean notNeedUpdateDepAllGroup(){
        String deviceType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        return (!TextUtils.isEmpty(deviceType)&&TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString()));
    }
}
