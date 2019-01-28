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
import cn.edu.bit.codesky.minidouyin.widget.CircleImageView;


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

        private TextView tvItemTitle;
        private CircleImageView ivItemAvatar;
        private ImageView imageView;
        private Feed feed;

        public VideoVideoHolder(View itemView) {
            super(itemView);
            tvItemTitle = itemView.findViewById(R.id.tv_item_title);
            ivItemAvatar = itemView.findViewById(R.id.iv_item_avatar);
            imageView = itemView.findViewById(R.id.iv_cover);

            itemView.setOnClickListener(VideoVideoHolder.this);
        }

        public void bind(int position) {
            feed = videoItems.get(position);
            tvItemTitle.setText(feed.getUsername());
            //屏幕的宽度(px值）
            int screenWidth = itemView.getContext().getResources().getDisplayMetrics().widthPixels;
            //Item的宽度，或图片的宽度
            int width = screenWidth / 2;
            Glide.with(itemView.getContext())
                    .setDefaultRequestOptions(new RequestOptions().centerCrop())
                    .load(feed.getImageUrl())
                    .into(imageView);

            Log.d(TAG, feed.toString());
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
