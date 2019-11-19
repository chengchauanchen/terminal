package cn.vsx.vc.search;

import android.content.Intent;

/**
 * Created by Administrator on 2018/5/9.
 */

public class CustomException extends RuntimeException {
    private int exceptionCode;
    private Intent intent;
    public CustomException(String message) {
        super(message);
    }

    public CustomException(int exceptionCode, String message){
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public CustomException(int exceptionCode, String message, Intent intent){
        super(message);
        this.exceptionCode = exceptionCode;
        this.intent = intent;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public Intent getIntent() {
        return intent;
    }
}
