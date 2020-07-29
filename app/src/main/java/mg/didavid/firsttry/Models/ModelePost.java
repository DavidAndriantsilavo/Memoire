package mg.didavid.firsttry.Models;

public class ModelePost {
    //spécifications du posteur
    String user_id, name, pseudo, post_kiff, profile_image, post_id, post_title, post_description, post_image, post_time;

    public ModelePost(){ }

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

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_description() {
        return post_description;
    }

    public void setPost_description(String post_description) {
        this.post_description = post_description;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

    public ModelePost(String user_id, String name, String pseudo, String post_kiff, String profile_image, String post_id, String post_title, String post_description, String post_image, String post_time) {
        this.user_id = user_id;
        this.name = name;
        this.pseudo = pseudo;
        this.post_kiff = post_kiff;
        this.profile_image = profile_image;
        this.post_id = post_id;
        this.post_title = post_title;
        this.post_description = post_description;
        this.post_image = post_image;
        this.post_time = post_time;



    }
}

