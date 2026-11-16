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
package cc.winboll.studio.appbase.develop;

import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.views.SMTPConfigView;

/**
 * @Author 豆包&BigPickle&MiMo&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/07/21 12:44
 * @Describe 应用异常信息邮件接收账号设置
 * 使用libaes的SMTPConfigView控件配置SMTP服务器参数
 */
public class APPExcetionMailSettingsActivity extends Activity {

    public static final String TAG = "APPExcetionMailSettingsActivity";

    private SMTPConfigView mSMTPConfigView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appexcetionmailsettings);

        mSMTPConfigView = (SMTPConfigView) findViewById(R.id.smtp_config_view);
    }
}
