package mg.didavid.firsttry.Models;

public class ModelComment {
    String comment_time, post_comment, post_id, user_id, name, pseudo, profile_image, comment_image;

    public ModelComment() {
    }

    public ModelComment(String comment_time, String post_comment, String post_id, String user_id, String name, String pseudo, String profile_image, String comment_image) {
        this.comment_time = comment_time;
        this.post_comment = post_comment;
        this.post_id = post_id;
        this.user_id = user_id;
        this.name = name;
        this.pseudo = pseudo;
        this.profile_image = profile_image;
        this.comment_image = comment_image;
    }

    public String getComment_image() {
        return comment_image;
    }

    public void setComment_image(String comment_image) {
        this.comment_image = comment_image;
    }

    public String getComment_time() {
        return comment_time;
    }

    public void setComment_time(String comment_time) {
        this.comment_time = comment_time;
    }

    public String getPost_comment() {
        return post_comment;
    }

    public void setPost_comment(String post_comment) {
        this.post_comment = post_comment;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }
}
