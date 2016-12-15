package kr.ac.hansung.simpletalk.android.setting;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import kr.ac.hansung.simpletalk.android.MainActivity;
import kr.ac.hansung.simpletalk.android.R;

public class NetworkSettingActivity extends AppCompatActivity {
    SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_setting);
        setting = getSharedPreferences("setting", Context.MODE_PRIVATE);

        EditText ipEditText = (EditText)findViewById(R.id.ipTextView);
        ipEditText.setText(setting.getString("ip", "192.168.1.128"));

        EditText portEditText = (EditText)findViewById(R.id.portTextView);
        portEditText.setText(Integer.toString(setting.getInt("port", 30000)));

        Button saveBtn = (Button)findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = setting.edit();

                EditText ipEditText = (EditText)findViewById(R.id.ipTextView);
                EditText portEditText = (EditText)findViewById(R.id.portTextView);

                edit.putString("ip", ipEditText.getText().toString());
                edit.putInt("port", Integer.parseInt(portEditText.getText().toString()));
                edit.commit();

                /* 앱 재시작 */
                Intent mStartActivity = new Intent(getBaseContext(), MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });


    }
}
