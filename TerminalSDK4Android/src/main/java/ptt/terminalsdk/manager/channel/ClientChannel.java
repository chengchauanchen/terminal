package ptt.terminalsdk.manager.channel;

import android.os.RemoteException;

import com.google.protobuf.GeneratedMessage;

import org.ddpush.im.client.v1.ServerConnectionChangedHandler;
import org.ddpush.im.client.v1.ServerConnectionEstablishedHandler;
import org.ddpush.im.common.v1.handler.PushMessageSendResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.TimerTask;

import cn.vsx.hamster.common.MessageFunEnum;
import cn.vsx.hamster.protolbuf.codec.PTTMsgCodec;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.channel.AbsClientChannel;
import cn.vsx.hamster.terminalsdk.manager.channel.ClientChannelMessageDispatcher;
import cn.vsx.hamster.terminalsdk.manager.channel.ServerMessageReceivedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.IMessageService;
import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionChangedHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl.Stub;
import ptt.terminalsdk.context.MyTerminalFactory;

public class ClientChannel extends AbsClientChannel {

	private Logger logger = LoggerFactory.getLogger(ClientChannel.class);
	private IMessageService messageService;
	private TimerTask timerTask;
	public ClientChannel(IMessageService messageService){
		this.messageService = messageService;
	}


	private ServerMessageReceivedHandlerAidl handler = new Stub() {
		@Override
		public void handle(byte[] data, int offset, int length) throws RemoteException {
			logger.info("收到消息 "+ Arrays.toString(data)+"of = "+offset+"  le = "+length);
			getDispatcher().notifyMessageReceived(PTTMsgCodec.INSTANCE.getDecode().decodeMessage(data, offset, length));
		}
	};
//	private List<ServerConnectionEstablishedHandler> serverConnectionEstablishedHandlers = new ArrayList<>();
	private ServerConnectionEstablishedHandler serverConnectionEstablishedHandler;
	private ServerConnectionEstablishedHandlerAidl handlerAidl = new ServerConnectionEstablishedHandlerAidl.Stub() {
		@Override
		public void handler(boolean connected) throws RemoteException {
			if(serverConnectionEstablishedHandler !=null){
				serverConnectionEstablishedHandler.handler(connected);
			}
//			for (ServerConnectionEstablishedHandler handler : serverConnectionEstablishedHandlers){
//				handler.handler(connected);
//			}
		}
	};

	private ServerConnectionChangedHandler serverConnectionChangedHandler;
	private ServerConnectionChangedHandlerAidl connectionChangedHandler = new ServerConnectionChangedHandlerAidl.Stub(){
		@Override
		public void handler(boolean connected) throws RemoteException{
			if(serverConnectionChangedHandler != null){
				serverConnectionChangedHandler.handler(connected);
			}
		}
	};

	@Override
	public <Message extends GeneratedMessage> void registMessageReceivedHandler(final ServerMessageReceivedHandler<Message> handler) {
		getDispatcher().registMessageReceivedHandler(handler);
	}

	@Override
	public <Message extends GeneratedMessage> void unregistMessageReceivedHandler(final ServerMessageReceivedHandler<Message> handler) {
		getDispatcher().unregistMessageReceivedHandler(handler);
	}

	@Override
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
					logger.error("发送命令失败！！",e);
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

	@Override
	public void registServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandler handler){
		serverConnectionEstablishedHandler = handler;
//		if (!serverConnectionEstablishedHandlers.contains(handler)){
//			serverConnectionEstablishedHandlers.add(handler);
//		}
		logger.error("registServerConnectionEstablishedHandler");
	}

	@Override
	public void unregistServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandler handler){
		serverConnectionEstablishedHandler = null;
//		serverConnectionEstablishedHandlers.remove(handler);
	}

	@Override
	public void registServerConnectionChangedHandler(ServerConnectionChangedHandler handler){
		serverConnectionChangedHandler = handler;
	}

	@Override
	public void unregistServerConnectionChangedHandler(){
		serverConnectionChangedHandler = null;
	}

	boolean isStarted;
	@Override
	public void start() {
		try {
			if (!isStarted && messageService != null){
				String protocolType = TerminalFactory.getSDK().getParam(Params.PROTOCOL_TYPE, Params.UDP);
				messageService.initConnectionClient(protocolType);
				messageService.registServerConnectionEstablishedHandler(handlerAidl);
				messageService.registServerConnectionChangedHandler(connectionChangedHandler);
				messageService.registMessageReceivedHandler(handler);
				isStarted = true;
				logger.info("ClientChannel  start成功!");
			}else {
				logger.error("ClientChannel  start失败");
				isStarted = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ClientChannel  Exception:"+e);
			//start失败，重新start，直到成功
			reTryStart();

		}
	}

	/**
	 * 重新尝试
	 */
	private void reTryStart(){
		try{
			if(timerTask != null){
				timerTask.cancel();
				timerTask = null;
			}
		}catch (Exception ef){
			logger.error("ClientChannel  Exception f:"+ef);
			timerTask = null;
		}finally {
			try{
				timerTask = new TimerTask() {
					@Override
					public void run() {
						start();
					}
				};
				TerminalFactory.getSDK().getTimer().schedule(timerTask,5*1000);
			}catch (Exception e){
				e.printStackTrace();
			}
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
			serverConnectionEstablishedHandler = null;
//			serverConnectionEstablishedHandlers.clear();
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
