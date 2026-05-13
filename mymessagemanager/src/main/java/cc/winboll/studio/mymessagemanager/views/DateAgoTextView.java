package cc.winboll.studio.mymessagemanager.views;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAgoTextView extends AppCompatTextView {
    long mnDate;

    public DateAgoTextView(Context context) {
        super(context);
    }

    public DateAgoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DateAgoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDate(long nDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date d = new Date(nDate);

        setText(dateFormat.format(d));
    }
}
