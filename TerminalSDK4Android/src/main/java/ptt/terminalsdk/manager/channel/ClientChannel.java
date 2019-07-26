package ptt.terminalsdk.manager.channel;

import android.os.RemoteException;

import com.google.protobuf.GeneratedMessage;

import org.ddpush.im.client.v1.ServerConnectionEstablishedHandler;
import org.ddpush.im.common.v1.handler.PushMessageSendResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.MessageFunEnum;
import cn.vsx.hamster.protolbuf.codec.PTTMsgCodec;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.channel.AbsClientChannel;
import cn.vsx.hamster.terminalsdk.manager.channel.ClientChannelMessageDispatcher;
import cn.vsx.hamster.terminalsdk.manager.channel.ServerMessageReceivedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.IMessageService;
import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl.Stub;
import ptt.terminalsdk.context.MyTerminalFactory;

public class ClientChannel extends AbsClientChannel {

	private Logger logger = LoggerFactory.getLogger(ClientChannel.class);
	private IMessageService messageService;
	public ClientChannel(IMessageService messageService){
		this.messageService = messageService;
	}


	private ServerMessageReceivedHandlerAidl handler = new Stub() {
		@Override
		public void handle(byte[] data, int offset, int length) throws RemoteException {
//			logger.info("收到消息 "+ Arrays.toString(data)+"of = "+offset+"  le = "+length);
			getDispatcher().notifyMessageReceived(PTTMsgCodec.INSTANCE.getDecode().decodeMessage(data, offset, length));
		}
	};
	private List<ServerConnectionEstablishedHandler> serverConnectionEstablishedHandlers = new ArrayList<>();
	private ServerConnectionEstablishedHandlerAidl handlerAidl = new ServerConnectionEstablishedHandlerAidl.Stub() {
		@Override
		public void handler(boolean connected) throws RemoteException {
			logger.info("ClientChannel--"+connected+"----"+serverConnectionEstablishedHandlers.size());
			for (ServerConnectionEstablishedHandler handler : serverConnectionEstablishedHandlers){
				handler.handler(connected);
			}
		}
	};

	public <Message extends GeneratedMessage> void registMessageReceivedHandler(final ServerMessageReceivedHandler<Message> handler) {
		getDispatcher().registMessageReceivedHandler(handler);
	}

	public <Message extends GeneratedMessage> void unregistMessageReceivedHandler(final ServerMessageReceivedHandler<Message> handler) {
		getDispatcher().unregistMessageReceivedHandler(handler);
	}

	public void sendMessage(GeneratedMessage message, final PushMessageSendResultHandler handler){
		sendMessage(message, handler, MessageFunEnum.MESSAGE_FOR_WORK.getCode());
	}

	@Override
	public void sendMessage(GeneratedMessage message, final PushMessageSendResultHandler handler, byte messageFun) {
		if(MyTerminalFactory.getSDK().hasNetwork()){
			if(TerminalFactory.getSDK().isServerConnected()){
				logger.info("发送消息："+message.getClass()+"----->"+message);
				try {
					messageService.sendMessage(PTTMsgCodec.INSTANCE.getEncode().encodeMessage(message, messageFun), new PushMessageSendResultHandlerAidl.Stub() {
						@Override
						public void handler(boolean sendOK, String uuid) throws RemoteException {
							handler.handler(sendOK, uuid);
						}
					});
				} catch (Exception e) {
					logger.error("发送命令失败！！"+e);
					handler.handler(false, null);
				}
			}else {
				logger.error("信令服务没有连接，不能发送命令！！");
				handler.handler(false, null);
			}
		}
		else{
			logger.error("网络没有连接，不能发送命令！！");
			handler.handler(false, null);
		}
	}

	public void registServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandler handler){
		if (!serverConnectionEstablishedHandlers.contains(handler)){
			serverConnectionEstablishedHandlers.add(handler);
		}
		logger.error("serverConnectionEstablishedHandlers = "+serverConnectionEstablishedHandlers);
	}
	public void unregistServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandler handler){
		serverConnectionEstablishedHandlers.remove(handler);
	}

	boolean isStarted;
	@Override
	public void start() {
		try {
			if (!isStarted && messageService != null){
				logger.info("ClientChannel  start成功!");
				String protocolType = TerminalFactory.getSDK().getParam(Params.PROTOCOL_TYPE, Params.UDP);
				messageService.initConnectionClient(protocolType);
				messageService.registServerConnectionEstablishedHandler(handlerAidl);
				messageService.registMessageReceivedHandler(handler);
				isStarted = true;
			}else {
				logger.error("ClientChannel  start失败");
				isStarted = false;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			//start失败，重新start，直到成功
			start();
		}
	}

	@Override
	public void stop() {
		try {
			logger.info("注销handler，并把对象置空。");
			if(messageService!=null){
				messageService.unregistMessageReceivedHandler(handler);
				messageService.unregistServerConnectionEstablishedHandler(handlerAidl);
			}
			serverConnectionEstablishedHandlers.clear();
			messageService = null;
			isStarted = false;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private ClientChannelMessageDispatcher getDispatcher(){
		return ClientChannelMessageDispatcher.getInstance();
	}
}
