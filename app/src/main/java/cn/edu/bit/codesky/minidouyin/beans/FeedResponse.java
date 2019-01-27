package cn.edu.bit.codesky.minidouyin.beans;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:17
 */
public class FeedResponse {

    /**
     * {
     * "feeds": [
     * {
     * "student_id": "2220186666",
     * "user_name": "doudou",
     * "image_url": "http://10.108.10.39:8080/minidouyin/storage/image?path=2220186666/doudou/1548136931357/Screenshot_2019-01-20-12-49-37-834_com.ss.android.ugc.aweme.png",
     * "video_url": "http://10.108.10.39:8080/minidouyin/storage/video?path=2220186666/doudou/1548136931357/e888dbed20a23e1a9ee8a3e3da644cb9.mp4"
     * }
     * ],
     * "success": true
     * }
     */
    @SerializedName("feeds")
    private List<Feed> feeds;

    @SerializedName("success")
    private boolean success;

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "FeedResponse{" +
                "feeds=" + feeds +
                ", success=" + success +
                '}';
    }
}
