package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Created by peterma on 1/2/16.
 */
public class TriggerActivity extends Activity{

    private final static String TAG = "TriggerActivity";

    private ImageButton mPlayButton;
    boolean mStartPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        MainActivity mainActivity = ((StarterApplication)getApplication()).getMainActivity();
        if(mainActivity!=null) {
            mainActivity.sendMessages();
        } else {
            Log.d(TAG,"mainActivity is null");
        }

        ArcToast("APPLICATION TRIGGERED!");

        mPlayButton = (ImageButton)findViewById(R.id.playerButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);

                if (mStartPlaying) {
                    mPlayButton.setImageResource(R.drawable.stop);
                } else {
                    mPlayButton.setImageResource(R.drawable.play);
                }
                mStartPlaying = !mStartPlaying;
            }
        });

        mRecordButton = (ImageButton)findViewById(R.id.recordButton);
        mRecordButton.setOnClickListener(new View.OnClickListener() {

            boolean mStartRecording = true;

            public void onClick(View v) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });


        //LinearLayout ll = new LinearLayout(this);
        //mPlayButton = new PlayButton(this);
       // ll.addView(mPlayButton,
         //       new LinearLayout.LayoutParams(
          //              ViewGroup.LayoutParams.WRAP_CONTENT,
           //             ViewGroup.LayoutParams.WRAP_CONTENT,
            //            0));
        //setContentView(ll);



        mRecordButton.performClick();
        Handler handlerTimer = new Handler();
        handlerTimer.postDelayed(new Runnable(){
            public void run() {
                ArcToast("Recorded!");
                mRecordButton.performClick();

            }}, 5000);
    }

    protected void ArcToast(CharSequence toastText) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, toastText, duration);
        toast.show();
    }


    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    //public RecordButton mRecordButton = null;
    public ImageButton mRecordButton  = null;
    private MediaRecorder mRecorder = null;

    //public PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        mRecordButton.setImageResource(R.drawable.record_dark);

    }

    private void stopRecording() {
        if(mRecorder == null){
            return;
        }
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        mRecordButton.setImageResource(R.drawable.record_red);

        ArcToast("Uploading voice file...");

        new UploadFilesTask().execute("");

    }

    /*

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    */


/*
    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }
*/
    public TriggerActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        finish();
    }

    private class UploadFilesTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            String url = "";
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"/audiorecordtest.3gp");
            try {
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httppost = new HttpPost(new URI(url));

                InputStreamEntity reqEntity = new InputStreamEntity(
                        new FileInputStream(file), -1);
                reqEntity.setContentType("binary/octet-stream");
                reqEntity.setChunked(true); // Send in multiple parts if needed
                httppost.setEntity(reqEntity);
                HttpResponse response = httpclient.execute(httppost);
                return response.toString();

            } catch (Exception e) {
                return e.getMessage().toString();
            }
        }


        protected void onPostExecute(String result) {
            Log.e("foobar", result);
            ArcToast(result);
        }
    }

}
