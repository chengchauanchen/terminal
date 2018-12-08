package ptt.terminalsdk.manager.channel;

import android.os.RemoteException;

import com.google.protobuf.GeneratedMessage;

import org.ddpush.im.client.v1.ServerConnectionEstablishedHandler;
import org.ddpush.im.common.v1.handler.PushMessageSendResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.vsx.hamster.protolbuf.codec.PTTMsgCodec;
import cn.vsx.hamster.terminalsdk.manager.channel.AbsClientChannel;
import cn.vsx.hamster.terminalsdk.manager.channel.ClientChannelMessageDispatcher;
import cn.vsx.hamster.terminalsdk.manager.channel.ServerMessageReceivedHandler;
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
		if(MyTerminalFactory.getSDK().hasNetwork()){
			logger.info("发送消息："+message.getClass()+"----->"+message);
			try {
				messageService.sendMessage(PTTMsgCodec.INSTANCE.getEncode().encodeMessage(message), new PushMessageSendResultHandlerAidl.Stub() {
                    @Override
                    public void handler(boolean sendOK, String uuid) throws RemoteException {
                        handler.handler(sendOK, uuid);
                    }
                });
			} catch (Exception e) {
				e.printStackTrace();
			}

			byte[] bytes = PTTMsgCodec.INSTANCE.getEncode().encodeMessage(message);
			logger.info("bytes="+ Arrays.toString(bytes));
		}
		else{
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
				logger.info("注册handler成功!");
				messageService.registMessageReceivedHandler(handler);
				messageService.registServerConnectionEstablishedHandler(handlerAidl);
				isStarted = true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		try {
			logger.info("注销handler，并把对象置空。");
			messageService.unregistMessageReceivedHandler(handler);
			messageService.unregistServerConnectionEstablishedHandler(handlerAidl);
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
