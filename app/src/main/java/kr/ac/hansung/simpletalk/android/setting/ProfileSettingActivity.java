package kr.ac.hansung.simpletalk.android.setting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.android.R;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class ProfileSettingActivity extends AppCompatActivity {
    TextView nameTextView;
    TextView statTextView;
    ChatService chatService;
    UserProfileVO myProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        chatService = ChatService.getInstance();
        myProfile = chatService.getMyProfile();

        Button profileEditButton = (Button)findViewById(R.id.profileEditButton);

        nameTextView = (TextView)findViewById(R.id.profileEditNameEditText);
        nameTextView.setText(myProfile.getName());
        statTextView = (TextView)findViewById(R.id.profileEditStatEditText);
        statTextView.setText(myProfile.getStateMsg());

        profileEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTextView.getText().toString();
                String stat = statTextView.getText().toString();

                ChatService chatService = ChatService.getInstance();

                myProfile.setName(name);
                myProfile.setStateMsg(stat);

                chatService.changeMyProfile(myProfile);

                finish();
            }
        });

        getSupportActionBar().setTitle("프로필 수정");
    }
}
