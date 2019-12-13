package com.imm.kk.util.x5;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.imm.kk.HemoApplication;
import com.tencent.smtt.sdk.WebSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rjyx on 2018/12/28.
 */

public class X5WebViewPool {

    private static final String TAG = "X5WebViewPool";//日志打印
    private static final String APP_CACAHE_DIRNAME = "/rjyx_webcache";//缓存目录

    private static final String DEMO_URL = "about:blank";
    private static List<X5WebView> available = new ArrayList<X5WebView>();
    private static List<X5WebView> inUse = new ArrayList<X5WebView>();

    private static final byte[] lock = new byte[]{};
    private static int maxSize = 3;
    private int currentSize = 0;

    private X5WebViewPool() {
        available = new ArrayList<X5WebView>();
        inUse = new ArrayList<X5WebView>();
    }

    private static volatile X5WebViewPool instance = null;

    public static X5WebViewPool getInstance() {
        if (instance == null) {
            synchronized (X5WebViewPool.class) {
                if (instance == null) {
                    instance = new X5WebViewPool();
                }
            }
        }
        return instance;
    }

    /**
     * Webview 初始化
     * 最好放在application oncreate里
     * */
    public static void init(Context context) {
        for (int i = 0; i < maxSize; i++) {
            X5WebView webView = new X5WebView(context);
            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            webView.setLayoutParams(params);

            initListener(HemoApplication.getInstance(),webView);
            dealJavascriptLeak(webView);

            webView.loadUrl(DEMO_URL);
            available.add(webView);
        }

        Log.i(TAG, "init(): available.size="+available.size()
                + "，inUse.size="+inUse.size()
                + "，maxSize="+maxSize);
    }

    private static void initListener(Context context, X5WebView webView) {
        WebSettings mWebSettings = webView.getSettings();
        if (mWebSettings != null) {
            int SDK_INT = Build.VERSION.SDK_INT;
            if (SDK_INT > 16) {
                try {
                    mWebSettings.setMediaPlaybackRequiresUserGesture(false);
                    if (SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        mWebSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            // 支持脚本，关闭为false
            // 部分手机setJavaScriptEnabled函数诡异崩溃
            try {
                mWebSettings.setJavaScriptEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 设置UserAgent
            String userAgent = mWebSettings.getUserAgentString();
            mWebSettings.setUserAgentString(userAgent);
            //提高渲染的优先级
            mWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            //设置 缓存模式
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

            // 开启 DOM storage API 功能
            mWebSettings.setDomStorageEnabled(true);
            //开启 database storage API 功能
            mWebSettings.setDatabaseEnabled(true);
            String cacheDirPath = context.getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME;
            Log.i(TAG, "cacheDirPath="+cacheDirPath);
            //设置数据库缓存路径
            mWebSettings.setDatabasePath(cacheDirPath);
            //设置  Application Caches 缓存目录
            mWebSettings.setAppCachePath(cacheDirPath);
            //开启 Application Caches 功能
            mWebSettings.setAppCacheEnabled(true);
            // 设置可以支持缩放
            mWebSettings.setSupportZoom(false);
            // 设置出现缩放工具
            mWebSettings.setBuiltInZoomControls(false);
            // 支持通过JS打开新窗口
            mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            // 自适应屏幕
            mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            // 将页面调整到适合webview的大小
            mWebSettings.setUseWideViewPort(true);
            mWebSettings.setLoadWithOverviewMode(true);

            // 设置默认的 WebViewClient
            webView.setWebViewClient(webView.X5WebViewClient);
        }

    }

    /**
     * 移除不安全方法
     */
    private static void dealJavascriptLeak(X5WebView webView) {
        try {
            if (webView != null) {
                webView.removeJavascriptInterface("searchBoxJavaBridge_");
                webView.removeJavascriptInterface("accessibility");
                webView.removeJavascriptInterface("accessibilityTraversal");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取webview
     *
     * */
    public X5WebView getWebView(Context mContext) {
        synchronized (lock) {
            X5WebView webView;
            if (available.size() > 0) {
                webView = available.get(0);
                available.remove(0);
                currentSize++;
                inUse.add(webView);
            } else {
                webView = new X5WebView(mContext);
                inUse.add(webView);
                currentSize++;
            }
            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            webView.setLayoutParams(params);

            initListener(mContext,webView);
            dealJavascriptLeak(webView);

            Log.i(TAG, "getWebView(): available.size="+available.size()
                        + "，inUse.size="+inUse.size()
                    + "，currentSize="+currentSize
                    + "，maxSize="+maxSize);

            webView.loadUrl("about:blank");
            return webView;
        }
    }

    /**
     * 回收webview ,不解绑
     *@param webView 需要被回收的webview
     *
     * */
    public void removeWebView(X5WebView webView) {
        webView.loadUrl("");
        webView.stopLoading();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);  //设置 缓存模式(true);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        webView.getSettings().setLoadWithOverviewMode(false);

        webView.getSettings().setUserAgentString("android_client");
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setDefaultFontSize(16);
        synchronized (lock) {
            inUse.remove(webView);
            if (available.size() < maxSize) {
                available.add(webView);
            } else {
                webView = null;
            }
            currentSize--;
        }

        Log.i(TAG, "removeWebView(X5WebView): available.size="+available.size()
                + "，inUse.size="+inUse.size()
                + "，currentSize="+currentSize
                + "，maxSize="+maxSize);
    }

    /**
     * 回收webview ,解绑
     *@param webView 需要被回收的webview
     *
     * */
    public void removeWebView(ViewGroup view, X5WebView webView) {
        if (view!=null && webView!=null){
            view.removeView(webView);
            webView.loadUrl("");
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.clearCache(true);
            webView.clearHistory();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);  //设置 缓存模式(true);
            webView.getSettings().setAppCacheEnabled(false);
            webView.getSettings().setSupportZoom(false);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.getSettings().setDomStorageEnabled(true);

            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

            webView.getSettings().setLoadWithOverviewMode(false);

            webView.getSettings().setUserAgentString("android_client");
            webView.getSettings().setDefaultTextEncodingName("UTF-8");
            webView.getSettings().setDefaultFontSize(16);
            synchronized (lock) {
                inUse.remove(webView);
                if (available.size() < maxSize) {
                    available.add(webView);
                } else {
                    webView = null;
                }
                currentSize--;
            }

            Log.i(TAG, "removeWebView(ViewGroup, X5WebView): available.size="+available.size()
                    + "，inUse.size="+inUse.size()
                    + "，currentSize="+currentSize
                    + "，maxSize="+maxSize);
        }
    }

    /**
     * 设置webview池个数
     * @param size webview池个数
     * */
    public void setMaxPoolSize(int size) {
        synchronized (lock) {
            maxSize = size;
        }
    }
}
