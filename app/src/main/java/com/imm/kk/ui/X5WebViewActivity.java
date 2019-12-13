package com.imm.kk.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.imm.kk.HemoApplication;
import com.imm.kk.R;
import com.imm.kk.ui.BaseActivity;
import com.imm.kk.util.CommonUtils;
import com.imm.kk.util.RuntimePermissionUtil;
import com.imm.kk.util.SharedPreferencesUtils;
import com.imm.kk.util.x5.X5WebView;
import com.imm.kk.util.x5.X5WebViewPool;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;


/**
 * Created by Administrator on 2018/12/18 0018.
 */

public class X5WebViewActivity extends BaseActivity {
    private static final String TAG = "X5WebViewActivity";//日志打印
    private static final int TIMER = 999;
    private static boolean flag = true;
    public static final String HOME_URL = "http://192.168.8.17:8080";
    private String mHomeUrl = "";
    private Context mContext;
    private Dialog menuDialog;
    protected FrameLayout mWebviewRoot;
    private X5WebView mX5WebView;//浏览器控件
    private LinearLayout connect_error;
    private boolean hasPermission = true;// 是否有权限
    private final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private final int PERMISSIONS_GRANTED = 0; // 权限授权
    private final int PERMISSIONS_DENIED = 1; // 权限拒绝

    /**
     * 跳转到当前页面页面
     */
    public static void showThisActivity(Activity activity) {
        Intent intent = new Intent(activity, X5WebViewActivity.class);
        activity.startActivity(intent);
        // 不要结束原先页面
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 布局必须放在super.onCreate上面，不然会找不到控件
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        // 先检测初始化的设置
        initSetting();
        initView();
    }


    @Override
    public void onResume() {
        super.onResume();
        // 打开网页
        flag = true;
//        setTimer();
        openWebPage();
        requestPermission();
//        baiduTTS = HemoApplication.getInstance().getBaiduTTS();
//        baiduTTS.conectTTs(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        baiduTTS.release();
//        baiduTTS = null;
//        HemoApplication.getInstance().releaseBaiduTTS();
        //停止服务
//        HemoApplication.getInstance().webSocketDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopTimer();
        //释放资源
        X5WebViewPool.getInstance().removeWebView(mWebviewRoot, mX5WebView);
    }

    private void startSocket(){
        System.out.println(TAG+"-----startSocket--");
        String mHomeUrl = SharedPreferencesUtils.getHttpUrl(mContext);
        if (!TextUtils.isEmpty(mHomeUrl) && mHomeUrl.contains("hdis")){
            HemoApplication.getInstance().webSocketStart();
        }
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        // 申请权限
        RuntimePermissionUtil permissionUtil = new RuntimePermissionUtil(this);
        hasPermission = true;
        if (permissionUtil.lacksPermissions(CommonUtils.REQUEST_BASIC_PERMISSIONS)) {
            if (hasPermission) {
                // 请求权限
                ActivityCompat.requestPermissions(this, CommonUtils.REQUEST_BASIC_PERMISSIONS, PERMISSION_REQUEST_CODE);
            } else {
                hasPermission = true;
            }
        }
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            hasPermission = true;
            allPermissionsGranted();
        } else {
            hasPermission = false;
        }
    }

    /**
     * 含有全部的权限
     */
    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 全部权限均已获取
     */
    private void allPermissionsGranted() {
        setResult(PERMISSIONS_GRANTED);
    }


