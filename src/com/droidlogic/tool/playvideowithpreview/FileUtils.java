package com.droidlogic.tool.playvideowithpreview;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.droidlogic.app.SystemControlManager;

import android.os.Environment;
import android.util.Log;

public class FileUtils {

	private static final String TAG = "FileUtils";
	public static final String STORAGE = "/storage";
    public static final String MEDIA_RW = "/mnt/media_rw";
    public static final String NAND = /*Environment.getExternalStorageDirectory().getPath();*/"storage/emulated/0";
    
    private static final String video_extensions = ".3gp,.avi,.dat,.f4v,.flv,.m2ts,.mkv,.mp4,.mov,.rmvb,.ts";
	
	public FileUtils() {
		// TODO Auto-generated constructor stub
	}

	public static boolean isVideo(String filename) {
        String name = filename.toLowerCase();
        String videos[] = video_extensions.split(",");
        for (String ext : videos) {
            if (name.endsWith(ext))
                return true;
        }
        return false;
    }
	
	public static List<String> getMediaList() {
		List<String> result = new ArrayList<String>();
        File filenand = new File(NAND);
        if (filenand != null && filenand.exists() && filenand.isDirectory() && filenand.listFiles() != null && filenand.listFiles().length > 0) {
        	File[] allfile = filenand.listFiles();
            for (File temp : allfile) {
            	if (temp.isFile() && isVideo(temp.getAbsolutePath())) {
            		result.add(NAND + "," + temp.getAbsolutePath());
            		Log.i(TAG, "[getMediaList] add NAND " + temp.getAbsolutePath());
            	}
            }
        }
        
        //check /storage/
        File filemedia = new File(STORAGE);
        if (filemedia != null && filemedia.exists() && filemedia.isDirectory() && filemedia.listFiles() != null && filemedia.listFiles().length > 0) {
        	File[] allfile = filemedia.listFiles();
            for (File temp : allfile) {
            	Log.i(TAG, "[getMediaList] STORAGE find device " + temp.getAbsolutePath());
            	if (temp != null && temp.exists() && temp.isDirectory() && temp.listFiles() != null && temp.listFiles().length > 0) {
            		File[] alltempfile = temp.listFiles();
            		for (File temp1 : alltempfile) {
                    	if (temp1.isFile() && isVideo(temp1.getAbsolutePath())) {
                    		result.add(STORAGE + "," + temp1.getAbsolutePath());
                    		Log.i(TAG, "[getMediaList] add STORAGE " + temp1.getAbsolutePath());
                    	}
                    }
            	}
            }
        }

        Log.i(TAG, "[getMediaList]====start print list==========================");
        if (result != null && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
            	Log.i(TAG, "[getMediaList]==== no." + i + "->" + result.get(i));
            }
        }
        Log.i(TAG, "[getMediaList]====end print list==========================");

        return result;
    }
	
	public static String getVideoFps() {
		String result = null;
        String file_path = "/sys/class/vdec/vdec_status";
        File file = new File(file_path);
        String strMode = null;
        try {
        	if (!file.exists()) {
                Log.d(TAG, "/sys/class/vdec/vdec_status not exist!");
                return result;
            }
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "getVideoFps not exists Exception = " + e.getMessage());
			e.printStackTrace();
			return result;
		}
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len = fis.read(buf, 0, buf.length);
            fis.close();
            Log.d(TAG, "getVideoFps len = " + len);
            strMode = new String(buf, 0, len).trim();
            // strMode = 1~7, 1--min, 7--max
            Log.d(TAG, "getVideoFps vdec_status = " + strMode);
            if(strMode != null && strMode.length() > 0){
            	result = parseVideoFrame(strMode);
            }
        } catch (Exception e) {
        	Log.e(TAG, "getVideoFps Exception = " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
	
	public static String getVideoFpsBySystemControl(SystemControlManager manager) {
		String result = null;
		if (manager != null) {
			String strMode = manager.readSysFs("/sys/class/vdec/vdec_status");
			if(strMode != null && strMode.length() > 0){
            	result = parseVideoFrame(strMode);
            }
		}
		return result;
	}
	
	private static String parseVideoFrame(String value) {
		String result = null;
		if (value != null && value.length() > 0) {
			int start = value.indexOf("frame rate : ");//+13
			int end = value.indexOf(" fps");
			int preLength = "frame rate : ".length();
			if (start != -1 && end != -1 && (start + preLength) < end) {
				String sub = value.substring(start + preLength, end);
				Log.d(TAG, "parseVideoFrame sub = " + sub);
				result = sub;
			}
		}
		return result;
	}
}
