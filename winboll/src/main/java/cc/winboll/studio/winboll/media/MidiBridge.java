package cc.winboll.studio.winboll.media;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe MIDI player bridge endpoint (MIDI bridge spec v1.2, phase 2).
 * Android side of the bidirectional protocol between the web MIDI module and
 * the WinBoLL browser:
 *   web -> native: window.WinBollMedia.postState(json)  (__midiPlayer messages)
 *   native -> web: window.__midiDispatchControl(json)   (__midiControl messages)
 * Owns the message router, the cover-hosting state machine (spec §4) and the
 * playlist cache. The hosted-mode sound comes from HostedMidiPlayer (native
 * MIDI engine, spec §10 phase-2). Java 7 syntax.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cc.winboll.studio.winboll.services.MediaPlaybackService;

public final class MidiBridge {

    public static final String TAG = "MidiBridge";

    /** Injected object name expected by the web bridge (spec §5). */
    public static final String JS_INTERFACE_NAME = "WinBollMedia";

    /** Protocol version this endpoint implements (spec §3.1 `v`, currently 1). */
    public static final int PROTOCOL_VERSION = 1;

    /** Max accepted postState payload (spec §8: <=16KB, playlist may be large). */
    private static final int MAX_POST_STATE_CHARS = 16384;

    /**
     * How long a host request waits for the trailing playlist message before
     * giving up (spec §4.3-②: the page sends host first and pushPlaylist()
     * right after, so the playlist may still be in flight when the host
     * message is processed — observed 2026-11-03 as "hosting aborted: no
     * playlist entry" racing the 9KB playlist parse).
     */
    private static final long HOST_PLAYLIST_WAIT_MS = 2000;

    /**
     * URL host whitelist for bridge injection (spec §8: www.winboll.cc,
     * winboll.cc and the intranet management domains mirroring the web-side
     * ALLOWED_MANAGER_DOMAINS config).
     */
    private static final String[] HOST_WHITELIST = {
        "www.winboll.cc",
        "winboll.cc",
        "develop.winboll.cc", // develop/test site (HTTP Auth test station)
        "management-develop.winboll.cc",
        "management-develop.studio.winboll",
        "develop.studio.winboll",
        "localhost",
        "127.0.0.1",
        "10.0.2.2" // emulator alias for the host loopback (dev instances)
    };

    private static final String PREFS_NAME = "winboll_media_bridge";
    private static final String KEY_PLAYLIST_JSON = "playlist_json";
    private static final String KEY_HOSTED = "hosted";
    private static final String KEY_HOST_STATE = "host_state";
    private static final String KEY_HOST_TRACK_ID = "host_track_id";
    private static final String KEY_HOST_TITLE = "host_title";
    private static final String KEY_HOST_POS = "host_pos";
    private static final String KEY_HOST_RATE = "host_rate";
    private static final String KEY_HOST_LOOP = "host_loop";

    /** Playback states, aligned with android.media.session.PlaybackState. */
    public static final int STATE_STOPPED = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;

    // ------------------- singleton state -------------------

    private static final Handler sMain = new Handler(Looper.getMainLooper());
    private static volatile WebView sWebView;
    private static volatile boolean sEngaged = false;
    private static volatile boolean sHosting = false;

    /** Page visible/foreground flag (spec §8: non-hosted control is dispatched
     *  only while the page is visible; hidden commands queue up and fall back
     *  to cover-hosting takeover). Fed from the fragment lifecycle. */
    private static volatile boolean sPageVisible = true;

    /** Fallback home URL for action:home without a usable url (spec §3.1). */
    private static volatile String sDefaultHomeUrl = "https://www.winboll.cc/";

    // Last non-hosted web playback snapshot (protocol §3.1 state messages).
    private static volatile int sWebState = STATE_STOPPED;
    private static volatile long sWebPositionMs = 0;
    private static volatile long sWebDurationMs = 0;
    private static volatile String sWebTitle = "WinBoLL MIDI 播放器";
    private static volatile String sWebArtist = "WinBoLL MIDI 播放器";
    private static volatile String sWebTrackId = "";
    private static volatile float sWebRate = 1.0f;
    private static volatile boolean sWebLoop = false;

    // Host request snapshot (web reference params at the time hosting started).
    private static volatile int sHostWebState = STATE_PLAYING;
    private static volatile long sHostWebPositionMs = 0;
    private static volatile float sHostWebRate = 1.0f;
    private static volatile boolean sHostWebLoop = false;

    // Hidden-page control queue (spec §8): non-hosted control is dispatched
    // only while the page is visible; while hidden the command waits here for
    // the cover-hosting takeover (spec §4.3-③) and lands on the native engine.
    private static volatile String sQueuedCmd = null;
    private static volatile long sQueuedCmdPos = -1;

    /** Pending host request waiting for the trailing playlist (spec §4.3-②). */
    private static boolean sHostPendingRetry = false;
    private static String sHostPendingTrackId;
    private static String sHostPendingTitle;
    private static long sHostPendingStartMs;
    private static float sHostPendingRate = 1.0f;
    private static boolean sHostPendingLoop;
    private static boolean sHostPendingPaused;

    /** Spec §9 idiom: a host negotiation that gets no ACK within 3s falls
     *  back to non-hosted mode — the queued command is delivered anyway. */
    private static final int HIDDEN_CMD_FALLBACK_MS = 3000;

