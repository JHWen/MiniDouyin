package cn.edu.bit.codesky.minidouyin.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:18
 */
public class Feed {

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
        return "Feed{" +
                "studentId='" + studentId + '\'' +
                ", username='" + username + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }
}
