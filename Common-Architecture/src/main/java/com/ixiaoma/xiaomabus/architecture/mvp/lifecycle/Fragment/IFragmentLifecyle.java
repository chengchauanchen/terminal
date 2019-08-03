package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by win on 2018/3/20.
 */

public interface IFragmentLifecyle<V,P> {
    void onCreate(@Nullable Bundle savedInstanceState);

    View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    void onViewCreated(View view, @Nullable Bundle savedInstanceState);

    void onSaveInstanceState(Bundle outState);

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroyView();

    void onDestroy();
}
