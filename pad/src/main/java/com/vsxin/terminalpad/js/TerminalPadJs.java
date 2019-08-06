package com.vsxin.terminalpad.js;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeConstans;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.entity.MemberInfoBean;
import com.vsxin.terminalpad.mvp.ui.fragment.MemberInfoFragment;

import java.util.logging.Logger;

/**
 * @author 地图web与原生交互
 */
public class TerminalPadJs {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Context context;

    public TerminalPadJs(Context context) {
        this.context = context;
    }

    /**
     * 点击单个气泡，打开成员详情
     *
     * @param memberInfo
     */
    @JavascriptInterface
    public void memberInfo(String memberInfo, String memberType) {
        logger.info("气泡：" + memberInfo+"数据:"+memberInfo);
        MemberTypeEnum typeEnum;
        switch (memberType) {
            case MemberTypeConstans.PATROL://巡逻船
                typeEnum = MemberTypeEnum.PATROL;
                break;
            case MemberTypeConstans.PHONE://警务通
                typeEnum = MemberTypeEnum.PHONE;
                break;
            case MemberTypeConstans.HAND://电台
                typeEnum = MemberTypeEnum.HAND;
                break;
            case MemberTypeConstans.LTE://LTE
                typeEnum = MemberTypeEnum.LTE;
                break;
            case MemberTypeConstans.UAV://无人机
                typeEnum = MemberTypeEnum.UAV;
                break;
            case MemberTypeConstans.VIDEO://执法仪
                typeEnum = MemberTypeEnum.VIDEO;
                break;
            case MemberTypeConstans.CAMERA://摄像头
                typeEnum = MemberTypeEnum.CAMERA;
                break;
            case MemberTypeConstans.BALL://布控球
                typeEnum = MemberTypeEnum.BALL;
                break;
                case MemberTypeConstans.DRONE_OPERATOR://布控球
                typeEnum = MemberTypeEnum.DRONE_OPERATOR;
                break;
            default:
                typeEnum = null;
                break;
        }

        try {
            MemberInfoBean memberInfoBean = new Gson().fromJson(memberInfo, MemberInfoBean.class);
            MemberInfoFragment.startMemberInfoFragment((FragmentActivity) context, memberInfoBean,typeEnum);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("数据解析异常");
        }

    }
}
