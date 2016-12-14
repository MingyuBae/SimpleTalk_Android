package kr.ac.hansung.simpletalk.android.chatroom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

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

    private FileSerivce() { }

    public static synchronized FileSerivce getInstance() {
        if (instance == null) {
            instance = new FileSerivce();
        }
        return instance;
    }

    public void uploadImage(Context context, final ChatService chatService, final int chatId, final Uri imageUri) {
        final StorageReference imagesRef = storageRef.child("images/" + imageUri.getLastPathSegment() + "_"+ UUID.randomUUID().toString());

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            Bitmap resizeBitmap = imageResize(bitmap, 500);                      // 이미지 리사이징

            /* JPEG로 파일 압축 */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            /* 파일 업로드 */
            UploadTask uploadTask = imagesRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.w("imageUpload", "실패 - " + exception.toString());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    chatService.sendImageUrlMsg(chatId, imagesRef.getPath());
                    Log.w("imageUpload", "성공 - " + imagesRef.toString());
                }
            });
        } catch (IOException e) {
            Log.w("imageUpload", "실패 - " + e.toString());
            e.printStackTrace();
        }
    }

    public void getImage(final ChatArrayAdapter chatArrayAdapter, final ChatMessage chatMsg, final MessageVO messageVo){
        if(messageVo.getObject() == null) {
            /* 디바이스에 이미지 파일이 없는 경우 */
            StorageReference imageRef = storageRef.child(messageVo.getData());
            try {
                final File localFile = File.createTempFile("images", "jpg");

                imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>(){
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot){
                        try {
                            chatMsg.bytes = getBytesFromFile(localFile);
                            chatMsg.type = ChatMessage.TYPE_IMAGE;
                            messageVo.setObject(localFile);
                            Log.w("ImageDownload", "성공 - " + localFile.getPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                            chatMsg.message = "이미지 로드 실패";
                        }
                        chatArrayAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        chatMsg.message = "이미지 로드 실패";
                        chatArrayAdapter.notifyDataSetChanged();
                        Log.w("ImageDownload", "실패 - " +  exception.toString());
                    }
                });
            } catch (IOException e) {}
        } else {
            /* 디바이스에 해당 이미지 파일이 있는 경우 */
            try {
                chatMsg.bytes = getBytesFromFile((File) messageVo.getObject());

                chatMsg.type = ChatMessage.TYPE_IMAGE;
            }catch (IOException e){
                e.printStackTrace();
                chatMsg.message = "이미지 로드 실패";
            }
            chatArrayAdapter.notifyDataSetChanged();
        }
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            return null;
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }

    private Bitmap imageResize(Bitmap bitmap, int maxSize){
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        Bitmap resized = null;
        while (height > maxSize) {
            resized = Bitmap.createScaledBitmap(bitmap, (width * maxSize) / height, maxSize, true);
            height = resized.getHeight();
            width = resized.getWidth();
        }
        return resized == null ? bitmap : resized;
    }
}