//    private void setTimer(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (flag){
//                    try {
//                        Thread.sleep(60000);
//                        Message msg = new Message();
//                        msg.what = 2;
//                        handler.sendMessage(msg);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }


    private void stopTimer(){
        flag = false;
    }

    public void showX5WebView(View view){
//        mX5WebView.loadUrl("javascript:mobileUpData('soloname')");
    }

    private void openWebPage() {
        System.out.println("111----openWebPage------------>");
        mHomeUrl = SharedPreferencesUtils.getHttpUrl(X5WebViewActivity.this);
//        if (NetworkUtils.isNetworkAvailable(this)) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mX5WebView.loadUrl(mHomeUrl);
//                }
//            });
//        } else {
//            Toast.makeText(this, "当前网络未连接", Toast.LENGTH_SHORT).show();
//        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mX5WebView.loadUrl(mHomeUrl);
            }
        });
    }

    /**
     * 初始化设置
     */
    private boolean initSetting() {
        // 先检测参数有没有带
        mContext = this;
        return true;
    }

    /**
     * 启用硬件加速
     */
    private void initHardwareAccelerate() {
        try {
            if (Integer.parseInt(Build.VERSION.SDK) >= 11) {
                getWindow()
                        .setFlags(
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
        }
    }

    private void initView() {
        mWebviewRoot = (FrameLayout) findViewById(R.id.webview_root_fl);
        connect_error = (LinearLayout) findViewById(R.id.connect_error);
        mX5WebView = X5WebViewPool.getInstance().getWebView(mContext);
        mX5WebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mX5WebView.clearHistory();
        mX5WebView.getSettings().setJavaScriptEnabled(true);
        mX5WebView.getSettings().setCacheMode(com.tencent.smtt.sdk.WebSettings.LOAD_NO_CACHE);  //设置 缓存模式(true);
        mX5WebView.getSettings().setAppCacheEnabled(false);
        mX5WebView.getSettings().setSupportZoom(false);
        mX5WebView.getSettings().setUseWideViewPort(true);
        mX5WebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mX5WebView.getSettings().setDomStorageEnabled(true);
        mX5WebView.getSettings().setBuiltInZoomControls(false);
        mX5WebView.getSettings().setLayoutAlgorithm(com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm.NORMAL);
        mX5WebView.getSettings().setLoadWithOverviewMode(false);
        mX5WebView.getSettings().setUserAgentString("android_client");
        mX5WebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mX5WebView.getSettings().setDefaultFontSize(16);
        mX5WebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mX5WebView.getSettings().setPluginsEnabled(true);//支持所有版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mX5WebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        mWebviewRoot.addView(mX5WebView);
        mX5WebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mX5WebView.requestFocus();
        mX5WebView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });
        mX5WebView.addJavascriptInterface(new JsToBrowser(mContext, handler), "WebUtil");
        mX5WebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
//
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                // TODO Auto-generated method stub
                super.onReceivedTitle(view, title);
                Log.d("ANDROID_LAB", "TITLE=" + title);
                //The_title.setText(title);
            }

        });
        mX5WebView.setWebViewClient(new MyWebClient());
    }

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:// 已认证
                    Toast.makeText(mContext, "" + msg.obj, Toast.LENGTH_SHORT).show();
