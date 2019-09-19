package com.droidlogic.tool.playvideowithpreview;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final int REQUEST_CHECK_TIMEOUT = 0;
    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_EXTERNAL_PERMISSION = 2;
    private static final int REQUEST_MEDIA_PERMISSION = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 4;
    private static final int REQUEST_CHECK_MEDIA = 5;
    private static final int REQUEST_CHECK_CAMERA = 6;
    private static final int REQUEST_RECORD_PERMISSION = 7;
    
    private TextView mTextView;
    private Button mButton;
    private ScrollView mScrollView;
    private StringBuilder mStringBuilder = new StringBuilder();
    private long mStartTime = 0;
    private boolean mStarted = false;

    private Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case REQUEST_CHECK_TIMEOUT:
					if (System.currentTimeMillis() - mStartTime > 10000) {
						mStringBuilder.append("检测超时，请点击播放按钮重新初始化...\n");
						mTextView.setText(mStringBuilder.toString());
						mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
						mHandler.removeCallbacksAndMessages(null);
						mButton.setEnabled(true);
						mButton.setText("播放");
						mButton.requestFocus();
						Log.d(TAG, "wait timeouted");
					} else {
						mHandler.sendEmptyMessageDelayed(REQUEST_CHECK_TIMEOUT, 1000);
						Log.d(TAG, "wait timeout");
					}
					break;
				case REQUEST_PERMISSION:
					Log.d(TAG, "REQUEST_PERMISSION");
					mStartTime = System.currentTimeMillis();
					mHandler.sendEmptyMessageDelayed(REQUEST_CHECK_TIMEOUT, 1000);
					mButton.setEnabled(false);
					mButton.setText("正在初始化...");
					if (!hasStoragePermission()) {
						mStringBuilder.append("检查存储权限...\n");
						//mHandler.sendEmptyMessage(REQUEST_EXTERNAL_PERMISSION);
						mHandler.sendEmptyMessageDelayed(REQUEST_EXTERNAL_PERMISSION, 1000);
					} else if (!hasCameraPermission()) {
						mStringBuilder.append("已获得存储权限...\n");
						mStringBuilder.append("检查Camera权限...\n");
						//mHandler.sendEmptyMessage(REQUEST_CAMERA_PERMISSION);
						mHandler.sendEmptyMessageDelayed(REQUEST_CAMERA_PERMISSION, 1000);
					} else if (!hasRecordPermission()) {
						mStringBuilder.append("已获得存储权限...\n");
						mStringBuilder.append("已获取Camera权限...\n");
						//mHandler.sendEmptyMessage(REQUEST_CAMERA_PERMISSION);
						mHandler.sendEmptyMessageDelayed(REQUEST_RECORD_PERMISSION, 1000);
					} else {
						mStringBuilder.append("已获得存储权限...\n");
						mStringBuilder.append("已获取Camera权限...\n");
						mStringBuilder.append("获取Record权限...\n");
						//mHandler.sendEmptyMessage(REQUEST_CHECK_MEDIA);
						mHandler.sendEmptyMessageDelayed(REQUEST_CHECK_MEDIA, 1000);
					}
					break;
				case REQUEST_EXTERNAL_PERMISSION:
					Log.d(TAG, "REQUEST_EXTERNAL_PERMISSION");
					mStringBuilder.append("申请存储权限...\n");
					requestWriteExternalPermission();
					break;
				case REQUEST_CAMERA_PERMISSION:
					Log.d(TAG, "REQUEST_CAMERA_PERMISSION");
					mStringBuilder.append("申请Camera权限...\n");
					requestCameraPermission();
					break;
				case REQUEST_RECORD_PERMISSION:
					Log.d(TAG, "REQUEST_RECORD__PERMISSION");
					mStringBuilder.append("申请Record权限...\n");
					requestRecordPermission();
					break;
				case REQUEST_CHECK_MEDIA:
					Log.d(TAG, "REQUEST_CHECK_MEDIA");
					if (checkMedia()) {
						mStringBuilder.append("发现媒体文件...\n");
						mHandler.sendEmptyMessageDelayed(REQUEST_CHECK_CAMERA, 1000);
					} else {
						mStringBuilder.append("未发现媒体文件，继续扫描媒体文件...\n");
						mHandler.sendEmptyMessageDelayed(REQUEST_CHECK_MEDIA, 1000);
					}
					break;
				case REQUEST_CHECK_CAMERA:
					Log.d(TAG, "REQUEST_CHECK_CAMERA");
					if (checkCamera()) {
						mStringBuilder.append("初始化完成，进入播放...\n");
						mStarted = true;
						startPlay();
						mButton.setEnabled(true);
						mButton.setText("播放");
					} else {
						mStringBuilder.append("未检测到摄像头，继续检测摄像头...\n");
						mHandler.sendEmptyMessageDelayed(REQUEST_CHECK_CAMERA, 1000);
					}
					break;
				default:
					break;
			}
			mTextView.setText(mStringBuilder.toString());
			mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
		}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);
        mTextView = (TextView)findViewById(R.id.status_text);
        mButton = (Button)findViewById(R.id.init_button);
        mScrollView = (ScrollView)findViewById(R.id.scrollView);
        mHandler.sendEmptyMessageDelayed(REQUEST_PERMISSION, 100);
        mStartTime = System.currentTimeMillis();
        mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHandler.removeCallbacksAndMessages(null);
				mHandler.sendEmptyMessageDelayed(REQUEST_PERMISSION, 100);
		        mStartTime = System.currentTimeMillis();
		        Log.d(TAG, "onClick reexamination");
			}
		});
        mButton.requestFocus();
    }

    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	Log.d(TAG, "onResume");
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	Log.d(TAG, "onPause");
    	mButton.requestFocus();
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	Log.d(TAG, "onStop");
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	Log.d(TAG, "onDestroy");
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    	Log.d(TAG, "onRequestPermissionsResult requestCode = " + requestCode + ", permissions = " + Arrays.toString(permissions) + ", grantResults = " + Arrays.toString(grantResults));
        if (requestCode == REQUEST_EXTERNAL_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mHandler.sendEmptyMessage(REQUEST_CAMERA_PERMISSION);
            } else {
            	requestWriteExternalPermission();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            	mHandler.sendEmptyMessage(REQUEST_RECORD_PERMISSION);
            } else {
                requestCameraPermission();
            }
        } else if (requestCode == REQUEST_RECORD_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            	mHandler.sendEmptyMessage(REQUEST_CHECK_MEDIA);
            } else {
                requestRecordPermission();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    
    private boolean checkMedia() {
    	boolean result = true;
    	List<String> list = FileUtils.getMediaList();
    	if (list != null && list.size() > 0) {
    		Log.d(TAG, "checkMedia found " + list.get(0));
    		result = true;
    	}
    	return result;
    }
    
    private boolean checkCamera() {
    	boolean result = false;
        try {
        	CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
			String[] liStrings = manager.getCameraIdList();
			if (liStrings != null && liStrings.length > 0) {
				Log.d(TAG, "checkCamera found " + liStrings.length);
				result = true;
			}
		} catch (CameraAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result;
    }
    
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean hasCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
    
    private boolean hasRecordPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
    
    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(MainActivity.this, "Camera permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void requestRecordPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(MainActivity.this, "Record permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_PERMISSION);
        }
    }
    
    private void requestWriteExternalPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this, "WRITE_EXTERNAL_STORAGE permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_PERMISSION);
        }
    }

    private void startPlay() {
    	Intent intent = new Intent(MainActivity.this, VideoShowActivity.class);
        startActivity(intent);
        mHandler.removeCallbacksAndMessages(null);
    }
}
