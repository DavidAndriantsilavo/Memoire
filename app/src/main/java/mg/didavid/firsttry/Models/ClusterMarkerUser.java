package mg.didavid.firsttry.Models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarkerUser implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private UserLocation userLocation;

    public ClusterMarkerUser(LatLng position, String title, String snippet, UserLocation userLocation) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.userLocation = userLocation;
    }

    public ClusterMarkerUser() {
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }
}
