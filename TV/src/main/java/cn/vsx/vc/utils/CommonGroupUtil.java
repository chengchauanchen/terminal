package cn.vsx.vc.utils;

import cn.vsx.vc.application.MyApplication;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ptt.terminalsdk.context.MyTerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;

public class CommonGroupUtil {

	private Logger logger = Logger.getLogger(CommonGroupUtil.class);
	
	public static void setCatchGroupIdList(final int toGroupId) {
		int num = -1;
		for (int i = 0; i < MyApplication.catchGroupIdList.size(); i++) {
			if (MyApplication.catchGroupIdList.get(i) == (toGroupId)) {
				num = i;
				break;
			}
		}
		if (num>=0) {
			MyApplication.catchGroupIdList.remove(num);
		}
		
		MyApplication.catchGroupIdList.add(toGroupId);
		//转组后，保存到本地
		MyTerminalFactory.getSDK().putParam(Params.CATCH_GROUPID_LIST, Util.object2String(MyApplication.catchGroupIdList ));
	}
	
	
	/**读取常用组列表 */
	@SuppressWarnings("unchecked")
	public static List<Integer> getCatchGroupIds(){
		String allCatchGroupString = MyTerminalFactory.getSDK().getParam(Params.CATCH_GROUPID_LIST, "");
		List<Integer> myCatchGroupIdList = new ArrayList<>();
		if(!Util.isEmpty(allCatchGroupString)){
			Object o = Util.string2Object(allCatchGroupString);
			if(o != null && o instanceof List){
				myCatchGroupIdList = (List<Integer>) o;
			}
		}
//		logger.info("常用组列表"+myCatchGroupIdList.toString());
		return myCatchGroupIdList;
	}

	//删除ID
	public static void removeCatchGroupIdList(final int toGroupId) {

		for (int i=0;i<MyApplication.catchGroupIdList.size();i++){
			if (MyApplication.catchGroupIdList.get(i) == toGroupId){
				MyApplication.catchGroupIdList.remove(i);
			}
		}
		//转组后，保存到本地
		MyTerminalFactory.getSDK().putParam(Params.CATCH_GROUPID_LIST, Util.object2String(MyApplication.catchGroupIdList ));
	}
}
