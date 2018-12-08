//package ptt.terminalsdk.context;
//
//import android.os.PowerManager.WakeLock;
//import cn.zectec.hamster.terminalsdk.tools.Params;
//import ptt.terminalsdk.manager.channel.ClientChannel;
//
//public class MyClientChannel extends ClientChannel {
//
//	private WakeLock wakeLock;
//
//	@Override
//	public boolean hasNetworkConnection() {
//		return MyTerminalFactory.getSDK().hasNetwork();
//	}
//
//	@Override
//	public void trySystemSleep() {
//		if(wakeLock != null && wakeLock.isHeld() == true){
//			wakeLock.release();
//		}
//	}
//
//	@Override
//	public void trySystemWake() {
//		if(wakeLock != null && wakeLock.isHeld() == false){
//			wakeLock.acquire();
//		}
//	}
//
//	@Override
//	protected byte getSeq() {
//		seq = (byte) MyTerminalFactory.getSDK().getParam(Params.SEQ, 0);
//		seq++;
//		MyTerminalFactory.getSDK().putParam(Params.SEQ, seq);
//		return seq;
//	}
//
//	public WakeLock getWakeLock() {
//		return wakeLock;
//	}
//
//	public void setWakeLock(WakeLock wakeLock) {
//		this.wakeLock = wakeLock;
//	}
//}
