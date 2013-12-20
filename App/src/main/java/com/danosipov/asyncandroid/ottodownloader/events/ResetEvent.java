package com.danosipov.asyncandroid.ottodownloader.events;

import android.view.View;

public class ResetEvent {
    private View view;

    public ResetEvent(View v) {
        view = v;
    }

    public View getView() {
        return view;
    }
}
