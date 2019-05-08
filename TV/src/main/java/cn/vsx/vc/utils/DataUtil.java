package cn.vsx.vc.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Folder;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.RotationImageType;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.model.RorationLiveData;
import cn.vsx.vc.model.RotationLiveBean;
import ptt.terminalsdk.context.MyTerminalFactory;

public class DataUtil {

    @SuppressWarnings("unused")
    private Logger logger = Logger.getLogger(DataUtil.class);

    /**通过组id，获取到文件夹对象，得到文件夹id和name*/
//	public static Folder getFolderByGroupId(int groupId) {
//		List<Folder> allFolders = MyTerminalFactory.getSDK().getConfigManager().getAllFolders();
//
////		logger.info("DataUtil-----allFolders"+allFolders);
//
//		Folder folder = null;
//		if (allFolders.size() > 0) {
//			for (int i = 0; i < allFolders.size(); i++) {
//
//				if (allFolders.get(i).groups.size() > 0) {
//					for (int j = 0; j < allFolders.get(i).groups.size(); j++) {
//
//						int mgroupId = allFolders.get(i).groups.get(j).id;
//						if (mgroupId == groupId) {
//							folder = new Folder();
//							folder.id = allFolders.get(i).id;
//							folder.name = allFolders.get(i).name;
//							folder.groups = allFolders.get(i).groups;
//							break;
//						}
//
//					}
//					if (folder != null) {
//						break;
//					}
//				}
//
//			}
//		}
//
//		if (folder == null) {
//			folder = new Folder();
//			folder.id = MyTerminalFactory.getSDK().getParam(Params.CURRENT_FOLDER_ID, 0);
//			folder.name = MyTerminalFactory.getSDK().getParam(Params.CURRENT_FOLDER_ID, 0)+"";
//		}
//
//		return folder;
//	}

    /**
     * 获得除了自己的所有的成员集合
     *
     * @param allMembers
     */
    public static List<Member> getAllMembersExceptMe(List<Member> allMembers) {
        List<Member> allMembersExceptMe = new ArrayList<>();
        Member member = null;
        int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

        if (allMembers != null && allMembers.size() >= 1) {

            for (int i = 0; i < allMembers.size(); i++) {
                if (allMembers.get(i).id != memberId) {
                    member = new Member();
                    member.id = allMembers.get(i).id;
                    member.setName(allMembers.get(i).getName());
                    allMembersExceptMe.add(member);
                }
            }

        }

        return allMembersExceptMe;
    }

    /**
     * 获得除了自己的当前组的在线成员集合
     *
     * @param currentGroupMembers
     */
    public static List<Member> getCurrentGroupMembersExceptMe(List<Member> currentGroupMembers) {
        List<Member> currentGroupMembersExceptMe = new ArrayList<>();
        Member member = null;
        int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

        if (currentGroupMembers != null && currentGroupMembers.size() > 1) {

            for (int i = 0; i < currentGroupMembers.size(); i++) {
                if (currentGroupMembers.get(i).id != memberId) {
                    member = new Member();
                    member.id = currentGroupMembers.get(i).id;
                    member.setName(currentGroupMembers.get(i).getName());
                    currentGroupMembersExceptMe.add(member);
                }
            }

        }

        return currentGroupMembersExceptMe;
    }

