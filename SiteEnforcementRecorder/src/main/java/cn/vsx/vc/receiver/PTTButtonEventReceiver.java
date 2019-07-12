package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import cn.vsx.vc.activity.MainActivity;
import cn.vsx.vc.activity.RegistNFCActivity;
import cn.vsx.vc.activity.SplashActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverPTTButtonEventHandler;
import cn.vsx.vc.service.PTTButtonEventService;
import cn.vsx.vc.utils.APPStateUtil;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * ptt实体按键的广播接收
 */
public class PTTButtonEventReceiver extends BroadcastReceiver {
  private Logger logger = Logger.getLogger(getClass());


  @Override
  public void onReceive(Context context, Intent intent) {
    String intentAction = intent.getAction();
    logger.info("PTTButtonEventReceiver = intentAction："+intentAction);
    //通过ptt抬起的事件判断app的运行状态
    if (Constants.PTTEVEVT_ACTION_UP.equals(intentAction)) {
      //判断应用是否已经打开
      if (APPStateUtil.activityIsOpened(context, MainActivity.class)
          || APPStateUtil.activityIsOpened(context, SplashActivity.class)) {
        logger.info("PTTButtonEventReceiver = APPState：opened");
        if (!APPStateUtil.isServiceWork(context, PTTButtonEventService.class)&&APPStateUtil.activityIsOpened(context, MainActivity.class)) {
          //当已经登录过但ptt按键的service没有运行
          MyApplication.instance.startPTTButtonEventService(intentAction);
          return;
        }else {
          //当ptt按键的service在运行中，但app在后台运行，切到前台
//          APPStateUtil.setTopApp(context);
        }
      } else {
        //没有打开过 --- 启动应用
        logger.info("PTTButtonEventReceiver = APPState：noopened");
        Intent jumpIntent = new Intent(context, SplashActivity.class);
        jumpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(jumpIntent);
        return;
      }
    }
    if (Constants.PTTEVEVT_ACTION_DOWN.equals(intentAction)) {
      //PTT实体按键-按下
      logger.info("PTTButtonEventReceiver = " + intentAction);
    } else if (Constants.PTTEVEVT_ACTION_UP.equals(intentAction)) {
      logger.info("PTTButtonEventReceiver = " + intentAction);
      //PTT实体按键-抬起
    }
    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverPTTButtonEventHandler.class,intentAction);

  }
}
