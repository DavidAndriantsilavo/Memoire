package mg.didavid.firsttry.Models;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

public class UserLocation {
    private Double latitude;
    private Double longitude;
    private String timestamp;
    private String display_name;
    private String user_id;

    public UserLocation(Double latitude, Double longitude, String timestamp, String display_name, String user_id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.display_name = display_name;
        this.user_id = user_id;
    }

    public UserLocation() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
