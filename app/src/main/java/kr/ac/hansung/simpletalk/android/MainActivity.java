package kr.ac.hansung.simpletalk.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

import kr.ac.hansung.simpletalk.android.chatroom.ChatArrayAdapter;
import kr.ac.hansung.simpletalk.android.chatroom.ChatMessage;
import kr.ac.hansung.simpletalk.android.chatroom.ChatRoomActivity;
import kr.ac.hansung.simpletalk.android.chatroom.ChatRoomClientVO;
import kr.ac.hansung.simpletalk.android.chatroom.MakeChatRoomActivity;
import kr.ac.hansung.simpletalk.android.userlist.UserListActivity;
import kr.ac.hansung.simpletalk.android.userlist.UserListAdapter;
import kr.ac.hansung.simpletalk.simpletalk.R;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ChatService chatService;
    ChatArrayAdapter chatArrayAdapter;
    ChatRoomListAdapter chatRoomAdapter;

    private Handler serviceHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            MessageVO msgData = (MessageVO)msg.getData().getSerializable("msg");
            if(msgData != null) {
                switch (msgData.getType()) {
                    case MessageVO.MSG_TYPE_TEXT:
                        // TODO 채팅방 리스트에 마지막 메시지가 출력되도록 처리해야 됨
                        break;
                    case MessageVO.MSG_TYPE_CHANGE_PROFILE:
                    case MessageVO.MSG_TYPE_INIT_PROFILE:
                        // TODO 내 정보일때만 프로필 업데이트 처리 필요 + 엑티비티에 업데이트 된것 알려줄것
                        UserProfileVO profile = (UserProfileVO) msgData.getObject();
                        if(profile.getId() == chatService.getMyProfile().getId()
                                || MessageVO.MSG_TYPE_INIT_PROFILE.equals(msgData.getType())){
                            TextView userName = (TextView) findViewById(R.id.userName);
                            TextView userStateMsg = (TextView) findViewById(R.id.userStateMsg);

                            userName.setText(profile.getName() + "(id: " + profile.getId() + ")");
                            userStateMsg.setText(profile.getStateMsg());
                        }
                        break;

                    case MessageVO.MSG_TYPE_ADD_CHATROOM_USER:
                    case MessageVO.MSG_TYPE_EXIT_CHATROOM_USER:
                        // TODO 내가 채팅방에 초대, 탈퇴된 경우 리스트를 갱신해야 됨
                        chatRoomAdapter.clear();
                        chatRoomAdapter.addAll(chatService.getChatRoomMap().values());
                        chatRoomAdapter.notifyDataSetChanged();
                        break;
                    case MessageVO.MSG_TYPE_MAKEROOM:
                        chatRoomAdapter.clear();
                        chatRoomAdapter.addAll(chatService.getChatRoomMap().values());
                        chatRoomAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 채팅방 생성 기능이 들어가야 됨
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(getBaseContext(), MakeChatRoomActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        chatService = ChatService.getInstance();
        chatService.setNowActivityHandler(serviceHandler);
        if(! chatService.isAlive()){
            chatService.run();
        }

        ListView chatRoomList = (ListView) findViewById(R.id.roomList);
        chatRoomAdapter = new ChatRoomListAdapter(this, R.layout.listview_item_chatroom);
        chatRoomAdapter.addAll(chatService.getChatRoomMap().values());
        chatRoomList.setAdapter(chatRoomAdapter);

        chatRoomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRoomClientVO chatRoomData = (ChatRoomClientVO) parent.getItemAtPosition(position);

                Intent intent = new Intent(getBaseContext(), ChatRoomActivity.class);
                intent.putExtra("roomId", chatRoomData.getChatRoomId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatService.setNowActivityHandler(serviceHandler);

        chatRoomAdapter.clear();
        chatRoomAdapter.addAll(chatService.getChatRoomMap().values());
        chatRoomAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_userlist) {
            Intent intent = new Intent(this, UserListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeProfile(UserProfileVO userProfileData){
        TextView userName = (TextView)findViewById(R.id.userName);
        TextView userStateMsg = (TextView)findViewById(R.id.userStateMsg);

        if(userName != null && userStateMsg != null) {
            // TODO 가끔씩 userName과 userStateMsg값이 NULL이 들어가는 경우가 있음, 수정필요
            userName.setText(userProfileData.getName() + " (userID: " + userProfileData.getId() + ")");
            userStateMsg.setText(userProfileData.getStateMsg());
        }
    }
}
