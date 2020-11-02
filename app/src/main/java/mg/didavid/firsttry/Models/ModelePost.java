package mg.didavid.firsttry.Models;

import java.util.HashMap;

public class ModelePost {
    //spécifications du posteur
    private String user_id, name, pseudo, post_kiff, comment_count, profile_image, post_id, post_description, post_image1, post_image2, post_image3, post_time;
    private HashMap<String, Object> user_location;

    public ModelePost(){ }

    public ModelePost(String user_id, String name, String pseudo, String post_kiff, String comment_count, String profile_image, String post_id, String post_description, String post_image1, String post_image2, String post_image3, String post_time, HashMap<String, Object> user_location) {
        this.user_id = user_id;
        this.name = name;
        this.pseudo = pseudo;
        this.post_kiff = post_kiff;
        this.comment_count = comment_count;
        this.profile_image = profile_image;
        this.post_id = post_id;
        this.post_description = post_description;
        this.post_image1 = post_image1;
        this.post_image2 = post_image2;
        this.post_image3 = post_image3;
        this.post_time = post_time;
        this.user_location = user_location;
    }

    public HashMap<String, Object> getUser_location() {
        return user_location;
    }

    public void setUser_location(HashMap<String, Object> user_location) {
        this.user_location = user_location;
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

    public String getPost_kiff() {
        return post_kiff;
    }

    public void setPost_kiff(String post_kiff) {
        this.post_kiff = post_kiff;
    }

    public String getComment_count() {
        return comment_count;
    }

    public void setComment_count(String comment_count) {
        this.comment_count = comment_count;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_description() {
        return post_description;
    }

    public void setPost_description(String post_description) {
        this.post_description = post_description;
    }

    public String getPost_image1() {
        return post_image1;
    }

    public void setPost_image1(String post_image1) {
        this.post_image1 = post_image1;
    }

    public String getPost_image2() {
        return post_image2;
    }

    public void setPost_image2(String post_image2) {
        this.post_image2 = post_image2;
    }

    public String getPost_image3() {
        return post_image3;
    }

    public void setPost_image3(String post_image3) {
        this.post_image3 = post_image3;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }
}

