package mg.didavid.firsttry.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class ModelAppointment {
    String resto_id, resto_name, date, description;
    ArrayList<String> selectedUser;

    public ModelAppointment() {
    }

    public ModelAppointment(String resto_id, String resto_name, String date, String description, ArrayList<String> selectedUser) {
        this.resto_id = resto_id;
        this.resto_name = resto_name;
        this.date = date;
        this.description = description;
        this.selectedUser = selectedUser;
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

    public ArrayList<String> getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(ArrayList<String> selectedUser) {
        this.selectedUser = selectedUser;
    }
}


