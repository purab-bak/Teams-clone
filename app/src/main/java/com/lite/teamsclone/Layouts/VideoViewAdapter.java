package com.lite.teamsclone.Layouts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.lite.teamsclone.Models.UserStatusData;
import com.lite.teamsclone.Models.VideoInfoData;
import com.lite.teamsclone.Models.VideoUserStatusHolder;
import com.lite.teamsclone.R;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class VideoViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final boolean DEBUG = false;

    protected final LayoutInflater mInflater;
    protected final Context mContext;
    protected final ArrayList<UserStatusData> mUsers;
    protected int mLocalUid;
    protected int mItemWidth;
    protected int mItemHeight;
    private int mDefaultChildItem = 0;

    public VideoViewAdapter(Activity activity, int localUid, HashMap<Integer, SurfaceView> uids) {
        mInflater = ((Activity) activity).getLayoutInflater();
        mContext = ((Activity) activity).getApplicationContext();

        mLocalUid = localUid;
        mUsers = new ArrayList<>();
        init(uids);
    }

    //called in constructor
    private void init(HashMap<Integer, SurfaceView> uids) {
        mUsers.clear();
        customizedInit(uids, true);
    }

    //called in init and in GridViewContainer
    protected abstract void customizedInit(HashMap<Integer, SurfaceView> uids, boolean force);

    public abstract void notifyUiChanged(HashMap<Integer, SurfaceView> uids, int uidExtra, HashMap<Integer, Integer> status, HashMap<Integer, Integer> volume);

    protected HashMap<Integer, VideoInfoData> mVideoInfo; // left user should removed from this HashMap


    //used in GridAdapter/container
    public void addVideoInfo(int uid, VideoInfoData video) {
        if (mVideoInfo == null) {
            mVideoInfo = new HashMap<>();
        }
        mVideoInfo.put(uid, video);
    }

    public void cleanVideoInfo() {
        mVideoInfo = null;
    }

    public void setLocalUid(int uid) {
        mLocalUid = uid;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) mInflater.inflate(R.layout.video_view_container, parent, false);

        //width and height calculated in GridContainerAdapter
        v.getLayoutParams().width = mItemWidth;
        v.getLayoutParams().height = mItemHeight;

        mDefaultChildItem = v.getChildCount();
        return new VideoUserStatusHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        VideoUserStatusHolder myHolder = ((VideoUserStatusHolder) holder);

        final UserStatusData user = mUsers.get(position);

        FrameLayout holderView = (FrameLayout) myHolder.itemView;


        /**view added here**/
        if (holderView.getChildCount() == mDefaultChildItem) {
            SurfaceView target = user.mView;
            VideoViewAdapterUtil.stripView(target);

            /**use this to create margin**/
//            int margin = 20;
//            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            lp.setMargins(20, 20, 20, 20);
//
//            target.setLayoutParams(lp);

            /** width and height controlled here **/
            holderView.addView(target, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //holderView.addView(target, 0, new FrameLayout.LayoutParams(400, 400));

        }

        VideoViewAdapterUtil.renderExtraData(mContext, user, myHolder);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public long getItemId(int position) {

        UserStatusData user = mUsers.get(position);

        SurfaceView view = user.mView;
        if (view == null) {
            throw new NullPointerException("SurfaceView destroyed for user " + user.mUid + " " + user.mStatus + " " + user.mVolume);
        }
        return (String.valueOf(user.mUid) + System.identityHashCode(view)).hashCode();
    }
}