    /**
     * 获得带有常用组的文件夹和组的集合
     *
     * @param allFolders
     */
    public static List<Folder> getAllFoldersWithCommonGroup(List<Folder> allFolders) {
        List<Folder> allFoldersWithCommonGroup = new ArrayList<>();

        Folder folder = new Folder();
        folder.id = 0;
        folder.name = "常用组";
        folder.groups = new ArrayList<>();
        for (int i = 0; i < CommonGroupUtil.getCatchGroupIds().size(); i++) {
            Group group = new Group();
            group.id = CommonGroupUtil.getCatchGroupIds().get(i);
            group.name = "a";
            folder.groups.add(group);
        }
        allFoldersWithCommonGroup.add(0, folder);

        allFoldersWithCommonGroup.addAll(allFolders);

        List<Integer> intList = new ArrayList<>();
        for (int i = 0; i < allFolders.size(); i++) {
            List<Group> groupList = allFolders.get(i).groups;
            for (int j = 0; j < groupList.size(); j++) {
                intList.add(groupList.get(j).id);
            }
        }
        List<Group> groupList = allFoldersWithCommonGroup.get(0).groups;
        for (int i = 0; i < groupList.size(); i++) {
            Group group = groupList.get(i);
            if (!intList.contains(group.id)) {
                CommonGroupUtil.removeCatchGroupIdList(groupList.get(i).id);
            }
        }
//		logger.info( "DataUtil-----allFoldersWithCommonGroup"+allFoldersWithCommonGroup.toString());
        return allFoldersWithCommonGroup;
    }

