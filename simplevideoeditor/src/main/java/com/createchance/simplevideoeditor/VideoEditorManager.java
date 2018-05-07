package com.createchance.simplevideoeditor;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 02/05/2018
 */
public class VideoEditorManager {
    private static final String TAG = VideoEditorManager.class.getSimpleName();

    private static VideoEditorManager sManager;

    private Context mContext;

    private Editor mCurrentEditor;

    private VideoEditorManager() {

    }

    public synchronized static VideoEditorManager getManager() {
        if (sManager == null) {
            sManager = new VideoEditorManager();
        }

        return sManager;
    }

    public synchronized void init(Application application) {
        this.mContext = application;
    }

    public Context getContext() {
        return mContext;
    }

    public synchronized Editor edit(File videoFile) {
        if (mCurrentEditor != null) {
            Logger.e(TAG, "One edit is on going, try again later.");
            return null;
        }
        mCurrentEditor = new Editor(videoFile);
        return mCurrentEditor;
    }

    File getBaseWorkFolder() {
        return mCurrentEditor == null ?
                null : mCurrentEditor.outputFile == null ?
                null : mCurrentEditor.outputFile.getParentFile();
    }

    File getOutputFile() {
        return mCurrentEditor == null ? null : mCurrentEditor.outputFile;
    }

    void onStart(String action) {
        if (mCurrentEditor.mCallback != null) {
            mCurrentEditor.mCallback.onStart(action);
        }
    }

    void onProgress(String action, float progress) {
        if (mCurrentEditor.mCallback != null) {
            mCurrentEditor.mCallback.onProgress(action, progress);
        }
    }

    void onSucceed(String action) {
        if (mCurrentEditor.mCallback != null) {
            mCurrentEditor.mCallback.onSucceeded(action);
        }

        // clean all the tmp files.
        for (AbstractAction act : mCurrentEditor.actionList) {
            act.release();
        }
    }

    void onFailed(String action) {
        if (mCurrentEditor.mCallback != null) {
            mCurrentEditor.mCallback.onFailed(action);
        }

        // clean all the tmp files.
        for (AbstractAction act : mCurrentEditor.actionList) {
            act.release();
        }
    }

    public static class Editor {
        private File inputFile;
        private File outputFile;
        private List<AbstractAction> actionList = new ArrayList<>();

        VideoEditCallback mCallback;

        private Editor(File input) {
            this.inputFile = input;
        }

        public Editor withAction(AbstractAction action) {
            if (action != null) {
                actionList.add(action);
            }

            return this;
        }

        public Editor saveAs(File outputFile) {
            this.outputFile = outputFile;

            return this;
        }

        public void commit(VideoEditCallback callback) {
            // check if input file is rational.
            if (inputFile == null ||
                    !inputFile.exists() ||
                    !inputFile.isFile()) {
                throw new IllegalArgumentException("Input video is illegal, input video: " + inputFile);
            }
            if (actionList.size() == 0) {
                Logger.e(TAG, "No edit action specified, please set at least one action!");
                return;
            }

            mCallback = callback;

            Log.d(TAG, "commit: " + outputFile);
            // start from the first one.
            actionList.get(0).start(inputFile);
        }
    }
}