package com.sirelon.library.youtubeoptions;

import android.content.Context;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by romanishin on 22.02.16.
 */
public class YoutubeFunctionality {
    public static final String TAG = "YoutubeFunctionality";

    private final Context mContext;

    public YoutubeFunctionality(Context context) {
        mContext = context;
    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    public Credential getCredential() {

//        Credential credential = new GoogleCredential();
//        credential.setAccessToken("ya29.ngL_CnXdjkl2Ug1XC4FLMtE9or9S4M3EZf0s8dz_PyMCRPwbIlupJ9ERjWZNH8v2lA");
//        credential.setExpiresInSeconds(29000L);
//        credential.setRefreshToken("1/vQbP7U0sXvydjeeDiF-k2qF93E2EciLC1buKHzKmSpoMEudVrK5jSpoR30zcRFq6");


        BufferedReader reader = null;
        GoogleClientSecrets load = null;
        try {
            InputStream inputStream = mContext.getAssets().open("client_json_web.json");

            reader = new BufferedReader(new InputStreamReader(inputStream));

            load = GoogleClientSecrets.load(new JacksonFactory(), reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Credential credential1 = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(new JacksonFactory())
                .setClientSecrets(load)
                .build();
        credential1.setRefreshToken("1/vh2zAOVE2cwM9SWtGiF2Y_Dtd-zhC3G1aA4h_21yLww");


//        try {
//            Log.d(TAG, "credential1.refreshToken():" + credential1.refreshToken());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Log.d(TAG, "" + credential1.getAccessToken());
        Log.e(TAG, "credential1.getExpiresInSeconds():" + credential1.getExpiresInSeconds());

        return credential1;
    }

    public String uploadVideo(String videoPath, List<String> tags, final ProgressListener onProgressListener) throws IOException {
        Credential credential = getCredential();

        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                credential
        ).setApplicationName("Spit16").build();

        Video video = new Video();
        VideoSnippet videoSnippet = new VideoSnippet();
        videoSnippet.setTitle("Spit16 A&R Selection");

        videoSnippet.setDescription(
                "Take a look for it!");

        // Set the keyword tags that you want to associate with the video.
        if (tags == null)
            tags = new ArrayList<String>();

        tags.add("spit16");
        tags.add("rap");

        // Set hashtag from spit16 video
        videoSnippet.setTags(tags);

        video.setSnippet(videoSnippet);

        File file = new File(videoPath);

        InputStreamContent mediaContent = new InputStreamContent("video/mp4", new FileInputStream(file));

        mediaContent.setLength(file.length());

        YouTube.Videos.Insert insert = youtube.videos().insert("snippet", video, mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();

        MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {

            public void progressChanged(MediaHttpUploader uploader) throws IOException {

                switch (uploader.getUploadState()) {
                    case INITIATION_STARTED:
                        Log.d(TAG, "Initiation Started");
                        break;
                    case INITIATION_COMPLETE:
                        Log.d(TAG, "Initiation Completed");
                        break;
                    case MEDIA_IN_PROGRESS:
                        Log.d(TAG, "Upload in progress");
                        Log.d(TAG, "Upload percentage: " + uploader.getProgress());

                        if (onProgressListener != null)
                            onProgressListener.onProgress(uploader.getProgress());
                        break;
                    case MEDIA_COMPLETE:
                        Log.d(TAG, "Upload Completed!");
                        break;
                    case NOT_STARTED:
                        Log.d(TAG, "Upload Not Started!");

                        break;
                }
            }
        };

        uploader.setProgressListener(progressListener);

        Video returnedVideo = insert.execute();

        // Print data about the newly inserted video from the API response.
        Log.d(TAG, "\n================== Returned Video ==================\n");
        Log.d(TAG, "  - Id: " + returnedVideo.getId());
        return returnedVideo.getId();
    }

}
