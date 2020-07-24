package mg.didavid.firsttry.Models;

import android.app.Application;

public class UserSingleton extends Application{
        private User user = null;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

    }

