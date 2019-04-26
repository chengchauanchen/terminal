package cn.vsx.vc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.log4j.Logger;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {
	
	protected Context context;
    protected View mRootView;
    public Logger logger = Logger.getLogger(getClass());
   
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mRootView =inflater.inflate(getContentViewId(),container,false);  
		
		ButterKnife.bind(this,mRootView);//绑定framgent  
        
        initView();
        
        initData();
        
		initListener();
		
		return mRootView;
	}

	@Override
	public void onAttach(Context context){
		super.onAttach(context);
		this.context = getActivity();
	}

	@Override
	public void onDetach(){
		super.onDetach();
		context = null;
	}

	/**
	 * 获取当前界面的布局
	 */
	public abstract int getContentViewId();
	
	/**
	 * 初始化界面
	 */
	public abstract void initView();
	
	/**
	 * 给控件添加监听
	 */
	public abstract void initListener();

	/** 解注册监听 */
	public  void unRegistListener (){}
	
	/**
	 * 初始化数据 给控件填充内容
	 */
	public abstract void initData();
	


	@Override  
    public void onDestroyView() {  
		ButterKnife.unbind(this);//解绑
		unRegistListener();
//		RefWatcher refWatcher = MyApplication.getRefWatcher(getActivity());
//		refWatcher.watch(this);
        super.onDestroyView();  
    }

	// 处理frame中的键盘事件,目前主要是适配F25手机
	@SuppressWarnings("unused")
	public void onMyKeyDown(KeyEvent event) {
		logger.info("BaseFragment onKeyDown ");
	}

}
