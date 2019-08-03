package cn.vsx.SpecificSDK.instruction.groupCall;

public interface SendGroupCallListener {
    void speaking();//开始
    void readySpeak();//准备说
    void forbid();//禁止组呼
    void waite();//等待
    void silence();//沉默
    void listening();//听
    void fail();//失败
}
