package kr.ac.hansung.simpletalk.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.ac.hansung.simpletalk.android.chatroom.ChatRoomClientVO;
import kr.ac.hansung.simpletalk.simpletalk.R;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

/**
 * Created by a3811 on 2016-12-02.
 */

public class ChatRoomListAdapter extends ArrayAdapter<ChatRoomClientVO> {
    private List<ChatRoomClientVO> chatRoomClientList = new LinkedList<>();
    private Map<Integer, Boolean> checkedUserMap = new HashMap<>();

    public ChatRoomListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public void add(ChatRoomClientVO object) {
        chatRoomClientList.add(object);
        super.add(object);
    }

    @Override
    public void addAll(Collection<? extends ChatRoomClientVO> collection) {
        chatRoomClientList.addAll(collection);
        super.addAll(collection);
    }

    @Override
    public void clear() {
        chatRoomClientList.clear();
        super.clear();
    }

    @Override
    public int getCount() {
        return chatRoomClientList.size();
    }

    @Nullable
    @Override
    public ChatRoomClientVO getItem(int position) {
        return chatRoomClientList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_item_chatroom, parent, false);
        }
        TextView chatRoomName = (TextView) convertView.findViewById(R.id.chatroom_name);

        ChatRoomClientVO chatRoomData = chatRoomClientList.get(position);

        chatRoomName.setText(chatRoomData.getRoomName() + "(RoomId: " + chatRoomData.getChatRoomId() + ")");

        return convertView;
    }
}
