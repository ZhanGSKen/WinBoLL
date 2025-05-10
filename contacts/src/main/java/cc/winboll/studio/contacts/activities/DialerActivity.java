package cc.winboll.studio.contacts.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/20 20:18:26
 */
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.contacts.R;

public class DialerActivity extends AppCompatActivity {

    public static final String TAG = "DialerActivity";

    private EditText phoneNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        Button dialButton = findViewById(R.id.dial_button);

        dialButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = phoneNumberEditText.getText().toString();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            });
    }
}

