package mg.didavid.firsttry.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.maps.model.LatLng;

import mg.didavid.firsttry.R;

public class FavoriteDialog extends AppCompatDialogFragment {
    private EditText editText_title;
    private EditText editText_description;
    private FavoriteDialogListner listner;
    private Location location;

    public FavoriteDialog() {
    }

    public FavoriteDialog( Location location) {
        this.location = location;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.favorite_dialog_layout, null);

        editText_title = view.findViewById(R.id.editText_title);
        editText_description = view.findViewById(R.id.editText_description);

        builder.setView(view)
                .setTitle("Ajouter un lieu favoris")
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(editText_title.getText().toString().trim())) {
                            String title = editText_title.getText().toString();
                            String description = editText_description.getText().toString();
                            listner.getMarkerOptions(title, description, location);
                        }else{
                            editText_title.setError("Veuillez remplir le titre");
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listner =  (FavoriteDialogListner) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " implement listner first");
        }
    }

    public interface FavoriteDialogListner{
        void getMarkerOptions(String title, String description, Location location);
    }
}
