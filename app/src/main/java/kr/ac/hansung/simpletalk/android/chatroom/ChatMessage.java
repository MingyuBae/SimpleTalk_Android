package kr.ac.hansung.simpletalk.android.chatroom;

public class ChatMessage {
	public static final String SIDE_LEFT = "left";
    public static final String SIDE_RIGHT = "right";
    public static final String SIDE_CENTER = "center";

    public String side;
	public String message;
    public String userName;

    public ChatMessage(String side, String message) {
        this(side, message, "");
    }

	public ChatMessage(String side, String message, String userName) {
		super();
		this.side = side;
		this.message = message;
        this.userName = userName;
	}
}