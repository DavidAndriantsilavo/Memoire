package mg.didavid.firsttry.Views;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import mg.didavid.firsttry.R;

@SuppressLint("Registered")
public class AppMode extends AppCompatActivity {
    boolean nightMode;

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }

    public AppMode (){}

    public AppMode (boolean nightMode) {
        this.nightMode = nightMode;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new AppMode().isNightMode()) {
            setTheme(R.style.NightMode);
        }else {
            setTheme(R.style.AppTheme);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (new AppMode().isNightMode()) {
            setTheme(R.style.NightMode);
        }else {
            setTheme(R.style.AppTheme);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (new AppMode().isNightMode()) {
            setTheme(R.style.NightMode);
        }else {
            setTheme(R.style.AppTheme);
        }
    }
}