    private static volatile String sPlaylistRev = "";
    private static final List<HostedMidiPlayer.Track> sTracks =
        new ArrayList<HostedMidiPlayer.Track>();
    private static volatile HostedMidiPlayer sEngine;
    private static boolean sPositionTickRunning = false;
    private static int sTickCount = 0;

    private MidiBridge() {
    }

    // ------------------- whitelist / injection (spec §5, §8) -------------------

    /** True when the URL host is allowed to receive the WinBollMedia bridge. */
    public static boolean isWhitelistedHost(String host) {
        if (host == null) {
            return false;
        }
        String h = host.toLowerCase();
        for (int i = 0; i < HOST_WHITELIST.length; i++) {
            if (HOST_WHITELIST[i].equals(h)) {
                return true;
            }
        }
        return false;
    }

    /** Convenience: whitelist check for a full page URL. */
    public static boolean isWhitelistedUrl(String url) {
        if (url == null) {
            return false;
        }
        try {
            return isWhitelistedHost(Uri.parse(url).getHost());
        } catch (Exception e) {
            return false;
        }
    }

    /** The object to inject as window.WinBollMedia. */
    public static Object getJavascriptInterface() {
        return sJsInterface;
    }

    /**
     * Native bridge exposed to the page (spec §5). Public named class so the
     * WebView reflection binder reliably sees the annotated method.
     */
    public static class WinBollMediaBridge {
        @JavascriptInterface
        public void postState(String json) {
            Log.d(TAG, "postState received, len=" + (json == null ? -1 : json.length()));
            MidiBridge.handlePostState(json);
        }
    }

    private static final WinBollMediaBridge sJsInterface = new WinBollMediaBridge();

    /** Bind the WebView used for native -> web control dispatch. */
    public static void attach(WebView webView) {
        sWebView = webView;
    }

    /** Unbind the WebView (hosted playback may continue without it). */
    public static void detach(WebView webView) {
        if (sWebView == webView) {
            sWebView = null;
        }
    }

    /**
     * Page visibility / activity foreground state (spec §8: non-hosted control
     * is dispatched only while the page is visible; hidden commands queue and
     * steer the session toward cover hosting). Fed from the fragment's
     * onResume/onPause/onHiddenChanged.
     */
    public static void setPageVisible(boolean visible) {
        boolean was = sPageVisible;
        sPageVisible = visible;
        if (visible && !was) {
            // Back to the foreground: a queued hidden command can now be
            // dispatched normally if hosting never took over.
            flushQueuedCommandIfNeeded();
        }
    }

    /** True when the page is visible/foreground (spec §8 dispatch gate). */
    public static boolean isPageVisible() {
        return sPageVisible;
    }

    /** Fallback home URL for action:home without a usable url (spec §3.1). */
    public static void setDefaultHomeUrl(String url) {
        if (url != null && url.length() > 0) {
            sDefaultHomeUrl = url;
        }
    }

    /** A new main-frame navigation started: the page must re-engage the bridge. */
    public static void onPageStarted(String url) {
        sEngaged = false;
        if (!isWhitelistedUrl(url)) {
            // Non-whitelist page: nothing to reset beyond engagement.
            return;
        }
    }

    /** True when the current page speaks the protocol (sticky until navigation). */
    public static boolean isProtocolEngaged() {
        return sEngaged;
    }

    /** True while the browser owns playback (cover hosting mode, spec §4). */
    public static boolean isHosting() {
        return sHosting;
    }

    /**
     * Browser-initiated hosting nudge (spec §4.3-③). The WebView does not
     * dispatch document visibilitychange reliably when the app goes to the
     * background, so the browser forwards a __midiHostAsk to embedded bridge
     * frames (or the top-level MidiBridge) while the page is playing; the page
     * then runs its normal host-request flow (§4.3-①/②).
     */
    public static void nudgeHostRequest() {
        nudgeHostRequest(false);
    }

