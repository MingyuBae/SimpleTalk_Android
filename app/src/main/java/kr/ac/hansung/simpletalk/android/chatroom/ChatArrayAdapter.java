package kr.ac.hansung.simpletalk.android.chatroom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.hansung.simpletalk.android.R;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {
	private TextView chatText;
	private TextView userName;
	private ImageView userImage;
	private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
	private RelativeLayout singleMessageContainer;

	@Override
	public void add(ChatMessage object) {
		chatMessageList.add(object);
		super.add(object);
	}

	public ChatArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public int getCount() {
		return this.chatMessageList.size();
	}

	public ChatMessage getItem(int index) {
		return this.chatMessageList.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.activity_chat_singlemessage, parent, false);
		}
		singleMessageContainer = (RelativeLayout) row.findViewById(R.id.singleMessageContainer);
		ChatMessage chatMessageObj = getItem(position);
		chatText = (TextView) row.findViewById(R.id.singleMessage);
		chatText.setText(chatMessageObj.message);

		//사용자이름
		userName = (TextView) row.findViewById(R.id.name);
		userName.setText(chatMessageObj.userName);

		//사용자 프로필사진
		userImage = (ImageView)row.findViewById(R.id.userImage);
		userImage.setImageResource(R.drawable.ic_menu_gallery);

		//chatText.setBackgroundResource(chatMessageObj.left ? R.drawable.bubble_b : R.drawable.bubble_a);
		//사용자에 따라 출력 방향 다르게하기
		switch (chatMessageObj.side){
			case ChatMessage.SIDE_LEFT:
				chatText.setBackgroundResource(R.drawable.bubble_b);
				singleMessageContainer.setGravity(Gravity.LEFT);
				break;
			case ChatMessage.SIDE_RIGHT:
				chatText.setBackgroundResource(R.drawable.bubble_a);
				singleMessageContainer.setGravity(Gravity.RIGHT);
				break;
			case ChatMessage.SIDE_CENTER:
				chatText.setBackgroundResource(R.drawable.bubble_c);
				chatText.setText(chatMessageObj.message);
				singleMessageContainer.setGravity(Gravity.CENTER);
				userName.setVisibility(View.INVISIBLE);
				userImage.setVisibility(View.INVISIBLE);
				break;
		}
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}