package kr.ac.hansung.simpletalk.android.setting;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.android.FileSerivce;
import kr.ac.hansung.simpletalk.android.R;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class ProfileSettingActivity extends AppCompatActivity {
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;

    private TextView nameTextView;
    private TextView statTextView;
    private ImageButton imageButton;
    private ChatService chatService;
    private UserProfileVO myProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        chatService = ChatService.getInstance();
        myProfile = chatService.getMyProfile();

        Button profileEditButton = (Button)findViewById(R.id.profileEditButton);

        imageButton = (ImageButton)findViewById(R.id.imageButton);
        if(myProfile.getImgFileName() != null && ! myProfile.getImgFileName().isEmpty()) {
            FileSerivce.getInstance().loadImage(imageButton, myProfile.getImgFileName());
        }
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

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // Gallery 호출
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                try {
                    intent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);
                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }
            }
        });

        getSupportActionBar().setTitle("프로필 수정");
    }

    /**
     * 파일, 이미지 선택 후 선택된 파일을 받는 영역
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null){
            Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == PICK_FROM_CAMERA) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                //imgview.setImageBitmap(photo);
            }
        }
        if (requestCode == PICK_FROM_GALLERY) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                FileSerivce.getInstance().sendProfileImg(this, chatService, fileUri);
            }
        }
    }
}
