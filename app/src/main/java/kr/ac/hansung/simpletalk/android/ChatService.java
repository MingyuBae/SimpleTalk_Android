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
    private String ip = "223.194.154.228"; //"223.194.157.33"; // IP
    private int port = 30000; // PORT번호

    private Handler socketHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
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
                        // TODO 내 정보일때만 프로필 업데이트 처리 필요 + 엑티비티에 업데이트 된것 알려줄것
                        Boolean isInit = (Boolean) msg.getData().getSerializable("init");
                        UserProfileVO getProfile = ((UserProfileVO)msgData.getObject());
                        if(MessageVO.MSG_TYPE_INIT_PROFILE.equals(msgData.getType()) || (myProfile.getId() == getProfile.getId())){
                            myProfile = getProfile;
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
                            // TODO 엑티비티에 채팅방에 접속 됬다고 통보해 줄것
                        }
                        roomData.setEnterUserProfileList((LinkedList<UserProfileVO>) msgData.getObject());
                        if(roomData.getChatRoomId() == 0){
                            mappingUserProfileMap((LinkedList<UserProfileVO>) msgData.getObject());
                        }
                        break;

                    case MessageVO.MSG_TYPE_EXIT_CHATROOM_USER:
                        ChatRoomClientVO chatRoomData = chatRoomMap.get(msgData.getRoomId());
                        if(chatRoomData == null){
                            Log.w("exitRoom", "접속하지 않은 채팅방의 알림 수신! - roomId: " + msgData.getRoomId());
                        }

                        if(msgData.getRoomId() == 0){
                            mappingUserProfileMap((LinkedList<UserProfileVO>) msgData.getObject());
                        }
                        break;
                    case MessageVO.MSG_TYPE_MAKEROOM:
                        ChatRoomClientVO newRoomData = new ChatRoomClientVO();
                        newRoomData.setChatRoomId(msgData.getRoomId());
                        newRoomData.setRoomName(msgData.getData());
                        newRoomData.setEnterUserProfileList((LinkedList<UserProfileVO>) msgData.getObject());
                        chatRoomMap.put(msgData.getRoomId(), newRoomData);
                        Log.i("enterRoom", "채팅방 생성 - newRoomData: " + newRoomData);
                        // TODO 엑티비티에 채팅방에 접속 됬다고 통보해 줄것

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

    public static ChatService getInstance(){
        if(instance == null){
            instance = new ChatService();
        }
        return instance;
    }

    private ChatService(){ }

    @Override
    public void run(){
        socketChatThread = new SocketChatThread(ip, port);
        socketChatThread.setHandler(socketHandler);
        socketChatThread.start();
    }

    public boolean sendTextMsg(int chatRoomId, String sendText){
        try {
            socketChatThread.sendTextMsg(chatRoomId, myProfile.getId(), sendText);
        }catch (IOException e){
            Log.w("network", "메시지 전송 실패 - 네트워크 오류 (chatRoomId: " + chatRoomId + ", sendText: " + sendText + ")");
            return false;
        }
        return true;
    }

    public boolean makeRoom(String roomName, String enterUserIdString){
        String userIdStringArray[] = enterUserIdString.split(" ");
        int enterUserIdArray[] = new int[enterUserIdString.length()];

        for(int i=0; i<userIdStringArray.length; i++){
            enterUserIdArray[i] = Integer.parseInt(userIdStringArray[i]);
        }

        try{
            socketChatThread.makeRoom(roomName, enterUserIdArray);
        } catch (ClassNotFoundException| IOException e) {
            e.printStackTrace();
            Log.w("makeChatRoom", "실패 (roomName: " + roomName + ", enterUserIdString: "
                    + enterUserIdString + ")");
            return false;
        }


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
