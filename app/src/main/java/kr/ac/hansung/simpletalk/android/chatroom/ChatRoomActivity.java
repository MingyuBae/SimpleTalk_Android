package kr.ac.hansung.simpletalk.android.chatroom;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.android.FileSerivce;
import kr.ac.hansung.simpletalk.android.R;
import kr.ac.hansung.simpletalk.android.setting.NetworkSettingActivity;
import kr.ac.hansung.simpletalk.android.setting.ProfileSettingActivity;
import kr.ac.hansung.simpletalk.android.userlist.UserListActivity;
import kr.ac.hansung.simpletalk.android.userlist.UserListAdapter;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class ChatRoomActivity extends AppCompatActivity {
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private static final int PICK_FROM_EMOTICON = 3;

    private Integer chatRoomId;
    private ChatService chatService;
    private ChatArrayAdapter chatArrayAdapter;
    private ChatRoomClientVO chatRoomData;
    private ImageButton buttonSend;
    private ImageButton otherSend;
    private EmojiEditText chatText;
    private EmojiPopup emojiPopup;
    private ImageButton emoticonSend;
    private ViewGroup rootView;
    //Map<Integer, UserProfileVO> userProfileMap;

    private Handler serviceHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            MessageVO msgData = (MessageVO)msg.getData().getSerializable("msg");
            if(msgData != null &&
                            (MessageVO.MSG_TYPE_IMAGE.equals(msgData.getType()) ||
                            MessageVO.MSG_TYPE_TEXT.equals(msgData.getType()) ||
                            MessageVO.MSG_TYPE_ADD_CHATROOM_USER.equals(msgData.getType()) ||
                            MessageVO.MSG_TYPE_EXIT_CHATROOM_USER.equals(msgData.getType())) ||
                            MessageVO.MSG_TYPE_EMOTICON.equals(msgData.getType())) {
                if(msgData.getRoomId() != null && chatRoomId.equals(msgData.getRoomId())) {
                    chatArrayAdapter.add(chatMassgeConverter(msgData));
                }
            }
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
        otherSend = (ImageButton)findViewById(R.id.otherSend);
        emoticonSend = (ImageButton) findViewById(R.id.emoticonSend);
        buttonSend = (ImageButton) findViewById(R.id.buttonSend);

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

        otherSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogSelectOption();
            }
        });

        getSupportActionBar().setTitle(chatRoomData.getRoomName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.enterUserListView:
            {
                View view = getLayoutInflater().inflate(R.layout.dialog_listview, null);

                ListView listview = (ListView) view.findViewById(R.id.listView);
                UserListAdapter userListAdapter = new UserListAdapter(this, R.layout.listview_item_userprofile);
                userListAdapter.addAll(chatRoomData.getEnterUserProfileList());
                listview.setAdapter(userListAdapter);

                AlertDialog.Builder listViewDialog = new AlertDialog.Builder(this);
                listViewDialog.setView(view);
                listViewDialog.setPositiveButton("확인", null);
                listViewDialog.show();

                return true;
            }
            case R.id.addUser:
            {
                final List<UserProfileVO> addTargetUserList = new LinkedList<>();
                final ArrayList<String> items = new ArrayList<String>() ;
                final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items) ;

                for(UserProfileVO userProfile: chatService.getUserProfileMap().values()) {
                    if(! chatRoomData.getEnterUserProfileList().contains(userProfile)) {
                        addTargetUserList.add(userProfile);
                        items.add(userProfile.getName() + " (id: " + userProfile.getId() + ")");
                    }
                }

                View view = getLayoutInflater().inflate(R.layout.dialog_listview, null);
                final ListView listview = (ListView) view.findViewById(R.id.listView);
                listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                listview.setAdapter(adapter);

                AlertDialog.Builder listViewDialog = new AlertDialog.Builder(this);
                listViewDialog.setView(view);
                listViewDialog.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userIdListString = "";
                        SparseBooleanArray checkedItems = listview.getCheckedItemPositions();

                        if(checkedItems != null || checkedItems.size() < 1){
                            int addUserIds[] = new int[checkedItems.size()];
                            for(int i=0; i<checkedItems.size(); i++){
                                addUserIds[i] = addTargetUserList.get(checkedItems.keyAt(i)).getId();
                            }

                            ChatService.getInstance().addUserRoom(chatRoomId, addUserIds);
                        } else {
                            Toast.makeText(getBaseContext(), "추가하는 사용자를 1인 이상 선택해주십시오.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                listViewDialog.setNegativeButton("취소", null);
                listViewDialog.show();

                return true;
            }
            case R.id.exitRoom:
            {
                final ChatRoomClientVO chatRoomData = chatService.getChatRoomMap().get(chatRoomId);

                AlertDialog.Builder ab = new AlertDialog.Builder(this);
                ab.setMessage("채팅방을 나가시겠습니까?");
                ab.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chatService.exitRoom(chatRoomData.getChatRoomId());
                        finish();
                    }
                });
                ab.setNegativeButton("아니요", null);
                ab.show();

                return true;
            }
        }
        return false;
    }

    private void DialogSelectOption() {
        final String items[] = { "사진", "카메라", "주사위", "이모티콘" };

        final android.app.AlertDialog.Builder ab = new android.app.AlertDialog.Builder(this);
        ab.setTitle("Contents");
        ab.setSingleChoiceItems(items,-1,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (i){
                    case 0:
                        /* 이미지 선택 */
                        Intent intent = new Intent();
                        // Gallery 호출
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        try {
                            intent.putExtra("return-data", true);
                            startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);
                        } catch (ActivityNotFoundException e) { }
                        break;
                    case 1:
                         /* 카메라 촬영 */
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                        cameraIntent.putExtra("return-data", true);

                        try {
                            startActivityForResult(Intent.createChooser(cameraIntent, "Complete action using"), PICK_FROM_CAMERA);
                        } catch (ActivityNotFoundException e) { }
                        break;
                    case 2:
                        /* 주사위 게임 */
                        dice();
                        break;
                    case 3:
                        /* 이모티콘 선택 */
                        Intent intent1 = new Intent(getBaseContext(), Emoticon.class);
                        // 이모티콘 선택 엑티비티 호출
                        try {
                            intent1.putExtra("return-data", true);
                            startActivityForResult(Intent.createChooser(intent1,"Complete action using"), PICK_FROM_EMOTICON);
                        } catch (ActivityNotFoundException e) {
                            // Do nothing for now
                        }
                        break;
                }
                dialogInterface.dismiss();
            }
        });
        ab.show();
    }

    private void dice(){
        int[] diceImgList = new int[]{R.drawable.emoji_0031, R.drawable.emoji_0032, R.drawable.emoji_0033, R.drawable.emoji_0034, R.drawable.emoji_0035, R.drawable.emoji_0036};
        int num;
        num = (int)(Math.random()*6)+1;

        chatService.sendEmoticonMsg(chatRoomId, Integer.toString(diceImgList[num-1]));
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
                break;
            case MessageVO.MSG_TYPE_EMOTICON:
                chatMag = new ChatMessage(chatService.getMyProfile().getId().equals(msgData.getSenderId()) ? ChatMessage.SIDE_RIGHT : ChatMessage.SIDE_LEFT,
                        ChatMessage.TYPE_EMOTICON, msgData.getData(), userName, profileImagePath);

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
                FileSerivce.getInstance().sendImageMsg(chatService, chatRoomId, photo);
            }
        }
        if (requestCode == PICK_FROM_GALLERY) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                FileSerivce.getInstance().sendImageMsg(this, chatService, chatRoomId, fileUri);
            }
        }

        if (requestCode == PICK_FROM_EMOTICON) {
            int fileid = data.getIntExtra("fileid", 0);
            chatService.sendEmoticonMsg(chatRoomId, Integer.toString(fileid));
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
