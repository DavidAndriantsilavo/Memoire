package mg.didavid.firsttry.Models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarkerRestaurant implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private ModelResto resto;

    public ClusterMarkerRestaurant(LatLng position, String title, String snippet, ModelResto resto) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.resto = resto;
    }

    public ClusterMarkerRestaurant() {
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

    public ModelResto getResto() {
        return resto;
    }

    public void setResto(ModelResto resto) {
        this.resto = resto;
    }
}
