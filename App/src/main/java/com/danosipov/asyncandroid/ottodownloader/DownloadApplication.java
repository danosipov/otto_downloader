package com.danosipov.asyncandroid.ottodownloader;

import android.app.Application;

import com.squareup.otto.Bus;

/**
 * Main application class
 */
public class DownloadApplication extends Application {
    private Bus eventBus;

    public Bus getBus() {
        if (eventBus == null) {
            eventBus = new Bus();
        }

        return eventBus;
    }
}
