package com.example.stepheng.eventity.Home;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.stepheng.eventity.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentPendingTeam extends Fragment {


    public FragmentPendingTeam() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_pending_team, container, false);
    }

}
