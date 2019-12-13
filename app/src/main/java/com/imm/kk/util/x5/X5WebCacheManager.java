package com.imm.kk.util.x5;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.imm.kk.HemoApplication;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressLint("NewApi")
public class X5WebCacheManager {
    private final String TAG = "X5WebCacheManager";

    public final String DEFAULT_SD_PATH = "rjyx_cache";//sd卡目录
    public final String APP_CACAHE_DIRNAME = "/rjyx_webcache";//缓存目录

    private ArrayList<String> extensionArray = null;//扩展名数组
    private ArrayList<WebFile> assetsArray = null;//assets资源目录文件列表
    private HashMap<String, String> mimeTypes = null;//MIME类型数组

    private Context mContext;//应用上下文
    private FileUtils fileTools = new FileUtils();//文件工具

    private static X5WebCacheManager mInstance = null;// 单例模式

    /**
     * 获取单例
     */
    public synchronized static X5WebCacheManager getInstance() {
        if (mInstance == null) {
            // 这里直接用application
            mInstance = new X5WebCacheManager(HemoApplication.getInstance());
        }
        return mInstance;
    }

    private String httpUrl = "";

    class WebFile {
        public String fileName;
        public String fileRelativePath;
        public String url;
        public String md5Str;
    }

    private X5WebCacheManager(Context context) {
        setContext(context);
        // assets目录文件
        assetsArray = new ArrayList<WebFile>();
//        copyAssetsFileName("", DEFAULT_SD_PATH);

        // 文件扩展名
        String extensions[] = {"js", "css", "eot", "svg", "ttf", "woff", "woff2", "map", "png", "jpeg", "gif",
                "html", "ico", "ftl"};
        extensionArray = new ArrayList<String>();
        for (String extension : extensions) {
            extensionArray.add(extension);
        }
        // 文件类型字典
        mimeTypes = new HashMap<String, String>();
        // 前端脚本
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "text/javascript");
        mimeTypes.put("map", "text/plain");
        // 字体文件
        mimeTypes.put("woff", "application/font-woff");
        mimeTypes.put("woff2", "application/font-woff2");
        mimeTypes.put("ttf", "application/x-font-ttf");
        mimeTypes.put("eot", "application/vnd.ms-fontobject");
        mimeTypes.put("svg", "image/svg+xml");

        // 图像
        mimeTypes.put("png", "image/png");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("ico", "image/x-icon");

