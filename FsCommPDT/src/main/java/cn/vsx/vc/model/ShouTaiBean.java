package cn.vsx.vc.model;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;

/**
 * Created by jamie on 2017/10/28.
 */

public class ShouTaiBean {
    public String shijuName;
    public List<BuMenBean> bumenList;
    public boolean isCommandCentre;

    public ShouTaiBean(String shijuName, List<BuMenBean> bumenList) {
        this.shijuName = shijuName;
        this.bumenList = bumenList;
    }

    public static class BuMenBean {
        public String bumenName;
        public List<Member> memberList;
        public boolean isCommandCentreBuMen;

        public BuMenBean(String bumenName, List<Member> memberList) {
            this.bumenName = bumenName;
            this.memberList = memberList;
        }

        @Override
        public String toString() {
            return "BuMenBean{" +
                    "bumenName='" + bumenName + '\'' +
                    ", memberList=" + memberList +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ShouTaiBean{" +
                "shijuName='" + shijuName + '\'' +
                ", bumenList=" + bumenList +
                '}';
    }
}
