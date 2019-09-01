package com.droidlogic.tool.playvideowithpreview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
}
