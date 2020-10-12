package mg.didavid.firsttry.Controllers.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSelectUser;
import mg.didavid.firsttry.Controllers.Fragments.GMapFragment;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Utils.FavoriteDialog;
import mg.didavid.firsttry.Utils.SelectUserDialog;

public class AppointmentActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, SelectUserDialog.SelectUserDialogListner {

    TextView textView_where, textView_with, textView_when;
    EditText editText_why;
    Button button_confirm, button_cancel;
    int day, month, year, hour, minute;
    int myday, myMonth, myYear, myHour, myMinute;
    ModelResto restoExtra;
    ArrayList <User> selectedUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        textView_where = findViewById(R.id.textView_where);
        textView_with = findViewById(R.id.textView_with);
        textView_when = findViewById(R.id.textView_when);
        editText_why = findViewById(R.id.editText_why);
        button_confirm = findViewById(R.id.button_confirm);
        button_cancel = findViewById(R.id.button_cancel);

        selectedUsers = new ArrayList<>();

        textView_when.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AppointmentActivity.this, AppointmentActivity.this,year, month,day);
                datePickerDialog.show();
            }
        });

        textView_with.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectUserDialog selectUserDialog = new SelectUserDialog(AppointmentActivity.this);
                selectUserDialog.show(getSupportFragmentManager(), "select user dialog");
            }
        });

        getRestaurantExtra();
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        myYear = year;
        myday = day;
        myMonth = month;
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(AppointmentActivity.this, AppointmentActivity.this, hour, minute, true);
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        myHour = hourOfDay;
        myMinute = minute;
        textView_when.setText("Year: " + myYear + "\n" +
                "Month: " + myMonth + "\n" +
                "Day: " + myday + "\n" +
                "Hour: " + myHour + "\n" +
                "Minute: " + myMinute);
    }

    private void getRestaurantExtra(){
        if(getIntent().hasExtra("restaurant_name")){
            textView_where.setText(getIntent().getStringExtra("restaurant_name"));
        }
    }

    @Override
    public void getSelectedUser(ArrayList<User> selectedUser) {

    }
}
