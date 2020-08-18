package mg.didavid.firsttry.Models;

import java.util.Map;

public class ModelResto {
    String user_id, user_email, user_pseudo, id_resto, name_resto, password_resto, phone_resto, email_resto, logo_resto;
    Map<String, Object> location_resto;

    public ModelResto() {
    }

    public ModelResto(String user_id, String user_email, String user_pseudo, String id_resto, String name_resto, String password_resto, String phone_resto, String email_resto, String logo_resto, Map<String, Object> location_resto) {
        this.user_id = user_id;
        this.user_email = user_email;
        this.user_pseudo = user_pseudo;
        this.id_resto = id_resto;
        this.name_resto = name_resto;
        this.password_resto = password_resto;
        this.phone_resto = phone_resto;
        this.email_resto = email_resto;
        this.logo_resto = logo_resto;
        this.location_resto = location_resto;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_pseudo() {
        return user_pseudo;
    }

    public void setUser_pseudo(String user_pseudo) {
        this.user_pseudo = user_pseudo;
    }

    public String getId_resto() {
        return id_resto;
    }

    public void setId_resto(String id_resto) {
        this.id_resto = id_resto;
    }

    public String getName_resto() {
        return name_resto;
    }

    public void setName_resto(String name_resto) {
        this.name_resto = name_resto;
    }

    public String getPassword_resto() {
        return password_resto;
    }

    public void setPassword_resto(String password_resto) {
        this.password_resto = password_resto;
    }

    public String getPhone_resto() {
        return phone_resto;
    }

    public void setPhone_resto(String phone_resto) {
        this.phone_resto = phone_resto;
    }

    public String getEmail_resto() {
        return email_resto;
    }

    public void setEmail_resto(String email_resto) {
        this.email_resto = email_resto;
    }

    public Map<String, Object> getLocation_resto() {
        return location_resto;
    }

    public void setLocation_resto(Map<String, Object> location_resto) {
        this.location_resto = location_resto;
    }

    public String getLogo_resto() {
        return logo_resto;
    }

    public void setLogo_resto(String logo_resto) {
        this.logo_resto = logo_resto;
    }
}
