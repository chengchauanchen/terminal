package ptt.terminalsdk.manager.voip;

import cn.vsx.hamster.terminalsdk.model.CallRecord;

public interface DownloadCompleteListener{
    public void succeed(CallRecord callRecord);

    public void failure();
}
