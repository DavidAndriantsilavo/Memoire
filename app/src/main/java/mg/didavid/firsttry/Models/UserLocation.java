package mg.didavid.firsttry.Models;

public class UserLocation {
    private Double latitude;
    private Double longitude;
    private String timestamp;
    private String name;
    private String user_id;
    private String profile_image;

    public UserLocation(Double latitude, Double longitude, String timestamp, String name, String user_id, String profile_image) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.name = name;
        this.user_id = user_id;
        this.profile_image = profile_image;
    }

    public UserLocation() {
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
