package ptt.terminalsdk.bean;

import java.util.List;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/8
 * 描述：
 * 修订历史：
 */
public class GroupBean{
    /**
     * id : 105
     * deptId : 520
     * createdMemberNo : 0
     * createdMemberUniqueNo : 0
     * createdMemberName : null
     * no : 72020994
     * name : 市局四组994
     * groupType : PDT
     * uniqueNo : 156014825795697940
     * departmentName : 武汉市公安局
     * tempGroupType : null
     * businessId : null
     * processingState : null
     * adminTerminalMemberUniqueNo : []
     * responseGroupType : RESPONSE_FALSE
     */

    private int id;
    private int deptId;
    private int createdMemberNo;
    private int createdMemberUniqueNo;
    private String createdMemberName;
    private int no;
    private String name;
    private String groupType;
    private long uniqueNo;
    private String departmentName;
    private String tempGroupType;
    private String businessId;
    private String processingState;
    private String responseGroupType;
    private List<String> adminTerminalMemberUniqueNo;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getDeptId(){
        return deptId;
    }

    public void setDeptId(int deptId){
        this.deptId = deptId;
    }

    public int getCreatedMemberNo(){
        return createdMemberNo;
    }

    public void setCreatedMemberNo(int createdMemberNo){
        this.createdMemberNo = createdMemberNo;
    }

    public int getCreatedMemberUniqueNo(){
        return createdMemberUniqueNo;
    }

    public void setCreatedMemberUniqueNo(int createdMemberUniqueNo){
        this.createdMemberUniqueNo = createdMemberUniqueNo;
    }

    public String getCreatedMemberName(){
        return createdMemberName;
    }

    public void setCreatedMemberName(String createdMemberName){
        this.createdMemberName = createdMemberName;
    }

    public int getNo(){
        return no;
    }

    public void setNo(int no){
        this.no = no;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getGroupType(){
        return groupType;
    }

    public void setGroupType(String groupType){
        this.groupType = groupType;
    }

    public long getUniqueNo(){
        return uniqueNo;
    }

    public void setUniqueNo(long uniqueNo){
        this.uniqueNo = uniqueNo;
    }

    public String getDepartmentName(){
        return departmentName;
    }

    public void setDepartmentName(String departmentName){
        this.departmentName = departmentName;
    }

    public String getTempGroupType(){
        return tempGroupType;
    }

    public void setTempGroupType(String tempGroupType){
        this.tempGroupType = tempGroupType;
    }

    public String getBusinessId(){
        return businessId;
    }

    public void setBusinessId(String businessId){
        this.businessId = businessId;
    }

    public String getProcessingState(){
        return processingState;
    }

    public void setProcessingState(String processingState){
        this.processingState = processingState;
    }

    public String getResponseGroupType(){
        return responseGroupType;
    }

    public void setResponseGroupType(String responseGroupType){
        this.responseGroupType = responseGroupType;
    }

    public List<String> getAdminTerminalMemberUniqueNo(){
        return adminTerminalMemberUniqueNo;
    }

    public void setAdminTerminalMemberUniqueNo(List<String> adminTerminalMemberUniqueNo){
        this.adminTerminalMemberUniqueNo = adminTerminalMemberUniqueNo;
    }

    @Override
    public String toString(){
        return "GroupBean{" + "id=" + id + ", deptId=" + deptId + ", createdMemberNo=" + createdMemberNo + ", createdMemberUniqueNo=" + createdMemberUniqueNo + ", createdMemberName='" + createdMemberName + '\'' + ", no=" + no + ", name='" + name + '\'' + ", groupType='" + groupType + '\'' + ", uniqueNo=" + uniqueNo + ", departmentName='" + departmentName + '\'' + ", tempGroupType='" + tempGroupType + '\'' + ", businessId='" + businessId + '\'' + ", processingState='" + processingState + '\'' + ", responseGroupType='" + responseGroupType + '\'' + ", adminTerminalMemberUniqueNo=" + adminTerminalMemberUniqueNo + '}';
    }
}
