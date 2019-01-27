package cn.edu.bit.codesky.minidouyin.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.18 17:53
 */
public class PostVideoResponse {

    /**
     * {
     * "success": true,
     * "item": {
     * "student_id": "3120186666",
     * "user_name": "⼩⻘",
     * "image_url": "http://10.108.10.39:8080/minidouyin/storage/image?path=32336667/
     * ahe/1548059515950/IMG_20180820_201006.png",
     * "video_url": "http://10.108.10.39:8080/minidouyin/storage/video?path=32336667/
     * ahe/1548059515950/b063fc96c6fd7a570180b6acccd7569d.mp4"
     * }
     * }
     */
    @SerializedName("success")
    private boolean success;

    @SerializedName("item")
    private ResponseItem item;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResponseItem getItem() {
        return item;
    }

    public void setItem(ResponseItem item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "PostVideoResponse{" +
                "success=" + success +
                ", item=" + item +
                '}';
    }
}
