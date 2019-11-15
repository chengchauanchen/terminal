package cn.vsx.vc.search;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.vsx.vc.R;
import ptt.terminalsdk.tools.PinyinUtils;

/**
 * 搜索键盘
 */
public class SearchKeyboardView extends RelativeLayout implements OnClickListener {

    public Logger logger = Logger.getLogger(getClass());

    private OnT9TelephoneDialpadView onT9TelephoneDialpadView;
    private Map<Integer, Integer> map = new HashMap<>();
    private ImageView hide;
    private LinearLayout delete;
    private AudioManager am;
    private SoundPool spool;

    //数字
    private String[] nums = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    //字母
    private String[] letters = {"", "", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ"};


    public static String STRS[] = {"", "", "[abc]", "[def]", "[ghi]", "[jkl]", "[mno]", "[pqrs]", "[tuv]", "[wxyz]"};

    private String inputValue = "";
    private List<String> inputPinyin = new ArrayList<>();

    public SearchKeyboardView(Context context) {
        super(context);
        initView();
    }

    public SearchKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SearchKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        initRingTone();

        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        if (layoutInflater == null) {
            return;
        }
        View view = layoutInflater.inflate(R.layout.search_keyboard_popup_window_layout, this, true);
        hide = view.findViewById(R.id.hide);
        delete = view.findViewById(R.id.delete);
        hide.setOnClickListener(this);
        delete.setOnClickListener(this);

        int[] numberID = new int[]{R.id.dialNum0, R.id.dialNum1, R.id.dialNum2, R.id.dialNum3, R.id.dialNum4, R.id.dialNum5, R.id.dialNum6, R.id.dialNum7, R.id.dialNum8, R.id.dialNum9};
        for (int i = 0; i < numberID.length; i++) {
            LinearLayout v = view.findViewById(numberID[i]);
            v.setOnClickListener(this);
        }
    }

