package kr.ac.hansung.simpletalk.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.ac.hansung.simpletalk.android.chatroom.ChatArrayAdapter;
import kr.ac.hansung.simpletalk.android.chatroom.ChatMessage;
import kr.ac.hansung.simpletalk.transformVO.MessageVO;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

import static android.R.attr.bitmap;

/**
 * Created by a3811 on 2016-12-14.
 */

public class FileSerivce {
    private static FileSerivce instance;

    private Map<String, File> tempFileMap = new HashMap<>();
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

    public static Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
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

    public static Bitmap imageResize(Bitmap bitmap, int maxSize){
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

    public void sendImageMsg(Context context, final ChatService chatService, final int chatId, final Uri imageUri) {
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
                    chatService.sendImageUrlMsg(chatId, taskSnapshot.getStorage().getPath());
                    Log.w("imageUpload", "성공 - " + imagesRef.toString());
                }
            });
        } catch (IOException e) {
            Log.w("imageUpload", "실패 - " + e.toString());
            e.printStackTrace();
        }
    }

    public void sendProfileImg(Context context, final ChatService chatService, final Uri imageUri) {
        try {
            sendProfileImg(chatService, MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri));
        } catch (IOException e) {
            Log.w("imageUpload", "실패 - " + e.toString());
            e.printStackTrace();
        }
    }

    public void sendProfileImg(final ChatService chatService, Bitmap bitmap) {
        final StorageReference imagesRef = storageRef.child("profile/" + UUID.randomUUID().toString());

        Bitmap resizeBitmap = imageResize(bitmap, 150);                      // 이미지 리사이징

        /* JPEG로 파일 압축 */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        /* 파일 업로드 */
        UploadTask uploadTask = imagesRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w("profileImageUpload", "실패 - " + exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                UserProfileVO myProfile = chatService.getMyProfile();
                myProfile.setImgFileName(taskSnapshot.getStorage().getPath());
                chatService.changeMyProfile(myProfile);
                Log.w("profileImageUpload", "성공 - " + imagesRef.toString());
            }
        });
    }

    public void roundLoadImage(Context context, final ArrayAdapter adapter, final ImageView imageView, final String path){
        File localFile = tempFileMap.get(path);

        if(localFile == null) {
            /* 디바이스에 이미지 파일이 없는 경우 */
            StorageReference imageRef = storageRef.child(path);
            try {
                localFile = File.createTempFile("images", "jpg");

                final File finalLocalFile = localFile;
                imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>(){
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot){
                        Log.w("ImageDownload", "성공 - " + finalLocalFile.getPath());
                        tempFileMap.put(path, finalLocalFile);

                        if(adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.w("ImageDownload", "실패 - " +  exception.toString());
                    }
                });
            } catch (IOException e) {}
        } else {
            try {
                Bitmap bitmap = decodeToBitmap(getBytesFromFile(localFile));
                RoundedBitmapDrawable bitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
                bitmapDrawable.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
                bitmapDrawable.setAntiAlias(true);

                imageView.setImageDrawable(bitmapDrawable);
            } catch (IOException e) {
                imageView.setImageResource(android.R.drawable.stat_notify_error);
                e.printStackTrace();
            }
        }
    }

    public void loadImage(ImageView imageView, String path){
        loadImage(null, imageView, path);
    }

    public void loadImage(final ArrayAdapter adapter, final ImageView imageView, final String path){
        File localFile = tempFileMap.get(path);

        if(localFile == null) {
            /* 디바이스에 이미지 파일이 없는 경우 */
            StorageReference imageRef = storageRef.child(path);
            try {
                localFile = File.createTempFile("images", "jpg");

                final File finalLocalFile = localFile;
                imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>(){
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot){
                    Log.w("ImageDownload", "성공 - " + finalLocalFile.getPath());
                    tempFileMap.put(path, finalLocalFile);

                    if(adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.w("ImageDownload", "실패 - " +  exception.toString());
                    }
                });
            } catch (IOException e) {}
        } else {
            try {
                imageView.setImageBitmap(decodeToBitmap(getBytesFromFile(localFile)));
            } catch (IOException e) {
                imageView.setImageResource(android.R.drawable.stat_notify_error);
                e.printStackTrace();
            }
        }
    }
}
