package kr.ac.hansung.simpletalk.android.chatroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.simpletalk.R;

public class MakeChatRoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_chat_room);

        Button addChatRoomBtn = (Button) findViewById(R.id.addChatRoomBtn);
        final EditText addChatRoomName = (EditText) findViewById(R.id.addChatRoomName);
        final EditText addChatRoomUserId = (EditText) findViewById(R.id.addChatRoomUserId);

        addChatRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ChatService.getInstance().makeRoom(addChatRoomName.getText().toString(),
                                    addChatRoomUserId.getText().toString());
                        }catch (Exception e) {e.printStackTrace();}
                    }
                });
                t.start();
            }
        });
    }
}
