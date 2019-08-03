package com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vlonjat Gashi (vlonjatg)
 */
public class LceLayout extends RelativeLayout {

    private static final String TAG_LOADING = "LceLayout.TAG_LOADING";
    private static final String TAG_EMPTY = "LceLayout.TAG_EMPTY";
    private static final String TAG_ERROR = "LceLayout.TAG_ERROR";

    final String CONTENT = "type_content";
    final String LOADING = "type_loading";
    final String EMPTY = "type_empty";
    final String ERROR = "type_error";

    LayoutInflater inflater;
    View view;
    LayoutParams layoutParams;
    Drawable currentBackground;

    List<View> contentViews = new ArrayList<>();

    RelativeLayout loadingStateRelativeLayout;
    ProgressBar loadingStateProgressBar;

    RelativeLayout emptyStateRelativeLayout;
    ImageView emptyStateImageView;
    TextView emptyStateTitleTextView;
    TextView emptyStateContentTextView;

    LinearLayout errorStateRelativeLayout;
    ImageView errorStateImageView;
    TextView errorStateTitleTextView;
    TextView errorStateContentTextView;

    int loadingStateProgressBarWidth;
    int loadingStateProgressBarHeight;
    int loadingStateBackgroundColor;

    int emptyStateImageWidth;
    int emptyStateImageHeight;
    int emptyStateTitleTextSize;
    int emptyStateContentTextSize;
    int emptyStateTitleTextColor;
    int emptyStateContentTextColor;
    int emptyStateBackgroundColor;

    int errorStateImageWidth;
    int errorStateImageHeight;
    int errorStateTitleTextSize;
    int errorStateContentTextSize;
    int errorStateTitleTextColor;
    int errorStateContentTextColor;
    int errorStateButtonTextColor;
    int errorStateBackgroundColor;

    private String state = CONTENT;
//    private Button errorStateButton_refrash;//errorStateButton
    private OnClickListener onErrorButtonClickListener = null;//error button 点击 事件


    //空页面属性
    private Button emptyStateButton_refrash;
    private TextView empty_text;
    private ImageView empty_img;

    private String emptyButtonText;
    private CharSequence emptyText;
    private String emptyText2;
    private String emptyText3;
    private int emptyImg = R.drawable.lce_default_empty_icon;
    private OnClickListener onEmptyButtonClickListener = null;//空 button 点击 事件
    private int isshowEmptyStateButton = GONE;
    private TextView empty_text2;
    private TextView empty_text3;
    private int isEmptyText2IsShow= GONE;
    private int isEmptyText3IsShow= GONE;

    private OnClickListener onEmptyTextClickListener = null;//空 TextView 点击 事件

    public LceLayout(Context context) {
        super(context);
    }

