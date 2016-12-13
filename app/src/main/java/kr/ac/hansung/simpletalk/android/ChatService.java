package kr.ac.hansung.simpletalk.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.ac.hansung.simpletalk.android.chatroom.ChatRoomClientVO;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

/**
 * Created by a3811 on 2016-12-01.
 */

public class ChatService extends Thread {
    private static ChatService instance;

    private SocketChatThread socketChatThread;
    private UserProfileVO myProfile;
    private Map<Integer, UserProfileVO> userProfileMap = Collections.synchronizedMap(new HashMap<Integer, UserProfileVO>());
    private Map<Integer, ChatRoomClientVO> chatRoomMap = Collections.synchronizedMap(new HashMap<Integer, ChatRoomClientVO>());

    private Handler nowActivityHandler;
    private String ip = "223.194.155.110"; //"223.194.157.33"; // IP
    private int port = 30000; // PORT번호
    private boolean runable = false;

    private Handler socketHandler = new Handler(){
        public synchronized void handleMessage(android.os.Message msg) {
            MessageVO msgData = (MessageVO)msg.getData().getSerializable("msg");
            if(msgData != null) {
                switch (msgData.getType()) {
                    case MessageVO.MSG_TYPE_TEXT:
                        //chatArrayAdapter.add(new ChatMessage(msgData.getSenderId().equals(socketChatThread.getUserProfile().getId()), msgData.toString()));
                        ChatRoomClientVO roomData1 = chatRoomMap.get(msgData.getRoomId());
                        roomData1.addMessageList(msgData);

                        break;
                    case MessageVO.MSG_TYPE_CHANGE_PROFILE:
                    case MessageVO.MSG_TYPE_INIT_PROFILE:
                        Boolean isInit = (Boolean) msg.getData().getSerializable("init");
                        UserProfileVO chagedUserProfile = ((UserProfileVO)msgData.getObject());
                        if(MessageVO.MSG_TYPE_INIT_PROFILE.equals(msgData.getType()) || (myProfile.getId() == chagedUserProfile.getId())){
                            /* 초기 프로필 데이터나 내 프로필일 경우 */
                            myProfile = chagedUserProfile;
                        } else {
                            userProfileMap.put(msgData.getSenderId(), chagedUserProfile);
                        }
                        break;

                    case MessageVO.MSG_TYPE_ADD_CHATROOM_USER:
                        ChatRoomClientVO roomData = chatRoomMap.get(msgData.getRoomId());
                        if(roomData == null){
                            /* 장치 사용자가 채팅방에 접속했을때 */
                            roomData = new ChatRoomClientVO();
                            roomData.setChatRoomId(msgData.getRoomId());
                            roomData.setRoomName("이름없음");
                            chatRoomMap.put(msgData.getRoomId(), roomData);
                            Log.i("enterRoom", "채팅방 접속 - roomData: " + roomData);
                        }
                        roomData.setEnterUserProfileList((LinkedList<UserProfileVO>) msgData.getObject());
                        if(roomData.getChatRoomId() == 0){
                            mappingUserProfileMap((LinkedList<UserProfileVO>) msgData.getObject());
                        }
                        String addUserNameString = "";
                        String addUserIdStringArray[] = msgData.getData().split(MessageVO.MSG_SPLIT_CHAR);

                        for(String addUserId: addUserIdStringArray){
                            addUserNameString = userProfileMap.get(Integer.parseInt(addUserId)).getName() + " ";
                        }
                        msgData.setData(addUserNameString + "입장");
                        roomData.addMessageList(msgData);
                        break;

                    case MessageVO.MSG_TYPE_EXIT_CHATROOM_USER:
                        ChatRoomClientVO chatRoomData = chatRoomMap.get(msgData.getRoomId());
                        if(chatRoomData == null) {
                            Log.w("exitRoom", "접속하지 않은 채팅방의 알림 수신! - roomId: " + msgData.getRoomId());
                        }

                        msgData.setData( msgData.getData() + " 퇴장");

                        if(msgData.getRoomId() == 0){
                            mappingUserProfileMap((LinkedList<UserProfileVO>) msgData.getObject());
                        }
                        chatRoomData.addMessageList(msgData);
                        break;
                    case MessageVO.MSG_TYPE_MAKEROOM:
                        ChatRoomClientVO newRoomData = new ChatRoomClientVO();
                        newRoomData.setChatRoomId(msgData.getRoomId());
                        newRoomData.setRoomName(msgData.getData());
                        newRoomData.setEnterUserProfileList((LinkedList<UserProfileVO>) msgData.getObject());
                        chatRoomMap.put(msgData.getRoomId(), newRoomData);
                        Log.i("enterRoom", "채팅방 생성 - newRoomData: " + newRoomData);
                        break;
                }

                Bundle data = new Bundle();
                data.putSerializable("msg", msgData);

                Message sendMsg = new Message();
                sendMsg.setData(data);

                nowActivityHandler.sendMessage(sendMsg);
            }

            //chat_list.append(r_msg + "\n");
            //if(!m.getId().equals(login_id))
            //if (!r_msg.contains(login_id))
            //   newNotification(r_msg);			// 알림기능 (알림바에 알림 추가)
        }
    };

    public synchronized  static ChatService getInstance(){
        if(instance == null){
            instance = new ChatService();
        }
        return instance;
    }

    private ChatService(){ }

    @Override
    public void run(){
        socketChatThread = SocketChatThread.getInstence();
        if(! socketChatThread.isAlive()){
            socketChatThread.setNetworkSetting(ip, port);
            socketChatThread.setHandler(socketHandler);
            socketChatThread.start();
        }
    }

    public void sendTextMsg(final int chatRoomId, final String sendText){
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    socketChatThread.sendTextMsg(chatRoomId, myProfile.getId(), sendText);
                } catch (IOException e) {
                    Log.w("network", "메시지 전송 실패 - 네트워크 오류 (chatRoomId: " + chatRoomId + ", sendText: " + sendText + ")");
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        return;
    }

    public void makeRoom(final String roomName, final String enterUserIdString){

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    socketChatThread.makeRoom(roomName, enterUserIdString);
                } catch (IOException | ClassNotFoundException e) {
                    Log.w("makeChatRoom", "실패 (roomName: " + roomName + ", enterUserIdString: "
                            + enterUserIdString + ")");
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        return;
    }

    public boolean changeMyProfile(final UserProfileVO userProfileVO){
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    socketChatThread.sendMsg(MessageVO.MSG_TYPE_CHANGE_PROFILE, myProfile.getId(), 0, "", myProfile);
                } catch (IOException e) {
                    Log.w("network", "프로필 수정 실패 - 네트워크 오류 (userProfileVO: " + userProfileVO + ")");
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        return true;
    }

    public void initNetwork(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public UserProfileVO getMyProfile() {
        return myProfile;
    }

    public Map<Integer, UserProfileVO> getUserProfileMap() {
        return userProfileMap;
    }

    public Map<Integer, ChatRoomClientVO> getChatRoomMap() {
        return chatRoomMap;
    }

    public void setNowActivityHandler(Handler nowActivityHandler) {
        this.nowActivityHandler = nowActivityHandler;
    }

    private void mappingUserProfileMap(List<UserProfileVO> userProfileList){
        userProfileMap.clear();

        for(UserProfileVO userProfileData: userProfileList){
            userProfileMap.put(userProfileData.getId(), userProfileData);
        }
    }
}
