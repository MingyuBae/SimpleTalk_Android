package kr.ac.hansung.simpletalk.android.chatroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.android.R;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class MakeChatRoomActivity extends AppCompatActivity {

    List<UserProfileVO> userProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_chat_room);

        getSupportActionBar().setTitle("채팅방 생성");

        ChatService chatService = ChatService.getInstance();
        userProfiles = new LinkedList(chatService.getUserProfileMap().values());

        Button addChatRoomBtn = (Button) findViewById(R.id.addChatRoomBtn);
        final EditText addChatRoomName = (EditText) findViewById(R.id.addChatRoomName);

        final ArrayList<String> items = new ArrayList<String>() ;
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items) ;

        for(UserProfileVO userProfile: userProfiles) {
            items.add(userProfile.getName() + " (id: " + userProfile.getId() + ")");
        }
        adapter.notifyDataSetChanged();

        final ListView listview = (ListView) findViewById(R.id.userListView) ;
        listview.setAdapter(adapter);

        addChatRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userIdListString = "";
                SparseBooleanArray checkedItems = listview.getCheckedItemPositions();

                if(checkedItems != null || checkedItems.size() < 1){
                    for(int i=0; i<checkedItems.size(); i++){
                        userIdListString += userProfiles.get(checkedItems.keyAt(i)).getId()
                                                                    + MessageVO.MSG_SPLIT_CHAR;
                    }

                    ChatService.getInstance().makeRoom(addChatRoomName.getText().toString(), userIdListString);

                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "채팅에 참여하는 사용자를 2인 이상 선택해주십시오.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
