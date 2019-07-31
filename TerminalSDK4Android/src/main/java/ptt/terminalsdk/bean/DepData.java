package ptt.terminalsdk.bean;

import java.util.List;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/8
 * 描述：
 * 修订历史：
 */
public class DepData{

    /**
     * memberGroups : {"deptList":[{"id":536,"name":"视频侦查支队","departmentVersion":11,"parentId":520,"children":null}],"groupList":[{"id":105,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020994,"name":"市局四组994","groupType":"PDT","uniqueNo":156014825795697940,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":123,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020998,"name":"全市电台998","groupType":"PDT","uniqueNo":156014825778693656,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":375,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020935,"name":"武警935","groupType":"PDT","uniqueNo":156014825652992695,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":122,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72040945,"name":"口子所点名945","groupType":"PDT","uniqueNo":156014826127963329,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":216560,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":200555,"name":"武汉市部门测试宽带组","groupType":"BROADBAND","uniqueNo":156014825670220792,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":106,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020995,"name":"市局五组995","groupType":"PDT","uniqueNo":15601482572050080,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":103,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020992,"name":"市局二组992","groupType":"PDT","uniqueNo":156014825710567144,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":336,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":100040,"name":"市局宽带组","groupType":"BROADBAND","uniqueNo":156014815431564102,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":121,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020990,"name":"市局点名990","groupType":"PDT","uniqueNo":156014825645008648,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":126,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020970,"name":"三圈970","groupType":"PDT","uniqueNo":156014825616066354,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":124,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020997,"name":"全市移动台997","groupType":"PDT","uniqueNo":15601482579632681,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":104,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020993,"name":"市局三组993","groupType":"PDT","uniqueNo":156014825725479874,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":3793,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":102917,"name":"市局宽带组111","groupType":"BROADBAND","uniqueNo":156014825677189976,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":141,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020948,"name":"市局巡逻948","groupType":"PDT","uniqueNo":156014825626698595,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":107,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020996,"name":"市局六组996","groupType":"PDT","uniqueNo":156014825723156583,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":125,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020900,"name":"全市基地台20900","groupType":"PDT","uniqueNo":15601482568906960,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"},{"id":102,"deptId":520,"createdMemberNo":0,"createdMemberUniqueNo":0,"createdMemberName":null,"no":72020991,"name":"市局一组991","groupType":"PDT","uniqueNo":15601482579118617,"departmentName":"武汉市公安局","tempGroupType":null,"businessId":null,"processingState":null,"adminTerminalMemberUniqueNo":[],"responseGroupType":"RESPONSE_FALSE"}]}
     */

    private MemberGroupsBean memberGroups;

    public MemberGroupsBean getMemberGroups(){
        return memberGroups;
    }

    public void setMemberGroups(MemberGroupsBean memberGroups){
        this.memberGroups = memberGroups;
    }

    public static class MemberGroupsBean{
        private List<DeptListBean> deptList;
        private List<GroupBean> groupList;

        public List<DeptListBean> getDeptList(){
            return deptList;
        }

        public void setDeptList(List<DeptListBean> deptList){
            this.deptList = deptList;
        }

        public List<GroupBean> getGroupList(){
            return groupList;
        }

        public void setGroupList(List<GroupBean> groupList){
            this.groupList = groupList;
        }

        public static class DeptListBean{
            /**
             * id : 536
             * name : 视频侦查支队
             * departmentVersion : 11
             * parentId : 520
             * children : null
             */

            private int id;
            private String name;
            private int departmentVersion;
            private int parentId;
            private Object children;

            public int getId(){
                return id;
            }

            public void setId(int id){
                this.id = id;
            }

            public String getName(){
                return name;
            }

            public void setName(String name){
                this.name = name;
            }

            public int getDepartmentVersion(){
                return departmentVersion;
            }

            public void setDepartmentVersion(int departmentVersion){
                this.departmentVersion = departmentVersion;
            }

            public int getParentId(){
                return parentId;
            }

            public void setParentId(int parentId){
                this.parentId = parentId;
            }

            public Object getChildren(){
                return children;
            }

            public void setChildren(Object children){
                this.children = children;
            }

            @Override
            public String toString(){
                return "DeptListBean{" + "id=" + id + ", name='" + name + '\'' + ", departmentVersion=" + departmentVersion + ", parentId=" + parentId + ", children=" + children + '}';
            }
        }

        @Override
        public String toString(){
            return "MemberGroupsBean{" + "deptList=" + deptList + ", groupList=" + groupList + '}';
        }
    }

    @Override
    public String toString(){
        return "DepData{" + "memberGroups=" + memberGroups + '}';
    }
}