    /**
     * 初始化铃声
     */
    private void initRingTone() {
        am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        spool = new SoundPool(11, AudioManager.STREAM_SYSTEM, 5);
        map.put(0, spool.load(getContext(), R.raw.dtmf0, 0));
        map.put(1, spool.load(getContext(), R.raw.dtmf1, 0));
        map.put(2, spool.load(getContext(), R.raw.dtmf2, 0));
        map.put(3, spool.load(getContext(), R.raw.dtmf3, 0));
        map.put(4, spool.load(getContext(), R.raw.dtmf4, 0));
        map.put(5, spool.load(getContext(), R.raw.dtmf5, 0));
        map.put(6, spool.load(getContext(), R.raw.dtmf6, 0));
        map.put(7, spool.load(getContext(), R.raw.dtmf7, 0));
        map.put(8, spool.load(getContext(), R.raw.dtmf8, 0));
        map.put(9, spool.load(getContext(), R.raw.dtmf9, 0));
        map.put(11, spool.load(getContext(), R.raw.dtmf11, 0));
        map.put(12, spool.load(getContext(), R.raw.dtmf12, 0));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialNum0:
                play(11);
                input(v.getTag().toString());
                break;
            case R.id.dialNum1:
                play(1);
                input(v.getTag().toString());
                break;
            case R.id.dialNum2:
                play(2);
                input(v.getTag().toString());
                break;
            case R.id.dialNum3:
                play(3);
                input(v.getTag().toString());
                break;
            case R.id.dialNum4:
                play(4);
                input(v.getTag().toString());
                break;
            case R.id.dialNum5:
                play(5);
                input(v.getTag().toString());
                break;
            case R.id.dialNum6:
                play(6);
                input(v.getTag().toString());
                break;
            case R.id.dialNum7:
                play(7);
                input(v.getTag().toString());
                break;
            case R.id.dialNum8:
                play(8);
                input(v.getTag().toString());
                break;
            case R.id.dialNum9:
                play(9);
                input(v.getTag().toString());
                break;
            case R.id.hide:
//                play(11);
                hideKeyboard();
                break;
            case R.id.delete:
                play(12);
                deleteSingleDialCharacter();
                break;
            default:
                break;
        }
    }

    /**
     * 播放按钮点击声音
     *
     * @param id
     */
    private void play(int id) {
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float value = (float) 0.7 / max * current;
        spool.setVolume(spool.play(id, value, value, 0, 0, 1f), value, value);
    }

    private void input(String str) {
        int index = Integer.parseInt(str);
        String num = nums[index];
        String letter = letters[index];
        inputValue += num;
        if(onT9TelephoneDialpadView!=null){
            onT9TelephoneDialpadView.onDialInputTextChanged(inputValue);
        }

//        List<String> indexStr0 = new ArrayList<>();
//        //获取字母
//        for (int i = 0; i < inputValue.length(); i++) {
//            char ch = inputValue.charAt(i);
//            int chInt = Integer.parseInt(String.valueOf(ch));
//            String letter1 = letters[chInt];
//            indexStr0 = splicing(letter1, indexStr0);
//        }
//        Log.e("input", indexStr0.toString());

//        String conversion = conversion(inputValue);

//        contains(conversion,"qiuzhiwen","qzw",inputValue);

//        Log.e("input", conversion);

//        ArrayList<String> allContacts = new ArrayList<>();
//        allContacts.add("邱志文");
//        allContacts.add("中国产品质量电子监管网");
//        allContacts.add("志文邱");
//        allContacts.add("志邱文");
//        allContacts.add("邱文志");
//        allContacts.add("文邱志");
//        allContacts.add("文志邱");
//        ArrayList<String> search = search(inputValue, allContacts, new ArrayList<>());
//
//        Log.e("input", search.toString());
//
//        List<GroupSearchBean> groups = new ArrayList<>();
//
//        GroupSearchBean groupSearchBean0 = new GroupSearchBean("AgentWeb");
//        GroupSearchBean groupSearchBean1 = new GroupSearchBean("爱阅读免费小说");
//        GroupSearchBean groupSearchBean2 = new GroupSearchBean("邱志文");
//        GroupSearchBean groupSearchBean3 = new GroupSearchBean("备忘录");
//        GroupSearchBean groupSearchBean4 = new GroupSearchBean("查找我的");
//        GroupSearchBean groupSearchBean5 = new GroupSearchBean("电子邮件");
//        GroupSearchBean groupSearchBean6 = new GroupSearchBean("华为钱包");
//        GroupSearchBean groupSearchBean7 = new GroupSearchBean("华为商城");
//        GroupSearchBean groupSearchBean8 = new GroupSearchBean("融合通信");
//        GroupSearchBean groupSearchBean9 = new GroupSearchBean("QQ浏览器");
//
//        groups.add(groupSearchBean0);
//        groups.add(groupSearchBean1);
//        groups.add(groupSearchBean2);
//        groups.add(groupSearchBean3);
//        groups.add(groupSearchBean4);
//        groups.add(groupSearchBean5);
//        groups.add(groupSearchBean6);
//        groups.add(groupSearchBean7);
//        groups.add(groupSearchBean8);
//        groups.add(groupSearchBean9);
//
//        List<GroupSearchBean> search = SearchUtil.search(inputValue, SearchUtil.getGroupSearchData(groups));
//
//        logger.info("SearchKeyboardView:"+search);

    }

    public void deleteSingleDialCharacter() {
        if (inputValue.length() > 0) {
            String deleteCharacter = inputValue.substring(inputValue.length() - 1, inputValue.length());
            if (null != onT9TelephoneDialpadView) {
                onT9TelephoneDialpadView.onDeleteDialCharacter(deleteCharacter);
            }
            inputValue=inputValue.substring(0,inputValue.length() - 1);
            if(onT9TelephoneDialpadView!=null){
                onT9TelephoneDialpadView.onDialInputTextChanged(inputValue);
            }
        }
    }




    public void setOnT9TelephoneDialpadView(OnT9TelephoneDialpadView onT9TelephoneDialpadView) {
        this.onT9TelephoneDialpadView = onT9TelephoneDialpadView;
    }

    public interface OnT9TelephoneDialpadView {
        void onDeleteDialCharacter(String deleteCharacter);

        void onDialInputTextChanged(String curCharacter);
    }




    /**************************************************************/



    private String conversion(String str) {

        StringBuffer sb = new StringBuffer();
        //获取每一个数字对应的字母列表并以'-'隔开
        for (int i = 0; i < str.length(); i++) {
            sb.append((str.charAt(i) <= '9' && str.charAt(i) >= '0')
                    ? STRS[str.charAt(i) - '0'] : str.charAt(i));
            if (i != str.length() - 1) {
                sb.append("-");
            }
        }


//        StringBuffer sb = new StringBuffer();
//
//        for (int i = 0; i < num.length(); i++){
//            char ch = num.charAt(i);
//            int chInt = Integer.parseInt(String.valueOf(ch));
//            String letter = letters[chInt];//数字对应的字母
//
//            for (int j = 0; j < letter.length(); j++){
//                char chJ = letter.charAt(i);
//                if(TextUtils.isEmpty(sb.toString())){
//                    sb.append(chJ);
//                }else{
//                    String[] oldStrs = sb.toString().split("-");
//                    for (String oldStr:oldStrs){
//
//                    }
//                }
//
//
//            }
//        }


        return sb.toString();
    }


    /**
     * 查询
     * @param str 输入数字对应的英文
     * @param quanPin 要匹配的数据源 全拼
     * @param initials 要匹配的数据源 首字母缩写
     * @param search 输入的数字
     * @return
     */
    public boolean contains(String str, String quanPin,String initials, String search) {
        if (TextUtils.isEmpty(quanPin) || TextUtils.isEmpty(initials)) {
            return false;
        }

        //先匹配 首字母
        Pattern pattern = Pattern.compile(str.toUpperCase().replace("-", ""), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(initials);
        boolean flag = matcher.find();
        if (flag) {
            String tempStr = matcher.group();
            return flag;
        }

        //全拼 首字母没找到就找全拼
        //根据全拼查询
        Pattern pattern2 = Pattern.compile(str.replace("-", ""), Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(quanPin);
        boolean flag2 = matcher2.find();
        if (flag2) {
            String tempStr = matcher2.group();
        }
        return flag2;


//        //搜索条件大于6个字符将不按拼音首字母查询
//        if (search.length() < 6) {
//            //根据首字母进行模糊查询
//            Pattern pattern = Pattern.compile("^" + str.toUpperCase().replace("-", "[*+#a-z]*"), Pattern.CASE_INSENSITIVE);
//            Matcher matcher = pattern.matcher(model);
//
//            if (matcher.find()) {
//                String tempStr = matcher.group();
//                for (int i = 0; i < tempStr.length(); i++) {
//                    if (tempStr.charAt(i) >= 'A' && tempStr.charAt(i) <= 'Z') {
////                        model.group += tempStr.charAt(i);
//                    }
//                }
//                return true;
//            }
//        }
//
//        //根据全拼查询
//        Pattern pattern = Pattern.compile(str.replace("-", ""), Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(model);
//        boolean flag = matcher.find();
//        if (flag) {
//            String tempStr = matcher.group();
//        }
//        return flag;
    }


    /**
     * 拼接字符
     *
     * @return
     */
    private List<String> splicing(String letter, List<String> oldList) {
        List<String> newStr = new ArrayList<>();
        for (int i = 0; i < letter.length(); i++) {
            char ch = letter.charAt(i);
            if (oldList.size() == 0) {
                newStr.add(String.valueOf(ch));
            } else {
                for (String oldstr : oldList) {
                    oldstr += ch;
                    newStr.add(oldstr);
                }
            }
        }
        return newStr;
    }

    private void hideKeyboard() {
        this.setVisibility(GONE);
    }

    public static boolean contains(String name, String search) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        boolean flag = false;

        // 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
        if (search.length() < 6) {
            String firstLetters = PinyinUtils.getPingYin(name);
            // 不区分大小写
            Pattern firstLetterMatcher = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
            flag = firstLetterMatcher.matcher(firstLetters).find();
        }

        if (!flag) { // 如果简拼已经找到了，就不使用全拼了
            // 全拼匹配
            ChineseSpelling finder = ChineseSpelling.getInstance();
            finder.setResource(name);
            // 不区分大小写
            Pattern pattern2 = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(finder.getSpelling());
            flag = matcher2.find();
        }

        return flag;
    }


    public static ArrayList<String> search(String str, ArrayList<String> allContacts, ArrayList<String> contactList) {

        contactList.clear();
        // 如果搜索条件以0 1 +开头则按号码搜索
//        if (str.startsWith("0") || str.startsWith("1") || str.startsWith("+")) {
//            for (Contact contact : allContacts) {
//                if (contact.getNumber() != null && contact.getName() != null) {
//                    if (contact.getNumber().contains(str)
//                            || contact.getName().contains(str)) {
//                        contact.setGroup(str);
//                        contactList.add(contact);
//                    }
//                }
//            }
//            return contactList;
//        }
        ChineseSpelling finder = ChineseSpelling.getInstance();

        String result = "";
        for (String contact : allContacts) {
            // 先将输入的字符串转换为拼音
            finder.setResource(str);
            result = finder.getSpelling();
            if (contains(contact, result)) {
                contactList.add(contact);
            } else if (contact.contains(str)) {
//                contact.setGroup(str);
                contactList.add(contact);
            }
        }
        return contactList;
    }

}
