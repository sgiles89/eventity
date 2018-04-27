package com.example.stepheng.eventity.Home;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.stepheng.eventity.Classes.Notification;
import com.example.stepheng.eventity.R;
import com.example.stepheng.eventity.Setup.LoginActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentNotifications extends Fragment {

    private String TAG = "FragmentNotifications";
    private String user_id;

    //declare Recyclerview
    @BindView(R.id.notif_view)
    RecyclerView notifView;
    @BindView(R.id.empty_notif_message) TextView mEmptyListMessage;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    private Unbinder unbinder;

    //declare Firebase variables
    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;

    //declare time variables
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public FragmentNotifications() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_notifications, container, false);

        unbinder = ButterKnife.bind(this, view);

        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();

        //retrieving user's Firebase ID and their Team ID

        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        init();
        getNotifications();
        return view;
    }

    private void init(){
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        notifView.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    private void getNotifications(){
        Query query = mFStore.collection("Users/"+user_id+"/Notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(9);

        FirestoreRecyclerOptions<Notification> response = new FirestoreRecyclerOptions.Builder<Notification>()
                .setQuery(query, Notification.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Notification, NotificationHolder>(response) {
            @Override
            public void onBindViewHolder(NotificationHolder holder, int position, final Notification model) {

                //get long form of createdAt timestamp to calculate timesince
                Date d = model.getCreatedAt();
                String timeAgo = "just now";
                if (d==null){
                    timeAgo = "just now";
                } else {
                    long time = d.getTime();
                    timeAgo = getTimeAgo(time);
                }



                //colouring the time as grey and italics using Spannable string to isolate out the date and colour it with a style from R.style
                String timesince = model.getMessage()+" "+timeAgo;
                String message = model.getMessage();
                int start = message.length();
                int end = timesince.length();
                Log.d(TAG, "The lengths are: "+start+" "+end+" "+timesince.length());
                SpannableString finalText = new SpannableString(timesince);
                finalText.setSpan(new TextAppearanceSpan(getContext(), R.style.notification_time_style), start, end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationTitle.setText(finalText,TextView.BufferType.SPANNABLE);

                //set the notification image to the correct one based on the notification type
                String notifyType = model.getNotify_type();
                switch (notifyType){
                    case "team_rejection":
                        holder.notificationImage.setImageDrawable(getResources().getDrawable(R.drawable.team_reject));
                        break;
                    case "team_accept":
                        holder.notificationImage.setImageDrawable(getResources().getDrawable(R.drawable.team_approve));
                        break;
                    case "team_remove":
                        holder.notificationImage.setImageDrawable(getResources().getDrawable(R.drawable.team_reject));
                        break;
                    default:
                        holder.notificationImage.setImageDrawable(getResources().getDrawable(R.drawable.default_notification));
                        break;
                }


            }



            @Override
            public NotificationHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.notification_layout, group, false);

                return new NotificationHolder(view);
            }
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
        notifView.setAdapter(adapter);
        adapter.startListening();
    }

    public class NotificationHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.notifcation_message_text)
        TextView notificationTitle;
        @BindView(R.id.notification_img)
        CircleImageView notificationImage;



        public NotificationHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
