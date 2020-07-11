package mg.didavid.firsttry.Controllers.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import mg.didavid.firsttry.R;

public class MessageFragment extends Fragment {

    public static MessageFragment newInstance() {
        return (new MessageFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page4, container, false);
    }
}
