package cc.winboll.studio.timestamp.views;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 12:53
 * @Describe TimeStampView
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.timestamp.R;
import cc.winboll.studio.timestamp.utils.AppConfigsUtil;
import com.hjq.toast.ToastUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class TimeStampView extends TextView {

    public static final String TAG = "TimeStampView";

    public static final int MSG_UPDATE_TIMESTAMP = 0;
    
    private Paint paint;
//    Context mContext;
//    Timer mTimer;
//    TextView mtvTimeStamp;
//    MyHandler mMyHandler;

    public TimeStampView(Context context) {
        super(context);
        //initView(context);
        init();
    }

    public TimeStampView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //initView(context);
        init();
    }

    public TimeStampView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //initView(context);
        init();
    }
    
    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(50, 50, 30, paint);
    }

//    void initView(Context context) {
//        View viewMain = inflate(context, R.layout.view_timestamp, null);
//        this.mContext = context;
//        mtvTimeStamp = viewMain.findViewById(R.id.tv_timestamp);
//        addView(viewMain);
//
//        mMyHandler = new MyHandler();
//
//        mTimer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                //System.out.println("定时任务执行了");
//                mMyHandler.sendEmptyMessage(MSG_UPDATE_TIMESTAMP);
//
//            }
//        };
//        // 延迟1秒后开始执行，之后每隔100毫秒执行一次
//        mTimer.schedule(task, 1000, 1000);
//    }

//    public void updateTimeStamp() {
//        try {
//            long currentMillis = System.currentTimeMillis();
//            Instant instant = Instant.ofEpochMilli(currentMillis);
//            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//            String szTimeStampFormatString = AppConfigsUtil.getInstance(this.mContext).getAppConfigsModel().getTimeStampFormatString();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(szTimeStampFormatString);
//            String formattedDateTime = ldt.format(formatter);
//            //System.out.println(formattedDateTime);
//            mtvTimeStamp.setText(formattedDateTime);
//        } catch (Exception e) {
//            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
//            ToastUtils.show(e);
//        }
//    }


    // 
    // 服务事务处理类
    //
//    class MyHandler extends Handler {
//
//        public void handleMessage(Message message) {
//            switch (message.what) {
//                case MSG_UPDATE_TIMESTAMP:
//                    {
//                        updateTimeStamp();
//                        break;
//                    }
//                default:
//                    break;
//            }
//            super.handleMessage(message);
//        }
//    }
}
