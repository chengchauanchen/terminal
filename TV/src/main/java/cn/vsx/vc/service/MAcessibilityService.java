package cn.vsx.vc.service;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.util.List;

import cn.vsx.vc.R;

/**
 * author: zjx.
 * data:on 2019/1/26
 */

public class MAcessibilityService extends AccessibilityService {
    static Logger logger = Logger.getLogger(MAcessibilityService.class.getName());
    /* 完成安装flag*/
    private final static int FINISH_INSTALL = 1009;

    /* 检测安装过程flag*/
    private final static int  CHECK_INSTALL = 1008;

    /* 是否执行*/
    private static boolean enable = false;

    /* 正在模拟点击*/
    private boolean clicking = false;

    /* 模拟点击的次数, 完成安装后的返回没有计算在里面*/
    private static int onClickCount = 0;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CHECK_INSTALL:

                    btnPerformClick((List<AccessibilityNodeInfo>)msg.obj);
                    break;
                case FINISH_INSTALL:

                    enable = false;
                    AccessibilityService service = (AccessibilityService) msg.obj;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    }
                    break;
            }
        }
    };

    /**
     * 设置是否监测
     * @param ok
     */
    public static void setEnable(boolean ok){
        enable = ok;
        onClickCount = 0;
        logger.error("开启监听");
    }

    /**
     * Callback for {@link AccessibilityEvent}s.
     *
     * @param event An event.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 如果不监测，和正在模拟点击中

        logger.error("clicking===" + clicking + ", enable===" + enable);
        if (!enable || clicking) return;

        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo == null) {
            logger.error("nodeInfo为空");
            return;
        }
        List<AccessibilityNodeInfo> nextBtn = null;

        if ((nextBtn = nodeInfo.findAccessibilityNodeInfosByText(getString(R.string.installing))) != null && (nextBtn.size() > 0)){   // 如果正在安装
            logger.info("正在安装");
            nextBtn.get(0).recycle();

        }else if((((nextBtn = nodeInfo.findAccessibilityNodeInfosByText(getString(R.string.next)))!=null && nextBtn.size() > 0) ||     // 获取下一步按钮控件
                ((nextBtn = nodeInfo.findAccessibilityNodeInfosByText(getString(R.string.install))) !=null && nextBtn.size() > 0)||       // 获取安装按钮控件
                ((nextBtn = nodeInfo.findAccessibilityNodeInfosByText(getString(R.string.done))) !=null && nextBtn.size() > 0)||       // 获取打开按钮控件
                ((nextBtn = nodeInfo.findAccessibilityNodeInfosByText(getString(R.string.done))) !=null && nextBtn.size() > 0))         // 获取完成按钮控件
                ) {
            // 模拟点击flag设置为true
            clicking = true;
            Message message = new Message();
            message.obj = nextBtn;
            message.what = CHECK_INSTALL;
            handler.sendMessageDelayed(message, 500);

//            SharedPreferenceUtil.saveBooleanData(G.FlagsConst.AUTO_INSTALL, true);
        }else {
            logger.info("安装完成");
//            // 判断是否完成安装
            handler.removeMessages(FINISH_INSTALL);
            Message message = new Message();
            message.what = FINISH_INSTALL;
            message.obj = this;
            handler.sendMessageDelayed(message, 1800);
        }

        nodeInfo.recycle();

    }


    /**
     * 模拟点击
     * @param nextBtn
     * @return
     */
    private boolean btnPerformClick(List<AccessibilityNodeInfo> nextBtn) {

        logger.error("onClickCount:" + ++onClickCount);
        AccessibilityNodeInfo nextInfo = nextBtn.get(nextBtn.size() - 1);
        if (nextInfo == null) {
            return true;
        }
        nextInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        nextInfo.recycle();
        clicking = false;
        return false;
    }

    /**
     * Callback for interrupting the accessibility feedback.
     */
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "onInterrupt", Toast.LENGTH_SHORT).show();
    }
}
