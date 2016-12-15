package kr.ac.hansung.simpletalk.android.chatroom;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.android.FileSerivce;
import kr.ac.hansung.simpletalk.android.R;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;

public class ChatRoomActivity extends AppCompatActivity {
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;

    private Integer chatRoomId;
    private ChatService chatService;
    private ChatArrayAdapter chatArrayAdapter;
    private ChatRoomClientVO chatRoomData;
    private Button buttonSend;
    private Button imageSend;
    private EmojiEditText chatText;
    private EmojiPopup emojiPopup;
    private ImageButton emoticonSend;
    private ViewGroup rootView;
    //Map<Integer, UserProfileVO> userProfileMap;

    private Handler serviceHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            MessageVO msgData = (MessageVO)msg.getData().getSerializable("msg");
            if(msgData != null) {
                if(msgData.getRoomId() != null && chatRoomId.equals(msgData.getRoomId())) {
                    chatArrayAdapter.add(chatMassgeConverter(msgData));
                }
            }

            //chat_list.append(r_msg + "\n");
            //if(!m.getId().equals(login_id))
            //if (!r_msg.contains(login_id))
            //   newNotification(r_msg);			// 알림기능 (알림바에 알림 추가)
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Intent intent = getIntent();

        chatRoomId = intent.getIntExtra("roomId", -1);

        ListView listView = (ListView) findViewById(R.id.chatList);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        listView.setAdapter(chatArrayAdapter);

        chatService = ChatService.getInstance();
        chatService.setNowActivityHandler(serviceHandler);
        chatRoomData = chatService.getChatRoomMap().get(chatRoomId);

        for(MessageVO message: chatRoomData.getMessageList()){
            chatArrayAdapter.add(chatMassgeConverter(message));
        }

        /* UI 영역 */
        rootView = (ViewGroup) findViewById(R.id.activity_chat_room);
        chatText = (EmojiEditText) findViewById(R.id.chatText);
        imageSend = (Button)findViewById(R.id.imageSend);
        emoticonSend = (ImageButton) findViewById(R.id.emoticonSend);
        buttonSend = (Button) findViewById(R.id.buttonSend);

        setUpEmojiPopup();

        emoticonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiPopup.toggle();
            }
        });

        chatText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId){
                    case EditorInfo.IME_ACTION_SEND:
                        sendMessage();
                        return true;
                }
                return false;
            }
        });

        imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // Gallery 호출
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // 잘라내기 셋팅
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 0);
                intent.putExtra("aspectY", 0);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 150);
                try {
                    intent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);
                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }
            }
        });

        getSupportActionBar().setTitle(chatRoomData.getRoomName());
    }

    private void sendMessage(){
        chatService.sendTextMsg(chatRoomId, chatText.getText().toString());

        chatText.setText("");
    }

    private ChatMessage chatMassgeConverter(MessageVO msgData){
        ChatMessage chatMag = null;

        String userName = "(퇴장한 사용자)";
        String profileImagePath = null;
        try {
            userName = chatService.getUserProfileMap().get(msgData.getSenderId()).getName();
            profileImagePath = chatService.getUserProfileMap().get(msgData.getSenderId()).getImgFileName();
        }catch (NullPointerException e){}

        switch (msgData.getType()) {
            case MessageVO.MSG_TYPE_TEXT:
                chatMag = new ChatMessage(chatService.getMyProfile().getId().equals(msgData.getSenderId()) ? ChatMessage.SIDE_RIGHT : ChatMessage.SIDE_LEFT,
                        ChatMessage.TYPE_TEXT, msgData.getData(), userName, profileImagePath);
                break;
            case MessageVO.MSG_TYPE_IMAGE:
                chatMag = new ChatMessage(chatService.getMyProfile().getId().equals(msgData.getSenderId()) ? ChatMessage.SIDE_RIGHT : ChatMessage.SIDE_LEFT,
                        ChatMessage.TYPE_IMAGE, msgData.getData(), userName, profileImagePath);

                //FileSerivce.getInstance().getImageMsg(chatArrayAdapter, chatMag, msgData);
                break;
            default:
                chatMag = new ChatMessage(ChatMessage.SIDE_CENTER, msgData.getData());
                break;
        }
        return chatMag;
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
                FileSerivce.getInstance().sendImageMsg(this, chatService, chatRoomId, fileUri);
            }
        }
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked(final View v) {
                Log.d("MainActivity", "Clicked on Backspace");
            }
        }).setOnEmojiClickedListener(new OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(final Emoji emoji) {
                Log.d("MainActivity", "Clicked on emoji");
            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                emoticonSend.setImageResource(R.drawable.ic_keyboard_grey_500_36dp);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
            @Override
            public void onKeyboardOpen(final int keyBoardHeight) {
                Log.d("MainActivity", "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                emoticonSend.setImageResource(R.drawable.emoji_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiPopup.dismiss();
            }
        }).build(chatText);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendMessage();
            }
        });
    }
}
