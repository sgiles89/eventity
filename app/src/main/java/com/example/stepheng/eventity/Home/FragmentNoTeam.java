package com.example.stepheng.eventity.Home;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.stepheng.eventity.R;
import com.example.stepheng.eventity.Setup.JoinTeamActivity;
import com.example.stepheng.eventity.Setup.TeamCreationActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentNoTeam extends Fragment {

    @BindView(R.id.no_team_create) Button create;
    @BindView(R.id.no_team_join) Button join;
    private Unbinder unbinder;

    public FragmentNoTeam() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_no_team, container, false);
        unbinder = ButterKnife.bind(this, view);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinIntent = new Intent(getContext(), JoinTeamActivity.class);
                startActivity(joinIntent);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createIntent = new Intent(getContext(), TeamCreationActivity.class);
                startActivity(createIntent);
            }
        });




        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
