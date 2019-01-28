package cn.edu.bit.codesky.minidouyin.beans;

public class datafeed {

    private String studentId;
    private String username;
    private String imageUrl;
    private String videoUrl;
    private boolean islike;
    private int likenum;

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

    public boolean getislike() { return islike; }

    public void setislike(boolean islike) {
        this.islike = islike;
    }

    public int getlikenum() { return likenum; }

    public void setlikenum(int likenum) {
        this.likenum = likenum;
    }
}
