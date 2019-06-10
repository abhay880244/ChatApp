package com.abhay.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar=findViewById(R.id.allusersAppBar);
        mUsersList=findViewById(R.id.users_list);



        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));




    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, new SnapshotParser<Users>() {
                            @NonNull
                            @Override
                            public Users parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Users(snapshot.child("name").getValue().toString(),
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("status").getValue().toString());
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(view);
            }



            @Override
            protected void onBindViewHolder(UsersViewHolder holder, final int position, Users users) {
                holder.setName(users.getName());
                holder.setImage(users.getImage());
                holder.setStatus(users.getStatus());

            }

        };
        adapter.startListening();
        mUsersList.setAdapter(adapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setName(String name){
            TextView mUserNameView=mView.findViewById(R.id.users_single_name);
            mUserNameView.setText(name);
        }
        public void setImage(String image){
            ImageView mUserImageView=mView.findViewById(R.id.users_single_image);
            Picasso.get().load(image).into(mUserImageView);
        }
        public void setStatus(String status){
            TextView mUserStatusView=mView.findViewById(R.id.users_single_status);
            mUserStatusView.setText(status);

        }


    }
}
