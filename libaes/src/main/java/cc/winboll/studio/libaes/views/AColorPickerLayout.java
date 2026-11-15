package cc.winboll.studio.libaes.views;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Composite color picker: HSV square/bar picker on top, preview strip and hex
 * input below. The hex field accepts #RRGGBB and #AARRGGBB.
 */
public class AColorPickerLayout extends ViewGroup implements AColorChangedListener {

    public static final String TAG = "AColorPickerLayout";

    private static final InputFilter[] FILTERS = new InputFilter[]{
            new InputFilter.LengthFilter(9)
    };

    private final AColorPickerView picker;
    private final AColorPreviewView preview;
    private final LinearLayout hintLayout;
    private final EditText hint;
    private int color;
    private boolean autoset;

    public AColorPickerLayout(Context context) {
        this(context, null);
    }

    public AColorPickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        autoset = false;
        picker = new AColorPickerView(context, attrs);
        preview = new AColorPreviewView(context, attrs);
        hintLayout = new LinearLayout(context, attrs);
        hint = new EditText(context, attrs);
        hintLayout.setOrientation(LinearLayout.VERTICAL);
        hint.setGravity(android.view.Gravity.CENTER);
        hint.setFilters(FILTERS);
        ColorHexWatcher watcher = new ColorHexWatcher(this);
        hint.addTextChangedListener(watcher);
        hint.setKeyListener(watcher);
        hintLayout.addView(hint);
        picker.setOnColorChangedListener(this);
        addView(picker);
        addView(preview);
        addView(hintLayout);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        AColorPickerView pickerView = picker;
        pickerView.measure(widthMeasureSpec, heightMeasureSpec);
        int pickerWidth = pickerView.getMeasuredWidth();
        int pickerHeight = pickerView.getMeasuredHeight();
        int available = MeasureSpec.getSize(heightMeasureSpec)
                - getPaddingBottom() - getPaddingTop();
        int hintHeight = available - pickerHeight;
        int hintWidthSpec = MeasureSpec.makeMeasureSpec(pickerWidth / 2, MeasureSpec.EXACTLY);
        int hintHeightSpec = MeasureSpec.makeMeasureSpec(hintHeight, MeasureSpec.AT_MOST);
        LinearLayout layout = hintLayout;
        layout.measure(hintWidthSpec, hintHeightSpec);
        pickerHeight += layout.getMeasuredHeight();
        setMeasuredDimension(pickerWidth, pickerHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        AColorPickerView pickerView = picker;
        int pickerWidth = pickerView.getMeasuredWidth();
        int pickerHeight = pickerView.getMeasuredHeight();
        pickerView.layout(l, t, l + pickerWidth, t + pickerHeight);
        LinearLayout layout = hintLayout;
        int hintHeight = layout.getMeasuredHeight();
        layout.layout(l + pickerWidth / 2, t + pickerHeight,
                l + pickerWidth, t + pickerHeight + hintHeight);
        AColorPreviewView previewView = preview;
        previewView.layout(l, t + pickerHeight,
                l + pickerWidth / 2, t + pickerHeight + hintHeight);
    }

    public void setColor(int color) {
        picker.setColor(color);
        preview.setInitColor(color);
        onColorChanged(color);
    }

    public int getColor() {
        return color;
    }

    @Override
    public void onColorChanged(int color) {
        this.color = color;
        preview.setColor(color);
        hint.setText(String.format("#%08X", color));
    }

    @Override
    public void beforeColorChanged() {
        autoset = true;
    }

    @Override
    public void afterColorChanged() {
        autoset = false;
    }

    void findColor(String text) {
        if (autoset) {
            return;
        }
        int parsed = Color.parseColor(text);
        picker.setColor(parsed);
        preview.setColor(parsed);
        color = parsed;
    }

    /** Keeps the hex field lowercase, auto prefixes '#' and parses complete values. */
    private static class ColorHexWatcher extends NumberKeyListener implements TextWatcher {

        private final AColorPickerLayout pickerLayout;
        private final String chars;

        ColorHexWatcher(AColorPickerLayout pickerLayout) {
            super();
            chars = "0123456789abcdefABCDEF#";
            this.pickerLayout = pickerLayout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString().toLowerCase();
            int length = text.length();
            if (!text.startsWith("#")) {
                s.insert(0, "#");
                return;
            }
            if (length == 7 || length == 9) {
                pickerLayout.findColor(text);
            }
        }

        @Override
        protected char[] getAcceptedChars() {
            return chars.toCharArray();
        }

        @Override
        public int getInputType() {
            return 2;
        }
    }
}
