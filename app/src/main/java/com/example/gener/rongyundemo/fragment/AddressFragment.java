package com.example.gener.rongyundemo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gener.rongyundemo.App;
import com.example.gener.rongyundemo.ConversationActivity;
import com.example.gener.rongyundemo.R;
import com.example.gener.rongyundemo.SealAppContext;
import com.example.gener.rongyundemo.SealConst;
import com.example.gener.rongyundemo.SealUserInfoManager;
import com.example.gener.rongyundemo.adapter.FriendListAdapter;
import com.example.gener.rongyundemo.broadcast.BroadcastManager;
import com.example.gener.rongyundemo.db.Friend;
import com.example.gener.rongyundemo.pinyin.CharacterParser;
import com.example.gener.rongyundemo.pinyin.PinyinComparator;
import com.example.gener.rongyundemo.pinyin.SideBar;
import com.example.gener.rongyundemo.widget.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.mention.MemberMentionedActivity;



import io.rong.imlib.model.UserInfo;

public class AddressFragment extends Fragment implements View.OnClickListener {

    private SelectableRoundedImageView mSelectableRoundedImageView;
    private TextView mNameTextView;
    private TextView mNoFriends;
    private TextView mUnreadTextView;
    private View mHeadView;
    private EditText mSearchEditText;
    private ListView mListView;
    private PinyinComparator mPinyinComparator;
    private SideBar mSidBar;
    /**`
     * ???????????????????????????
     */
    private TextView mDialogTextView;
    private List<Friend> mFriendList;
    private List<Friend> mFilteredFriendList;

    /**
     * ??????????????? mFriendListAdapter
     */
    private FriendListAdapter mFriendListAdapter;

    /**
     * ?????????????????????ListView??????????????????
     */

    private String mId;
    private String mCacheName;

    /**
     * ???????????????????????????
     */
    private CharacterParser mCharacterParser;


    public static AddressFragment getInstance(String flag){
        Bundle bundle = new Bundle();
        bundle.putString(AddressFragment.class.getSimpleName(),flag);
        AddressFragment addressFragment = new AddressFragment();
        addressFragment.setArguments(bundle);
        return addressFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_address, container, false);
        initView(inflate);
        initData();
        updateUI();
        refreshUIListener();
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            BroadcastManager.getInstance(getActivity()).destroy(SealAppContext.UPDATE_FRIEND);
            BroadcastManager.getInstance(getActivity()).destroy(SealAppContext.UPDATE_RED_DOT);
            BroadcastManager.getInstance(getActivity()).destroy(SealConst.CHANGEINFO);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void initView(View view) {
        mSearchEditText = (EditText) view.findViewById(R.id.search);
        mListView = (ListView) view.findViewById(R.id.listview);
        mNoFriends = (TextView) view.findViewById(R.id.show_no_friend);
        mSidBar = (SideBar) view.findViewById(R.id.sidrbar);
        mDialogTextView = (TextView) view.findViewById(R.id.group_dialog);
        mSidBar.setTextView(mDialogTextView);
        LayoutInflater mLayoutInflater = LayoutInflater.from(getActivity());
        mHeadView = mLayoutInflater.inflate(R.layout.item_contact_list_header,
                null);
        mUnreadTextView = (TextView) mHeadView.findViewById(R.id.tv_unread);
        RelativeLayout newFriendsLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_newfriends);
        RelativeLayout groupLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_chatroom);
        RelativeLayout publicServiceLayout = (RelativeLayout) mHeadView.findViewById(R.id.publicservice);
        RelativeLayout selfLayout = (RelativeLayout) mHeadView.findViewById(R.id.contact_me_item);
        mSelectableRoundedImageView = (SelectableRoundedImageView) mHeadView.findViewById(R.id.contact_me_img);
        mNameTextView = (TextView) mHeadView.findViewById(R.id.contact_me_name);
        updatePersonalUI();
        mListView.addHeaderView(mHeadView);
        mNoFriends.setVisibility(View.VISIBLE);

        selfLayout.setOnClickListener(this);
        groupLayout.setOnClickListener(this);
        newFriendsLayout.setOnClickListener(this);
        publicServiceLayout.setOnClickListener(this);
        //????????????????????????
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //??????????????????????????????
                int position = mFriendListAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });
    }

    private void initData() {
        mFriendList = new ArrayList<>();
        FriendListAdapter adapter = new FriendListAdapter(getActivity(), mFriendList);
        mListView.setAdapter(adapter);
        mFilteredFriendList = new ArrayList<>();
        //???????????????????????????
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = PinyinComparator.getInstance();
    }

    private void refreshUIListener() {
        BroadcastManager.getInstance(getActivity()).addAction(SealAppContext.UPDATE_FRIEND, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    updateUI();
                }
            }
        });

        BroadcastManager.getInstance(getActivity()).addAction(SealAppContext.UPDATE_RED_DOT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    mUnreadTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        BroadcastManager.getInstance(getActivity()).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updatePersonalUI();
            }
        });
    }

    private void updatePersonalUI() {
        SharedPreferences sp = getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        mId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "pfmjInLlY");//?????????????????????userId
        mCacheName = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "?????????");//name
        final String header = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");//????????????
        mNameTextView.setText(mCacheName);
        if (!TextUtils.isEmpty(mId)) {
            UserInfo userInfo = new UserInfo(mId, mCacheName, Uri.parse(header));
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(userInfo);
            ImageLoader.getInstance().displayImage(portraitUri, mSelectableRoundedImageView, App.getOptions());
        }
    }

    private void updateUI() {
//        SealUserInfoManager.getInstance().getFriends(new SealUserInfoManager.ResultCallback<List<Friend>>() {
//            @Override
//            public void onSuccess(List<Friend> friendsList) {
//                updateFriendsList(friendsList);
//            }
//
//            @Override
//            public void onError(String errString) {
//                updateFriendsList(null);
//            }
//        });

        List<Friend> friendsList = new ArrayList<>();
        Friend friend = new Friend("zIDknjp8U","??????",Uri.parse("http://7xogjk.com1.z0.glb.clouddn.com/zIDknjp8U1526431878366443115"),"??????");
        Friend friend2 = new Friend("jiangdashuai","?????????",Uri.parse("http://7xogjk.com1.z0.glb.clouddn.com/zIDknjp8U1526431878366443115"),"?????????");
        friendsList.add(friend);
        friendsList.add(friend2);

        updateFriendsList(friendsList);
    }


    private void updateFriendsList(List<Friend> friendsList) {
        //updateUI fragment?????????????????????????????????????????????,isReloadList????????????????????????????????????
        boolean isReloadList = false;
        if (mFriendList != null && mFriendList.size() > 0) {
            mFriendList.clear();
            isReloadList = true;
        }
        mFriendList = friendsList;
        if (mFriendList != null && mFriendList.size() > 0) {
            handleFriendDataForSort();
            mNoFriends.setVisibility(View.GONE);
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        // ??????a-z?????????????????????
        Collections.sort(mFriendList, mPinyinComparator);
        if (isReloadList) {
            mSidBar.setVisibility(View.VISIBLE);
            mFriendListAdapter.updateListView(mFriendList);
        } else {
            mSidBar.setVisibility(View.VISIBLE);
            mFriendListAdapter = new FriendListAdapter(getActivity(), mFriendList);

            mListView.setAdapter(mFriendListAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mListView.getHeaderViewsCount() > 0) {
                        startFriendDetailsPage(mFriendList.get(position - 1));
                    } else {
                        startFriendDetailsPage(mFilteredFriendList.get(position));
                    }
                }
            });


            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    Friend bean = mFriendList.get(position - 1);
                    startFriendDetailsPage(bean);
                    return true;
                }
            });
            mSearchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //???????????????????????????????????????????????????????????????????????????????????????
