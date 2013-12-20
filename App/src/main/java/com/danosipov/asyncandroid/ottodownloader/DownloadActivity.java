package com.danosipov.asyncandroid.ottodownloader;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danosipov.asyncandroid.ottodownloader.events.DownloadPauseEvent;
import com.danosipov.asyncandroid.ottodownloader.events.DownloadProgressEvent;
import com.danosipov.asyncandroid.ottodownloader.events.DownloadResumeEvent;
import com.danosipov.asyncandroid.ottodownloader.events.DownloadStartEvent;
import com.danosipov.asyncandroid.ottodownloader.events.ResetEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class DownloadActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DownloadFragment())
                    .commit();
        }

    }

    private Bus getEventBus() {
        return ((DownloadApplication) getApplication()).getBus();
    }

    public static class DownloadFragment extends Fragment {

        private ProgressBar progressBar;
        private EditText urlEditText;
        private Downloader downloadThread;
        private TextView downloadProgress;

        private View.OnClickListener handleReset = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEventBus().post(new ResetEvent(v));
            }
        };
        private View.OnClickListener handleDownload = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEventBus().post(new DownloadStartEvent(v));
            }
        };
        private View.OnClickListener handlePause = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEventBus().post(new DownloadPauseEvent(v));
            }
        };
        private View.OnClickListener handleResume = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEventBus().post(new DownloadResumeEvent(v));
            }
        };

        public DownloadFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_download, container, false);

            progressBar = ((ProgressBar) rootView.findViewById(R.id.downloadProgressBar));
            urlEditText = ((EditText) rootView.findViewById(R.id.urlEditText));
            downloadProgress = ((TextView) rootView.findViewById(R.id.downloadProgressTextView));

            // See how to better handle this code:
            Button resetButton = ((Button) rootView.findViewById(R.id.resetButton));
            resetButton.setOnClickListener(handleReset);
            Button downloadButton = ((Button) rootView.findViewById(R.id.downloadButton));
            downloadButton.setOnClickListener(handleDownload);


            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            getEventBus().register(this);
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onDestroy() {
            getEventBus().unregister(this);
            super.onDestroy();
        }

        private Bus getEventBus() {
            return ((DownloadApplication) getActivity().getApplication()).getBus();
        }

        @Subscribe
        public void answerProgress(DownloadProgressEvent event) {
            progressBar.setProgress((int) event.getProgress());
            downloadProgress.setText(String.format("%s / %s", event.getLoadedBytes(), event.getTotalBytes()));
        }

        @Subscribe
        public void answerDownloadStart(DownloadStartEvent event) {
            downloadThread = new Downloader(urlEditText.getText().toString(), new Downloader.DownloadProgressReport() {
                @Override
                public void reportProgress(final long loaded, final long total) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getEventBus().post(new DownloadProgressEvent(loaded, total));
                        }
                    });
                }

                @Override
                public void reset() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switchToDownload(((Button) getView().findViewById(R.id.downloadButton)));
                        }
                    });
                }
            });
            downloadThread.start();
            switchToPause(((Button) event.getView()));
        }

        @Subscribe
        public void answerDownloadPause(DownloadPauseEvent event) {
            downloadThread.pause(true);
            switchToResume(((Button) event.getView()));
        }

        @Subscribe
        public void answerDownloadResume(DownloadResumeEvent event) {
            downloadThread.pause(false);
            switchToPause(((Button) event.getView()));
        }

        @Subscribe
        public void answerReset(ResetEvent event) {
            if (downloadThread != null && downloadThread.isAlive()) {
                downloadThread.kill();
            }
            switchToDownload(((Button) this.getView().findViewById(R.id.downloadButton)));
        }

        private void switchToPause(Button downloadButton) {
            downloadButton.setText(getString(R.string.pause));
            downloadButton.setOnClickListener(handlePause);
        }

        private void switchToResume(Button downloadButton) {
            downloadButton.setText(getString(R.string.resume));
            downloadButton.setOnClickListener(handleResume);
        }

        private void switchToDownload(Button downloadButton) {
            downloadButton.setText(getString(R.string.download));
            downloadButton.setOnClickListener(handleDownload);
            downloadProgress.setText(getString(R.string.zero_bytes));
            progressBar.setProgress(0);
        }
    }
}
