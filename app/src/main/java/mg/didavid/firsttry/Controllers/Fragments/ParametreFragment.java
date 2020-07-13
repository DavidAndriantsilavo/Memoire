package mg.didavid.firsttry.Controllers.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import mg.didavid.firsttry.R;

public class ParametreFragment extends Fragment {

    public static ParametreFragment newInstance() {
        return (new ParametreFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page3, container, false);
    }
}