//                    filterData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0) {
                        if (mListView.getHeaderViewsCount() > 0) {
                            mListView.removeHeaderView(mHeadView);
                        }
                    } else {
                        if (mListView.getHeaderViewsCount() == 0) {
                            mListView.addHeaderView(mHeadView);
                        }
                    }
                }
            });
        }
    }

    private void startFriendDetailsPage(Friend friend) {
//        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
//        intent.putExtra("type", CLICK_CONTACT_FRAGMENT_FRIEND);
//        intent.putExtra("friend", friend);
//        startActivity(intent);
        RongIM.getInstance().startPrivateChat(getActivity(), friend.getUserId(), friend.getDisplayName());
    }
    /**
     * ????????????????????????????????????????????????ListView
     *
     * @param filterStr ??????????????? String
     */
    private void filterData(String filterStr) {
        List<Friend> filterDateList = new ArrayList<>();

        try {
            if (TextUtils.isEmpty(filterStr)) {
                filterDateList = mFriendList;
            } else {
                filterDateList.clear();
                for (Friend friendModel : mFriendList) {
                    String name = friendModel.getName();
                    String displayName = friendModel.getDisplayName();
                    if (!TextUtils.isEmpty(displayName)) {
                        if (name.contains(filterStr) || mCharacterParser.getSpelling(name).startsWith(filterStr) || displayName.contains(filterStr) || mCharacterParser.getSpelling(displayName).startsWith(filterStr)) {
                            filterDateList.add(friendModel);
                        }
                    } else {
                        if (name.contains(filterStr) || mCharacterParser.getSpelling(name).startsWith(filterStr)) {
                            filterDateList.add(friendModel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ??????a-z????????????
        Collections.sort(filterDateList, mPinyinComparator);
        mFilteredFriendList = filterDateList;
        mFriendListAdapter.updateListView(filterDateList);
    }

    private void handleFriendDataForSort() {
        for (Friend friend : mFriendList) {
            if (friend.isExitsDisplayName()) {
                String letters = replaceFirstCharacterWithUppercase(friend.getDisplayNameSpelling());
                friend.setLetters(letters);
            } else {
                String letters = replaceFirstCharacterWithUppercase(friend.getNameSpelling());
                friend.setLetters(letters);
            }
        }
    }
    private String replaceFirstCharacterWithUppercase(String spelling) {
        if (!TextUtils.isEmpty(spelling)) {
            char first = spelling.charAt(0);
            char newFirst = first;
            if (first >= 'a' && first <= 'z') {
                newFirst -= 32;
            }
            return spelling.replaceFirst(String.valueOf(first), String.valueOf(newFirst));
        } else {
            return "#";
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.re_newfriends:
                mUnreadTextView.setVisibility(View.GONE);
//                Intent intent = new Intent(getActivity(), NewFriendListActivity.class);
//                startActivityForResult(intent, 20);
                break;
            case R.id.re_chatroom:
//                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
            case R.id.publicservice:
//                Intent intentPublic = new Intent(getActivity(), PublicServiceActivity.class);
//                startActivity(intentPublic);
                break;
            case R.id.contact_me_item:
                if (RongIM.getInstance() != null) {
                    RongIM.getInstance().startPrivateChat(getActivity(), mId, mCacheName);
//                     startActivity(new Intent(getContext(),ConversationActivity.class));
                }
                break;
        }
    }
}
