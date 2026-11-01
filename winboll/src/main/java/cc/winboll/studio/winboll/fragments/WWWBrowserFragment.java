package cc.winboll.studio.winboll.fragments;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe WWWBrowser 浏览器启动窗口 Fragment（Java 7 语法）。
 * 由原 MainActivity 简易浏览器窗口改造为 Fragment，
 * 遵循 DrawerFragmentActivity 架构，通过 addFragment()/showFragment() 组装。
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.media.MidiBridge;
import cc.winboll.studio.winboll.services.MediaPlaybackService;

public class WWWBrowserFragment extends Fragment implements View.OnClickListener,
    MediaPlaybackService.PlaybackController {

    /** 默认主页（Settings 首页设置可覆盖） */
    public static final String DEFAULT_HOME_URL = "https://www.winboll.cc/";
    /** 旧版默认主页（域名已失效），检测到持久化了该值时自动迁移到新默认主页 */
    public static final String LEGACY_DEFAULT_HOME_URL = "https://www.studio.winboll.cc";
    private static final String ARG_INITIAL_URL = "initial_url";
    /** 自定义首页 SharedPreferences 文件名（与 SettingsActivity 共用） */
    public static final String PREFS_NAME = "winboll_prefs";
    /** 自定义首页 URL 键 */
    public static final String KEY_HOME_URL = "www_browser_home_url";
    // Fragment 标题（主窗口副标题使用，Fragment 的标题就是副标题）
    public static final String FRAGMENT_TITLE = "WWWBrowser";

    private WebView mWebView;
    private EditText mEtUrl;
    private Button mBtnBack;
    private Button mBtnForward;
    private Button mBtnHome;
    private Button mBtnRefresh;
    private Button mBtnLoad;

    // ------------------- Web page audio background playback support -------------------
    // Injected JS patches AudioContext construction and media element events so that
    // playback state can be probed without exposing a JavascriptInterface bridge.
    private static final String JS_AUDIO_PATCH =
        "(function(){"
        + "function patchWindow(w){"
        + " try{"
        + "  if(!w||w.__wbPatched){return;}"
        + "  w.__wbPatched=true;"
        + "  w.__wbStarted=0;"
        + "  w.__wbSuspended=false;"
        + "  w.__wbMediaPlaying=0;"
        + "  w.__wbCtxs=[];"
        // Counting semantics: start() increments __wbStarted; the 'ended' event
        // decrements it. Scheduled stop(when) must NOT decrement at call time
        // (Web Audio allows stop() to be scheduled in the future).
        + "  function hookSource(src){"
        + "   var started=false;"
        + "   var dec=function(){if(started){started=false;w.__wbStarted=Math.max(0,(w.__wbStarted||0)-1);}};"
        + "   try{"
        + "    var oStart=src.start;"
        + "    if(oStart){src.start=function(){if(!started){started=true;w.__wbStarted=(w.__wbStarted||0)+1;}return oStart.apply(src,arguments);};}"
        + "    if(src.addEventListener){src.addEventListener('ended',dec,false);}"
        + "   }catch(e){}"
        + "   return src;"
        + "  }"
        + "  function hookProto(P){"
        + "   try{"
        + "    if(!P||P.__wbHooked){return;}"
        + "    P.__wbHooked=true;"
        + "    var ms=['createOscillator','createBufferSource','createMediaElementSource'];"
        + "    for(var i=0;i<ms.length;i++){"
        + "     (function(m){"
        + "      var orig=P[m];"
        + "      if(typeof orig!=='function'){return;}"
        + "      P[m]=function(){"
        + "       try{if(this&&w.__wbCtxs.indexOf(this)<0){w.__wbCtxs.push(this);}}catch(e){}"
        + "       return hookSource(orig.apply(this,arguments));"
        + "      };"
        + "     })(ms[i]);"
        + "    }"
        + "   }catch(e){}"
        + "  }"
        + "  try{"
        + "   var C=w.AudioContext||w.webkitAudioContext;"
        + "   if(C){hookProto(C.prototype);}"
        + "   if(w.AudioContext&&w.webkitAudioContext&&w.webkitAudioContext.prototype!==w.AudioContext.prototype){hookProto(w.webkitAudioContext.prototype);}"
        + "   var Orig=w.AudioContext||w.webkitAudioContext;"
        + "   if(Orig&&!Orig.__wbWrapped){"
        + "    var W=function(){var c=new Orig();if(w.__wbCtxs.indexOf(c)<0){w.__wbCtxs.push(c);}return c;};"
        + "    W.prototype=Orig.prototype;"
        + "    W.__wbWrapped=true;"
        + "    if(w.AudioContext){w.AudioContext=W;}"
        + "    if(w.webkitAudioContext){w.webkitAudioContext=W;}"
        + "   }"
        + "  }catch(e){}"
        + "  try{"
        + "   var ps=['play','playing'];"
        + "   for(var p=0;p<ps.length;p++){(function(t){w.document.addEventListener(t,function(){w.__wbMediaPlaying=1;},true);})(ps[p]);}"
        + "   var qs=['pause','ended','emptied','abort'];"
        + "   for(var q=0;q<qs.length;q++){(function(t){w.document.addEventListener(t,function(){w.__wbMediaPlaying=0;},true);})(qs[q]);}"
        + "  }catch(e){}"
        + " }catch(e){}"
        + "}"
        + "function walk(w,depth){"
        + " try{"
        + "  patchWindow(w);"
        + "  if(depth<3){"
        + "   var els=w.document.querySelectorAll('iframe,frame');"
        + "   for(var i=0;i<els.length;i++){try{if(els[i].contentWindow){walk(els[i].contentWindow,depth+1);}}catch(e){}}"
        + "   for(var j=0;j<w.frames.length;j++){try{walk(w.frames[j],depth+1);}catch(e){}}"
        + "  }"
        + " }catch(e){}"
        + "}"
        + "walk(window,0);"
        + "})()";

    // Probe JS: 'Y' + diagnostics when any frame produces audio.
    // Playing when: media element playing, OR (sources started AND at least one
    // tracked AudioContext is running). A suspended context means the audio is
    // silenced (user pause/stop), regardless of sources the page keeps creating.
    private static final String JS_AUDIO_PROBE =
        "(function(){"
        + "function probeFrame(w,depth,r){"
        + " try{"
        + "  if(w.__wbPatched){r.patched=1;}"
        + "  var ms=w.document.querySelectorAll('audio,video');"
        + "  for(var i=0;i<ms.length;i++){var el=ms[i];if(el&&!el.paused&&!el.ended){r.playing=1;}}"
        + "  var st=w.__wbStarted||0;"
        + "  r.started+=st;"
        + "  var running=false;"
        + "  if(w.__wbCtxs){for(var k=0;k<w.__wbCtxs.length;k++){try{if(w.__wbCtxs[k].state==='running'){running=true;}}catch(e){}}}"
        + "  if(st>0&&running&&!w.__wbSuspended){r.playing=1;}"
        + "  if(w.__wbMediaPlaying){r.playing=1;}"
        + "  r.ctxs+=(w.__wbCtxs?w.__wbCtxs.length:0);"
        + "  if(depth<3){for(var j=0;j<w.frames.length;j++){try{probeFrame(w.frames[j],depth+1,r);}catch(e){}}}"
        + " }catch(e){}"
        + "}"
        + "var r={playing:0,started:0,ctxs:0,patched:0};"
        + "probeFrame(window,0,r);"
        + "return (r.playing?'Y':'N')+'|patch='+r.patched+',started='+r.started+',ctxs='+r.ctxs;"
        + "})()";

    // Pause all web page audio in every same-origin frame (suspend flag keeps state).
    private static final String JS_AUDIO_PAUSE_ALL =
        "(function(){"
        + "function eachWin(w,fn,depth){"
        + " try{fn(w);if(depth<3){for(var j=0;j<w.frames.length;j++){try{eachWin(w.frames[j],fn,depth+1);}catch(e){}}}}catch(e){}"
        + "}"
        + "eachWin(window,function(w){"
        + " w.__wbResumeEls=[];"
        + " var ms=w.document.querySelectorAll('audio,video');"
        + " for(var i=0;i<ms.length;i++){var el=ms[i];if(el&&!el.paused&&!el.ended){w.__wbResumeEls.push(el);el.pause();}}"
        + " if(w.__wbCtxs){for(var j=0;j<w.__wbCtxs.length;j++){try{w.__wbCtxs[j].suspend();}catch(e){}}}"
        + " w.__wbSuspended=true;"
        + " w.__wbMediaPlaying=0;"
        + "},0);"
        + "return '1';"
        + "})()";

    // Resume web page audio paused by JS_AUDIO_PAUSE_ALL (all frames).
    private static final String JS_AUDIO_PLAY_ALL =
        "(function(){"
        + "function eachWin(w,fn,depth){"
        + " try{fn(w);if(depth<3){for(var j=0;j<w.frames.length;j++){try{eachWin(w.frames[j],fn,depth+1);}catch(e){}}}}catch(e){}"
        + "}"
        + "eachWin(window,function(w){"
        + " if(w.__wbResumeEls){for(var i=0;i<w.__wbResumeEls.length;i++){try{w.__wbResumeEls[i].play();}catch(e){}}}"
        + " if(w.__wbCtxs){for(var j=0;j<w.__wbCtxs.length;j++){try{w.__wbCtxs[j].resume();}catch(e){}}}"
        + " w.__wbSuspended=false;"
        + "},0);"
        + "return '1';"
        + "})()";

    // Stop web page audio in every frame: pause+rewind elements, suspend Web Audio, reset counters.
    private static final String JS_AUDIO_STOP_ALL =
        "(function(){"
        + "function eachWin(w,fn,depth){"
        + " try{fn(w);if(depth<3){for(var j=0;j<w.frames.length;j++){try{eachWin(w.frames[j],fn,depth+1);}catch(e){}}}}catch(e){}"
        + "}"
        + "eachWin(window,function(w){"
        + " var ms=w.document.querySelectorAll('audio,video');"
        + " for(var i=0;i<ms.length;i++){var el=ms[i];if(el){try{el.pause();el.currentTime=0;}catch(e){}}}"
        + " if(w.__wbCtxs){for(var j=0;j<w.__wbCtxs.length;j++){try{w.__wbCtxs[j].suspend();}catch(e){}}}"
        + " w.__wbResumeEls=[];"
        + " w.__wbStarted=0;"
        + " w.__wbSuspended=false;"
        + " w.__wbMediaPlaying=0;"
        + "},0);"
        + "return '1';"
        + "})()";

    // Audio state polling (runs on main thread; continues while app is backgrounded
    // so the media notification stays in sync with the page audio).
    private static final long AUDIO_POLL_INTERVAL_MS = 800;
    private final Handler mAudioPollHandler = new Handler();
    private boolean mLastAudioPlaying = false;
    private int mStoppedProbes = 0;
    private boolean mAudioPollRunning = false;
    private final Runnable mAudioPollRunnable = new Runnable() {
        @Override
        public void run() {
            pollAudioState();
            mAudioPollHandler.postDelayed(this, AUDIO_POLL_INTERVAL_MS);
        }
    };

    // 工厂方法（Java 7 显式工厂模式）
    public static WWWBrowserFragment newInstance() {
        return new WWWBrowserFragment();
    }

    // 创建带初始URL的实例（供外部应用 ACTION_VIEW 传入时使用）
    public static WWWBrowserFragment newInstance(String initialUrl) {
        WWWBrowserFragment fragment = new WWWBrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_URL, initialUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载 WWWBrowser 窗口布局（Java 7 显式强转）
        View view = inflater.inflate(R.layout.fragment_www_browser, container, false);

        initViews(view);
        initEvents();
        initWebView();

        // Register as the media playback controller and start audio state polling.
        MidiBridge.init(getActivity());
        // action:home without a usable url falls back to the browser home
        // (spec §3.1) — feed it the same home the UI uses.
        MidiBridge.setDefaultHomeUrl(getHomeUrl());
        MediaPlaybackService.setPlaybackController(this);
        startAudioPoll();

        // 外部传入的初始 URL，无则使用主页（优先读取 Settings 里自定义的首页）
        String initialUrl = getHomeUrl();
        if (getArguments() != null) {
            String arg = getArguments().getString(ARG_INITIAL_URL);
            if (arg != null && !arg.isEmpty()) {
                initialUrl = arg;
            }
        }
        mEtUrl.setText(initialUrl);
        loadUrl(initialUrl);
        return view;
    }

    /**
     * 获取 WWWBrowser 首页地址：优先读取 Settings 保存的自定义首页，未设置时使用默认主页。
     * 兼容处理：已保存的值为旧版默认地址（域名已失效）时，自动清除该设置并回落到新默认主页。
     */
    private String getHomeUrl() {
        String url = DEFAULT_HOME_URL;
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String saved = prefs.getString(KEY_HOME_URL, "");
            if (!TextUtils.isEmpty(saved)) {
                if (isLegacyDefaultHomeUrl(saved)) {
                    prefs.edit().remove(KEY_HOME_URL).apply();
                } else {
                    url = saved;
                }
            }
        }
        return url;
    }

    /**
     * 判断给定地址是否为旧版默认主页（兼容带/不带末尾斜杠两种写法）
     */
    public static boolean isLegacyDefaultHomeUrl(String url) {
        if (url == null) {
            return false;
        }
        String trimmed = url.trim();
        return trimmed.equals(LEGACY_DEFAULT_HOME_URL)
            || trimmed.equals(LEGACY_DEFAULT_HOME_URL + "/");
    }

    /**
     * 初始化控件
     */
    private void initViews(View view) {
        mEtUrl = (EditText) view.findViewById(R.id.et_url);
        mWebView = (WebView) view.findViewById(R.id.webview);
        mBtnBack = (Button) view.findViewById(R.id.btn_back);
        mBtnForward = (Button) view.findViewById(R.id.btn_forward);
        mBtnHome = (Button) view.findViewById(R.id.btn_home);
        mBtnRefresh = (Button) view.findViewById(R.id.btn_refresh);
        mBtnLoad = (Button) view.findViewById(R.id.btn_load);
    }

    /**
     * 绑定点击事件（Java 7 匿名内部类，无 Lambda）
     */
    private void initEvents() {
        mBtnBack.setOnClickListener(this);
        mBtnForward.setOnClickListener(this);
        mBtnHome.setOnClickListener(this);
        mBtnRefresh.setOnClickListener(this);
        mBtnLoad.setOnClickListener(this);

        // 输入框软键盘“前往”按钮事件
        mEtUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    loadUrlFromInput();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 初始化 WebView
     */
    private void initWebView() {
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        // Allow audio to start without a direct user gesture so that the
        // notification play button can resume background web page audio.
        ws.setMediaPlaybackRequiresUserGesture(false);
        // MIDI bridge (spec v1.2 §5): bind the WebView for native -> web control
        // dispatch. The interface must be registered BEFORE the first loadUrl so
        // it is bound when the page's window object is created (injecting in
        // onPageStarted races fast/cached loads and stays invisible). Per-page
        // whitelist add/remove below keeps it off non-whitelisted pages.
        MidiBridge.attach(mWebView);
        mWebView.addJavascriptInterface(MidiBridge.getJavascriptInterface(),
            MidiBridge.JS_INTERFACE_NAME);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // MIDI bridge (spec §5/§8): inject the native bridge only for
                // whitelisted URL hosts, remove it elsewhere. Also resets the
                // per-page protocol engagement flag.
                MidiBridge.onPageStarted(url);
                if (MidiBridge.isWhitelistedUrl(url)) {
                    view.addJavascriptInterface(MidiBridge.getJavascriptInterface(),
                        MidiBridge.JS_INTERFACE_NAME);
                    android.util.Log.d("MidiBridge", "bridge injected for " + url);
                } else {
                    view.removeJavascriptInterface(MidiBridge.JS_INTERFACE_NAME);
                    android.util.Log.d("MidiBridge", "bridge removed (non-whitelist) " + url);
                }
                // Inject as early as possible so AudioContext instances created by
                // page scripts are wrapped for playback state probing.
                injectAudioPatch();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Re-inject (idempotent) for pages whose scripts ran before the patch.
                injectAudioPatch();
                // Diagnostic: report bridge visibility to the console (WBWebConsole).
                view.evaluateJavascript(
                    "(function(){try{console.log('WB-BRIDGE-CHECK '+(typeof window.WinBollMedia));}catch(e){}})()", null);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                android.util.Log.d("WBWebConsole", message + " @" + sourceID + ":" + lineNumber);
            }
        });
    }

    /**
     * Inject the audio state patch JS into the current page (idempotent).
     */
    private void injectAudioPatch() {
        if (mWebView != null) {
            mWebView.evaluateJavascript(JS_AUDIO_PATCH, null);
        }
    }

    /**
     * Start periodic audio state polling (continues in background).
     */
    private void startAudioPoll() {
        if (!mAudioPollRunning) {
            mAudioPollRunning = true;
            mAudioPollHandler.post(mAudioPollRunnable);
        }
    }

    /**
     * Probe the page for active audio and sync state to MediaPlaybackService.
     */
    private void pollAudioState() {
        if (mWebView == null) {
            return;
        }
        // MIDI bridge engaged (protocol page) or cover-hosted: the bridge and
        // the native engine own the session state. The heuristic probe must not
        // fight them (spec v1.2 §6 routing).
        if (MidiBridge.isProtocolEngaged() || MidiBridge.isHosting()) {
            return;
        }
        // Belt and braces: keep the patch installed even if early injection raced
        // the page scripts (patch itself is idempotent).
        injectAudioPatch();
        mWebView.evaluateJavascript(JS_AUDIO_PROBE, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (cc.winboll.studio.winboll.App.isDebugging()) {
                    android.util.Log.d("WBWebAudio", "probe=" + value);
                }
                boolean playing = value != null && value.indexOf('Y') >= 0;
                if (playing) {
                    mStoppedProbes = 0;
                }
                // Debounce: only report "stopped" after 2 consecutive stopped
                // probes so brief gaps between synthesized notes do not flicker
                // the media notification.
                if (playing != mLastAudioPlaying) {
                    if (playing) {
                        mLastAudioPlaying = true;
                        mStoppedProbes = 0;
                        if (getActivity() != null) {
                            MediaPlaybackService.pushState(getActivity().getApplicationContext(),
                                true, "WinBoLL+ 网页音频");
                        }
                    } else {
                        mStoppedProbes++;
                        if (mStoppedProbes >= 2) {
                            mLastAudioPlaying = false;
                            mStoppedProbes = 0;
                            if (getActivity() != null) {
                                MediaPlaybackService.pushState(getActivity().getApplicationContext(),
                                    false, "WinBoLL+ 网页音频");
                            }
                        }
                    }
                } else if (!playing) {
                    mStoppedProbes = 0;
                }
            }
        });
    }

    // ------------------- MediaPlaybackService.PlaybackController -------------------
    // Routing (spec v1.2 §6): cover-hosted playback drives the native MIDI
    // engine directly; protocol pages receive __midiControl commands; other
    // pages fall back to the heuristic injected-JS suspend/resume patch.

    @Override
    public void onPlayRequested() {
        if (MidiBridge.onMediaPlayCommand()) {
            return;
        }
        if (mWebView != null) {
            mWebView.evaluateJavascript(JS_AUDIO_PLAY_ALL, null);
        }
    }

    @Override
    public void onPauseRequested() {
        if (MidiBridge.onMediaPauseCommand()) {
            return;
        }
        if (mWebView != null) {
            mWebView.evaluateJavascript(JS_AUDIO_PAUSE_ALL, null);
        }
    }

    @Override
    public void onStopRequested() {
        if (MidiBridge.onMediaStopCommand()) {
            return;
        }
        if (mWebView != null) {
            mWebView.evaluateJavascript(JS_AUDIO_STOP_ALL, null);
        }
    }

    @Override
    public void onSeekRequested(long positionMs) {
        // Protocol/hosted only: the heuristic channel has no position concept.
        MidiBridge.onMediaSeekCommand(positionMs);
    }

    @Override
    public void onSkipToNextRequested() {
        // Skip exists only in hosted mode (spec §6).
        MidiBridge.onMediaSkipCommand(true);
    }

    @Override
    public void onSkipToPreviousRequested() {
        MidiBridge.onMediaSkipCommand(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_load) {
            loadUrlFromInput();
        } else if (id == R.id.btn_refresh) {
            mWebView.reload();
        } else if (id == R.id.btn_back) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            }
        } else if (id == R.id.btn_forward) {
            if (mWebView.canGoForward()) {
                mWebView.goForward();
            }
        } else if (id == R.id.btn_home) {
            loadHomePage();
        }
    }

    /**
     * 从输入框获取 URL 并加载
     */
    private void loadUrlFromInput() {
        if (mEtUrl == null) {
            showToast("控件初始化失败");
            return;
        }
        String url = mEtUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            showToast("请输入网址");
            return;
        }
        loadUrl(url);
        hideSoftKeyboard();
    }

    /**
     * 加载 URL，自动补全协议头
     */
    private void loadUrl(String url) {
        if (mWebView == null || TextUtils.isEmpty(url)) {
            return;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        mWebView.loadUrl(url);
    }

    /**
     * 加载首页：优先 Settings 自定义首页，未设置时使用默认主页。
     * 供主窗口 HOME 菜单调用。
     */
    public void loadHomePage() {
        if (mWebView == null || mEtUrl == null) {
            return;
        }
        String homeUrl = getHomeUrl();
        mEtUrl.setText(homeUrl);
        loadUrl(homeUrl);
    }

    /**
     * 供宿主 Activity 处理返回键：WebView 可后退则后退并返回 true
     */
    public boolean handleBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    /**
     * 获取 Fragment 标题
     */
    public String getFragmentTitle() {
        return FRAGMENT_TITLE;
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftKeyboard() {
        if (getActivity() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * 显示 Toast 提示
     */
    private void showToast(String msg) {
        if (getActivity() == null || msg == null) {
            return;
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    // 生命周期管理：销毁 WebView，避免内存泄漏

    @Override
    public void onResume() {
        super.onResume();
        // MIDI bridge spec §8: the page is visible again — flush any control
        // command that was queued while hidden.
        MidiBridge.setPageVisible(true);
    }

    /**
     * Drawer navigation switches fragments with show/hide instead of replace,
     * so page visibility must also be tracked here (spec §8 dispatch gate).
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        MidiBridge.setPageVisible(!hidden);
        if (hidden) {
            // Same §4.3-③ nudge as onPause: leaving a playing protocol page
            // steers the session toward cover hosting.
            MidiBridge.nudgeHostRequest();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // MIDI bridge spec §8: page hidden — non-hosted control commands queue.
        MidiBridge.setPageVisible(false);
        // MIDI bridge spec §4.3-③: app going to the background while a protocol
        // page is playing triggers the browser-initiated hosting negotiation
        // (the page's own visibilitychange is unreliable in WebView).
        MidiBridge.nudgeHostRequest();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop audio polling and detach from the media playback service.
        mAudioPollRunning = false;
        mAudioPollHandler.removeCallbacksAndMessages(null);
        MediaPlaybackService.setPlaybackController(null);
        // MIDI bridge: unbind the WebView. Hosted playback keeps running in the
        // foreground service (spec §4.2 D-2), so only the heuristic path tears
        // the notification down with the page.
        boolean hosting = MidiBridge.isHosting();
        MidiBridge.detach(mWebView);
        if (!hosting && mLastAudioPlaying && getActivity() != null) {
            // Page audio dies with the WebView: dismiss the media notification.
            mLastAudioPlaying = false;
            MediaPlaybackService.stopService(getActivity().getApplicationContext());
        }
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        mEtUrl = null;
        mBtnBack = null;
        mBtnForward = null;
        mBtnHome = null;
        mBtnRefresh = null;
        mBtnLoad = null;
    }
}
