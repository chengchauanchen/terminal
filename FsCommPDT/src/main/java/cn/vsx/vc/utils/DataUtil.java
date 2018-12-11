package cn.vsx.vc.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

public class DataUtil {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataUtil.class);


	/**通过组id，获取到组的对象，id和name*/
	public static Group getGroupByGroupNo(int groupNo) {
		List<Group> groupList=MyTerminalFactory.getSDK().getConfigManager().getAllGroups();
		Group group = null;
		if (null != groupList){
			for (Group g:groupList){
				if (groupNo==g.getNo()){
					group=g;
					break;
				}
			}
		}
		if (group == null) {
			logger.info("通讯录没有这个组："+groupNo);
			group = new Group();
			group.id = groupNo;
			group.no = groupNo;
			group.name = groupNo+"";
		}

		return group;
	}

	/**获得除了自己的所有的成员集合
	 * @param allMembers */
	public static List<Member> getAllMembersExceptMe(List<Member> allMembers) {
		List<Member> allMembersExceptMe = new ArrayList<>();
		allMembersExceptMe.addAll(allMembers);
		int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
		Iterator<Member> iterator = allMembersExceptMe.iterator();
		while(iterator.hasNext()){
			Member next = iterator.next();
			if(next.getNo() == memberId){
				iterator.remove();
			}

		}

		return allMembersExceptMe;
	}

	/**获得除了自己的当前组的在线成员集合
	 * @param currentGroupMembers */
	public static List<Member> getCurrentGroupMembersExceptMe(List<Member> currentGroupMembers) {
		List<Member> currentGroupMembersExceptMe = new ArrayList<>();
		Member member = null;
		int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

		if (currentGroupMembers!= null && currentGroupMembers.size() > 1) {

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
	/**邀请码是否合法 */
	public static boolean isLegalOrg(CharSequence mobiles) {
		Pattern p = Pattern.compile("^[0-9]+$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	/**姓名是否合法 */
	public static boolean isLegalName(String mobiles) {
		Pattern p = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9][\\u4e00-\\u9fa5a-zA-Z0-9]+$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	/**搜索的联系人是否合法，一到多位汉字、大小写字母、数字*/
	public static boolean isLegalSearch(CharSequence mobiles) {
		Pattern p = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9]+$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/**
	 * @param memberNo 人员编号
	 * @return 根据编号查找人员
	 */
	public static Member getMemberByMemberNo(int memberNo) {
		List<Member> allMembers = MyTerminalFactory.getSDK().getConfigManager().getAllMembers();
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
			member.setName(HandleIdUtil.handleName(memberNo+""));
		}

		return member;
	}

	public static Member getMemberInfoByMemberNo(int memberNo) {
		List<Member> allMembers = MyTerminalFactory.getSDK().getConfigManager().getAllMembers();
		Member member = null;
		if (allMembers.size() > 0) {
			for (int i = 0; i < allMembers.size(); i++) {
				int mMemberId = allMembers.get(i).id;
				if (mMemberId == memberNo) {
					member = new Member();
					member.id = memberNo;
					member.setName(allMembers.get(i).getName());
					member.setTerminalMemberTypeEnum(allMembers.get(i).getTerminalMemberTypeEnum());
					break;
				}
			}
		}

		return member;
	}

	/** 通过memberId获得member对象
	 * @param memberId */
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
			member.setName(memberId+"");
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

	/**个呼通讯录中是否存在此成员*/
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
			PackageManager pManager=context.getPackageManager();
			PackageInfo info = pManager.getPackageInfo(context.getPackageName(), 0);
			version = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 根据uri获取文件的绝对路径
	 * @param uri
	 * @return
	 */
	public static String getFilePath (Uri uri, Context context) {
		String path = "";
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if(cursor.moveToFirst()) {
			path = cursor.getString(cursor.getColumnIndex("_data"));
		}

		return path;
	}

	/**
	 * 获取指定文件大小
	 * @param file
	 * @return
	 * @throws Exception 　　
	 */
	public static long getFileSize(File file) {
		long size = 0;
		try {
			if (file.exists()) {
				FileInputStream fis = null;
				fis = new FileInputStream(file);
				size = fis.available();
			} else {
				file.createNewFile();
				Log.e("获取文件大小", "文件不存在!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	public static boolean isExistGroup(int groupId){
		boolean isExistGroup = false;
		List<Group> groupList=MyTerminalFactory.getSDK().getConfigManager().getAllGroups();
		if (groupList!=null){
			for (Group group:groupList){
				if (group.getNo()==groupId){
					isExistGroup = true;
					break;
				}
			}
		}
		logger.info("isExistGroup:" + isExistGroup+"=====groupId:"+groupId);
		return isExistGroup;
	}
	public static List<Member> getMemberInList(List<Member> memberList, int pageIndex, int number){
		List<Member> pageMember = new ArrayList<>();
		for (int i = pageIndex * number; i < memberList.size(); i++){
			pageMember.add(memberList.get(i));
		}
		return pageMember;
	}

	/**
	 * 功能：<br/>
	 *
	 * @author Tony
	 * @version 2016年12月16日 下午4:41:51 <br/>
	 */
	public static String getTodayShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	Calendar ca=Calendar.getInstance();

	String year = ca.get(Calendar.YEAR) + "";
	String month = (ca.get(Calendar.MONTH) + 1) < 10 ? "0"
			+ (ca.get(Calendar.MONTH) + 1) : ca.get(Calendar.MONTH) + 1 + "";
	String date = (ca.get(Calendar.DATE)) < 10 ? "0" + ca.get(Calendar.DATE)
			: ca.get(Calendar.DATE) + "";
	String hour = (ca.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
			+ ca.get(Calendar.HOUR_OF_DAY) : ca.get(Calendar.HOUR_OF_DAY) + "");
	String minute = (ca.get(Calendar.MINUTE) < 10 ? "0"
			+ ca.get(Calendar.MINUTE) : ca.get(Calendar.MINUTE) + "");
	String second = (ca.get(Calendar.SECOND) < 10 ? "0"
			+ ca.get(Calendar.SECOND) : ca.get(Calendar.SECOND) + "");

	//获取当前年月日，小时分钟秒，并按照需求格式化为 2016-07-08 12:01:01这种格式
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String getFiveMinTime() {
		Calendar ca=Calendar.getInstance();
		int now = ca.get(Calendar.MINUTE);
		ca.set(Calendar.MINUTE, now - 5);
		String date = SDF.format(ca.getTime());
		return date;
	}
	//获取当前时间前五分钟

	public String getTenMinTime(){
		Calendar ca=Calendar.getInstance();
		int now = ca.get(Calendar.MINUTE);
		ca.set(Calendar.MINUTE, now - 10);
		String date = SDF.format(ca.getTime());
		return date;
	}//获取当前时间前十分钟

	public String get30MinTime(){
		Calendar ca=Calendar.getInstance();
		int now = ca.get(Calendar.MINUTE);
		ca.set(Calendar.MINUTE, now - 30);
		String date = SDF.format(ca.getTime());
		return date;
	}//获取当前时间前半小时

	public String get60MinTime(){
		Calendar ca=Calendar.getInstance();
		int now = ca.get(Calendar.MINUTE);
		ca.set(Calendar.MINUTE, now - 60);
		String date = SDF.format(ca.getTime());
		return date;
	}//获取当前时间前一小时

	public String getOneDayTime(){
		Calendar ca=Calendar.getInstance();
		int now = ca.get(Calendar.DATE);
		ca.set(Calendar.DATE, now - 1);
		String date = SDF.format(ca.getTime());
		return date;
	}//获取当前时间前一天


	public static String getNowTime() {
		Calendar ca=Calendar.getInstance();
		String date = SDF.format(ca.getTime());
		return date;
	}//获取当前时间

	public String getYear() {
		return year;
	}

	public String getMonth() {
		return month;
	}

	public String getDate() {
		return date;
	}

	public String getHour() {
		return hour;
	}

	public String getMinute() {
		return minute;
	}

	public String getSecond() {
		return second;
	}

	/**
	 *毫秒转成时分秒显示
	 */
	public static String getTime(long t){
		if(t<60000){
			long s = (t % 60000) / 1000;
			if(s<10){
				return "00:0"+s;
			}
			return "00:"+s;
		}else if((t>=60000)&&(t<3600000)){
			return formatTime((t % 3600000)/60000)+":"+formatTime((t % 60000 )/1000);
		}else {
			return formatTime(t / 3600000)+":"+formatTime((t % 3600000)/60000)+":"+formatTime((t % 60000 )/1000);
		}
	}

	private static String formatTime(long t){
		String m="";
		if(t>0){
			if(t<10){
				m="0"+t;
			}else{
				m=t+"";
			}
		}else{
			m="00";
		}
		return m;
	}


	public static String secToTime(int time) {
		String timeStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (time <= 0)
			return "0秒";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = unitFormat(minute) + "分" + unitFormat(second)+"秒";
			} else {
				hour = minute / 60;
				if (hour > 99)
					return "99小时59分59秒";
				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + "小时" + unitFormat(minute) + "分" + unitFormat(second)+"秒";
			}
		}
		return timeStr;
	}


	private static String unitFormat(int i) {
		String retStr = null;
		if (i >= 0 && i < 10) retStr = "0" + Integer.toString(i);
		else retStr = "" + i;
		return retStr;
	}

	}
