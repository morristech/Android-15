package com.huy.monthlyfinance.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huy.monthlyfinance.Listener.MainListener;

/**
 * Created by Phuong on 25/08/2016.
 */
public abstract class BaseFragment extends Fragment {
    protected MainListener mListener;

    protected abstract int getLayoutXML();
    protected abstract void initUI(View view);
    protected abstract void setStatusBarColor();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutXML(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mListener != null) {
            setStatusBarColor();
        }
    }

    public void setListener(MainListener Listener) {
        this.mListener = Listener;
    }
}