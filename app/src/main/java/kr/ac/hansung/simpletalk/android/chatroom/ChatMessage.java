package kr.ac.hansung.simpletalk.android.chatroom;

public class ChatMessage {
	public static final String SIDE_LEFT = "left";
    public static final String SIDE_RIGHT = "right";
    public static final String SIDE_CENTER = "center";

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;

    public String side;
	public String message;
    public String userName;
    public int type = TYPE_TEXT;
    public byte[] bytes;

    public ChatMessage(String side, String message) {
        this(side, message, "");
    }

	public ChatMessage(String side, String message, String userName) {
		super();
		this.side = side;
		this.message = message;
        this.userName = userName;
	}

    public void drawImage(byte[] bytes){

    }
}