package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import mg.didavid.firsttry.Models.ModelAppointment;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Utils.SelectUserDialog;

import static com.google.firebase.inappmessaging.internal.Logging.TAG;

public class NewAppointmentActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, SelectUserDialog.SelectUserDialogListner {

    private TextView textView_where, textView_with, textView_when;
    private EditText editText_why;
    private Button button_confirm, button_cancel;
    private int day, month, year, hour, minute;
    private int myday, myMonth, myYear, myHour, myMinute;
    private ModelResto restoExtra;
    private ArrayList <User> selectedUserList;
    private ArrayList <User> userList;
    private ArrayList <String> selectedUserListId;
    private User currentUser;
    private Date date;
    private boolean isDateOk = false, isTagOk = false;
    private ModelResto resto;
    private SimpleDateFormat format;
//    private HashMap <String, HashMap> selectedUserMap;

    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_appointment);

        currentUser = ((UserSingleton) getApplicationContext()).getUser();

        textView_where = findViewById(R.id.textView_where);
        textView_with = findViewById(R.id.textView_with);
        textView_when = findViewById(R.id.textView_when);
        editText_why = findViewById(R.id.editText_why);
        button_confirm = findViewById(R.id.button_confirm);
        button_cancel = findViewById(R.id.button_cancel);

        selectedUserList = new ArrayList<>();
        userList = new ArrayList<>();
        date = new Date();

        getUserList();
        textView_when.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewAppointmentActivity.this, NewAppointmentActivity.this,year, month,day);
                datePickerDialog.show();
            }
        });

        textView_with.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectUserDialog selectUserDialog = new SelectUserDialog(NewAppointmentActivity.this, userList, selectedUserList);
                selectUserDialog.show(getSupportFragmentManager(), "select user dialog");
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDateOk && isTagOk){
                    ModelAppointment appointment = createAppointment();

                    userReference.document(currentUser.getUser_id())
                            .collection("AppointmentCollection")
                            .document("" + date.getTime())
                            .set(appointment)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(NewAppointmentActivity.this, "Rendez-vous enregistrer", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }else{
                                        Toast.makeText(NewAppointmentActivity.this, "Une erreur s'est produite!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(NewAppointmentActivity.this, "Veuillez renseigner les informations de votre rendez-vous!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getRestaurantExtra();
    }

    //create the appointment object
    //FUCK MY ANONYMOUS MAP WORKS GG
    private ModelAppointment createAppointment(){
//        selectedUserMap = new HashMap<>();
//
//        for(final User user : selectedUserList){
//                selectedUserMap.put(user.getUser_id(), new HashMap<String, String>(){
//////                    {
//////                        put("user_id", user.getUser_id());
//////                        put("name   ", user.getName());
//////                        put("profile_image", user.getProfile_image());
//////                    }
//////                });
//        }
        selectedUserListId = new ArrayList<>();
        selectedUserListId.add(currentUser.getUser_id());
        for(User user : selectedUserList) {
            selectedUserListId.add(user.getUser_id());
        }
            ModelAppointment appointment = new ModelAppointment(resto.getUser_id(),
                    resto.getName_resto(), format.format(date),
                    editText_why.getText().toString(), selectedUserListId);
            return appointment;
    }

    //get appointment date
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        myYear = year;
        myday = day;
        myMonth = month;
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(NewAppointmentActivity.this, NewAppointmentActivity.this, hour, minute, true);
        timePickerDialog.show();
    }

    //get appointment time
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        myHour = hourOfDay;
        myMinute = minute;

        format =  new SimpleDateFormat("dd/MM/yyyy à HH:mm");

        try {
            date = format.parse(myday + "/" + myMonth + "/" + myYear + " à " + myHour + ":" + myMinute);
            textView_when.setText(format.format(date));

            isDateOk =true;

            Log.d(TAG, "onTimeSet: " + date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //get the restaurant where the appointment will be
    private void getRestaurantExtra(){
        if(getIntent().hasExtra("restaurant")){
            resto = getIntent().getParcelableExtra("restaurant");
            textView_where.setText(resto.getName_resto());
        }
    }

    //get the result from the dialog --> the selected users
    @Override
    public void getSelectedUser(ArrayList<User> selectedUser) {
        selectedUserList = selectedUser;
        int size = selectedUser.size();
        if(size == 0){
            textView_with.setText("Ajouter");
            isTagOk = false;
        }else{
            String default_selected = selectedUser.get(0).getName();
            isTagOk = true;

            if(size < 2){
                textView_with.setText(default_selected);
            }else{
                textView_with.setText(default_selected + " et " + (size-1) + " autres");
            }
        }
    }

    //get all user expect current user
    private void getUserList(){
        //path of all post
        final CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Users");
        //get all data from this reference
        collectionUsers.whereNotEqualTo("name", currentUser.getName())
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            userList.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                User user = documentSnapshot.toObject(User.class);
                                userList.add(user);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewAppointmentActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //get the month name from an integer value
    //need to translate into FRENCH before
//    private String getMonthName(int month){
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
//        calendar.set(Calendar.MONTH, month);
//        String month_name = month_date.format(calendar.getTime());
//
//        return month_name;
//    }
}