    /**
     * 邀请码是否合法
     */
    public static boolean isLegalOrg(CharSequence mobiles) {
        Pattern p = Pattern.compile("^[0-9]+$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 姓名是否合法
     */
    public static boolean isLegalName(String mobiles) {
        Pattern p = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5a-zA-Z0-9]+$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 搜索的联系人是否合法，一到多位汉字、大小写字母、数字
     */
    public static boolean isLegalSearch(CharSequence mobiles) {
        Pattern p = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9]+$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 通过memberId获得member对象
     *
     * @param memberId
     */
    public static Member getMemberByMemberId(int memberId) {
        List<Member> allMembers = MyTerminalFactory.getSDK().getConfigManager().getAllMembers();
        Member member = null;
//		logger.info("datautil-----getMemberByMemberId"+allMembers.toString()+"-----------"+allMembers.size());	
        if (allMembers.size() > 0) {
            for (int i = 0; i < allMembers.size(); i++) {
                int mMemberId = allMembers.get(i).id;
                if (mMemberId == memberId) {
                    member = new Member();
                    member.id = memberId;
                    member.setName(allMembers.get(i).getName());
                    break;
                }
            }
        }

        if (member == null) {
            member = new Member();
            member.id = memberId;
            member.setName(memberId + "");
        }

        return member;
    }

    /**
     * 通过memberId获得member对象
     *
     * @param memberId
     */
    public static Member getMemberByMemberIdCurr(int memberId) {
        List<Member> currentGroupMembers = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers();
        Member member = null;
        try {
            if (currentGroupMembers.size() > 0) {
                for (int i = 0; i < currentGroupMembers.size(); i++) {

                    int mMemberId = currentGroupMembers.get(i).id;
                    if (mMemberId == memberId) {
                        member = new Member();
                        member.id = memberId;
                        member.setName(currentGroupMembers.get(i).getName());
                        break;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (member == null) {
            member = new Member();
            member.id = memberId;
            member.setName(memberId + "");
        }

        return member;
    }


    public static String getAPM() {
        String mAP = "上下午";
        int j = Calendar.getInstance().get(Calendar.AM_PM);
        if (j == 0) {
            mAP = "上午";
        } else if (j == 1) {
            mAP = "下午";
        }
        return mAP;
    }

    public static String getWeek() {
        int i = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        String mWeek = "星期";
        if (i == 1) {
            mWeek = "日";
        } else if (i == 2) {
            mWeek = "一";
        } else if (i == 3) {
            mWeek = "二";
        } else if (i == 4) {
            mWeek = "三";
        } else if (i == 5) {
            mWeek = "四";
        } else if (i == 6) {
            mWeek = "五";
        } else if (i == 7) {
            mWeek = "六";
        }
        return mWeek;
    }

    /**
     * 个呼通讯录中是否存在此成员
     */
    public static boolean isExistContacts(Member member) {
        List<Member> allMembers = MyTerminalFactory.getSDK().getConfigManager().getAllMembers();//获取到个呼通讯录
        for (Member member2 : allMembers) {
            if (member2.id == member.id) {
                return true;
            }
        }
        return false;
    }

    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageManager pManager = context.getPackageManager();
            PackageInfo info = pManager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * @param memberNo 人员编号
     * @return 根据编号查找人员
     */
    public static Member getMemberByMemberNo(int memberNo) {
        List<Member> allMembers = TerminalFactory.getSDK().getConfigManager().getAllMembers();
        Member member = null;
        if (allMembers.size() > 0) {
            for (int i = 0; i < allMembers.size(); i++) {
                int mMemberNo = allMembers.get(i).getNo();
                if (mMemberNo == memberNo) {
                    member = allMembers.get(i);
                    break;
                }
            }
        }

        if (member == null) {
            member = new Member();
            member.id = memberNo;
            member.no = memberNo;
            member.setName(handleName(memberNo + ""));
        }

        return member;
    }

    public static String handleName(String memberName) {
        String account = memberName;
        if (!Util.isEmpty(memberName) && memberName.length() > 2 && "88".equals(memberName.substring(0, 2))) {
            account = memberName.substring(2);
        } else {
            account = memberName;
        }
        return account;
    }

    /**
     * 解析轮播对象
     * @param data
     * @return
     */
    public static RotationLiveBean getRotationLiveBean(String data) {
        RotationLiveBean bean = null;
        try{
            if(!TextUtils.isEmpty(data)){
                bean = new RotationLiveBean();
                //类型
                String[] string  = data.split(":");
                int type = stringToInt(string[0]);
                bean.setType(type);
                //信息
                String json = data.substring(data.indexOf(":")+1);
                Log.d("---", "getRotationLiveBean: "+json);
                if(!TextUtils.isEmpty(json)){
                    RorationLiveData live = new RorationLiveData();
                    if (type == RotationImageType.RTSP.getCode()) {
                        String[] d = json.split("_");
                        if(d.length>0){
                            live.setLiveNo(stringToInt(d[0]));
                        }
                        if(d.length>1){
                            live.setLiveUniqueNo(stringToLong(d[1]));
                        }
                        if(d.length>2){
                            live.setCallId(d[2]);
                        }
                    }else if(type == RotationImageType.OuterGB28121.getCode()){
                        live.setDeviceId(json);
                    }else if(type == RotationImageType.GB28121.getCode()){
                        live.setDeviceId(json);
                    }
                    bean.setData(live);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            bean = null;
        }
        return bean;
    }

    /**
     * 根据设备类型获取设备类型
     *
     * @param type
     * @return
     */
    public static TerminalMemberType getTerminalMemberTypeByType(int type) {
        if (type == TerminalMemberType.TERMINAL_PC.getCode()) {
            return TerminalMemberType.TERMINAL_PC;
        } else if (type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()) {
            return TerminalMemberType.TERMINAL_BODY_WORN_CAMERA;
        } else if (type == TerminalMemberType.TERMINAL_UAV.getCode()) {
            return TerminalMemberType.TERMINAL_UAV;
        } else if (type == TerminalMemberType.TERMINAL_HDMI.getCode()) {
            return TerminalMemberType.TERMINAL_HDMI;
        } else if (type == TerminalMemberType.TERMINAL_LTE.getCode()) {
            return TerminalMemberType.TERMINAL_LTE;
        } else {
            return null;
        }
    }

    public static int stringToInt(String data){
        int result = 0;
        if(!TextUtils.isEmpty(data)){
            try{
                result = Integer.valueOf(data);
            }catch (Exception e){
                e.printStackTrace();
                result = 0;
            }
        }
        return result;
    }

    public static long stringToLong(String data){
        long result = 0;
        if(!TextUtils.isEmpty(data)){
            try{
                result = Long.valueOf(data);
            }catch (Exception e){
                e.printStackTrace();
                result = 0;
            }
        }
        return result;
    }
}
