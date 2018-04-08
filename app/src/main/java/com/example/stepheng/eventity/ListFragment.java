package com.example.stepheng.eventity;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListFragment extends DialogFragment {

    @BindView(R.id.list_container) RecyclerView list_container;
    private FirebaseFirestore mFStore;
    private String team_id, eventid, list;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_list_view_message)
    TextView mEmptyListMessage;
    private Unbinder unbinder;

    static ListFragment newInstance(String team_id, String eventid, String list){
        ListFragment f = new ListFragment();
        Bundle args = new Bundle();
        args.putString("teamID", team_id);
        args.putString("eventID", eventid);
        args.putString("list", list);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team_id = getArguments().getString("teamID");
        eventid = getArguments().getString("eventID");
        list = getArguments().getString("list");
        setStyle(DialogFragment.STYLE_NORMAL, DialogFragment.STYLE_NORMAL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(true);

        View view = inflater.inflate(R.layout.list_frag_layout, container);
        unbinder = ButterKnife.bind(this, view);
        linearLayoutManager = new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        list_container.setLayoutManager(linearLayoutManager);
        mFStore = FirebaseFirestore.getInstance();
        Query query;
        if (list.equals("Going")){
            query = mFStore.collection("Teams/"+team_id+"/Events/"+eventid+"/Going");
            Log.d("Hey Stephen", "Query is"+"Teams/"+team_id+"/Events/"+eventid+"Going");
        } else if (list.equals("Maybe")){
            query = mFStore.collection("Teams/"+team_id+"/Events/"+eventid+"/Maybe");
        } else {
            query = mFStore.collection("Teams/"+team_id+"/Events/"+eventid+"/Not");
        }



        FirestoreRecyclerOptions<ListMember> response = new FirestoreRecyclerOptions.Builder<ListMember>()
                .setQuery(query, ListMember.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ListMember, ListMemberHolder>(response) {
            @Override
            public void onBindViewHolder(ListMemberHolder holder, int position, final ListMember model) {
                holder.name.setText(model.getName());
            }



            @Override
            public ListMemberHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.list_layout, group, false);

                return new ListMemberHolder(view);
            }

            @Override
            public void onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        list_container.setAdapter(adapter);
        adapter.startListening();

        return view;
    }

    public class ListMemberHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_name)
        TextView name;

        public ListMemberHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(500, 750);
        window.setGravity(Gravity.CENTER);
    }
}
