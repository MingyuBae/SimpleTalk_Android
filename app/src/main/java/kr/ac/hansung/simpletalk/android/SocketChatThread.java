package kr.ac.hansung.simpletalk.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.Socket;

import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

/**
 * Created by MingyuBae on 2016-11-24.
 */

public class SocketChatThread extends Thread {
    private static SocketChatThread instence;
    private Handler handler;
    private UserProfileVO userData;
    private MessageVO lastMsg;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private String ip = "223.194.157.33"; // IP
    private int port = 30000; // PORT번호

    private SocketChatThread(){}

    public synchronized static SocketChatThread getInstence(){
        if(instence == null){
            instence = new SocketChatThread();
        }
        return instence;
    }

    public void run(){
        try {
            setSocket(ip, port);
            sendUserProfileData("사용자 이름", "상태 메시지 !");
            recvUserData();
            addChatRoomUser(0, new int[]{userData.getId()});

            while(true){
                MessageVO msgData = recvMsgData();
                lastMsg = msgData;

                Bundle data = new Bundle();
                data.putSerializable("msg", msgData);

                Message sendMsg = new Message();
                sendMsg.setData(data);

                handler.sendMessage(sendMsg);

            }

        } catch (Exception e1) {
            e1.printStackTrace();

            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String excptionAsString = sw.toString();
            Log.e("network", excptionAsString);

        }
        Log.i("network", "마지막");
        onStop();
    }


    public void makeRoom(String roomName, String enterUserIdListString) throws IOException, ClassNotFoundException {
        MessageVO msg = new MessageVO();
        msg.setType(MessageVO.MSG_TYPE_MAKEROOM);
        msg.setData(roomName);
        msg.setObject(enterUserIdListString);

        objectOutputStream.writeObject(msg);

        //MessageVO roomMakeReturnMsg = (MessageVO) objectInputStream.readObject();
        //Log.i("network", "채팅방 생성 정보 - " + roomMakeReturnMsg.toString());
    }

    public void addChatRoomUser(int chatRoomId, int addUserIdArray[]) throws IOException {
        String addUserIdListString = "";
        for(int addUserId : addUserIdArray){
            addUserIdListString += addUserId + MessageVO.MSG_SPLIT_CHAR;
        }
        MessageVO msg = new MessageVO();
        msg.setRoomId(chatRoomId);
        msg.setType(MessageVO.MSG_TYPE_ADD_CHATROOM_USER);
        msg.setData(addUserIdListString);

        objectOutputStream.writeObject(msg);
    }

    public void sendTextMsg(int chatRoomId, int myId, String sendText) throws IOException {
        sendMsg(MessageVO.MSG_TYPE_TEXT, myId, chatRoomId, sendText, null);
    }

    public synchronized void sendMsg(String type, int senderId, int chatRoomId, String data, Serializable object) throws IOException {
        MessageVO msg2 = new MessageVO();
        msg2.setType(type);
        msg2.setSenderId(senderId);
        msg2.setRoomId(chatRoomId);
        msg2.setData(data);
        msg2.setObject(object);
        objectOutputStream.reset();             // 리셋하지 않으면 동일한 레퍼런스값을 가진 객체를 전송시 리셋하지 않으면 갱신된 데이터를 전송하지 않는 문제가 있음
                                                   // 참조: http://stackoverflow.com/questions/12341086/java-socket-serialization-object-wont-update
        objectOutputStream.writeObject(msg2);
    }

    public void setNetworkSetting(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public UserProfileVO getUserProfile(){
        return userData;
    }

    public void setUserProfile(UserProfileVO userData){
        this.userData = userData;
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }

    private void onStop() {
        if(socket == null){
            return;
        }

        try {
            socket.close();
            inputStream.close();
            outputStream.close();
            objectOutputStream.close();
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSocket(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        outputStream = socket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        inputStream = socket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
    }

    private void sendUserProfileData(String name, String stateMsg) throws IOException{
        userData = new UserProfileVO();
        userData.setName(name);
        userData.setStateMsg(stateMsg);

        objectOutputStream.writeObject(userData);
    }

    private void recvUserData() throws IOException, ClassNotFoundException {
        MessageVO recvMsg = (MessageVO) objectInputStream.readObject();
        userData = (UserProfileVO) recvMsg.getObject();

        Bundle data = new Bundle();
        data.putSerializable("msg", recvMsg);

        Message sendMsg = new Message();
        sendMsg.setData(data);

        handler.sendMessage(sendMsg);
        Log.i("network", "init 사용자 정보 수신 - " + userData.toString());
    }

    private MessageVO recvMsgData() throws IOException, ClassNotFoundException {
        MessageVO msgData = (MessageVO)objectInputStream.readObject();
        Log.i("network", "메시지 수신 - " + msgData);
        return msgData;
    }

    /*
    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                Log.w("ChattingStart", "Start Thread");
                while (true) {
                    Log.w("Chatting is running", "chatting is running");
                    line = networkReader.readLine();
                    html = line;
                    mHandler.post(showUpdate);
                }
            } catch (Exception e) {

            }
        }
    };

    private Runnable showUpdate = new Runnable() {

        public void run() {
            Toast.makeText(NewClient.this, "Coming word: " + html, Toast.LENGTH_SHORT).show();
        }

    };
    */
}