        //布局
        mimeTypes.put("ftl", "text/html");
    }

    private void copyAssetsFileName(String assetDir, String dir) {
        String[] files;
        try {
            // 获得Assets一共有几多文件
            files = this.getResources().getAssets().list(assetDir);
        } catch (IOException e1) {
            return;
        }
        File mWorkingPath = new File(dir);
        // 如果文件路径不存在
        if (!mWorkingPath.exists()) {
            // 创建文件夹
            if (!mWorkingPath.mkdirs()) {
                // 文件夹创建不成功时调用
            }
        }
        for (int i = 0; i < files.length; i++) {
            // 获得每个文件的名字
            String fileName = files[i];
            // 根据路径判断是文件夹还是文件
            if (!fileName.contains(".")) {
                if (0 == assetDir.length()) {
                    copyAssetsFileName(fileName, dir + fileName + "/");
                } else {
                    copyAssetsFileName(assetDir + "/" + fileName, dir + "/"
                            + fileName + "/");
                }
                continue;
            }
            String assetsPath = assetDir + "/" + fileName;
            WebFile file = new WebFile();
            file.fileName = fileName;
            file.fileRelativePath = assetsPath;
            assetsArray.add(file);
            System.out.println("66666------55----"+assetsPath);
            putAssetsToSDCard(assetsPath,DEFAULT_SD_PATH);
        }
    }

    /**
     * 将assets下的文件放到sd指定目录下
     *
     * @param assetsPath assets下的路径
     * @param sdCardPath sd卡的路径
     */
    private void putAssetsToSDCard(String assetsPath,
                                         String sdCardPath) {
        try {
            String mString[] = this.getResources().getAssets().list(assetsPath);
            if (mString.length == 0) { // 说明assetsPath为空,或者assetsPath是一个文件
                InputStream mIs = this.getResources().getAssets().open(assetsPath); // 读取流
                byte[] mByte = new byte[1024];
                int bt = 0;
                String subing = getFileNameNoEx(assetsPath);

                String unsureTarget = getFilePathNoEx(assetsPath);

                String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+sdCardPath+"/"+ MD5Util.MD5(unsureTarget)+subing;
                System.out.println("66666----------"+fileName);
                File file = new File(fileName);
                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }else if (!file.exists()){
                    file.createNewFile();
                }else {
                    return;//已经存在直接退出
                }

                FileOutputStream fos = new FileOutputStream(file); // 写入流
                while ((bt = mIs.read(mByte)) != -1) { // assets为文件,从文件中读取流
                    fos.write(mByte, 0, bt);// 写入流到文件中
                }
                fos.flush();// 刷新缓冲区
                mIs.close();// 关闭读取流
                fos.close();// 关闭写入流
            }
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(dot, filename.length());
            }
        }
        return filename;
    }

    public static String getFilePathNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }


    /**
     * 应用上下文
     *
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 返回MIME类型数组
     *
     * @return
     */
    public HashMap<String, String> getMimeTypes() {
        return mimeTypes;
    }

    public WebResourceResponse readX5WebResourceResponse(String url) {

        if (thisURLRequestlsFile(url)) {
            InputStream fileInputStream = thisURLRequestIsSDCardFile(url);
            if (fileInputStream!=null) {
                int startIndex = url.lastIndexOf("/");
                String unsureTarget = url.substring(startIndex + 1);
                String mimeType = mimeTypes.get(unsureTarget);
                return new WebResourceResponse(mimeType, "utf-8", fileInputStream);
            } else {
                return null;
            }
        }
        return null;
    }

    public WebResourceResponse saveSDCard(String url) {
        if (thisURLRequestlsFile(url)) {
            InputStream fileInputStream = saveSDCardFile(url);
            if (fileInputStream!=null) {
                int startIndex = url.lastIndexOf("/");
                String unsureTarget = url.substring(startIndex + 1);
                String mimeType = mimeTypes.get(unsureTarget);
                return new WebResourceResponse(mimeType, "utf-8", fileInputStream);
            } else {
                return null;
            }
        }
        return null;
    }





    private Resources getResources() {
        return this.getContext().getResources();
    }

    /**
     * 判断 unsureTarget 是否文件名称
     *
     * @return
     */
    private boolean thisIsFileName(String unsureTarget) {
        int startIndex = unsureTarget.lastIndexOf(".");
        String extension = unsureTarget.substring(startIndex + 1);
        if (extensionArray.contains(extension)) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前url是否包含文件名称 将？号左右的值进行拆分 如果没有？号直接返回/后的字符串 如果有将？左右值进行拆分
     *
     * @param url
     * @return
     */
    public boolean thisURLRequestlsFile(String url) {
        int startIndex = url.lastIndexOf("/");
        String unsureTarget = url.substring(startIndex + 1);
        String[] items = unsureTarget.split("\\?");
        boolean result = false;
        if (thisIsFileName(items[0])) {
            Log.i(TAG, items[0] + "是文件");
            result = true;
        } else {
            Log.i(TAG, items[0] + "不是文件");
            result = false;
        }
        return result;
    }

    /**
     * 获取url包含的本地资源文件名称 首先检查url是否为文件路径，如果不是则返回 “” 如果文件名称与Assets目录文件匹配，则返回文件名称
     * 否则返回 “”
     *
     * @param url
     * @return
     */
    public String thisURLRequestIsAssetsFile(String url) {
        int startIndex = url.lastIndexOf("/");
        String unsureTarget = url.substring(startIndex + 1);
        String[] items = unsureTarget.split("\\?");
        String fileRelativePath = isTrueAssets(items[0]);
        if (thisIsFileName(items[0]) && !TextUtils.isEmpty(fileRelativePath)) {
            Log.i(TAG, items[0] + "是assets文件");
            return fileRelativePath;
        } else {
            Log.i(TAG, items[0] + "不是assets文件");
            return "";
        }
    }


    /**
     * 获取url包含的本地资源文件名称 首先检查url是否为文件路径，如果不是则返回 “” 如果文件名称与SDCard目录文件匹配，则返回文件名称
     * 否则返回 “”
     *
     * @param url
     * @return
     */
    public InputStream thisURLRequestIsSDCardFile(String url) {
        int startIndex = url.lastIndexOf("/");
        String unsureTarget = url.substring(startIndex + 1);
        String[] items = unsureTarget.split("\\?");
        if(url.indexOf("mobile")!=-1){
            String filePath = url.substring(url.indexOf("m")+7);
            if (thisIsFileName(items[0])) {
                InputStream inputStream = isTrueSDCardFile(filePath);
                Log.i(TAG, items[0] + "是SDCard文件");
                return inputStream;
            } else {
                Log.i(TAG, items[0] + "不是SDCard文件");
                return null;
            }
        }else{ System.out.println("不包含");
            Log.i(TAG, items[0] + "不是SDCard文件");
            return null;
        }
    }



    /**
     * 先保存到SD卡，再读取返回
     * @param url
     * @return
     */
    public InputStream saveSDCardFile(String url) {
        int startIndex = url.lastIndexOf("/");
        String unsureTarget = url.substring(startIndex + 1);
        String[] items = unsureTarget.split("\\?");
        if(url.indexOf("mobile")!=-1){
            String filePath = url.substring(url.indexOf("m")+7);
            if (thisIsFileName(items[0])) {
                InputStream inputStream = aaveSDCardFile(url,filePath);
                Log.i(TAG, items[0] + "是SDCard文件");
                return inputStream;
            } else {
                Log.i(TAG, items[0] + "不是SDCard文件");
                return null;
            }
        }else{ System.out.println("不包含");
            Log.i(TAG, items[0] + "不是SDCard文件");
            return null;
        }
    }


    public String isTrueAssets(String fileName) {
        if (assetsArray != null) {
            for (WebFile file : assetsArray) {
                if (file.fileName.equals(fileName)) {
                    return file.fileRelativePath;
                }
            }
        }
        return "";
    }



    public InputStream isTrueSDCardFile(String url) {
        String subing = getFileNameNoEx(url);
        String unsureTarget = getFilePathNoEx(url);
        String filePath = MD5Util.MD5(unsureTarget)+subing;
        InputStream inputStream = fileTools.streamFromSD(DEFAULT_SD_PATH, filePath);
        return inputStream;
    }

    public InputStream aaveSDCardFile(String url, String filePathName) {
        String subing = getFileNameNoEx(filePathName);
        String unsureTarget = getFilePathNoEx(filePathName);
        String filePath = MD5Util.MD5(unsureTarget)+subing;
        putToSDCard(url,filePath);
        InputStream inputStream = fileTools.streamFromSD(DEFAULT_SD_PATH, filePath);
        return inputStream;
    }


    /**
     * 将文件放到sd指定目录下
     * @param sdCardPath sd卡的路径
     */
    private void putToSDCard(String url,
                                   String sdCardPath) {

        try {
            int startIndex = url.lastIndexOf("/");
            String unsureTarget = url.substring(startIndex + 1);
            String[] items = unsureTarget.split("\\?");
            //请求网络得到byte数组
            byte[] data = HttpUtil.doGetBytes(url);
            if (data != null && data.length>0) {
                String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DEFAULT_SD_PATH+"/"+ sdCardPath;
                System.out.println("66666----------"+fileName);
                File file = new File(fileName);
                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }else if (!file.exists()){
                    file.createNewFile();
                }else {
                    return;//已经存在直接退出
                }
                fileTools.write2SD(DEFAULT_SD_PATH, sdCardPath, data);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取url包含的远程文件名称
    // 首先检查url是否为文件路径，
    // 如果不是则返回 “”
    // 如果是文件名称，判断是否在assets目录，
    // 如果存在返回“”，
    // 如果不存在则返回url的md5值，然后拼接成存储路径 例如“/data/sdcard/fcyd_cache/文件名称”
    public WebResourceResponse cachedLocationForX5URLRequest(String url) {
        int startIndex = url.lastIndexOf("/");
        String unsureTarget = url.substring(startIndex + 1);
        String[] items = unsureTarget.split("\\?");
        //如果uil是文件类型 并且不再assets目录，则尝试从 fcyd_cache 读取
        if (thisIsFileName(items[0]) && !assetsArray.contains(items[0])) {
            //将url按“？”拆分,左侧是文件链接
            String[] urlItems = url.split("\\?");
            int startIndex2 = items[0].lastIndexOf(".");
            //得到文件的扩展名
            String extension = items[0].substring(startIndex2 + 1);
            //得到文件的MIME类型
            String mimeType = mimeTypes.get(extension);
            //将文件的url去除参数后，进行MD5加密。得到32位长度的名称
            String cacheFile = MD5Util.MD5(urlItems[0]) + "." + extension;
            if (fileTools.isFileExist(DEFAULT_SD_PATH, cacheFile)) {
                Log.i(TAG, items[0] + "从sd卡读取:" + cacheFile);
                InputStream inputStream = fileTools.streamFromSD(DEFAULT_SD_PATH, cacheFile);

                return new com.tencent.smtt.export.external.interfaces.WebResourceResponse(mimeType, "utf-8", inputStream);
            }
        }
        return null;
    }

    public void ClearCaches() {
        //清空所有Cookie
        CookieSyncManager.createInstance(this.getContext().getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookie();
            cookieManager.flush();
        } else {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
        WebStorage.getInstance().deleteAllData(); //清空WebView的localStorage

        new DeleteFileTask().execute(fileTools.getSDCardRoot(), DEFAULT_SD_PATH);
        new DeleteFileTask().execute(fileTools.getSDCardRoot(), APP_CACAHE_DIRNAME);
    }
}
