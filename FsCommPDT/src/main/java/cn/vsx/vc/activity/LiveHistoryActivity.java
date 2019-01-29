package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class LiveHistoryActivity extends BaseActivity{


    @Bind(R.id.iv_close)
    ImageView iv_close;
    @Bind(R.id.tv_theme)
    TextView tv_theme;
    @Bind(R.id.tv_live_start_time)
    TextView tv_live_start_time;
    @Bind(R.id.iv_live_image)
    ImageView iv_live_image;
    @Bind(R.id.iv_play)
    ImageView iv_play;
    @Bind(R.id.tv_live_duration)
    TextView tv_live_duration;
    @Bind(R.id.ll_volume)
    LinearLayout ll_volume;
    @Bind(R.id.iv_volume)
    ImageView iv_volume;
    @Bind(R.id.tv_volume)
    TextView tv_volume;
    private static final int GET_URL = 0;
    private static final int RECEIVEVOICECHANGED = 2;
    private String liveUrl;
    private String start_time;
    private String duration;
    private String liveTheme;
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case GET_URL:
                    String year = start_time.substring(0, 4);
                    String month = start_time.substring(4, 6);
                    String day = start_time.substring(6, 8);
                    String hour = start_time.substring(8, 10);
                    String min = start_time.substring(10, 12);
                    String second = start_time.substring(12, 14);
                    StringBuilder sb = new StringBuilder();
                    sb.append(year).append("-").append(month).append("-").append(day).append("  ").append(hour).append(":").append(min).append(":").append(second);
                    tv_live_start_time.setText(sb.toString());
                    tv_live_duration.setText(DataUtil.getTime((int) (Float.valueOf(duration)*1000)));
                break;
                case RECEIVEVOICECHANGED:
                    ll_volume.setVisibility(View.GONE);
                    break;
            }
        }
    };


    @Override
    public int getLayoutResId(){
        return R.layout.activity_live_history;
    }

    @Override
    public void initView(){
    }

    @Override
    public void initListener(){
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
    }

    @Override
    public void initData(){
        final TerminalMessage terminalMessage = (TerminalMessage) getIntent().getSerializableExtra("terminalMessage");

        JSONObject messageBody = terminalMessage.messageBody;
        String liver = messageBody.getString(JsonParam.LIVER);
        String[] split = liver.split("_");
        //设置默认上报视频者，防止数组下标越界异常
        int memberNo = terminalMessage.messageFromId;
        if(split.length>0){
            memberNo = Integer.valueOf(split[0]);
        }
        //上报主题，如果没有就取上报者的名字
        liveTheme = messageBody.getString(JsonParam.TITLE);
        if(TextUtils.isEmpty(liveTheme)){
            if(split.length>1){
                String memberName = split[1];
                liveTheme = memberName+"上报图像";
            }else {
                Member member = DataUtil.getMemberByMemberNo(memberNo);
                liveTheme = member.getName()+"上报图像";
            }
        }
        tv_theme.setText(liveTheme);

        //获取播放url
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String serverIp = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP, "");
            String serverPort = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT, 0)+"";
            String url = "http://"+serverIp+":"+serverPort+"/api/v1/query_records";
            Map<String,String> paramsMap = new HashMap<>();
            logger.info("消息："+terminalMessage);
            String messagePath = terminalMessage.messagePath;
            int index = messagePath.lastIndexOf("/");
            int pointIndex = messagePath.lastIndexOf(".");
            String id = messagePath.substring(index+1,pointIndex);
            paramsMap.put("id",id);
            logger.info("获取视频回放url："+url);
            String result = TerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
            logger.info("获取视频回放结果："+result);
            if(!Util.isEmpty(result)){
                JSONObject jsonResult = JSONObject.parseObject(result);
                Integer code = jsonResult.getInteger("code");
                if(code == 0){
                    JSONObject data = jsonResult.getJSONObject("data");
                    JSONArray list = data.getJSONArray("list");
                    if(list.isEmpty()){
                        ToastUtil.showToast(LiveHistoryActivity.this,"获取视频信息失败");
                        return;
                    }
                    JSONObject jsonObject = list.getJSONObject(0);
                    start_time = jsonObject.getString("start_time");
                    duration = jsonObject.getString("duration");
                    String hls = jsonObject.getString("hls");
                    String fileServerIp = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP);
                    String port = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT,0)+"";

                    liveUrl = "http://"+fileServerIp+":"+port+hls;
                    mHandler.sendEmptyMessage(GET_URL);
                }
            }
        });

    }

    @Override
    public void doOtherDestroy(){
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
    }

    @OnClick(R.id.iv_close)
    public void close(){
        finish();
    }

    @OnClick(R.id.iv_play)
    public void play(){
        if(liveUrl==null){
            ToastUtil.showToast(LiveHistoryActivity.this,"无法找到该视频，视频已失效");
            return;
        }
        Intent intent = new Intent(this,PlayLiveHistoryActivity.class);
        intent.putExtra("URL",liveUrl);
        intent.putExtra("liveTheme",liveTheme);
        intent.putExtra("DURATION",duration);
        startActivity(intent);
    }

    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler(){
        @Override
        public void handler(boolean isVolumeOff, int status){
            mHandler.removeMessages(RECEIVEVOICECHANGED);
            if (status == 0){
                ll_volume.setVisibility(View.GONE);
            }else if (status ==1){
                ll_volume.setVisibility(View.VISIBLE);
            }
            tv_volume.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
            mHandler.sendEmptyMessageDelayed(RECEIVEVOICECHANGED,2000);
        }
    };

}
