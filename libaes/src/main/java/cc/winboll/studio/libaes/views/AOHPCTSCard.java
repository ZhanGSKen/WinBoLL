package cc.winboll.studio.libaes.views;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:49:23
 * @Describe AOneHundredPercantClickToStartCard
 */
import android.content.Context;
import android.util.AttributeSet;

public class AOHPCTSCard extends ACard {

    public static final String TAG = "AOHPCTSCard";

    public AOHPCTSCard(Context context) {
        super(context);
    }

    public AOHPCTSCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setPadding(0 + 0 + 2 + 1, 0 + 0 + 2 + 1, 0 + 1 + 3 + 1, 0 + 2 + 3 + 1);

        // 获得TypedArray
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AToolbar);
        // 获得attrs.xml里面的属性值,格式为:名称_属性名,后面是默认值
        //int colorBackgroud = a.getColor(R.styleable.ACard_backgroudColor, context.getColor(R.color.colorACardBackgroung));
        //int centerColor = a.getColor(R.styleable.AToolbar_centerColor, context.getColor(R.color.colorAToolbarCenterColor));
        //int endColor = a.getColor(R.styleable.AToolbar_endColor, context.getColor(R.color.colorAToolbarEndColor));
        //float tSize = a.getDimension(R.styleable.CustomView_tSize, 35);
        //p.setColor(tColor);
        //p.setTextSize(tSize);
        //Drawable drawable = context.getDrawable(R.drawable.frame_atoolbar);

        //setBackground(context.getDrawable(R.drawable.acard_frame_main));

        // 返回一个绑定资源结束的信号给资源
        //a.recycle();
    }

    public AOHPCTSCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
