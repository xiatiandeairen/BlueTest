package com.zero.test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioManager mAudioManager;
    private MediaRecorder recorder;
    private Button openBluetoothMIC_bt, record_bt, stop_bt, play_bt,openPhoneMIC_bt;
    private String path;
    private Toast toast;
    private TextView useMic_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        openBluetoothMIC_bt = (Button) findViewById(R.id.openBluetoothMIC_bt);
        record_bt = (Button) findViewById(R.id.record_bt);
        stop_bt = (Button) findViewById(R.id.stop_bt);
        play_bt = (Button) findViewById(R.id.play_bt);
        openPhoneMIC_bt = (Button) findViewById(R.id.openPhoneMIC_bt);
        useMic_tv = (TextView) findViewById(R.id.useMic_tv);
        openBluetoothMIC_bt.setOnClickListener(this);
        record_bt.setOnClickListener(this);
        stop_bt.setOnClickListener(this);
        play_bt.setOnClickListener(this);
        openPhoneMIC_bt.setOnClickListener(this);
        initPermission();
        boolean sdExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdExist) {
            if (getExternalFilesDir(null) != null) {
                path = getExternalFilesDir(null).getAbsolutePath()  + "/sound.amr";  //设置音频文件保存路径
                System.out.println("有内存卡");
            } else {
                path = getFilesDir().getAbsolutePath() + "/sound.amr";
                System.out.println("有内存卡但是读取失败");
            }
        } else {
            path = getFilesDir().getAbsolutePath() + "/sound.amr";
            System.out.println("无内存卡");
        }
        ((TextView)findViewById(R.id.tips_tv)).setText("音频存储路径："+path);
        showToast("初始化完成");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.openBluetoothMIC_bt:
                mAudioManager.startBluetoothSco();
                mAudioManager.setSpeakerphoneOn(false);
                mAudioManager.setBluetoothScoOn(true);
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

//                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
//                mAudioManager.setBluetoothScoOn(true);
//                mAudioManager.startBluetoothSco();
                useMic_tv.setText("蓝牙麦克风");
                break;
            case R.id.openPhoneMIC_bt:
                if(mAudioManager.isBluetoothScoOn()){
                    mAudioManager.stopBluetoothSco();
                }
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                mAudioManager.setSpeakerphoneOn(true);
                mAudioManager.setBluetoothScoOn(false);
                useMic_tv.setText("本机麦克风");
                break;
            case R.id.record_bt:
                try {
                    File file = new File(path);
                    System.out.println(path);
                    if(file.exists()){
                        file.delete();
                    }
                    file.createNewFile();

                    showToast("文件创建成功");
                    if (recorder != null) {
                        try {
                            recorder.stop();
                        } catch (IllegalStateException e) {
                            // TODO 如果当前java状态和jni里面的状态不一致，
                            //e.printStackTrace();
                            recorder = null;
                            recorder = new MediaRecorder();

                            try {
                                recorder.stop();
                                recorder.release();
                                recorder = null;
                                showToast("停止录音");
                                Log.e("startRecording: ", "recorder关闭成功");
                            } catch (Exception e1) {
                                Log.e("startRecording: ", "recorder加载失败");
                                showToast("录音结束");
                            }
                        }

                    }
                    recorder = new MediaRecorder();
//                    recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);//通话中，对方、自己声音都会录下来

                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);//只录取扬声器、听筒声音

//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);//跟MIC一样，只录取麦克风声音，但扬声器太大声的话也会录到
                    recorder.setMaxDuration(1000*60*3);
                    recorder.setOutputFile(path);
//
//                  设置录制的声音的输出格式（必须在设置声音编码格式之前设置）
                    recorder.setOutputFormat(MediaRecorder
                            .OutputFormat.RAW_AMR);
//                  设置声音编码的格式
                    recorder.setAudioEncoder(MediaRecorder
                            .AudioEncoder.AMR_NB);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                        Log.e("startRecording: ", "recorder使用默认");
                    }
//                    recorder.setVideoFrameRate(15);
//                    recorder.setMaxDuration(30000);
                    Log.e("startRecording: ", "recorder初始化成功");
                    try {
                        recorder.prepare();
                        recorder.start();
                        showToast("开始录音");
                        record_bt.setVisibility(View.GONE);
                        stop_bt.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e("startRecording: ", "recorder加载失败");
                        showToast("开始失败");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stop_bt:
                if (recorder != null) {
                    try {
                        recorder.stop();
                    } catch (IllegalStateException e) {
                        // TODO 如果当前java状态和jni里面的状态不一致，
                        //e.printStackTrace();
                        recorder = null;
                        recorder = new MediaRecorder();

                        try {
                            recorder.stop();
                            recorder.release();
                            recorder = null;
                            showToast("停止录音");
                        } catch (Exception e1) {
                            Log.e("startRecording: ", "recorder加载失败");
                            showToast("录音结束");
                        }
                    }

                }
                stop_bt.setVisibility(View.GONE);
                record_bt.setVisibility(View.VISIBLE);
                break;
            case R.id.play_bt:
                File file2 = new File(path);
                if (!file2.exists()){
                    showToast("没有录音文件");
                }
                Uri uri = Uri.parse(path);
                final int mode = mAudioManager.getMode();
                final boolean ifBlue = mAudioManager.isBluetoothScoOn();
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                if(ifBlue){
                    mAudioManager.stopBluetoothSco();
//                    mAudioManager.setBluetoothScoOn(false);
//                    mAudioManager.setSpeakerphoneOn(true);
                }
                MediaPlayer player =MediaPlayer.create(this,uri);
                try {
                    player.start();
                    showToast("开始播放");
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mAudioManager.setMode(mode);
                            if(ifBlue){
                                mAudioManager.startBluetoothSco();
                                Log.i("ssss","startSco");
                            }
                        }
                    });
                } catch (Exception e1) {
                    Log.e("startRecording: ", "recorder加载失败");
                    showToast("播放错误");
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        openPhoneMIC_bt.performClick();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    private void showToast(String s){
        if (toast!=null){
            toast.setText(s);
        }else {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    private void initPermission() {
        String permissions[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};

        // 1 检查权限
        ArrayList<String> permissionsNeedRequest = new ArrayList<String>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            permissionsNeedRequest.add(permission);
        }
        String[] permissions2 = new String[permissionsNeedRequest.size()];
        permissions2 = permissionsNeedRequest.toArray(permissions2);
        ActivityCompat.requestPermissions(this, permissions2, 0);

    }

    private void initial( MediaRecorder recorderl) {
        recorderl=new MediaRecorder();
        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.amr";
        recorderl.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorderl.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        recorderl.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorderl.setMaxDuration(1000*60*3);
        recorderl.setOutputFile(path);
    }


}
