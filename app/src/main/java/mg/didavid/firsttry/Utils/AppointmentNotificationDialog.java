package mg.didavid.firsttry.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterAppointmentNotification;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSelectUser;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSelectedUser;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

import static com.google.firebase.inappmessaging.internal.Logging.TAG;

public class AppointmentNotificationDialog extends AppCompatDialogFragment implements AdapterAppointmentNotification.OnSelectNotificationListner{
    private ArrayList<String> notificationList;
    private RecyclerView recyclerView_notification;
    private AppointmentNotificationDialog.AppointmentNotificationDialogListner listner;

    private Context context;
    private LinearLayoutManager layoutManager;
    private AdapterAppointmentNotification adapterAppointmentNotification;
    private User currentUser;

    public AppointmentNotificationDialog() {
    }

    public AppointmentNotificationDialog(Context context, ArrayList<String> notificationList){
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.appointment_notification_layout, null);

        currentUser = ((UserSingleton) getActivity().getApplicationContext()).getUser();

        recyclerView_notification = view.findViewById(R.id.recyclerView_notification);

        layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);

        recyclerView_notification.setHasFixedSize(true);
        recyclerView_notification.setLayoutManager(layoutManager);

        adapterAppointmentNotification = new AdapterAppointmentNotification(context, notificationList, this);
        recyclerView_notification.setAdapter(adapterAppointmentNotification);


        builder.setView(view)
                .setTitle("Notification")
                .setNegativeButton("Retour", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listner.startAppointmentListActivity();
                    }
                });

        return builder.create();
    }

    @Override
    public void onSelectNotificationClick(int position) {

    }

    public interface AppointmentNotificationDialogListner{
        void startAppointmentListActivity();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listner =  (AppointmentNotificationDialog.AppointmentNotificationDialogListner) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " implement listner first");
        }
    }
}
