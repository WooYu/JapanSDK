package com.example.wy201.test0904;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nano.karaoksound.ncKaraokSound;
import nano.karaoksound.ncKaraokSoundMix;
import nano.karaoksound.ncKaraokSoundPlayer;
import nano.karaoksound.ncKaraokSoundRecording;
import nano.karaoksound.ncKaraokSoundScoring;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button recordStart;
    private Button recordEnd;
    private Button mixStart;
    private Button mixEnd;
    private Button playStart;
    private Button playStop;
    private Button playTest;
    private TextView startTime;
    private TextView endTime;
    private TextView status;

    private int _mode;
    private String _mp3;
    private String _mid;        //採点（ガイドメロディ
    private String _tmp;    //作業領域
    private String _out;        //編集

    // ---------------------play time-----------------------------

    private final Handler handler = new Handler();
    private final Runnable _TaskMain = new Runnable() {
        @Override
        public void run() {
            double v1 = 0;
            double v2 = 0;

            if (_mode == 1) {
                ncKaraokSoundRecording o = new ncKaraokSoundRecording();
                v1 = o.getPositionSec();
                v2 = o.getDurationSec();
            }
            if (_mode == 2) {
                ncKaraokSoundMix o = new ncKaraokSoundMix();
                v1 = o.getPositionSec();
                v2 = o.getDurationSec();
            }
            if (_mode == 3) {
                ncKaraokSoundPlayer o = new ncKaraokSoundPlayer();
                v1 = o.getPositionSec();
                v2 = o.getDurationSec();
            }

            startTime.setText(String.format("%02d:%02d", (int) v1 / 60, (int) v1 % 60));
            endTime.setText(String.format("%02d:%02d", (int) v2 / 60, (int) v2 % 60));
            handler.postDelayed(this, 50);
        }
    };
    private ncKaraokSoundRecording mRecording;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordStart = (Button) findViewById(R.id.button0);
        recordEnd = (Button) findViewById(R.id.button1);
        mixStart = (Button) findViewById(R.id.button2);
        mixEnd = (Button) findViewById(R.id.button3);
        playStart = (Button) findViewById(R.id.button4);
        playStop = (Button) findViewById(R.id.button5);
        playTest = (Button) findViewById(R.id.button6);
        startTime = (TextView) findViewById(R.id.tv_time1);
        endTime = (TextView) findViewById(R.id.tv_time2);
        status = (TextView) findViewById(R.id.tv_status);

        recordStart.setOnClickListener(this);
        recordEnd.setOnClickListener(this);
        mixStart.setOnClickListener(this);
        mixEnd.setOnClickListener(this);
        playStart.setOnClickListener(this);
        playStop.setOnClickListener(this);
        playTest.setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);

        initPath();
        resourceCopy(R.raw.music0, _mp3);
        resourceCopy(R.raw.m365mid, _mid);
        handler.postDelayed(_TaskMain, 50);
    }

    private void initPath() {
        String rootDir = Environment.getExternalStorageDirectory().getPath() + "/japan";
        File rootFile = new File(rootDir);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }

        _mp3 = rootDir + "/m365.mp3";        //録音
        _mid = rootDir + "/m365.mid";        //採点（ガイドメロディ
        _tmp = rootDir + "/ncKaraokSoundRecording";    //作業領域
        _out = rootDir + "/out.mp3";        //編集
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button0://边播放音乐边录制-开始
                record_start();
                break;
            case R.id.button1://边播放音乐边录制-结束
                record_end();
                break;
            case R.id.button2://编辑开始
                soundmix_setup();
                break;
            case R.id.button3://编辑结束
                soundmix_end();
                break;
            case R.id.button4:
                soundplay_start();//编辑确认
                break;
            case R.id.button5://停止
                soundplay_end();
                break;
            case R.id.button6://播放音乐
                test();
                break;
            case R.id.button7://播放暂停
                playAndPause();
                break;
            case R.id.button8://转换音频
                break;
        }
    }

    private void test() {
        _mode = 3;

        ncKaraokSoundPlayer o = new ncKaraokSoundPlayer();
        o.setup(_mp3);
        o.start();
    }

    private void soundplay_end() {
        ncKaraokSound.terminate();
    }

    private void soundplay_start() {
        _mode = 3;

        ncKaraokSoundPlayer o = new ncKaraokSoundPlayer();
        o.setup(_out);
        o.start();
    }

    private void soundmix_end() {
        ncKaraokSoundMix mix = new ncKaraokSoundMix();
        mix.write(_out);
        mix.end();
    }

    private void soundmix_setup() {
        _mode = 2;

        ncKaraokSoundMix mix = new ncKaraokSoundMix();
        mix.setup();    //(ncKaraokSoundRecorderから情報を取得)
        mix.start();

    }

    //录制结束
    private void record_end() {
        ncKaraokSoundRecording rec = new ncKaraokSoundRecording();
        System.out.println("录制状态："+rec.getStatus());
        rec.end();
    }

    //录制开始
    private void record_start() {
        _mode = 1;
        mRecording = new ncKaraokSoundRecording();
        mRecording.setup(_mp3, _tmp);
        System.out.println("录制时间：getDurationSec = " + mRecording.getDurationSec() + ", getPositionSec = "+ mRecording.getPositionSec());
        System.out.println("录制状态："+ mRecording.getStatus());
        ncKaraokSoundScoring mSoundScore = new ncKaraokSoundScoring();
        mSoundScore.setup(_mid, ncKaraokSound.NONE);
        System.out.println("打分数量：count = "+mSoundScore.getCount());
        mRecording.start();

        System.out.println("打分中音量：" + mSoundScore.getVolumeGuideMelody());
        //採点の設定
//		        ncKaraokSoundSc score = new ncKaraokSoundScore();
//		        score.setup(_mid, 0);

    }

    private void playAndPause(){
//        ncKaraokSoundRecording rec = new ncKaraokSoundRecording();
        mRecording.togglePlayback();
        System.out.println("录制状态："+mRecording.getStatus());
    }

    public void resourceCopy(int res, String out) {
        AssetFileDescriptor fd0 = getResources().openRawResourceFd(res);
        int fileAoffset = (int) fd0.getStartOffset();
        int fileAlength = (int) fd0.getLength();
        try {
            fd0.getParcelFileDescriptor().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileInputStream fi = new FileInputStream(getPackageResourcePath());
            FileOutputStream fo = new FileOutputStream(out);

            fi.skip(fileAoffset);
            byte buf[] = new byte[8192];
            int len;

            while ((len = fi.read(buf)) != -1) {
                fo.write(buf, 0, len);
                if (len > fileAlength)
                    break;
            }
            fo.flush();
            fo.close();
            fi.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertMp3(){
    }
}
