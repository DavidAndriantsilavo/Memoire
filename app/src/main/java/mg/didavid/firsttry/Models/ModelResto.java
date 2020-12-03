package mg.didavid.firsttry.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.Display;

import java.util.List;
import java.util.Map;

public class ModelResto implements Parcelable {
    private String user_id, user_email, user_pseudo, id_resto, name_resto, phone_resto, logo_resto, coverPhoto_resto, speciality_resto;
    private String rating_resto, nbrRating_resto;
    private Double latitude, longitude;
    private List<ModelRestoSampleMenu> sampleMenuList;

    public ModelResto() {
    }

    public ModelResto(String user_id, String user_email, String user_pseudo, String id_resto, String name_resto, String phone_resto, String logo_resto, String coverPhoto_resto, String speciality_resto, String rating_resto, String nbrRating_resto, Double latitude, Double longitude, List<ModelRestoSampleMenu> sampleMenuList) {
        this.user_id = user_id;
        this.user_email = user_email;
        this.user_pseudo = user_pseudo;
        this.id_resto = id_resto;
        this.name_resto = name_resto;
        this.phone_resto = phone_resto;
        this.logo_resto = logo_resto;
        this.coverPhoto_resto = coverPhoto_resto;
        this.speciality_resto = speciality_resto;
        this.rating_resto = rating_resto;
        this.nbrRating_resto = nbrRating_resto;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sampleMenuList = sampleMenuList;
    }

    protected ModelResto(Parcel in) {
        name_resto = in.readString();
        user_id = in.readString();
        logo_resto = in.readString();
    }

    public static final Creator<ModelResto> CREATOR = new Creator<ModelResto>() {
        @Override
        public ModelResto createFromParcel(Parcel in) {
            return new ModelResto(in);
        }

        @Override
        public ModelResto[] newArray(int size) {
            return new ModelResto[size];
        }
    };

    public static Creator<ModelResto> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name_resto);
        dest.writeString(user_id);
        dest.writeString(logo_resto);
    }

    public String getCoverPhoto_resto() {
        return coverPhoto_resto;
    }

    public void setCoverPhoto_resto(String coverPhoto_resto) {
        this.coverPhoto_resto = coverPhoto_resto;
    }

    public String getRating_resto() {
        return rating_resto;
    }

    public void setRating_resto(String rating_resto) {
        this.rating_resto = rating_resto;
    }

    public String getNbrRating_resto() {
        return nbrRating_resto;
    }

    public void setNbrRating_resto(String nbrRating_resto) {
        this.nbrRating_resto = nbrRating_resto;
    }

    public String getSpeciality_resto() {
        return speciality_resto;
    }

    public void setSpeciality_resto(String speciality_resto) {
        this.speciality_resto = speciality_resto;
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

    public String getPhone_resto() {
        return phone_resto;
    }

    public void setPhone_resto(String phone_resto) {
        this.phone_resto = phone_resto;
    }

    public String getLogo_resto() {
        return logo_resto;
    }

    public void setLogo_resto(String logo_resto) {
        this.logo_resto = logo_resto;
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

    public List<ModelRestoSampleMenu> getSampleMenuList() {
        return sampleMenuList;
    }

    public void setSampleMenuList(List<ModelRestoSampleMenu> sampleMenuList) {
        this.sampleMenuList = sampleMenuList;
    }
}
