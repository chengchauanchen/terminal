package com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager;

import android.view.View;

/**
 * Created by Administrator on 2017/7/17 0017.
 */

public class BuilderLceLayout {
    //Empty  空页面
    private LceLayout lceLayout;

    public BuilderLceLayout(LceLayout lceLayout) {
        this.lceLayout = lceLayout;
    }

    //设置空背景
    public BuilderLceLayout setEmptyStateBackgroundColor(int emptyStateBackgroundColor){
        lceLayout.setEmptyStateBackgroundColor(emptyStateBackgroundColor);
        return this;
    }

    //设置错误背景
    public BuilderLceLayout setErrorStateBackgroundColor(int errorStateBackgroundColor){
        lceLayout.setErrorStateBackgroundColor(errorStateBackgroundColor);
        return this;
    }

    //显示 空刷新button
    public BuilderLceLayout setEmptyButtonIsShow(){
        lceLayout.setEmptyButtonIsShow();
        return this;
    }
    //显示 空刷新button
    public BuilderLceLayout setEmptyText2IsShow(){
        lceLayout.setEmptyText2IsShow();
        return this;
    }

    //显示 空刷新button
    public BuilderLceLayout setEmptyText3IsShow(){
        lceLayout.setEmptyText3IsShow();
        return this;
    }
    //刷新button 文字
    public BuilderLceLayout setEmptyButtonText(String emptyButtonText ){
        lceLayout.setEmptyButtonText(emptyButtonText);
        return this;
    }
    //空点击
    public BuilderLceLayout setEmptyButtonClickListener(View.OnClickListener onEmptyButtonClickListener ){
        lceLayout.setEmptyButtonClickListener(onEmptyButtonClickListener);
        return this;
    }

    //空文字点击
    public BuilderLceLayout setEmptyTextClickListener(View.OnClickListener onEmptyTextClickListener ){
        lceLayout.setEmptyTextClickListener(onEmptyTextClickListener);
        return this;
    }

    //空文子提示
    public BuilderLceLayout setEmptyText(CharSequence emptyText ){
        lceLayout.setEmptyText(emptyText);
        return this;
    }
    //空文子提示
    public BuilderLceLayout setEmptyText2(String emptyText2 ){
        lceLayout.setEmptyText2(emptyText2);
        return this;
    }

    //空文子提示
    public BuilderLceLayout setEmptyText3(String emptyText3 ){
        lceLayout.setEmptyText3(emptyText3);
        return this;
    }

    //空图片
    public BuilderLceLayout setEmptyImage(int rImg){
        lceLayout.setEmptyImage(rImg);
        return this;
    }

    //错误 button 点击事件
    public BuilderLceLayout setErrorButtonClickListener(View.OnClickListener onErrorButtonClickListener ){
        lceLayout.setErrorButtonClickListener(onErrorButtonClickListener);
        return this;
    }

    public LceLayout create(){
        return lceLayout;
    }


}
