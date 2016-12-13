package kr.ac.hansung.simpletalk.android.chatroom;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import kr.ac.hansung.simpletalk.android.ChatService;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;

/**
 * Created by a3811 on 2016-12-14.
 */

public class FileSerivce {
    public static final long MAX_FILESIZE = 1024 * 1024 * 5;
    private static FileSerivce instance;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReferenceFromUrl("gs://simpletalk-ce063.appspot.com");
    private StorageReference imagesRef = storageRef.child("images");
    private StorageReference emoticonRef = storageRef.child("emoticons");

    private FileSerivce() {
    }

    public synchronized FileSerivce getInstance() {
        if (instance == null) {
            instance = new FileSerivce();
        }
        return instance;
    }

    public void uploadImage(final ChatService chatService, final int chatId, final Uri imageUri) {
        StorageReference imagesRef = storageRef.child("images/" + imageUri.getLastPathSegment());
        UploadTask uploadTask = imagesRef.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w("FileUpload", "실패 - " + exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                chatService.sendImageUrlMsg(chatId, imageUri.toString());
                Log.w("FileUpload", "성공 - " + imageUri.toString());
            }
        });
    }

    public void getImage(final ChatArrayAdapter chatArrayAdapter, final ChatMessage chatMessage, String path){
        StorageReference imageRef = storageRef.child(path);

        imageRef.getBytes(MAX_FILESIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {

            @Override
            public void onSuccess(byte[] bytes) {
                chatMessage.bytes = bytes;
                chatMessage.type = ChatMessage.TYPE_IMAGE;

                chatArrayAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                chatMessage.message = "이미지 로드 실패";
                chatArrayAdapter.notifyDataSetChanged();
            }
        });
    }
}
