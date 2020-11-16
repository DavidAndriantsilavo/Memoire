package mg.didavid.firsttry.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class ModelAppointment {
    String resto_id, resto_name, date, resto_logo, description, timestamp;
    ArrayList<String> selectedUserId;
    ArrayList<String> selectedUserName;
    HashMap<String, Boolean> confirmUser;

    public ModelAppointment() {
    }

    public ModelAppointment(String resto_id, String resto_name, String resto_logo,
                            String date, String description, String timestamp,
                            ArrayList<String> selectedUserId, ArrayList<String> selectedUserName,
                            HashMap<String, Boolean> confirmUser) {
        this.resto_id = resto_id;
        this.resto_name = resto_name;
        this.date = date;
        this.resto_logo = resto_logo;
        this.description = description;
        this.timestamp = timestamp;
        this.selectedUserId = selectedUserId;
        this.selectedUserName = selectedUserName;
        this.confirmUser = confirmUser;
    }

    public ArrayList<String> getSelectedUserName() {
        return selectedUserName;
    }

    public void setSelectedUserName(ArrayList<String> selectedUserName) {
        this.selectedUserName = selectedUserName;
    }

    public String getResto_id() {
        return resto_id;
    }

    public void setResto_id(String resto_id) {
        this.resto_id = resto_id;
    }

    public String getResto_name() {
        return resto_name;
    }

    public void setResto_name(String resto_name) {
        this.resto_name = resto_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(ArrayList<String> selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, Boolean> getConfirmUser() {
        return confirmUser;
    }

    public void setConfirmUser(HashMap<String, Boolean> confirmUser) {
        this.confirmUser = confirmUser;
    }

    public String getResto_logo() {
        return resto_logo;
    }

    public void setResto_logo(String resto_logo) {
        this.resto_logo = resto_logo;
    }
}


