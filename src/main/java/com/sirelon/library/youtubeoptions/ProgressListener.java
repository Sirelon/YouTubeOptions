package com.sirelon.library.youtubeoptions;

public interface ProgressListener {
    void onProgress(double progress);

    void onComplete(boolean success);
}