//                    if (msg.obj.toString().equals("保存成功")) {
//                        X5WebViewActivity.this.finish();
//                    }
                    break;
                case 2:
                    openWebPage();
                    break;
                case 3:
                    String content = (String) msg.obj;
                    break;
                default:
                    break;
            }
        }
    };

    class JsToBrowser {
        Context mContext;

        public JsToBrowser(Context m, Handler h) {
            mContext = m;
            handler = h;
        }

        //javascript中需要调用该方法
        @JavascriptInterface
        public void refreshData() {
            System.out.println("111----refreshData------------>");
            openWebPage();
        }

        /**
         * 日志打印
         * @param
         */
        @JavascriptInterface
        public void sendMsg(String text) {
            System.out.println("sendMsg---->"+text);
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        }

        //js中调的方法
        @JavascriptInterface
        public void speak(String content) {
            System.out.println("sendMsg---->"+content);
            Message msg = new Message();
            msg.obj = content;
            msg.what = 3;
            handler.sendMessage(msg);
        }
    }

    //==============================================================
    // 遥控器支持

    private static final int KEYCODE_ENUM = 82;         // 菜单按钮

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_ENUM) {
            showMenuDialog();
        }
        return super.onKeyUp(keyCode, event);
    }

    private void showMenuDialog(){
        //释放资源
        X5WebViewPool.getInstance().removeWebView(mWebviewRoot, mX5WebView);
        // 布局管理
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 设置自定义的样
        if (menuDialog == null){
            menuDialog = new Dialog(this, R.style.allergic_show_style);
        }

        // 布局选择
        View layout = inflater.inflate(R.layout.layout_menu_dialog, null);

        // 把布局添加置到dialog上
        menuDialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        /** 设置dialog相关的显示参数和动画效果 **/
//        // 不允许点外面取消，返回键也不生效
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(false);

        WindowManager m = this.getWindowManager();
        // 为获取屏幕宽、高
        Display d = m.getDefaultDisplay();
        // 获取对话框当前的参数值
        WindowManager.LayoutParams p = menuDialog.getWindow().getAttributes();

        // 宽度设置为屏幕(0.0-1.0)宽度满屏
        p.width = (int) (d.getWidth() * 0.5);

        // 设置生效
        menuDialog.getWindow().setAttributes(p);

        Window window = menuDialog.getWindow();
        // 此处可以设置dialog显示的位置
        window.setGravity(Gravity.CENTER);

        // 重新部署这个布局
        menuDialog.setContentView(layout);
        // 不允许点外面取消，返回键也不生效
        menuDialog.setCanceledOnTouchOutside(false);
        menuDialog.setCancelable(false);

        // 设置
        setModifySetting(layout);
        // 最后显示
        menuDialog.show();
    }

    /**
     * 界面设置
     */
    private void setModifySetting(final View layout) {
        // 绑定
        Button btn_connect = (Button) layout.findViewById(R.id.btn_connect);
        final EditText ed_url_path = (EditText)layout.findViewById(R.id.url_path);
        Button btn_close = (Button) layout.findViewById(R.id.btn_close);

        String full_version = getVersionCodeName();
        TextView tv_version = (TextView)layout.findViewById(R.id.tv_version);
        tv_version.setText(full_version);

        mHomeUrl = SharedPreferencesUtils.getHttpUrl(X5WebViewActivity.this);
        ed_url_path.setText(mHomeUrl);
        ed_url_path.setSelection(mHomeUrl.length());//将光标移至文字末尾
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url_path = ed_url_path.getText().toString();
                if (!TextUtils.isEmpty(url_path)){
                    menuDialog.dismiss();
                    mHomeUrl = url_path;
                    SharedPreferencesUtils.put(mContext,SharedPreferencesUtils.HTTP_URL,mHomeUrl);
                    openWebPage();
                }else {
                    Toast.makeText(mContext, "有效地址不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuDialog.dismiss();
                finish();
            }
        });

    }


    /**
     * 获得当前app的版本号名   异常返回1.001
     */
    public String getVersionCodeName() {
        PackageManager packageManager = getPackageManager();
        String versionCodeName;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            versionCodeName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionCodeName = "1.000";
        }
        return versionCodeName;
    }


    /**
     * 返回键监听
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (mX5WebView != null && mX5WebView.canGoBack()) {
//                mX5WebView.goBack();
//                return true;
//            } else {
//                return super.onKeyDown(keyCode, event);
//            }
            X5WebViewActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    class MyWebClient extends WebViewClient {
        /**
         * 防止加载网页时调起系统浏览器
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            WebView.HitTestResult hitTestResult = view.getHitTestResult();
            //hitTestResult==null解决重定向问题(刷新后不能退出的bug)
            System.out.println("2222222--hitTestResult-----"+hitTestResult.toString());
            if (!TextUtils.isEmpty(url) && hitTestResult == null) {
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            SimpleHUD.showProgressLoading(getActivity(), KProgressHUD.Style.PIE_DETERMINATE);
            super.onPageStarted(view, url, favicon);
            connect_error.setVisibility(View.INVISIBLE);
            mX5WebView.setVisibility(View.VISIBLE);
//            startSocket();
        }

        @Override
        public void onPageFinished(WebView view, String url) {//当页面加载完成 备用传值
            super.onPageFinished(view, url);
            mX5WebView.getSettings().setBlockNetworkImage(false);//通过图片的延迟载入，让网页能更快地显示。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//当Android SDK>=4.4时
//                callEvaluateJavascript(view);
            } else {
//                callMethod(view);
            }
        }

        @Override
        public void onReceivedError(WebView webView, int i, String s, String s1) {
            super.onReceivedError(webView, i, s, s1);
            connect_error.setVisibility(View.VISIBLE);
            mX5WebView.setVisibility(View.INVISIBLE);
//            HemoApplication.getInstance().webSocketDestroy();
            Toast.makeText(mContext, "加载失败，请重新设置地址", Toast.LENGTH_SHORT).show();
        }
    }


}
