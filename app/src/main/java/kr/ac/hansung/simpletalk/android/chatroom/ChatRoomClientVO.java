package kr.ac.hansung.simpletalk.android.chatroom;

import java.util.LinkedList;
import java.util.List;

import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

public class ChatRoomClientVO {
    private Integer chatRoomId;
    private String roomName = "";
    private List<MessageVO> messageList = new LinkedList<>();
    private List<UserProfileVO> enterUserProfileList;

    public Integer getChatRoomId() {
        return chatRoomId;
    }
    public void setChatRoomId(Integer chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    public List<UserProfileVO> getEnterUserProfileList() {
        return enterUserProfileList;
    }
    public void setEnterUserProfileList(List<UserProfileVO> enterUserProfileList) {
        this.enterUserProfileList = enterUserProfileList;
    }
    public List<MessageVO> getMessageList() {
        return messageList;
    }
    public void setMessageList(List<MessageVO> messageList) {
        this.messageList = messageList;
    }
    public void addMessageList(MessageVO message){
        this.messageList.add(message);
    }
}
