package cn.edu.bit.codesky.minidouyin.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import cn.edu.bit.codesky.minidouyin.R;
import cn.edu.bit.codesky.minidouyin.beans.Feed;


public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoVideoHolder> {

    private ArrayList<Feed> videoItems;
    private ListItemOnClickListener listItemOnClickListener;

    public static final String TAG = VideoListAdapter.class.getName();

    public VideoListAdapter(ListItemOnClickListener listItemOnClickListener) {
        this.listItemOnClickListener = listItemOnClickListener;
        videoItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public VideoVideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.video_item_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        VideoVideoHolder viewHolder = new VideoVideoHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoVideoHolder holder, int position) {
        holder.bind(position);
    }


    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    public void refresh(List<Feed> feeds) {
        if (feeds != null) {
            Log.d(TAG, "adapter refresh");
            videoItems.clear();
            videoItems.addAll(feeds);
            notifyDataSetChanged();
        }
    }

    public class VideoVideoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private StandardGSYVideoPlayer videoPlayer;
        private TextView tvItemTitle;
        private Feed feed;

        public VideoVideoHolder(View itemView) {
            super(itemView);
            videoPlayer = itemView.findViewById(R.id.video_item_player);
            tvItemTitle = itemView.findViewById(R.id.tv_item_title);

            itemView.setOnClickListener(VideoVideoHolder.this);
        }

        public void bind(int position) {
            feed = videoItems.get(position);

            tvItemTitle.setText(feed.getUsername());
            Log.d(TAG, feed.toString());
            videoPlayer.setUpLazy(feed.getVideoUrl(), true, null, null, feed.getUsername());
            //设置Title
            videoPlayer.getTitleTextView().setVisibility(View.GONE);
            //设置返回键
            videoPlayer.getBackButton().setVisibility(View.GONE);
            //设置全屏功能按钮
            videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayer.startWindowFullscreen(v.getContext(), false, true);
                }
            });
            //防止错位设置
            videoPlayer.setPlayTag(TAG);
            videoPlayer.setPlayPosition(position);
            //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
            videoPlayer.setAutoFullWithSize(true);
            //音频焦点冲突时是否释放
            videoPlayer.setReleaseWhenLossAudio(false);
            //全屏动画
            videoPlayer.setShowFullAnimation(true);
            //小屏时不触摸滑动
            videoPlayer.setIsTouchWiget(false);

            //增加封面
            ImageView imageView = new ImageView(itemView.getContext());
            loadCover(imageView, feed.getImageUrl());
            videoPlayer.setThumbImageView(imageView);
        }

        private void loadCover(ImageView imageView, String url) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(R.mipmap.error_disp);
            Glide.with(itemView.getContext())
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .centerCrop()
                                    .error(R.mipmap.error_disp)
                                    .placeholder(R.mipmap.default_disp))
                    .load(url)
                    .into(imageView);
        }


        @Override
        public void onClick(View v) {
            listItemOnClickListener.onListItemClick(feed);
        }
    }

    public interface ListItemOnClickListener {
        void onListItemClick(Feed feed);
    }
}
