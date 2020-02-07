package cn.vsx.vc.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

/**
 * @author martian on 2020/2/6.
 */
public class SpaceFilter implements InputFilter {

  @Override
  public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
      int dend) {
    //返回null表示接收输入的字符,返回空字符串表示不接受输入的字符
    if (TextUtils.equals(" ",source)) {
      return "";
    }
    return null;
  }
}
