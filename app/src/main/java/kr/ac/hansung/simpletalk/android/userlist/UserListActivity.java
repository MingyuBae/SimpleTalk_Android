package kr.ac.hansung.simpletalk.android.userlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.Map;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.android.R;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class UserListActivity extends AppCompatActivity {
    ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        getSupportActionBar().setTitle("현재 접속자");

        chatService = ChatService.getInstance();

        Map<Integer, UserProfileVO> userProfileMap = chatService.getUserProfileMap();

        ListView userListView = (ListView)findViewById(R.id.userListView);
        final UserListAdapter userListAdapter = new UserListAdapter(this, R.layout.listview_item_userprofile);
        userListAdapter.addAll(userProfileMap.values());

        userListView.setAdapter(userListAdapter);

    }
}
