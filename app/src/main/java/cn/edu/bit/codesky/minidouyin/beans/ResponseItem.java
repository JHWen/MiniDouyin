package cn.edu.bit.codesky.minidouyin.beans;

import com.google.gson.annotations.SerializedName;

public class ResponseItem {

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
    @SerializedName("student_id")
    private String studentId;

    @SerializedName("user_name")
    private String username;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("video_url")
    private String videoUrl;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public String toString() {
        return "ResponseItem{" +
                "studentId='" + studentId + '\'' +
                ", username='" + username + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }
}
