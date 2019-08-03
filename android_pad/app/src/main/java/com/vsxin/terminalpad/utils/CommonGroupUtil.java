package com.vsxin.terminalpad.utils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;

public class CommonGroupUtil {

	private static List<Integer> catchGroupIdList = new ArrayList<>();

	private Logger logger = Logger.getLogger(CommonGroupUtil.class);

	public static List<Integer> getCatchGroupIdList(){
		return catchGroupIdList;
	}

	public static void setCatchGroupIdList(final int toGroupId) {
		int num = -1;
		for (int i = 0; i < catchGroupIdList.size(); i++) {
			if (catchGroupIdList.get(i) == (toGroupId)) {
				num = i;
				break;
			}
		}
		if (num>=0) {
			catchGroupIdList.remove(num);
		}

		catchGroupIdList.add(toGroupId);
		//转组后，保存到本地
		MyTerminalFactory.getSDK().putParam(Params.CATCH_GROUPID_LIST, Util.object2String(catchGroupIdList));
	}


	/**读取常用组列表 */
	@SuppressWarnings("unchecked")
	public static List<Integer> getCatchGroupIds(){
		String allCatchGroupString = MyTerminalFactory.getSDK().getParam(Params.CATCH_GROUPID_LIST, "");
		if(!Util.isEmpty(allCatchGroupString)){
			Object o = Util.string2Object(allCatchGroupString);
			if(o != null && o instanceof List){
				catchGroupIdList = (List<Integer>) o;
			}
		}
		//logger.info("常用组列表"+catchGroupIdList.toString());
		return catchGroupIdList;
	}

	//删除ID
	public static void removeCatchGroupIdList(final int toGroupId) {

		for (int i=0;i<catchGroupIdList.size();i++){
			if (catchGroupIdList.get(i) == toGroupId){
				catchGroupIdList.remove(i);
			}
		}
		//转组后，保存到本地
		MyTerminalFactory.getSDK().putParam(Params.CATCH_GROUPID_LIST, Util.object2String(catchGroupIdList));
	}
}
