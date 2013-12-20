package com.danosipov.asyncandroid.ottodownloader;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Downloader extends Thread {
    private String url;
    private long totalSize;
    private long loadedSize;
    private DownloadProgressReport callback;
    private volatile boolean running = true;

    public Downloader(String url, DownloadProgressReport reportCallback) {
        this.url = url;
        callback = reportCallback;
    }

    public String loadInBackground() {
        try {
            URL toDownload = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) toDownload.openConnection();

            urlConnection.setRequestMethod("GET");
            // Causes issues on ICS:
            //urlConnection.setDoOutput(true);
            urlConnection.connect();

            File sdCardRoot = Environment.getExternalStorageDirectory();
            File outFile = new File(sdCardRoot, "outfile");
            FileOutputStream fileOutput = new FileOutputStream(outFile);

            InputStream inputStream = urlConnection.getInputStream();
            totalSize = urlConnection.getContentLength();
            loadedSize = 0;

            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                while(!running) {
                    Thread.sleep(500);
                }
                fileOutput.write(buffer, 0, bufferLength);
                loadedSize += bufferLength;
                reportProgress();
            }

            fileOutput.close();
        } catch (MalformedURLException e) {
            // TODO: handle failure gracefully
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: handle failure gracefully
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO: restart download?
            e.printStackTrace();
        }
        return "outfile";
    }

    private void reportProgress() {
        callback.reportProgress(loadedSize, totalSize);
    }

    @Override
    public void run() {
        loadInBackground();
    }

    public void pause(boolean pause) {
        running = !pause;
    }

    public interface DownloadProgressReport {
        public void reportProgress(long loaded, long total);
    }
}
