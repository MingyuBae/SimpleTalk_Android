package kr.ac.hansung.simpletalk.android.chatroom;

public class ChatMessage {
	public static final String SIDE_LEFT = "left";
    public static final String SIDE_RIGHT = "right";
    public static final String SIDE_CENTER = "center";

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_EMOTICON = 3;

    public String side;
    public int type = TYPE_TEXT;
	public String message;
    public String userName;
    public String profileImagePath;


    public ChatMessage(String side, String message) {
        this(side, TYPE_TEXT, message, "", null);
    }

	public ChatMessage(String side, int type, String message, String userName, String profileImagePath) {
		super();
		this.side = side;
        this.type = type;
		this.message = message;
        this.userName = userName;
        this.profileImagePath = profileImagePath;
	}

    public void drawImage(byte[] bytes){

    }
}