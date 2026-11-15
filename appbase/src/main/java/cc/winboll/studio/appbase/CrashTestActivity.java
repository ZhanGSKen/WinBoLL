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
package cc.winboll.studio.appbase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;

public class CrashTestActivity extends Activity {

    public static final String TAG = "CrashTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_test);
        LogUtils.d(TAG, "CrashTestActivity onCreate()");
    }

    public void onBack(View view) {
        finish();
    }

    public void onTestCrash(View view) {
        LogUtils.d(TAG, "onTestCrash()");
        ToastUtils.show("测试布局崩溃...");
    }
}
