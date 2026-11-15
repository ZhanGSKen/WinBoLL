/*
 * Copyright 2026 WinBoLL Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package cc.winboll.studio.libappbase.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import cc.winboll.studio.libappbase.GlobalApplication;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/06 19:32
 * @Describe 具有调试模式切换功能的应用Logo控件，连续点击10次弹出提示
 */
public class DebugSwitchImageView extends ImageView {

    public static final String TAG = "DebugSwitchImageView";

    // 连续点击计数
    private int mClickCount = 0;
    // 目标点击次数
    private static final int TARGET_CLICK_COUNT = 10;

	public DebugSwitchImageView(Context context) {
        super(context);
        init();
    }

    public DebugSwitchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DebugSwitchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DebugSwitchImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mClickCount++;
					if (mClickCount == TARGET_CLICK_COUNT) {
						// 达到10次，弹出Toast
						Toast.makeText(getContext(), "连续点击已达到10次，现在开启应用调试功能。", Toast.LENGTH_SHORT).show();
						GlobalApplication.setIsDebugging(true);
						GlobalApplication.saveDebugStatus(GlobalApplication.getInstance());
						// 重置计数，可再次触发
						mClickCount = 0;
					}
				}
			});
    }
}

