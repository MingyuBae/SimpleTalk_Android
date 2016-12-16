package kr.ac.hansung.simpletalk.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import kr.ac.hansung.simpletalk.android.chatroom.ChatRoomActivity;
import kr.ac.hansung.simpletalk.android.chatroom.ChatRoomClientVO;
import kr.ac.hansung.simpletalk.android.chatroom.MakeChatRoomActivity;
import kr.ac.hansung.simpletalk.android.setting.NetworkSettingActivity;
import kr.ac.hansung.simpletalk.android.setting.ProfileSettingActivity;
import kr.ac.hansung.simpletalk.android.userlist.UserListActivity;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences setting;
    private ChatService chatService;
    private ChatRoomListAdapter chatRoomAdapter;

    private Handler serviceHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            MessageVO msgData = (MessageVO)msg.getData().getSerializable("msg");
            if(msgData != null) {
                switch (msgData.getType()) {
                    case MessageVO.MSG_TYPE_TEXT:
                        chatRoomAdapter.clear();
                        chatRoomAdapter.addAll(chatService.getChatRoomMap().values());
                        chatRoomAdapter.notifyDataSetChanged();
                        break;
                    case MessageVO.MSG_TYPE_CHANGE_PROFILE:
                    case MessageVO.MSG_TYPE_INIT_PROFILE:
                        UserProfileVO profile = (UserProfileVO) msgData.getObject();
                        if(profile.getId().equals(chatService.getMyProfile().getId())
                                || MessageVO.MSG_TYPE_INIT_PROFILE.equals(msgData.getType())){
                            TextView userName = (TextView) findViewById(R.id.userName);
                            TextView userStateMsg = (TextView) findViewById(R.id.userStateMsg);
                            ImageView userImage = (ImageView)findViewById(R.id.userProfileImage);
                            if(profile != null && userName != null && userStateMsg != null && userImage != null) {
                                userName.setText(profile.getName() + "(id: " + profile.getId() + ")");
                                userStateMsg.setText(profile.getStateMsg());

                                if (profile.getImgFileName() != null && !profile.getImgFileName().isEmpty()) {
                                    FileSerivce.getInstance().roundLoadImage(getBaseContext(), null, userImage, profile.getImgFileName());
                                }
                            }
                        }
                        break;

                    case MessageVO.MSG_TYPE_ADD_CHATROOM_USER:
                    case MessageVO.MSG_TYPE_EXIT_CHATROOM_USER:
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
        setting = getSharedPreferences("setting", Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("채팅방 목록");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if(! chatService.isDaemon()){
            chatService.initNetwork(setting.getString("ip", "192.168.1.128"), setting.getInt("port", 30000));
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

        TextView userName = (TextView) findViewById(R.id.userName);
        TextView userStateMsg = (TextView) findViewById(R.id.userStateMsg);
        ImageView userImage = (ImageView)findViewById(R.id.userProfileImage);

        UserProfileVO profile = chatService.getMyProfile();

        if(profile != null && userName != null && userStateMsg != null && userImage != null) {
            userName.setText(profile.getName() + "(id: " + profile.getId() + ")");
            userStateMsg.setText(profile.getStateMsg());

            if(profile.getImgFileName() != null && !profile.getImgFileName().isEmpty()){
                FileSerivce.getInstance().roundLoadImage(this, null, userImage, profile.getImgFileName());
            }
        }
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
        } else if (id == R.id.nav_network_setting) {
            Intent intent = new Intent(this, NetworkSettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile_setting) {
            Intent intent = new Intent(this, ProfileSettingActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
