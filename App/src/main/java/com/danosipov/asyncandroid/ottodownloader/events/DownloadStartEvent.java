package com.danosipov.asyncandroid.ottodownloader.events;

import android.view.View;

public class DownloadStartEvent {
    private View view;

    public DownloadStartEvent(View v) {
        view = v;
    }

    public View getView() {
        return view;
    }
}
