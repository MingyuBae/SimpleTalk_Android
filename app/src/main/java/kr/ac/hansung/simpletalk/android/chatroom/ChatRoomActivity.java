package kr.ac.hansung.simpletalk.android.chatroom;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Map;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.simpletalk.R;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class ChatRoomActivity extends AppCompatActivity {
    Integer chatRoomId;
    ChatService chatService;
    ChatArrayAdapter chatArrayAdapter;
    ChatRoomClientVO chatRoomData;
    EditText editText;
    Button sendBtn;
    //Map<Integer, UserProfileVO> userProfileMap;

    private Handler serviceHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            MessageVO msgData = (MessageVO)msg.getData().getSerializable("msg");
            if(msgData != null) {
                if(msgData.getRoomId() != null && chatRoomId.equals(msgData.getRoomId())) {
                    switch (msgData.getType()) {
                        case MessageVO.MSG_TYPE_TEXT:
                            chatArrayAdapter.add(new ChatMessage(chatService.getMyProfile().getId().equals(msgData.getSenderId()),
                                    msgData.getData()));
                            break;
                        case MessageVO.MSG_TYPE_ADD_CHATROOM_USER:
                        case MessageVO.MSG_TYPE_EXIT_CHATROOM_USER:
                            chatArrayAdapter.add(new ChatMessage(false, msgData.getData()));
                            break;
                    }
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

        //getActionBar().setTitle("Hello world App");
        Intent intent = getIntent();

        chatRoomId = intent.getIntExtra("roomId", -1);

        ListView listView = (ListView) findViewById(R.id.chatList);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        listView.setAdapter(chatArrayAdapter);

        chatService = ChatService.getInstance();
        chatService.setNowActivityHandler(serviceHandler);
        chatRoomData = chatService.getChatRoomMap().get(chatRoomId);

        for(MessageVO message: chatRoomData.getMessageList()){
            chatArrayAdapter.add(new ChatMessage(chatService.getMyProfile().getId().equals(message.getSenderId()),
                    message.getData()));
        }

        editText = (EditText)findViewById(R.id.chatEditText);
        sendBtn = (Button)findViewById(R.id.sendBtn);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
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

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    chatService.sendTextMsg(chatRoomId, editText.getText().toString());
                }catch (Exception e) {e.printStackTrace();}
            }
        });
        t.start();

        editText.setText("");
    }
}
