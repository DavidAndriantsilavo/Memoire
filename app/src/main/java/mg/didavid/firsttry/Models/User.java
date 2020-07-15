package mg.didavid.firsttry.Models;

import android.os.Parcel;
import android.os.Parcelable;

    public class User implements Parcelable{

        private String email;
        private String user_id;
        private String name;
        private String phone;
        private String password;

        public User(String email, String user_id, String name, String phone, String password) {
            this.email = email;
            this.user_id = user_id;
            this.name = name;
            this.phone =  phone;
            this.password = password;
        }

        public User() {

        }

        protected User(Parcel in) {
            email = in.readString();
            user_id = in.readString();
            name = in.readString();
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
            @Override
            public User createFromParcel(Parcel in) {
                return new User(in);
            }

            @Override
            public User[] newArray(int size) {
                return new User[size];
            }
        };

        public static Creator<User> getCREATOR() {
            return CREATOR;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        @Override
        public String toString() {
            return "User{" +
                    "email='" + email + '\'' +
                    ", user_id='" + user_id + '\'' +
                    ", name='" + name + '\'' +
                    ", phone='" + phone +'\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(email);
            dest.writeString(user_id);
            dest.writeString(name);
            dest.writeString(phone);
            dest.writeString(password);
        }
    }
