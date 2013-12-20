package com.danosipov.asyncandroid.ottodownloader.events;

import android.view.View;

public class DownloadResumeEvent {
    private View view;

    public DownloadResumeEvent(View v) {
        view = v;
    }

    public View getView() {
        return view;
    }
}
