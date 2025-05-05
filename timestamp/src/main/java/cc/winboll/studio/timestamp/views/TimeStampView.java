package cc.winboll.studio.timestamp.views;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 12:53
 * @Describe TimeStampView
 */
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class TimeStampView extends View {

    public static final String TAG = "TimeStampView";

    public static final int MSG_UPDATE_TIMESTAMP = 0;

    //private Paint circlePaint;
    //private Paint textPaint;

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

	public TimeStampView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //initView(context);
        init();
    }

    private void init() {
//        circlePaint = new Paint();
//        circlePaint.setColor(Color.BLUE);
//        circlePaint.setStyle(Paint.Style.FILL);
//
//        textPaint = new Paint();
//        textPaint.setColor(Color.WHITE);
//        textPaint.setTextSize(40);
//        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //int width = getWidth();
        //int height = getHeight();
//        int width = 50;
//        int height = 50;
//        float radius = Math.min(width, height) / 2;
//        canvas.drawCircle(width / 2, height / 2, radius, circlePaint);
//        String text = "自定义";
//        RectF rect = new RectF(0, 0, width, height);
//        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
//        int baseline =(int)((rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2);
//        canvas.drawText(text, width / 2, baseline, textPaint);
    }

//    void initView(Context context) {
//        View viewMain = inflate(context, R.layout.view_timestamp, null);
//        this.mContext = context;
//        mtvTimeStamp = viewMain.findViewById(R.id.tv_timestamp);
//        addView(viewMain);
//
//        mMyHandler = new MyHandler();
//

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



}
