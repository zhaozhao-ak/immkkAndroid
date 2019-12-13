package com.imm.kk.util.x5;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;

/**
 * 通过异步的方式来执行删除操作
 * @author ytkj
 *
 */
public class DeleteFileTask extends AsyncTask<String, String, String> {

    private final String TAG = "DeleteFileTask";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String delete_part = params[0];
        final String delete_file = params[1];
        File part = new File(delete_part);
        Log.d(TAG,"delete_part = " + delete_part + " ,delete_file = " + delete_file + " ,part = " + part);
        if (!part.exists() || (!part.isDirectory())){
            Log.d(TAG,"part = " + part);
            return "请检查参数";
        }
        //返回要删除的目录列表
        File[] childFiles = part.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName().toLowerCase();
                Log.d(TAG,"accept---------------name = " + name);
//					return name.contains(delete_file);
                return name.equals(delete_file);
            }
        });

        if (null == childFiles){
            Log.d(TAG,"null == childFiles");
            return "请检查参数";
        }else {
            boolean result = true;
            for (File child : childFiles){
                Log.d(TAG,"child = " + child.getAbsolutePath());
                boolean ret = deleteDirs(child.getAbsolutePath());
                result = result && ret;
                if (!ret){
                    Log.d(TAG,"delete failed : " + child.getAbsolutePath());
                }
            }
            return result ? "删除成功" : "删除失败";
        }

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG,"result = " + result);
    }

    /**
     * 删除某个目录
     * @param path 要删除的目录路径
     * @return
     */
    private boolean deleteDirs(String path){
        Log.d(TAG,"path = " + path);
        File file = new File(path);
        if (!file.exists()){
            return true;
        }
        if (file.isDirectory()){
            File[] childs = file.listFiles();
            if (null == childs){
                return false;
            }
            boolean result = true;
            for (File child : childs){
                result = result && deleteDirs(child.getAbsolutePath());
            }
            Log.d(TAG,"file1-------------------" + file.toString());
            try{
                boolean ret = file.delete();
                return result && ret;
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }

        }else {
            Log.d(TAG,"file2-------------------" + file.toString());
            try {
                boolean ret = file.delete();
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }
    }
}

