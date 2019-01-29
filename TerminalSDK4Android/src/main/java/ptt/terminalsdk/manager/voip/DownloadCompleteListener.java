package ptt.terminalsdk.manager.voip;

import cn.vsx.hamster.terminalsdk.model.CallRecord;

public interface DownloadCompleteListener{
     void succeed(CallRecord callRecord);

     void failure();
}
