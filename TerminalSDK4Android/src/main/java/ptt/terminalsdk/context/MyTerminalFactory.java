package ptt.terminalsdk.context;

import cn.vsx.hamster.terminalsdk.TerminalFactory;

public class MyTerminalFactory extends TerminalFactory {
	
	public static TerminalSDK4Android getSDK(){
		return (TerminalSDK4Android)_terminalSDK;
	}
	
	public static void setTerminalSDK(TerminalSDK4Android terminalSDK){
		_terminalSDK = terminalSDK;
	}
}
