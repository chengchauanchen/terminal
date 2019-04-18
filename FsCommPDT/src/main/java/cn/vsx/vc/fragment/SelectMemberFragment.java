package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.vc.R;
import cn.vsx.vc.view.TabView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectMemberFragment extends Fragment implements View.OnClickListener{

    private LinearLayout mLlSelected;
    private RecyclerView mCatalogRecyclerview;
    private ImageView mIvSelect;
    private TabView mTabPc;
    private TabView mTabPolice;
    private TabView mTabRecoder;
    private TabView mTabUav;
    private LinearLayout mLlLayoutSearch;
    private ImageView mIvSearch;
    private TextView mRecoderTv;
    private FrameLayout mContactsViewPager;
    private MemberListFragment lastFragment;
    private MemberListFragment pcFragment;
    private MemberListFragment policeFragment;
    private MemberListFragment recoderFragment;
    private MemberListFragment uavFragment;

    public SelectMemberFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.layout_select_member, container, false);
        findView(view);
        initTab();
        initListener();
        switchFragment(0);
        return view;
    }

    private void findView(View view){
        mLlSelected = view.findViewById(R.id.ll_selected);
        mCatalogRecyclerview = view.findViewById(R.id.catalog_recyclerview);
        mIvSelect = view.findViewById(R.id.iv_select);
        mTabPc = view.findViewById(R.id.tab_pc);
        mTabPolice = view.findViewById(R.id.tab_police);
        mTabRecoder = view.findViewById(R.id.tab_recoder);
        mTabUav = view.findViewById(R.id.tab_uav);
        mLlLayoutSearch = view.findViewById(R.id.ll_layout_search);
        mIvSearch = view.findViewById(R.id.iv_search);
        mRecoderTv = view.findViewById(R.id.recoder_tv);
        mContactsViewPager = view.findViewById(R.id.contacts_viewPager);
    }



    private void initTab(){
        mTabPc.setChecked(true);
        mTabPolice.setChecked(false);
        mTabRecoder.setChecked(false);
        mTabUav.setChecked(false);
    }

    private void initListener(){
        mTabPc.setOnClickListener(this);
        mTabPolice.setOnClickListener(this);
        mTabRecoder.setOnClickListener(this);
        mTabUav.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.tab_pc:
                mTabPc.setChecked(true);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(false);
                switchFragment(0);
                break;
            case R.id.tab_police:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(true);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(false);
                switchFragment(1);
                break;
            case R.id.tab_recoder:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(true);
                mTabUav.setChecked(false);
                switchFragment(2);
                break;
            case R.id.tab_uav:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(true);
                switchFragment(3);
                break;
            default:
                break;
        }
    }

    private void switchFragment(int position){
        switch(position){
            case 0:
                if(pcFragment == null){
                    pcFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PC.toString());
                }
                if(pcFragment.isAdded()){
                    if(lastFragment !=null){

                        getFragmentManager().beginTransaction().hide(lastFragment).show(pcFragment).commit();
                    }else {
                        getFragmentManager().beginTransaction().show(pcFragment).commit();
                    }
                }else {
                    if(lastFragment !=null){
                        getFragmentManager().beginTransaction().hide(lastFragment).add(R.id.contacts_viewPager, pcFragment).show(pcFragment).commit();
                    }else {
                        getFragmentManager().beginTransaction().add(R.id.contacts_viewPager, pcFragment).show(pcFragment).commit();
                    }
                }
                lastFragment = pcFragment;
                break;
            case 1:
                if(policeFragment == null){
                    policeFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PHONE.toString());
                }
                if(policeFragment.isAdded()){
                    if(lastFragment !=null){
                        getFragmentManager().beginTransaction().hide(lastFragment).show(policeFragment).commit();
                    }else {
                        getFragmentManager().beginTransaction().show(policeFragment).commit();
                    }
                }else {
                    if(lastFragment !=null){
                        getFragmentManager().beginTransaction().hide(lastFragment).add(R.id.contacts_viewPager, policeFragment).show(policeFragment).commit();
                    }else {
                        getFragmentManager().beginTransaction().add(R.id.contacts_viewPager, policeFragment).show(policeFragment).commit();
                    }
                }
                lastFragment = policeFragment;
                break;
            case 2:
                if(recoderFragment == null){
                    recoderFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
                }
                if(recoderFragment.isAdded()){
                    if(lastFragment != null){
                        getFragmentManager().beginTransaction().hide(lastFragment).show(recoderFragment).commit();
                    }else {
                        getFragmentManager().beginTransaction().show(recoderFragment).commit();
                    }
                }else {
                    if(lastFragment !=null){
                        getFragmentManager().beginTransaction().hide(lastFragment).add(R.id.contacts_viewPager, recoderFragment).show(recoderFragment).commit();
                    }else {
                        getFragmentManager().beginTransaction().add(R.id.contacts_viewPager, recoderFragment).show(recoderFragment).commit();

                    }
                }
                lastFragment = recoderFragment;
                break;
            case 3:
                if(uavFragment == null){
                    uavFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_UAV.toString());
                }
                if(uavFragment.isAdded()){
                    if(lastFragment !=null){
                        getFragmentManager().beginTransaction().hide(lastFragment).show(uavFragment).commit();
                    }else {
                        getFragmentManager().beginTransaction().show(uavFragment).commit();
                    }
                }else {
                    if(lastFragment !=null){
                        getFragmentManager().beginTransaction().hide(lastFragment).add(R.id.contacts_viewPager, uavFragment).show(uavFragment).commit();
                    }else{
                        getFragmentManager().beginTransaction().add(R.id.contacts_viewPager, uavFragment).show(uavFragment).commit();
                    }
                }
                lastFragment = uavFragment;
                break;
            default:
                break;
        }
    }
}
