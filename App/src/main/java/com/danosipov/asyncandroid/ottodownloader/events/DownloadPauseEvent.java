package com.danosipov.asyncandroid.ottodownloader.events;

import android.view.View;

public class DownloadPauseEvent {
    private View view;

    public DownloadPauseEvent(View v) {
        view = v;
    }

    public View getView() {
        return view;
    }
}
