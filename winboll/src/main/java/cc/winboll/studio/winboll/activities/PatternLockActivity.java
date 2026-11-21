package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.winboll.R;

public class PatternLockActivity extends BaseWinBoLLActivity {

    public static final String TAG = "PatternLockActivity";

    private static final int DOT_RADIUS = 8;
    private static final int PATTERN_ERROR_DURATION = 1500;

    private static final String PREFS_NAME = "pattern_lock_prefs";
    static final String KEY_LOCK_PATTERN = "lock_pattern";
    static final String KEY_ERROR_STATE = "error_state";
    private static final String KEY_ERROR_REPEAT_PATTERN = "error_repeat_pattern";

    private boolean mIsInErrorState;
    private boolean mNeedRestart;
    private Handler mHandler;

    private FrameLayout mContainer;
    private PatternView mPatternView;

    public PatternLockActivity() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    PatternLockActivity(Context context) {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_lock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(TAG);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                finish();
            }
        });

        mContainer = findViewById(R.id.container);
        mPatternView = new PatternView(this);
        mContainer.addView(mPatternView);
        mPatternView.invalidate();
        
        mNeedRestart = false;
        boolean isEnoughPoints = savedInstanceIsEnoughPoints();

        if (savedInstanceState != null) {
            mIsInErrorState = savedInstanceState.getBoolean(KEY_ERROR_STATE, false);
            mNeedRestart = savedInstanceState.getBoolean(KEY_ERROR_REPEAT_PATTERN, false);
        }

        if (mIsInErrorState) {
            mPatternView.invalidate();
        }
    }

    boolean savedInstanceIsEnoughPoints() {
        int count = 0;
        if (mPatternView != null) {
            for (int i = 0; i < 9; i++) {
                if (mPatternView.mDotState[i] == 1) {
                    count++;
                }
            }
        }
        return count >= 4 || count == 0;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mIsInErrorState) {
            outState.putBoolean(KEY_ERROR_STATE, mIsInErrorState);
        }
        if (mNeedRestart) {
            outState.putBoolean(KEY_ERROR_REPEAT_PATTERN, mNeedRestart);
        }
        super.onSaveInstanceState(outState);
    }

    private void showErrorState() {
        mIsInErrorState = true;
        invalidatePattern();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsInErrorState = false;
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_ERROR_STATE, false).apply();
                invalidatePattern();
                if (mPatternView != null) mPatternView.invalidate();
            }
        }, PATTERN_ERROR_DURATION);
    }

    private void clearErrorState() {
        mIsInErrorState = false;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ERROR_STATE, false).apply();
        invalidatePattern();
        if (mPatternView != null) mPatternView.invalidate();
    }

    private void showErrorToast() {
        android.widget.Toast.makeText(this, "图案点数不足，请重新绘制", 
            android.widget.Toast.LENGTH_SHORT).show();
        mNeedRestart = true;
    }

    private void showSuccessDialog() {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this)
            .setTitle("设置成功")
            .setMessage("图案密码已设置成功")
            .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setCancelable(false)
            .create();
        alertDialog.show();
    }

    void finishWithRestart() {
        finish();
    }

    private void invalidatePattern() {
        if (mPatternView != null) {
            mPatternView.invalidate();
        }
    }

    class PatternView extends FrameLayout {
        int mPatternSize = 0;
        int MAX_DOT_COUNT = 9;
        int[] mDotX = new int[MAX_DOT_COUNT];
        int[] mDotY = new int[MAX_DOT_COUNT];
        int[] mDotState = new int[MAX_DOT_COUNT];
        Bitmap mDotBitmap;
        Paint mPaintConnector;
        Paint mPaintErrorBackground;
        int mDotCount = 0;

        PatternView(Context context) {
            super(context);
            setBackgroundColor(Color.WHITE);
            for (int i = 0; i < MAX_DOT_COUNT; i++) {
                mDotX[i] = -1;
                mDotY[i] = -1;
                mDotState[i] = 0;
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w == 0 || h == 0) return;
            mPatternSize = w > h ? h : w;
            int grid = 3;
            int cell = mPatternSize / grid;

            for (int i = 0; i < MAX_DOT_COUNT; i++) {
                mDotX[i] = (i % grid) * cell + cell / 2 - cell / 24;
                mDotY[i] = (i / grid) * cell + cell / 2 - cell / 24;
                mDotState[i] = 0;
            }

            if (mDotBitmap == null) {
                mDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dot_darkgreen_dark);
            }
            if (mPaintConnector == null) {
                mPaintConnector = new Paint(Paint.FILTER_BITMAP_FLAG);
                mPaintConnector.setColor(-0xFF006400);
            }
            if (mPaintErrorBackground == null) {
                mPaintErrorBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaintErrorBackground.setColor(Color.RED);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mDotCount > 0) return false;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();

                    for (int i = 0; i < MAX_DOT_COUNT; i++) {
                        int dx = (int) Math.abs(x - mDotX[i]);
                        int dy = (int) Math.abs(y - mDotY[i]);
                        if (dx <= DOT_RADIUS && dy <= DOT_RADIUS && mDotState[i] == 0) {
                            mDotState[i] = 1;
                            mDotCount++;
                        }
                    }

                    for (int i = 0; i < mDotCount - 1; i++) {
                        int a = -1, b = -1;
                        for (int k = 0; k < MAX_DOT_COUNT; k++) {
                            if (mDotState[k] == 1) {
                                if (a < 0) a = k;
                                else b = k;
                            }
                        }
                        if (a >= 0 && b >= 0) {
                            a = Math.min(a, b);
                            b = Math.max(a, b);
                        }
                        if (mDotState[a] == 1 && mDotState[b] == 1) {
                            int dx = mDotX[b] - mDotX[a];
                            int dy = mDotY[b] - mDotY[a];
                            if ((Math.abs(dx) <= 1 && Math.abs(dy) <= 1) ||
                                (Math.abs(dx) <= 2 && Math.abs(dy) <= 1)) {
                                if (mDotState[b] == 1) {
                                    for (int k = a + 1; k < b; k++) {
                                        if (mDotState[k] == 0) {
                                            mDotState[k] = 1;
                                        }
                                    }
                                    mDotCount += (b - a - 1);
                                }
                            }
                        }
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mDotCount < 4) {
                        showErrorState();
                        showErrorToast();
                    }
                    break;
            }
            return true;
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (mPatternSize == 0) return;

            int activeCount = 0;
            for (int i = 0; i < MAX_DOT_COUNT; i++) {
                if (mDotState[i] == 1) activeCount++;
            }

            if (activeCount == 0) {
                mPaintErrorBackground.setAlpha(50);
            } else {
                mPaintErrorBackground.setAlpha(mIsInErrorState ? 80 : 60);
            }

            canvas.clipRect(0, 0, mPatternSize * 80 / 100, mPatternSize * 80 / 100);

            canvas.drawRect(0, 0, mPatternSize, mPatternSize, mPaintErrorBackground);

            if (mDotBitmap != null) {
                for (int i = 0; i < MAX_DOT_COUNT; i++) {
                    if (mDotState[i] == 1) {
                        canvas.drawBitmap(mDotBitmap, mDotX[i], mDotY[i], mPaintConnector);
                    }
                }
            }
        }
    }
}
