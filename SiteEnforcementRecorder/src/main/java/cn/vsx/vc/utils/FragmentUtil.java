package cn.vsx.vc.utils;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import cn.vsx.vc.fragment.AppListFragment;
import cn.vsx.vc.fragment.BindFragment;
import cn.vsx.vc.fragment.FileListFragment;
import cn.vsx.vc.fragment.FilePictureShowFragment;
import cn.vsx.vc.fragment.FileVideoShowFragment;
import cn.vsx.vc.fragment.GroupChangeFragment;
import cn.vsx.vc.fragment.GroupSearchFragment;
import cn.vsx.vc.fragment.InputPoliceIdFragment;
import cn.vsx.vc.fragment.MenuFragment;
import cn.vsx.vc.fragment.NFCFragment;
import cn.vsx.vc.fragment.QRScanFragment;
import cn.vsx.vc.fragment.SetFragment;
import cn.vsx.vc.fragment.SetInfraRedFragment;
import cn.vsx.vc.fragment.SetLivingStopTimeFragment;
import cn.vsx.vc.fragment.SetServerFragment;

public class FragmentUtil {

    public static Fragment getFragmentByTag(String tag, Bundle bundle){
        Fragment mFragment = null;
        switch (tag){
            case Constants.FRAGMENT_TAG_MENU:
                //menu
                mFragment = MenuFragment.newInstance();
//                clearFragmentBackStack();
                break;
            case Constants.FRAGMENT_TAG_BIND:
                //绑定方式
                mFragment = BindFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_NFC:
                //NFC
                mFragment = NFCFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_QR:
                //QR
                mFragment = QRScanFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_INPUT:
                //QR
                mFragment = InputPoliceIdFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_FILE_LIST:
                //录像、照片
                mFragment = FileListFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_SET:
                //设置
                mFragment = SetFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_INFRA_RED:
                //红外设置
                mFragment = SetInfraRedFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_SET_LIVING_STOP_TIME:
                //设置实时视频上报时长
                mFragment = SetLivingStopTimeFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_SET_SERVER:
                //设置IP、PORT
                mFragment = SetServerFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_APP_LIST:
                //设置-应用列表
                mFragment = AppListFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_GROUP_CHANGE:
                //组-转组
                mFragment = GroupChangeFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_GROUP_SEARCH:
                //组-搜索
                mFragment = GroupSearchFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_FILE_VIDEO_SHOW:
                //文件查看-录像
                mFragment = FileVideoShowFragment.newInstance();
                break;
            case Constants.FRAGMENT_TAG_FILE_PICTURE_SHOW:
                //文件查看-图片
                mFragment = FilePictureShowFragment.newInstance();
                break;
                default:break;
        }
        if(mFragment!=null&&bundle!=null){
            mFragment.setArguments(bundle);
        }
        return  mFragment;
    }
}