    /**
     * Hosting nudge with optional force. {@code force} also nudges a page that
     * is not currently playing — used by the hidden-page control queue (spec
     * §8): a media command that cannot be dispatched to the hidden page is
     * answered by taking over playback, which needs a host request even from a
     * paused page (the engine then parks or plays per the queued command).
     */
    public static void nudgeHostRequest(boolean force) {
        Log.d(TAG, "nudgeHostRequest force=" + force + " engaged=" + sEngaged
            + " hosting=" + sHosting + " webState=" + sWebState);
        if (!sEngaged || sHosting) {
            return;
        }
        if (!force && sWebState != STATE_PLAYING) {
            return;
        }
        sMain.post(new Runnable() {
            @Override
            public void run() {
                WebView wv = sWebView;
                if (wv == null) {
                    return;
                }
                wv.evaluateJavascript(
                    "(function(){try{"
                        + "var els=document.querySelectorAll('iframe,frame');"
                        + "for(var i=0;i<els.length;i++){try{"
                        + "if(els[i].contentWindow){els[i].contentWindow.postMessage({__midiHostAsk:true},location.origin);}"
                        + "}catch(e){}}"
                        + "window.postMessage({__midiHostAsk:true},location.origin);"
                        + "if(window.MidiBridge&&window.MidiBridge.askHost){window.MidiBridge.askHost();}"
                        + "return 'nudged:'+els.length;"
                        + "}catch(e){return 'err:'+e;}})()", new android.webkit.ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d(TAG, "nudgeHostRequest eval=" + value);
                        }
                    });
            }
        });
    }

    // ------------------- hidden-page control queue (spec §8, §9) -------------------

    /**
     * Queue a media command that cannot be dispatched because the page is
     * hidden (spec §8) and steer the session toward cover hosting (§4.3-③):
     * the takeover then answers the command on the native engine. If no host
     * ACK arrives within {@link #HIDDEN_CMD_FALLBACK_MS}, the command is
     * delivered to the page anyway (spec §9 hosting capability fallback).
     */
    private static void queueHiddenCommand(String cmd, long positionMs) {
        Log.d(TAG, "queueHiddenCommand " + cmd + " pos=" + positionMs);
        sQueuedCmd = cmd;
        sQueuedCmdPos = positionMs;
        sMain.removeCallbacks(mHiddenCmdFallback);
        sMain.postDelayed(mHiddenCmdFallback, HIDDEN_CMD_FALLBACK_MS);
        nudgeHostRequest(true);
    }

    /** Drop the queued hidden-page command and its fallback timer. */
    private static void clearQueuedCommand() {
        sQueuedCmd = null;
        sQueuedCmdPos = -1;
        sMain.removeCallbacks(mHiddenCmdFallback);
    }

    /**
     * The page became visible again before hosting took over: deliver the
     * queued command through the normal non-hosted path (spec §8).
     */
    private static void flushQueuedCommandIfNeeded() {
        String cmd = sQueuedCmd;
        if (cmd == null) {
            return;
        }
        if (sHosting) {
            drainQueuedCommandToEngine();
            return;
        }
        long pos = sQueuedCmdPos;
        clearQueuedCommand();
        Log.d(TAG, "flushQueuedCommand " + cmd + " to page");
        sendControlSimple(cmd, pos);
    }

    /** The queued hidden-page command now lands on the native engine (spec §8). */
    private static void drainQueuedCommandToEngine() {
        String cmd = sQueuedCmd;
        if (cmd == null) {
            return;
        }
        long pos = sQueuedCmdPos;
        clearQueuedCommand();
        HostedMidiPlayer engine = sEngine;
        if (engine == null) {
            return;
        }
        Log.d(TAG, "drainQueuedCommand " + cmd + " to engine");
        if ("play".equals(cmd)) {
            engine.play();
        } else if ("pause".equals(cmd)) {
            engine.pause();
        } else if ("stop".equals(cmd)) {
            // Stop also cancels the just-forced takeover (see onMediaStopCommand).
            stopHostedAndUnhost();
        } else if ("seek".equals(cmd)) {
            engine.seekTo(Math.max(0, pos));
        } else if ("next".equals(cmd)) {
            onMediaSkipCommand(true);
        } else if ("prev".equals(cmd)) {
            onMediaSkipCommand(false);
        }
    }

    /** Spec §9 3s fallback for the hidden-page command queue. */
    private static final Runnable mHiddenCmdFallback = new Runnable() {
        @Override
        public void run() {
            String cmd = sQueuedCmd;
            if (cmd == null) {
                return;
            }
            if (sHosting) {
                // Takeover raced the timer: land the command on the engine.
                drainQueuedCommandToEngine();
                return;
            }
            // No host ACK within the window: fall back to non-hosted mode and
            // deliver the command to the page anyway (spec §9).
            long pos = sQueuedCmdPos;
            clearQueuedCommand();
            Log.d(TAG, "hidden cmd fallback: dispatching " + cmd + " to page");
            sendControlSimple(cmd, pos);
        }
    };

    // ------------------- web -> native (spec §3.1) -------------------

    /**
     * JavascriptInterface entry (spec §5). Invoked on a JS bridge thread;
     * all handling is marshalled to the main thread. Parse failures are
     * silently ignored (spec §8).
     */
    static void handlePostState(final String json) {
        if (json == null || json.length() == 0 || json.length() > MAX_POST_STATE_CHARS) {
            return;
        }
        sMain.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // Authoritative whitelist enforcement (spec §8): the injected
                    // interface can briefly outlive a whitelist boundary around
                    // navigations, so re-check the live URL at message time.
                    WebView wv = sWebView;
                    if (wv != null) {
                        String liveUrl = wv.getUrl();
                        if (liveUrl != null && liveUrl.length() > 0
                            && !isWhitelistedUrl(liveUrl)) {
                            return;
                        }
                    }
                    JSONObject root = new JSONObject(json);
                    JSONObject p = root.optJSONObject("__midiPlayer");
                    if (p == null) {
                        return;
                    }
                    // Protocol version gate (spec §3.1 `v`, §9 compatibility):
                    // messages from other protocol versions are ignored whole —
                    // field semantics cannot be trusted across versions.
                    int v = p.optInt("v", -1);
                    if (v != PROTOCOL_VERSION) {
                        Log.d(TAG, "postState ignored: unsupported v=" + v);
                        return;
                    }
                    sEngaged = true;
                    String type = p.optString("type");
                    if ("state".equals(type)) {
                        handleState(p);
                    } else if ("action".equals(type)) {
                        handleAction(p);
                    } else if ("host".equals(type)) {
                        handleHost(p);
                    } else if ("playlist".equals(type)) {
                        handlePlaylist(p);
                    }
                    // Unknown types are ignored for forward compatibility (spec §9).
                } catch (Exception e) {
                    Log.w(TAG, "postState parse failed: " + e.getMessage());
                }
            }
        });
    }

    /** State report (spec §3.1). Ignored while hosting (web is silenced, §4.2). */
    private static void handleState(JSONObject p) {
        Log.i(TAG, "handleState" + (sHosting ? " (dropped)" : "") + " " + p.toString());
        if (sHosting) {
            return;
        }
        sWebState = parseState(p.optString("state"));
        sWebPositionMs = Math.max(0, p.optLong("positionMs", 0));
        sWebDurationMs = Math.max(0, p.optLong("durationMs", 0));
        String title = p.optString("title");
        if (title != null && title.length() > 0) {
            sWebTitle = title;
        }
        String artist = p.optString("artist");
        if (artist != null && artist.length() > 0) {
            sWebArtist = artist;
        }
        String trackId = p.optString("trackId");
        if (trackId != null) {
            sWebTrackId = trackId;
        }
        double rate = p.optDouble("rate", 0);
        if (rate > 0) {
            sWebRate = (float) rate;
        }
        sWebLoop = p.optBoolean("loop", false);
        pushWebState();
    }

    /** Action broadcast (spec §3.1): install is informational, home navigates. */
    private static void handleAction(JSONObject p) {
        String action = p.optString("action");
        if ("home".equals(action)) {
            // Spec §3.1: load `url` (default homepage). Hardening: only http(s)
            // navigation targets are accepted; an empty or non-web url falls
            // back to the browser default home page instead of failing.
            String url = sanitizeHttpUrl(p.optString("url"));
            if (url == null) {
                url = sDefaultHomeUrl;
            }
            WebView wv = sWebView;
            if (wv != null && url.length() > 0) {
                wv.loadUrl(url);
            }
        }
        // "install": the web PWA flow is unchanged (spec D-3); nothing to do here.
    }

    /** Returns the url when it is a well-formed http(s) URL, else null. */
    private static String sanitizeHttpUrl(String url) {
        if (url == null) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.length() == 0) {
            return null;
        }
        try {
            Uri u = Uri.parse(trimmed);
            String scheme = u.getScheme();
            if (scheme == null) {
                return null;
            }
            scheme = scheme.toLowerCase();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                return null;
            }
            if (u.getHost() == null || u.getHost().length() == 0) {
                return null;
            }
            return trimmed;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Host message (spec §3.1/§4.3):
     *   hosted:true  -> request to host: ACK (cmd:host) and take over playback;
     *   hosted:false -> unhost request / host-query: if we are hosting this
     *                   session, stop the native engine and reply cmd:unhost
     *                   with the playback handoff params (spec D-6); if we are
     *                   not hosting, this is a host-query and needs no reply.
     */
    private static void handleHost(JSONObject p) {
        boolean want = p.optBoolean("hosted", false);
        Log.d(TAG, "handleHost want=" + want + " hosting=" + sHosting);
        if (want) {
            sHostWebState = parseState(p.optString("state"));
            sHostWebPositionMs = Math.max(0, p.optLong("positionMs", 0));
            double rate = p.optDouble("rate", 0);
            sHostWebRate = rate > 0 ? (float) rate : 1.0f;
            sHostWebLoop = p.optBoolean("loop", false);
            String trackId = p.optString("trackId");
            String title = p.optString("title");
            if (sHosting) {
                // Idempotent re-request (e.g. install ⓘ while already hosted).
                sendControlHostAck();
                return;
            }
            sHosting = true;
            saveHostPrefs(true, sHostWebState, trackId != null ? trackId : "",
                title != null ? title : "", sHostWebPositionMs, sHostWebRate, sHostWebLoop);
            sendControlHostAck();
            // Spec §4/§8: the takeover respects the page's playback state — a
            // paused/stopped page parks the native engine (startPaused) instead
            // of auto-playing under the user.
            startHostedPlayback(trackId, title, sHostWebPositionMs, sHostWebRate, sHostWebLoop,
                sHostWebState != STATE_PLAYING);
        } else {
            // A pending deferred takeover is cancelled by this unhost/query.
            sHostPendingRetry = false;
            sMain.removeCallbacks(sHostPendingTimeout);
            if (!sHosting) {
                // Host-query (spec §4.3-C): not hosting -> normal non-hosted session.
                return;
            }
            // Unhost request (cover button) or re-browse query while hosting.
            HostedMidiPlayer engine = sEngine;
            int state = STATE_STOPPED;
            long pos = 0;
            String trackId = sWebTrackId;
            String title = sWebTitle;
            float rate = sWebRate;
            boolean loop = sWebLoop;
            HostedMidiPlayer.Track track = null;
            if (engine != null && !engine.isPreparing()) {
                state = engine.getState();
                pos = engine.getPositionMs();
                rate = engine.getRate();
                loop = engine.isLooping();
                track = engine.getTrack();
            } else {
                // No meaningful native position yet (takeover still
                // downloading/preparing, or the engine never started):
                // hand playback back with the web's own host-time snapshot
                // instead of a stopped/0 reset -- same semantics as the
                // pending-timeout and onError unwind paths.
                state = sHostWebState;
                pos = sHostWebPositionMs;
                rate = sHostWebRate;
                loop = sHostWebLoop;
            }
            if (track != null) {
                trackId = track.id;
                title = track.title;
            }
            Log.i(TAG, "unhost snapshot: rawState=" + state + " pos=" + pos
                + " trackId=" + trackId + " title=" + title + " rate=" + rate + " loop=" + loop);
            sHosting = false;
            stopHostedPlayback();
            saveHostPrefs(false, STATE_STOPPED, "", "", 0, 1.0f, false);
            sendControlUnhost(state, pos, trackId, title, rate, loop);
        }
    }

    /** Playlist message (spec §3.1): cache and persist for recovery. */
    private static void handlePlaylist(JSONObject p) {
        String rev = p.optString("rev");
        JSONArray tracks = p.optJSONArray("tracks");
        if (tracks == null) {
            return;
        }
        synchronized (sTracks) {
            sTracks.clear();
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject t = tracks.optJSONObject(i);
                if (t == null) {
                    continue;
                }
                String url = t.optString("url");
                if (url == null || url.length() == 0) {
                    continue;
                }
                sTracks.add(new HostedMidiPlayer.Track(
                    t.optString("id", "t" + i),
                    t.optString("title", ""),
                    url,
                    Math.max(0, t.optLong("durationMs", 0))));
            }
        }
        if (rev != null && rev.length() > 0) {
            sPlaylistRev = rev;
        }
        try {
            JSONObject save = new JSONObject();
            save.put("v", 1);
            save.put("rev", sPlaylistRev);
            save.put("tracks", tracks);
            prefs().edit().putString(KEY_PLAYLIST_JSON, save.toString()).apply();
        } catch (Exception e) {
            Log.w(TAG, "playlist persist failed: " + e.getMessage());
        }
        // A host request deferred for this playlist now completes (§4.3-②).
        if (sHostPendingRetry) {
            sHostPendingRetry = false;
            Log.d(TAG, "host retry: playlist arrived");
            startHostedPlayback(sHostPendingTrackId, sHostPendingTitle, sHostPendingStartMs,
                sHostPendingRate, sHostPendingLoop, sHostPendingPaused);
        }
    }

    // ------------------- native -> web (spec §3.2) -------------------

    private static void sendControlHostAck() {
        try {
            JSONObject c = new JSONObject();
            c.put("v", 1);
            c.put("cmd", "host");
            dispatchControl(c);
        } catch (Exception e) {
            Log.w(TAG, "host ACK failed: " + e.getMessage());
        }
    }

    /**
     * Unhost reply carrying the playback handoff params (spec D-6): the web
     * page removes the cover and restores playback from these values.
     */
    private static void sendControlUnhost(int state, long positionMs, String trackId,
                                          String title, float rate, boolean loop) {
        try {
            JSONObject c = new JSONObject();
            c.put("v", 1);
            c.put("cmd", "unhost");
            c.put("state", stateToString(state));
            c.put("positionMs", positionMs);
            c.put("trackId", trackId != null ? trackId : "");
            c.put("title", title != null ? title : "");
            c.put("rate", rate);
            c.put("loop", loop);
            Log.i(TAG, "sendControlUnhost " + c.toString());
            dispatchControl(c);
        } catch (Exception e) {
            Log.w(TAG, "unhost send failed: " + e.getMessage());
        }
    }

    private static void sendControlSimple(String cmd, long positionMs) {
        try {
            JSONObject c = new JSONObject();
            c.put("v", 1);
            c.put("cmd", cmd);
            if (positionMs >= 0) {
                c.put("positionMs", positionMs);
            }
            dispatchControl(c);
        } catch (Exception e) {
            Log.w(TAG, "control send failed: " + e.getMessage());
        }
    }

    /** Deliver a __midiControl payload to the page (spec §5, UI thread). */
    private static void dispatchControl(JSONObject control) {
        try {
            JSONObject root = new JSONObject();
            root.put("__midiControl", control);
            final String json = root.toString();
            sMain.post(new Runnable() {
                @Override
                public void run() {
                    WebView wv = sWebView;
                    if (wv == null) {
                        return;
                    }
                    // The page-side handler accepts a JSON object as well as a
                    // string, so the object literal is passed directly (spec §5).
                    // Diagnostic wrapper reports whether the page handler ran.
                    Log.i(TAG, "dispatchControl " + json);
                    wv.evaluateJavascript(
                        "(function(){try{"
                            + "if(window.__midiDispatchControl){"
                            + "window.__midiDispatchControl(" + json + ");return 'HANDLED';}"
                            + "return 'NO_HANDLER';"
                            + "}catch(e){return 'ERR:'+e;}})()",
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.i(TAG, "dispatchControl result=" + value);
                            }
                        });
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "dispatch failed: " + e.getMessage());
        }
    }

    // ------------------- media session command routing -------------------

    // Fragment (heuristic fallback) entry points route here first: hosted mode
    // drives the native engine directly (spec §6 Callback table); non-hosted
    // protocol sessions forward __midiControl to the page.

    public static boolean onMediaPlayCommand() {
        if (sHosting) {
            HostedMidiPlayer engine = sEngine;
            if (engine != null) {
                engine.play();
            } else if (sHostWebState != STATE_STOPPED) {
                // Engine lost (restart edge): replay the host snapshot.
                startHostedPlayback(sWebTrackId, sWebTitle,
                    sHostWebPositionMs, sHostWebRate, sHostWebLoop, false);
            }
            return true;
        }
        if (sEngaged) {
            // Spec §8: non-hosted control is dispatched only while the page is
            // visible; a hidden-page command queues and steers the session
            // toward cover hosting (§4.3-③) instead of being dispatched.
            long pos = sWebState == STATE_PAUSED ? sWebPositionMs : -1;
            if (sPageVisible) {
                sendControlSimple("play", pos);
            } else {
                queueHiddenCommand("play", pos);
            }
            return true;
        }
        return false;
    }

    public static boolean onMediaPauseCommand() {
        if (sHosting) {
            HostedMidiPlayer engine = sEngine;
            if (engine != null) {
                engine.pause();
            }
            return true;
        }
        if (sEngaged) {
            if (sPageVisible) {
                sendControlSimple("pause", -1);
            } else {
                queueHiddenCommand("pause", -1);
            }
            return true;
        }
        return false;
    }

    public static boolean onMediaStopCommand() {
        if (sHosting) {
            // Notification ✕ / stop ends the hosted session as well: cancel
            // hosting and hand control back to the page via cmd:unhost (D-6)
            // with a stopped state so the page drops the cover and shows its
            // player stopped.
            stopHostedAndUnhost();
            return true;
        }
        if (sEngaged) {
            if (sPageVisible) {
                sendControlSimple("stop", -1);
            } else {
                queueHiddenCommand("stop", -1);
            }
            return true;
        }
        return false;
    }

    public static boolean onMediaSeekCommand(long positionMs) {
        Log.i(TAG, "onMediaSeekCommand pos=" + positionMs + " hosting=" + sHosting
            + " engaged=" + sEngaged);
        if (sHosting) {
            HostedMidiPlayer engine = sEngine;
            if (engine != null) {
                engine.seekTo(positionMs);
            }
            return true;
        }
        if (sEngaged) {
            long pos = Math.max(0, positionMs);
            if (sPageVisible) {
                sendControlSimple("seek", pos);
            } else {
                queueHiddenCommand("seek", pos);
            }
            return true;
        }
        return false;
    }

    /**
     * Skip to next/previous track. Hosted mode switches playlist entries on the
     * native engine (spec §6); non-hosted mode forwards next/prev to the page
     * (spec §9: unknown cmds are ignorable, so this is forward-compatible).
     */
    public static boolean onMediaSkipCommand(boolean next) {
        if (sHosting) {
            synchronized (sTracks) {
                if (sTracks.isEmpty()) {
                    return false;
                }
                int idx = currentTrackIndex();
                int target = next ? idx + 1 : idx - 1;
                if (target < 0) {
                    target = sTracks.size() - 1;
                } else if (target >= sTracks.size()) {
                    target = 0;
                }
                HostedMidiPlayer.Track t = sTracks.get(target);
                startHostedPlayback(t.id, t.title, 0, sHostWebRate, sHostWebLoop, false);
            }
            return true;
        }
        if (sEngaged) {
            String cmd = next ? "next" : "prev";
            if (sPageVisible) {
                sendControlSimple(cmd, -1);
            } else {
                queueHiddenCommand(cmd, -1);
            }
            return true;
        }
        return false;
    }

    // ------------------- hosted engine (spec §4) -------------------

    /** Timeout for a host request that never saw its trailing playlist. */
    private static final Runnable sHostPendingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!sHostPendingRetry) {
                return;
            }
            sHostPendingRetry = false;
            Log.w(TAG, "hosting aborted: playlist never arrived");
            sHosting = false;
            saveHostPrefs(false, STATE_STOPPED, "", "", 0, 1.0f, false);
            sendControlUnhost(sHostWebState, sHostWebPositionMs,
                sHostPendingTrackId != null ? sHostPendingTrackId : "",
                sHostPendingTitle != null ? sHostPendingTitle : "",
                sHostPendingRate, sHostPendingLoop);
        }
    };

    private static void startHostedPlayback(String trackId, String title, long startPosMs,
                                            float rate, boolean loop, boolean startPaused) {
        HostedMidiPlayer.Track track = findTrack(trackId, title);
        if (track == null) {
            // Spec §4.3-② pushes the playlist right after the host message, so
            // when no playlist has landed yet the entry may simply still be in
            // flight: defer and let handlePlaylist retry (avoids the abort race
            // seen on 2026-11-03 AVD matrix), instead of unwinding. A repeat
            // call inside the wait window (e.g. skip track) just refreshes the
            // pending snapshot.
            if (sTracks.isEmpty()) {
                Log.d(TAG, "host deferred: playlist not yet received for " + trackId);
                sHostPendingRetry = true;
                sHostPendingTrackId = trackId;
                sHostPendingTitle = title;
                sHostPendingStartMs = startPosMs;
                sHostPendingRate = rate;
                sHostPendingLoop = loop;
                sHostPendingPaused = startPaused;
                sMain.removeCallbacks(sHostPendingTimeout);
                sMain.postDelayed(sHostPendingTimeout, HOST_PLAYLIST_WAIT_MS);
                return;
            }
            // Playlist is present but has no usable entry: unwind hosting
            // gracefully so the web synthesizer resumes (spec §9 fallback).
            Log.w(TAG, "hosting aborted: no playlist entry for " + trackId);
            sHostPendingRetry = false;
            sHosting = false;
            saveHostPrefs(false, STATE_STOPPED, "", "", 0, 1.0f, false);
            sendControlUnhost(sHostWebState, sHostWebPositionMs,
                trackId != null ? trackId : "", title != null ? title : "",
                rate, loop);
            return;
        }
        sHostPendingRetry = false;
        sMain.removeCallbacks(sHostPendingTimeout);
        if (sEngine == null) {
            sEngine = new HostedMidiPlayer(appContext(), sEngineListener);
        }
        sEngine.playTrack(track, startPosMs, rate, loop, startPaused);
        startPositionTick();
        // A hidden-page command queued ahead of the takeover now lands on
        // the native engine (spec §8: hosted mode is driven natively).
        drainQueuedCommandToEngine();
    }

    private static void stopHostedPlayback() {
        stopPositionTick();
        HostedMidiPlayer engine = sEngine;
        if (engine != null) {
            engine.release();
            sEngine = null;
        }
    }

    /**
     * Stop ends the hosted session too (notification ✕): stop the native
     * engine, cancel hosting and send cmd:unhost to the page with a stopped
     * state (spec D-6), so the page drops the cover and restores its own
     * player in the stopped state.
     */
    private static void stopHostedAndUnhost() {
        HostedMidiPlayer engine = sEngine;
        String trackId = sWebTrackId;
        String title = sWebTitle;
        float rate = sHostWebRate;
        boolean loop = sHostWebLoop;
        if (engine != null) {
            HostedMidiPlayer.Track track = engine.getTrack();
            if (track != null) {
                trackId = track.id;
                title = track.title;
            }
            rate = engine.getRate();
            loop = engine.isLooping();
        }
        sHosting = false;
        stopHostedPlayback();
        saveHostPrefs(false, STATE_STOPPED, "", "", 0, 1.0f, false);
        sendControlUnhost(STATE_STOPPED, 0, trackId, title, rate, loop);
    }

    private static final HostedMidiPlayer.Listener sEngineListener =
        new HostedMidiPlayer.Listener() {
            @Override
            public void onTrackReady(String trackId, String title, long durationMs) {
                pushHostedState();
            }

            @Override
            public void onStateChanged(int state, long positionMs, long durationMs) {
                pushHostedState();
                saveHostPrefs(true, state,
                    sEngine != null && sEngine.getTrack() != null ? sEngine.getTrack().id : "",
                    sEngine != null && sEngine.getTrack() != null ? sEngine.getTrack().title : "",
                    positionMs,
                    sEngine != null ? sEngine.getRate() : 1.0f,
                    sEngine != null && sEngine.isLooping());
            }

            @Override
            public void onTrackEnded() {
                // Auto-advance through the playlist; stop at the end.
                synchronized (sTracks) {
                    int idx = currentTrackIndex();
                    if (idx >= 0 && idx + 1 < sTracks.size()) {
                        HostedMidiPlayer.Track t = sTracks.get(idx + 1);
                        if (sEngine != null) {
                            sEngine.playTrack(t, 0, sHostWebRate, sHostWebLoop);
                            return;
                        }
                    }
                }
                pushHostedState();
            }

            @Override
            public void onError(String message) {
                // Native playback unavailable: hand playback back to the web
                // synthesizer with the original host params so audio continues.
                if (!sHosting) {
                    return;
                }
                sHosting = false;
                stopHostedPlayback();
                saveHostPrefs(false, STATE_STOPPED, "", "", 0, 1.0f, false);
                sendControlUnhost(sHostWebState, sHostWebPositionMs,
                    sWebTrackId, sWebTitle, sHostWebRate, sHostWebLoop);
            }
        };

    /** Periodic PlaybackState position refresh while hosting (spec §6). */
    private static void startPositionTick() {
        if (sPositionTickRunning) {
            return;
        }
        sPositionTickRunning = true;
        sTickCount = 0;
        sMain.post(mPositionTick);
    }

    private static void stopPositionTick() {
        sPositionTickRunning = false;
        sMain.removeCallbacks(mPositionTick);
    }

    private static final Runnable mPositionTick = new Runnable() {
        @Override
        public void run() {
            if (!sPositionTickRunning || !sHosting) {
                sPositionTickRunning = false;
                return;
            }
            pushHostedState();
            sTickCount++;
            if (sTickCount % 10 == 0) {
                // Persist a coarse position snapshot every ~5s for recovery.
                HostedMidiPlayer engine = sEngine;
                if (engine != null) {
                    saveHostPrefs(true, engine.getState(),
                        engine.getTrack() != null ? engine.getTrack().id : "",
                        engine.getTrack() != null ? engine.getTrack().title : "",
                        engine.getPositionMs(), engine.getRate(), engine.isLooping());
                }
            }
            sMain.postDelayed(this, 500);
        }
    };

    // ------------------- service state pushes -------------------

    private static void pushWebState() {
        MediaPlaybackService.pushState(appContext(), sWebState,
            sWebPositionMs, sWebDurationMs, sWebRate, sWebTitle, sWebArtist, false);
    }

    private static void pushHostedState() {
        HostedMidiPlayer engine = sEngine;
        if (engine == null) {
            return;
        }
        // A preparing engine still reports STOPPED; pushing that would dismiss
        // the media session mid-takeover (MediaPlaybackService treats stopped
        // as terminal). The real state lands via onTrackReady/onStateChanged.
        if (engine.isPreparing()) {
            return;
        }
        HostedMidiPlayer.Track track = engine.getTrack();
        String title = track != null ? track.title : sWebTitle;
        long durationMs = engine.getDurationMs();
        MediaPlaybackService.pushState(appContext(), engine.getState(),
            engine.getPositionMs(), durationMs, engine.getRate(),
            title, "WinBoLL MIDI 播放器", true);
    }

    // ------------------- process-death recovery (spec §3.1 playlist HTTP) -------------------

    /**
     * Restore hosted playback after a service restart. Returns true when the
     * service should stay alive for the restored engine.
     */
    public static boolean restoreHostedPlayback(Context context) {
        SharedPreferences p = prefs();
        if (!p.getBoolean(KEY_HOSTED, false)) {
            return false;
        }
        int state = p.getInt(KEY_HOST_STATE, STATE_STOPPED);
        if (state != STATE_PLAYING && state != STATE_PAUSED) {
            return false;
        }
        if (sHosting) {
            return true;
        }
        loadPlaylistFromPrefs();
        String trackId = p.getString(KEY_HOST_TRACK_ID, "");
        String title = p.getString(KEY_HOST_TITLE, "");
        long pos = p.getLong(KEY_HOST_POS, 0);
        float rate = p.getFloat(KEY_HOST_RATE, 1.0f);
        boolean loop = p.getBoolean(KEY_HOST_LOOP, false);
        synchronized (sTracks) {
            if (sTracks.isEmpty()) {
                return false;
            }
        }
        sHosting = true;
        sHostWebPositionMs = pos;
        sHostWebRate = rate;
        sHostWebLoop = loop;
        sHostWebState = state;
        // startPaused survives the async prepare window (HostedMidiPlayer
        // mPausePending), replacing the old racy pause()-after-start that was
        // a no-op while the engine was still preparing.
        startHostedPlayback(trackId, title, pos, rate, loop, state != STATE_PLAYING);
        return true;
    }

    private static void loadPlaylistFromPrefs() {
        String json = prefs().getString(KEY_PLAYLIST_JSON, null);
        if (json == null || json.length() == 0) {
            return;
        }
        try {
            JSONObject root = new JSONObject(json);
            JSONArray tracks = root.optJSONArray("tracks");
            if (tracks == null) {
                return;
            }
            synchronized (sTracks) {
                sTracks.clear();
                for (int i = 0; i < tracks.length(); i++) {
                    JSONObject t = tracks.optJSONObject(i);
                    if (t == null) {
                        continue;
                    }
                    String url = t.optString("url");
                    if (url == null || url.length() == 0) {
                        continue;
                    }
                    sTracks.add(new HostedMidiPlayer.Track(
                        t.optString("id", "t" + i),
                        t.optString("title", ""),
                        url,
                        Math.max(0, t.optLong("durationMs", 0))));
                }
            }
            sPlaylistRev = root.optString("rev");
        } catch (Exception e) {
            Log.w(TAG, "playlist restore failed: " + e.getMessage());
        }
    }

    // ------------------- helpers -------------------

    private static HostedMidiPlayer.Track findTrack(String trackId, String title) {
        synchronized (sTracks) {
            if (sTracks.isEmpty()) {
                loadPlaylistFromPrefs();
            }
            if (trackId != null && trackId.length() > 0) {
                for (int i = 0; i < sTracks.size(); i++) {
                    if (trackId.equals(sTracks.get(i).id)) {
                        return sTracks.get(i);
                    }
                }
            }
            if (title != null && title.length() > 0) {
                for (int i = 0; i < sTracks.size(); i++) {
                    if (title.equalsIgnoreCase(sTracks.get(i).title)) {
                        return sTracks.get(i);
                    }
                }
            }
            if (!sTracks.isEmpty()) {
                return sTracks.get(0);
            }
        }
        return null;
    }

    private static int currentTrackIndex() {
        HostedMidiPlayer engine = sEngine;
        String id = engine != null && engine.getTrack() != null ? engine.getTrack().id : "";
        synchronized (sTracks) {
            for (int i = 0; i < sTracks.size(); i++) {
                if (sTracks.get(i).id.equals(id)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static void saveHostPrefs(boolean hosted, int state, String trackId, String title,
                                      long pos, float rate, boolean loop) {
        prefs().edit()
            .putBoolean(KEY_HOSTED, hosted)
            .putInt(KEY_HOST_STATE, state)
            .putString(KEY_HOST_TRACK_ID, trackId != null ? trackId : "")
            .putString(KEY_HOST_TITLE, title != null ? title : "")
            .putLong(KEY_HOST_POS, pos)
            .putFloat(KEY_HOST_RATE, rate)
            .putBoolean(KEY_HOST_LOOP, loop)
            .apply();
    }

    private static SharedPreferences prefs() {
        return appContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static Context sAppContext;

    /** Seed the application context (called once from the fragment/service). */
    public static void init(Context context) {
        if (sAppContext == null) {
            sAppContext = context.getApplicationContext();
        }
    }

    private static Context appContext() {
        return sAppContext;
    }

    private static int parseState(String s) {
        if ("playing".equals(s)) {
            return STATE_PLAYING;
        }
        if ("paused".equals(s)) {
            return STATE_PAUSED;
        }
        return STATE_STOPPED;
    }

    private static String stateToString(int state) {
        if (state == STATE_PLAYING) {
            return "playing";
        }
        if (state == STATE_PAUSED) {
            return "paused";
        }
        return "stopped";
    }
}