    public LceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LceLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressActivity);

        //Loading state attrs
        loadingStateProgressBarWidth =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_loadingProgressBarWidth, 250);

        loadingStateProgressBarHeight =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_loadingProgressBarHeight, 250);

        loadingStateBackgroundColor =
                typedArray.getColor(R.styleable.ProgressActivity_loadingBackgroundColor, Color.TRANSPARENT);

        //Empty state attrs
        emptyStateImageWidth =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_emptyImageWidth, 308);

        emptyStateImageHeight =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_emptyImageHeight, 308);

        emptyStateTitleTextSize =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_emptyTitleTextSize, 15);

        emptyStateContentTextSize =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_emptyContentTextSize, 14);

        emptyStateTitleTextColor =
                typedArray.getColor(R.styleable.ProgressActivity_emptyTitleTextColor, Color.BLACK);

        emptyStateContentTextColor =
                typedArray.getColor(R.styleable.ProgressActivity_emptyContentTextColor, Color.BLACK);

        emptyStateBackgroundColor =
                typedArray.getColor(R.styleable.ProgressActivity_emptyBackgroundColor, Color.TRANSPARENT);

        //Error state attrs
        errorStateImageWidth =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_errorImageWidth, 308);

        errorStateImageHeight =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_errorImageHeight, 308);

        errorStateTitleTextSize =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_errorTitleTextSize, 15);

        errorStateContentTextSize =
                typedArray.getDimensionPixelSize(R.styleable.ProgressActivity_errorContentTextSize, 14);

        errorStateTitleTextColor =
                typedArray.getColor(R.styleable.ProgressActivity_errorTitleTextColor, Color.BLACK);

        errorStateContentTextColor =
                typedArray.getColor(R.styleable.ProgressActivity_errorContentTextColor, Color.BLACK);

        errorStateButtonTextColor =
                typedArray.getColor(R.styleable.ProgressActivity_errorButtonTextColor, Color.BLACK);

        errorStateBackgroundColor =
                typedArray.getColor(R.styleable.ProgressActivity_errorBackgroundColor, Color.TRANSPARENT);

        typedArray.recycle();

        currentBackground = this.getBackground();
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        if (child.getTag() == null || (!child.getTag().equals(TAG_LOADING) &&
                !child.getTag().equals(TAG_EMPTY) && !child.getTag().equals(TAG_ERROR))) {

            contentViews.add(child);
        }
    }

    /**
     * Hide all other states and show content
     */
    public void showContent() {
        switchState(CONTENT, Collections.<Integer>emptyList());
    }

    /**
     * Hide all other states and show content
     *
     * @param skipIds Ids of views not to show
     */
    public void showContent(List<Integer> skipIds) {
        switchState(CONTENT, skipIds);
    }

    /**
     * Hide content and show the progress bar
     */
    public void showLoading() {
        switchState(LOADING, Collections.<Integer>emptyList());
    }

    /**
     * Hide content and show the progress bar
     *
     * @param skipIds Ids of views to not hide
     */
    public void showLoading(List<Integer> skipIds) {
        switchState(LOADING, skipIds);
    }

    /**
     * Show empty view when there are not data to show
     */
    public void showEmpty() {
        switchState(EMPTY, Collections.<Integer>emptyList());
    }

    /**
     * Show empty view when there are not data to show
     *
     * @param skipIds Ids of views to not hide
     */
    public void showEmpty(List<Integer> skipIds) {
        switchState(EMPTY, skipIds);
    }

    /**
     * Show error view with a button when something goes wrong and prompting the user to try again
     */
    public void showError() {
        switchState(ERROR, Collections.<Integer>emptyList());
    }

    /**
     * Show error view with a button when something goes wrong and prompting the user to try again
     *
     * @param skipIds Ids of views to not hide
     */
    public void showError(List<Integer> skipIds) {
        switchState(ERROR, skipIds);
    }

    /**
     * Get which state is set
     *
     * @return State
     */
    public String getState() {
        return state;
    }

    /**
     * Check if content is shown
     *
     * @return boolean
     */
    public boolean isContent() {
        return state.equals(CONTENT);
    }

    /**
     * Check if loading state is shown
     *
     * @return boolean
     */
    public boolean isLoading() {
        return state.equals(LOADING);
    }

    /**
     * Check if empty state is shown
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return state.equals(EMPTY);
    }

    /**
     * Check if error state is shown
     *
     * @return boolean
     */
    public boolean isError() {
        return state.equals(ERROR);
    }

    private void switchState(String state, List<Integer> skipIds) {
        this.state = state;
        switch (state) {
            case CONTENT:
                //Hide all state views to display content
                hideLoadingView();
                hideEmptyView();
                hideErrorView();

                setContentVisibility(true, skipIds);
                break;
            case LOADING:
                hideEmptyView();
                hideErrorView();

                setLoadingView();
                setContentVisibility(false, skipIds);
                break;
            case EMPTY:
                hideLoadingView();
                hideErrorView();

                setEmptyView();
                emptyStateButton_refrash.setVisibility(isshowEmptyStateButton);
                empty_text2.setVisibility(isEmptyText2IsShow);
                empty_text3.setVisibility(isEmptyText3IsShow);
                emptyStateButton_refrash.setText(emptyButtonText);
                emptyStateButton_refrash.setOnClickListener(onEmptyButtonClickListener);
                if (!TextUtils.isEmpty(emptyText)){
                    empty_text.setText(emptyText);
                    empty_text.setOnClickListener(onEmptyTextClickListener);
                }

                if (!TextUtils.isEmpty(emptyText2))
                    empty_text2.setText(emptyText2);
                if (!TextUtils.isEmpty(emptyText3))
                    empty_text3.setText(emptyText3);
                empty_img.setImageResource(emptyImg);

                setContentVisibility(false, skipIds);
                break;
            case ERROR:
                hideLoadingView();
                hideEmptyView();

                setErrorView();
//                errorStateButton.setOnClickListener(null);
                errorStateRelativeLayout.setOnClickListener(onErrorButtonClickListener);
                setContentVisibility(false, skipIds);
                break;
        }
    }

    private void setLoadingView() {
        if (loadingStateRelativeLayout == null) {
            view = inflater.inflate(R.layout.progress_loading_view, null);
            loadingStateRelativeLayout = (RelativeLayout) view.findViewById(R.id.loadingStateRelativeLayout);
            loadingStateRelativeLayout.setTag(TAG_LOADING);

//            loadingStateProgressBar = (ProgressBar) view.findViewById(R.id.loadingStateProgressBar);
////            dots = (DotsTextView) view.findViewById(R.id.dots);
////            dots.start();
//
//            loadingStateProgressBar.getLayoutParams().width = loadingStateProgressBarWidth;
//            loadingStateProgressBar.getLayoutParams().height = loadingStateProgressBarHeight;
//            loadingStateProgressBar.requestLayout();

            //Set background color if not TRANSPARENT
            if (loadingStateBackgroundColor != Color.TRANSPARENT) {
                this.setBackgroundColor(loadingStateBackgroundColor);
            }

            layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(CENTER_IN_PARENT);

            addView(loadingStateRelativeLayout, layoutParams);
        } else {
            loadingStateRelativeLayout.setVisibility(VISIBLE);
        }
    }


    private void setEmptyView() {
        if (emptyStateRelativeLayout == null) {
            view = inflater.inflate(R.layout.progress_empty_view, null);
            emptyStateRelativeLayout = (RelativeLayout) view.findViewById(R.id.emptyStateRelativeLayout);
            emptyStateRelativeLayout.setTag(TAG_EMPTY);

            emptyStateButton_refrash = (Button) view.findViewById(R.id.emptyStateButton_refrash);
            empty_text = (TextView) view.findViewById(R.id.empty_text);
            empty_text2 = (TextView) view.findViewById(R.id.empty_text2);
            empty_text3 = (TextView) view.findViewById(R.id.empty_text3);
            empty_img = (ImageView) view.findViewById(R.id.empty_img);

            if (emptyStateBackgroundColor != Color.TRANSPARENT) {
                emptyStateRelativeLayout.setBackgroundResource(emptyStateBackgroundColor);
            }

            layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(CENTER_IN_PARENT);
            addView(emptyStateRelativeLayout, layoutParams);


        } else {
            emptyStateRelativeLayout.setVisibility(VISIBLE);
        }
    }

    //显示 空刷新button
    public void setEmptyText2IsShow() {
        isEmptyText2IsShow = VISIBLE;
//        emptyStateButton_refrash.setVisibility(View.VISIBLE);
    }
    //显示 空刷新button
    public void setEmptyText3IsShow() {
        isEmptyText3IsShow = VISIBLE;
//        emptyStateButton_refrash.setVisibility(View.VISIBLE);
    }

    //显示 空刷新button
    public void setEmptyButtonIsShow() {
        isshowEmptyStateButton = VISIBLE;
//        emptyStateButton_refrash.setVisibility(View.VISIBLE);
    }

    public void setEmptyButtonText(String emptyButtonText) {
        this.emptyButtonText = emptyButtonText;
    }

    public void setEmptyStateBackgroundColor(int emptyStateBackgroundColor) {
        this.emptyStateBackgroundColor = emptyStateBackgroundColor;
    }

    public void setErrorStateBackgroundColor(int errorStateBackgroundColor) {
        this.errorStateBackgroundColor = errorStateBackgroundColor;
    }

    public void setEmptyButtonClickListener(OnClickListener onEmptyButtonClickListener) {
//        emptyStateButton_refrash.setOnClickListener(onEmptyButtonClickListener);
        this.onEmptyButtonClickListener = onEmptyButtonClickListener;
    }

    public void setEmptyTextClickListener(OnClickListener onEmptyTextClickListener) {
//        emptyStateButton_refrash.setOnClickListener(onEmptyButtonClickListener);
        this.onEmptyTextClickListener = onEmptyTextClickListener;
    }

    public void setEmptyText(CharSequence emptyText) {
        this.emptyText = emptyText;
    }

    public void setEmptyText2(String emptyText2) {
        this.emptyText2 = emptyText2;
    }

    public void setEmptyText3(String emptyText3) {
        this.emptyText3 = emptyText3;
    }

    public void setEmptyImage(int rImg) {
        emptyImg = rImg;
//        empty_img.setImageResource(rImg);
    }


    private void setErrorView() {
        if (errorStateRelativeLayout == null) {
            view = inflater.inflate(R.layout.progress_error_view, null);
            errorStateRelativeLayout = (LinearLayout) view.findViewById(R.id.errorStateRelativeLayout);
            errorStateRelativeLayout.setTag(TAG_ERROR);
//
//            errorStateImageView = (ImageView) view.findViewById(R.id.errorStateImageView);
//            errorStateTitleTextView = (TextView) view.findViewById(R.id.errorStateTitleTextView);
//            errorStateContentTextView = (TextView) view.findViewById(R.id.errorStateContentTextView);
//            errorStateButton = (Button) view.findViewById(R.id.errorStateButton);
//            errorStateButton_refrash = (Button) view.findViewById(R.id.errorStateButton_refrash);
//
//            //Set error state image width and height
//            errorStateImageView.getLayoutParams().width = errorStateImageWidth;
//            errorStateImageView.getLayoutParams().height = errorStateImageHeight;
//            errorStateImageView.requestLayout();
//
//            errorStateTitleTextView.setTextSize(errorStateTitleTextSize);
//            errorStateContentTextView.setTextSize(errorStateContentTextSize);
//            errorStateTitleTextView.setTextColor(errorStateTitleTextColor);
//            errorStateContentTextView.setTextColor(errorStateContentTextColor);
//            errorStateButton.setTextColor(errorStateButtonTextColor);
//
//            //Set background color if not TRANSPARENT
            if (errorStateBackgroundColor != Color.TRANSPARENT) {
                errorStateRelativeLayout.setBackgroundResource(errorStateBackgroundColor);
            }
//
            layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(CENTER_IN_PARENT);

            addView(errorStateRelativeLayout, layoutParams);
        } else {
            errorStateRelativeLayout.setVisibility(VISIBLE);
        }
    }


    public void setErrorButtonClickListener(OnClickListener onErrorButtonClickListener) {
        this.onErrorButtonClickListener = onErrorButtonClickListener;
    }


    private void setContentVisibility(boolean visible, List<Integer> skipIds) {
        for (View v : contentViews) {
            if (!skipIds.contains(v.getId())) {
                v.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void hideLoadingView() {
        if (loadingStateRelativeLayout != null) {
            loadingStateRelativeLayout.setVisibility(GONE);

            //Restore the background color if not TRANSPARENT
            if (loadingStateBackgroundColor != Color.TRANSPARENT) {
                this.setBackgroundDrawable(currentBackground);
            }
        }
    }

    private void hideEmptyView() {
        if (emptyStateRelativeLayout != null) {
            emptyStateRelativeLayout.setVisibility(GONE);

            //Restore the background color if not TRANSPARENT
            if (emptyStateBackgroundColor != Color.TRANSPARENT) {
                this.setBackgroundDrawable(currentBackground);
            }
        }
    }

    private void hideErrorView() {
        if (errorStateRelativeLayout != null) {
            errorStateRelativeLayout.setVisibility(GONE);

            //Restore the background color if not TRANSPARENT
            if (errorStateBackgroundColor != Color.TRANSPARENT) {
                this.setBackgroundDrawable(currentBackground);
            }

        }
    }
}