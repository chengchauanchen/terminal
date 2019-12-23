package cn.vsx.vc.utils;

import com.alibaba.fastjson.JSONObject;

import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.RecorderBindBean;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

public class MessageUtil {
    /**
     * 在组内发一条消息
     */
    public static void sendGroupMessage(String streamMediaServerIp, int streamMediaServerPort, long callId) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            RecorderBindBean bean = cn.vsx.hamster.terminalsdk.tools.DataUtil.getRecorderBindBean();
            int groupId = (bean!=null)?bean.getGroupId():MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                Group group = cn.vsx.hamster.terminalsdk.tools.DataUtil.getGroupByGroupNoFromAllGroup(groupId);
                if (group!=null) {
                    int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                    long memberUniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
                    String memberName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");

                    String sdp = TerminalFactory.getSDK().getLiveManager().getLivePathSdp();

                    String url = "rtsp://"+streamMediaServerIp+":"+streamMediaServerPort+"/"+memberUniqueNo+"_"+callId+sdp;
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                    jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
//                  jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
                    jsonObject.put(JsonParam.CALLID, String.valueOf(callId));
                    jsonObject.put(JsonParam.REMARK, 2);
                    jsonObject.put(JsonParam.LIVER, memberUniqueNo+"_"+memberName);
                    jsonObject.put(JsonParam.LIVERNO, memberId);
                    jsonObject.put(JsonParam.BACKUP, memberId+"_"+memberName);
                    jsonObject.put(JsonParam.EASYDARWIN_RTSP_URL, url);
                    TerminalMessage mTerminalMessage = new TerminalMessage();
                    mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                    mTerminalMessage.messageFromName = memberName;
                    mTerminalMessage.messageToId = NoCodec.encodeGroupNo(group.getNo());
                    mTerminalMessage.messageToName = group.getName();
                    mTerminalMessage.messageBody = jsonObject;
                    mTerminalMessage.sendTime = System.currentTimeMillis();
                    mTerminalMessage.messageType = MessageType.VIDEO_LIVE.getCode();
                    mTerminalMessage.messageUrl = url;
                    TerminalMessage terminalMessage1 = (TerminalMessage) mTerminalMessage.clone();
                    MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", terminalMessage1);
                }
        });
    }
}
