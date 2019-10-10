package cn.vsx.vc.utils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.model.CatalogBean;
import ptt.terminalsdk.tools.ApkUtil;

public class WuTieUtil {

    /**
     * 获取部门tab显示内容
     * @param deptId
     * @param deptName
     * @return
     */
    public static CatalogBean getCatalogBean(int deptId,String deptName){
        if(DataUtil.isRootDeptment(deptId)){
            return WuTieUtil.addRootDetpCatalogNames();
        }else{
            return new CatalogBean(deptName,deptId);
        }
    }

    /**
     * 添加根部门
     * (当是武铁包时，不显示“武汉市公安局”)
     */
    public static CatalogBean addRootDetpCatalogNames(){
        CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        if(ApkUtil.isWuTie()){
            groupCatalogBean.setName("");
        }
        return groupCatalogBean;
    }

    /**
     * 武铁包，过滤其他组
     * @param depId
     * @param groups
     * @return
     */
    public static List<Group> filterRootDeptment(int depId,List<Group> groups){
        if(DataUtil.isRootDeptment(depId) && ApkUtil.isWuTie()){
            return filter(groups);
        }else{
            return groups;
        }
    }

    /**
     * 过滤某组的数据
     * @param groups
     * @return
     */
    public static  List<Group> filter(List<Group> groups){
//        logger.info("filterRootDeptment---groups:"+groups);
        List<Group> resources = new ArrayList<Group>(){{
            add(new Group(72020991,72020991,"市局一组991"));
            add(new Group(72020992,72020992,"市局二组992"));
            add(new Group(72020993,72020993,"市局三组993"));
            add(new Group(72020994,72020994,"市局四组994"));
            add(new Group(72020995,72020995,"市局五组995"));
            add(new Group(72020996,72020996,"市局六组996"));
        }};
        List<Group> result = new ArrayList<>();
        for (Group group:groups) {
            if(resources.contains(group)){
                result.add(group);
            }
        }
        return result;
    }
}
