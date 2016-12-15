package kr.ac.hansung.simpletalk.android.userlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.ac.hansung.simpletalk.android.FileSerivce;
import kr.ac.hansung.simpletalk.android.R;
import kr.ac.hansung.simpletalk.transformVO.UserProfileVO;

/**
 * Created by a3811 on 2016-12-02.
 */

public class UserListAdapter extends ArrayAdapter<UserProfileVO> {
    private List<UserProfileVO> userProfileList = new LinkedList<>();
    private Map<Integer, Boolean> checkedUserMap = new HashMap<>();

    public UserListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public void add(UserProfileVO object) {
        userProfileList.add(object);
        super.add(object);
    }

    @Override
    public void addAll(Collection<? extends UserProfileVO> collection) {
        userProfileList.addAll(collection);
        super.addAll(collection);
    }

    @Override
    public void clear() {
        userProfileList.clear();
        super.clear();
    }

    @Override
    public int getCount() {
        return userProfileList.size();
    }

    @Nullable
    @Override
    public UserProfileVO getItem(int position) {
        return userProfileList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_item_userprofile, parent, false);
        }
        ImageView userProfileImage = (ImageView) convertView.findViewById(R.id.userProfileImage);
        TextView userProfileName = (TextView) convertView.findViewById(R.id.userProfileName);
        TextView userProfileStatusMsg = (TextView) convertView.findViewById(R.id.userProfileStatusMsg);
        CheckBox userProfileCheckbox = (CheckBox) convertView.findViewById(R.id.userProfileCheckbox);

        UserProfileVO userProfileData = userProfileList.get(position);

        userProfileName.setText(userProfileData.getName() + "(id: "+ userProfileData.getId() + ")");
        userProfileStatusMsg.setText(userProfileData.getStateMsg());

        userProfileCheckbox.setChecked((checkedUserMap.get(position) == null)
                ? false : checkedUserMap.get(position));
        userProfileCheckbox.setFocusable(false);
        userProfileCheckbox.setFocusableInTouchMode(false);

        if(userProfileData.getImgFileName() != null && !userProfileData.getImgFileName().isEmpty()) {
            FileSerivce.getInstance().loadImage(this, userProfileImage, userProfileData.getImgFileName());
        }

        return convertView;
    }

    public void checkedUser(int position){
        UserProfileVO userProfile = userProfileList.get(position);

        if(checkedUserMap.get(position) == null){
            checkedUserMap.put(position, true);
        } else {
            checkedUserMap.put(position, ! checkedUserMap.get(position));
        }
    }
}
