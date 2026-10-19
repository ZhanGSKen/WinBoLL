package cc.winboll.studio.positions.views;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/10/22 02:15
 * @Describe DateTimePickerPopup
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import java.util.Calendar;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.utils.DensityUtils;

/**
 * 日期时间选择弹窗（竖直滚动行：年、月、日、时、分）
 */
public class DateTimePickerPopup extends PopupWindow {
	public static final String TAG = "DateTimePickerPopup";

    private Context mContext;
    private NumberPicker mPickerYear;
    private NumberPicker mPickerMonth;
    private NumberPicker mPickerDay;
    private NumberPicker mPickerHour;
    private NumberPicker mPickerMinute;
    private Button mBtnCancel;
    private Button mBtnConfirm;
    private OnDateTimeSelectedListener mListener;

    // 时间范围默认值
    private int mMinYear = 2000;
    private int mMaxYear = Calendar.getInstance().get(Calendar.YEAR) + 10;
    private int mMinMonth = 1;
    private int mMaxMonth = 12;
    private int mMinDay = 1;
    private int mMaxDay = 31;
    private int mMinHour = 0;
    private int mMaxHour = 23;
    private int mMinMinute = 0;
    private int mMaxMinute = 59;

    // 默认选中时间
    private int mDefaultYear;
    private int mDefaultMonth;
    private int mDefaultDay;
    private int mDefaultHour;
    private int mDefaultMinute;

    /**
     * 日期时间选择回调
     */
    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(int year, int month, int day, int hour, int minute);
        void onCancel();
    }

    /**
     * Builder 模式
     */
    public static class Builder {
        private Context mContext;
        private DateTimePickerPopup mPopup;

        public Builder(Context context) {
            this.mContext = context;
            mPopup = new DateTimePickerPopup(context);
            Calendar calendar = Calendar.getInstance();
            mPopup.mDefaultYear = calendar.get(Calendar.YEAR);
            mPopup.mDefaultMonth = calendar.get(Calendar.MONTH) + 1;
            mPopup.mDefaultDay = calendar.get(Calendar.DAY_OF_MONTH);
            mPopup.mDefaultHour = calendar.get(Calendar.HOUR_OF_DAY);
            mPopup.mDefaultMinute = calendar.get(Calendar.MINUTE);
        }

        public Builder setDateTimeRange(int minYear, int maxYear, int minMonth, int maxMonth,
                                       int minDay, int maxDay, int minHour, int maxHour,
                                       int minMinute, int maxMinute) {
            mPopup.mMinYear = minYear;
            mPopup.mMaxYear = maxYear;
            mPopup.mMinMonth = minMonth;
            mPopup.mMaxMonth = maxMonth;
            mPopup.mMinDay = minDay;
            mPopup.mMaxDay = maxDay;
            mPopup.mMinHour = minHour;
            mPopup.mMaxHour = maxHour;
            mPopup.mMinMinute = minMinute;
            mPopup.mMaxMinute = maxMinute;
            return this;
        }

        public Builder setDefaultDateTime(int year, int month, int day, int hour, int minute) {
            mPopup.mDefaultYear = year;
            mPopup.mDefaultMonth = month;
            mPopup.mDefaultDay = day;
            mPopup.mDefaultHour = hour;
            mPopup.mDefaultMinute = minute;
            return this;
        }

        public Builder setOnDateTimeSelectedListener(OnDateTimeSelectedListener listener) {
            mPopup.mListener = listener;
            return this;
        }

        public DateTimePickerPopup build() {
            mPopup.initView();
            mPopup.initPickers();
            mPopup.bindButtonClick();
            mPopup.setPopupStyle();
            return mPopup;
        }
    }

    private DateTimePickerPopup(Context context) {
        super(context);
        this.mContext = context;
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View rootView = inflater.inflate(R.layout.dialog_date_time_picker, null, false);
        setContentView(rootView);

        mPickerYear = (NumberPicker) rootView.findViewById(R.id.picker_year);
        mPickerMonth = (NumberPicker) rootView.findViewById(R.id.picker_month);
        mPickerDay = (NumberPicker) rootView.findViewById(R.id.picker_day);
        mPickerHour = (NumberPicker) rootView.findViewById(R.id.picker_hour);
        mPickerMinute = (NumberPicker) rootView.findViewById(R.id.picker_minute);
        mBtnCancel = (Button) rootView.findViewById(R.id.btn_cancel);
        mBtnConfirm = (Button) rootView.findViewById(R.id.btn_confirm);
    }

    private void initPickers() {
        // 初始化年选择器
        mPickerYear.setMinValue(mMinYear);
        mPickerYear.setMaxValue(mMaxYear);
        mPickerYear.setValue(mDefaultYear);
        mPickerYear.setWrapSelectorWheel(false);

        // 初始化月选择器
        mPickerMonth.setMinValue(mMinMonth);
        mPickerMonth.setMaxValue(mMaxMonth);
        mPickerMonth.setValue(mDefaultMonth);
        mPickerMonth.setWrapSelectorWheel(false);

        // 初始化日选择器（根据年月动态调整范围）
        updateDayRange(mDefaultYear, mDefaultMonth);
        mPickerDay.setValue(mDefaultDay);
        mPickerDay.setWrapSelectorWheel(false);

        // 初始化时选择器
        mPickerHour.setMinValue(mMinHour);
        mPickerHour.setMaxValue(mMaxHour);
        mPickerHour.setValue(mDefaultHour);
        mPickerHour.setWrapSelectorWheel(false);

        // 初始化分选择器
        mPickerMinute.setMinValue(mMinMinute);
        mPickerMinute.setMaxValue(mMaxMinute);
        mPickerMinute.setValue(mDefaultMinute);
        mPickerMinute.setWrapSelectorWheel(false);

        // 年月变化时更新日范围（Java 7 匿名内部类）
        mPickerYear.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDayRange(newVal, mPickerMonth.getValue());
            }
        });

        mPickerMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDayRange(mPickerYear.getValue(), newVal);
            }
        });
    }

    private void updateDayRange(int year, int month) {
        int maxDay;
        switch (month) {
            case 2:
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    maxDay = 29;
                } else {
                    maxDay = 28;
                }
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                maxDay = 30;
                break;
            default:
                maxDay = 31;
        }
        mPickerDay.setMaxValue(maxDay);
        if (mPickerDay.getValue() > maxDay) {
            mPickerDay.setValue(maxDay);
        }
    }

    private void bindButtonClick() {
        // 取消按钮（Java 7 匿名内部类）
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });

        // 确认按钮（Java 7 匿名内部类）
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = mPickerYear.getValue();
                int month = mPickerMonth.getValue();
                int day = mPickerDay.getValue();
                int hour = mPickerHour.getValue();
                int minute = mPickerMinute.getValue();

                if (mListener != null) {
                    mListener.onDateTimeSelected(year, month, day, hour, minute);
                }
                dismiss();
            }
        });
    }

    private void setPopupStyle() {
        int width = (int) (DensityUtils.getScreenWidth(mContext) * 0.85f);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        setWidth(width);
        setHeight(height);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_dialog_round));
        setAnimationStyle(R.style.PopupDateTimePickerAnim);
    }

    public void showAsDropDown(View anchorView) {
        super.showAsDropDown(anchorView, 0, DensityUtils.dp2px(mContext, 10));
    }
}
