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

import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.StringUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class LiveHistoryActivity extends BaseActivity implements View.OnClickListener{



    ImageView iv_close;

    TextView tv_theme;

    TextView tv_live_start_time;

    ImageView iv_live_image;

    ImageView iv_play;

    TextView tv_live_duration;

    LinearLayout ll_volume;

    ImageView iv_volume;

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
                    tv_live_duration.setText((StringUtil.toFloat(duration) != 0)? DateUtils.getTime((int) (Float.valueOf(duration)*1000)):"");
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
        tv_volume = (TextView) findViewById(R.id.tv_volume);
        iv_volume = (ImageView) findViewById(R.id.iv_volume);
        ll_volume = (LinearLayout) findViewById(R.id.ll_volume);
        tv_live_duration = (TextView) findViewById(R.id.tv_live_duration);
        iv_play = (ImageView) findViewById(R.id.iv_play);
        iv_live_image = (ImageView) findViewById(R.id.iv_live_image);
        tv_live_start_time = (TextView) findViewById(R.id.tv_live_start_time);
        tv_theme = (TextView) findViewById(R.id.tv_theme);
        iv_close = (ImageView) findViewById(R.id.iv_close);
        findViewById(R.id.iv_play).setOnClickListener(this);
        findViewById(R.id.iv_close).setOnClickListener(this);
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
        //上报主题，如果没有就取上报者的名字
        liveTheme = messageBody.getString(JsonParam.TITLE);
        if(TextUtils.isEmpty(liveTheme)){
            if(split.length>1){
                String memberName = split[1];
                liveTheme = String.format(getString(R.string.text_living_theme_member_name),memberName);
                tv_theme.setText(liveTheme);
            }else {
                int memberNo = terminalMessage.messageFromId;
                TerminalFactory.getSDK().getThreadPool().execute(() -> {
                    Account account = cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMemberNo(memberNo,true);
                    String name = (account!=null)?account.getName():terminalMessage.messageFromName;
                    mHandler.post(() -> {
                        tv_theme.setText(String.format(this.getString(R.string.current_push_member),name));
                    });
                });
            }
        }else {
            tv_theme.setText(liveTheme);
        }


        //获取播放url
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String serverIp = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP, "");
            String serverPort = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT, 0)+"";
            String url = "http://"+serverIp+":"+serverPort+"/api/v1/query_records";
            Map<String,String> paramsMap = new HashMap<>();
            logger.info("消息："+terminalMessage);
            paramsMap.put("id",getCallId(terminalMessage));
            logger.info("获取视频回放url："+url);
            String result = TerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
//            result = "{\"msg\":\"success\",\"code\":0,\"data\":{\"list\":[{\"name\":\"88045832_6540978884229379386\",\"start_time\":\"20190517154511\",\"duration\":\"13.598\",\"hls\":\"/hls/88045832_6540978884229379386/20190517/20190517154511/88045832_6540978884229379386_record.m3u8\"}]}}";
            logger.info("获取视频回放结果："+result);
            if(!Util.isEmpty(result)){
                JSONObject jsonResult = JSONObject.parseObject(result);
                Integer code = jsonResult.getInteger("code");
                if(code == 0){
                    JSONObject data = jsonResult.getJSONObject("data");
                    JSONArray list = data.getJSONArray("list");
                    if(list.isEmpty()){
                        ToastUtil.showToast(LiveHistoryActivity.this,getString(R.string.text_get_video_info_fail));
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

    /**
     * 获取callId
     * @param terminalMessage
     * @return
     */
    private String getCallId(TerminalMessage terminalMessage){
        String id = "";
        if(terminalMessage.messageBody!=null){
            if(terminalMessage.messageBody.containsKey(JsonParam.EASYDARWIN_RTSP_URL)){
                String url = terminalMessage.messageBody.getString(JsonParam.EASYDARWIN_RTSP_URL);
                if(!TextUtils.isEmpty(url)&&url.contains("/")&&url.contains(".")){
                    int index = url.lastIndexOf("/");
                    int pointIndex = url.lastIndexOf(".");
                    id = url.substring(index+1,pointIndex);
                }
            }
        }
        return id;
    }

    @Override
    public void doOtherDestroy(){
        mHandler.removeCallbacksAndMessages(null);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
    }


    public void close(){
        finish();
    }


    public void play(){
        if(liveUrl==null){
            ToastUtil.showToast(LiveHistoryActivity.this,getString(R.string.text_can_not_find_video_invalided));
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
            tv_volume.setText(String.format(getString(R.string.text_percent_sign_volume),MyTerminalFactory.getSDK().getAudioProxy().getVolume()));
            mHandler.sendEmptyMessageDelayed(RECEIVEVOICECHANGED,2000);
        }
    };

    @Override
    public void onClick(View v){
        int i = v.getId();
        if(i == R.id.iv_play){
            play();
        }else if(i == R.id.iv_close){
            close();
        }
    }
}
