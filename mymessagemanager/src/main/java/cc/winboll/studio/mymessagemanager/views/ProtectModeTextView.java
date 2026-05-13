package cc.winboll.studio.mymessagemanager.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cc.winboll.studio.mymessagemanager.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 保护模式自定义控件
 * 最终规则：
 * 1. 刻度范围 0~12
 * 2. 刻度值 = 每一组截取【相邻字符个数】
 * 3. 从头到尾按固定长度切块分组
 * 4. 所有分组收集后随机打乱再拼接输出
 * 5. 刻度0 = 不打乱，显示原文
 * 6. 按原生字符计算（包含空格、标点）
 */
public class ProtectModeTextView extends LinearLayout {

    public interface OnScaleChangedListener {
        void onScaleChanged(int progress);
    }

    private TextView tvContent;
    private SeekBar seekBarScale;
    private String originText;
    private List<Character> charAllList;
    private final Random random = new Random();
    private OnScaleChangedListener mOnScaleChangedListener;

    public ProtectModeTextView(Context context) {
        super(context);
        initView(context);
    }

    public ProtectModeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ProtectModeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_protect_mode_textview, this, true);
        tvContent = findViewById(R.id.tv_content);
        seekBarScale = findViewById(R.id.seek_bar_scale);

        // 刻度 0 ~ 12
        seekBarScale.setMax(12);
        seekBarScale.setProgress(0);

        charAllList = new ArrayList<>();

        seekBarScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					handleTextLogic(progress);
					if (fromUser && mOnScaleChangedListener != null) {
						mOnScaleChangedListener.onScaleChanged(progress);
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			});
    }

    public void setOnScaleChangedListener(OnScaleChangedListener listener) {
        mOnScaleChangedListener = listener;
    }

    public void setContentTextWithScale(String text, int scaleProgress) {
        this.originText = text;
        convertToCharList(text);
        seekBarScale.setProgress(scaleProgress);
        handleTextLogic(scaleProgress);
    }

    public void setContentText(String text) {
        this.originText = text;
        convertToCharList(text);
        handleTextLogic(seekBarScale.getProgress());
    }

    private void convertToCharList(String text) {
        charAllList.clear();
        if (text == null || text.isEmpty()) {
            return;
        }
        char[] chars = text.toCharArray();
        for (char c : chars) {
            charAllList.add(c);
        }
    }

    private void handleTextLogic(int groupSize) {
        if (charAllList.isEmpty()) {
            tvContent.setText(originText);
            return;
        }

        // 刻度0 原样不打乱
        if (groupSize <= 0) {
            tvContent.setText(originText);
            return;
        }

        List<String> groupList = new ArrayList<>();
        int totalLen = charAllList.size();

        // 从头到尾 按 groupSize 个相邻字符切块
        int index = 0;
        while (index < totalLen) {
            StringBuilder sb = new StringBuilder();
            // 每一组取 groupSize 个相邻字符
            for (int i = 0; i < groupSize && index < totalLen; i++) {
                sb.append(charAllList.get(index));
                index++;
            }
            groupList.add(sb.toString());
        }

        // 所有分组随机打乱
        Collections.shuffle(groupList, random);

        // 拼接输出
        StringBuilder result = new StringBuilder();
        for (String item : groupList) {
            result.append(item);
        }

        tvContent.setText(result.toString());
    }

    public String getOriginText() {
        return originText;
    }

    public void resetSeekBar() {
        seekBarScale.setProgress(0);
    }
}

