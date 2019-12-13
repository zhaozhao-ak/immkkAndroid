package com.imm.kk.util.x5;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class X5WebView extends WebView {

    private final String TAG = "X5WebView";
    private X5WebCacheManager webCacheManager = null;
    private FileUtils fileTools = new FileUtils();

    public X5WebView(Context context) {
        super(context);

        webCacheManager = X5WebCacheManager.getInstance();
        this.setWebViewClient(X5WebViewClient);
        // this.setWebChromeClient(chromeClient);
        // WebStorage webStorage = WebStorage.getInstance();
        initWebViewSettings();
        this.getView().setClickable(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public X5WebView(Context context, AttributeSet arg1) {
        super(context, arg1);

        webCacheManager = X5WebCacheManager.getInstance();
        initWebViewSettings();
        this.getView().setClickable(true);
    }

    TextView title;
    public WebViewClient X5WebViewClient = new WebViewClient() {
        /**
         * 防止加载网页时调起系统浏览器
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        // TODO shouldInterceptRequest
        @SuppressLint("NewApi")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            //优先从assets资源目录读取文件
            //其次检查文件是否缓存到了fcyd_cache目录
            if (!TextUtils.isEmpty(url) && Uri.parse(url).getScheme() != null) {
                String scheme = Uri.parse(url).getScheme().trim();
                if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                    return super.shouldInterceptRequest(view, injectIsParams(url));
                }
            }
            Log.i(TAG, "请求网络:" + url);
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            //优先从assets资源目录读取文件
            //其次检查文件是否缓存到了fcyd_cache目录
            WebResourceResponse resourceResponse = webCacheManager.readX5WebResourceResponse(url);
            if (resourceResponse != null) {
                return resourceResponse;
            }

            //如果第一次请求未读取到本地，则自主请求网络并保存到sd卡，同时返回给浏览器
            if (webCacheManager.thisURLRequestlsFile(url)) {
                WebResourceResponse response = webCacheManager.saveSDCard(url);
                if (response != null) {
                    return response;
                }
            }

            //如果请求不是文件类型，让浏览器自行处理
            Log.i(TAG, "请求网络:" + url);
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
            view.getSettings().setJavaScriptEnabled(true);
        }
    };


    public static String injectIsParams(String url) {
        if (url != null && !url.contains("xxx=")){
            if (url.contains("?")) {
                return url + "&xxx=1";
            } else {
                return url + "?xxx=1";
            }
        }else{
            return url;
        }
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        //部分手机setJavaScriptEnabled函数诡异崩溃
        try {
            webSetting.setJavaScriptEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计
    }

}
