package com.abhay.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {


    private TextView mProfileDisplayName,mProfileStatus,mProfileTotalFriends;

    private ImageView mProfileImage;

    private Button mProfileSendReqBtn;

    private DatabaseReference mUsersdatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private int mCurrentState;//0=Not a Friend ,1=Request sent ,2=Request Received,3=Friends

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileDisplayName=findViewById(R.id.profile_displayName);
        mProfileStatus=findViewById(R.id.profile_status);
        mProfileTotalFriends=findViewById(R.id.profile_totalfriends);
        mProfileImage=findViewById(R.id.profile_display_image);
        mProfileSendReqBtn=findViewById(R.id.profile_send_req_btn);

        //progress dialog
        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User;s Data");
        mProgressDialog.setMessage("Please wait while we fetching the user's data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();




        //user id of user's profile which we are currently viewing
        final String user_id=getIntent().getStringExtra("user_id");

        //database reference to current user id in database
        mUsersdatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        //database reference to Friend_req in database
        mFriendRequestDatabase= FirebaseDatabase.getInstance().getReference().child("Friend_req");

        //database reference to Friends in database
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");

        //getting current user by which we are logged in
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();


        //get values like name,status,image of current userid from database and set it to textfields in activity_profile
        mUsersdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileDisplayName.setText(display_name);
                mProfileStatus.setText(status);;
                Picasso.get().load(image).placeholder(R.drawable.defaultimg).into(mProfileImage);

                //----------FRIENDS LIST/REQUEST FEATURE OR DETECT IF THE PERSON'S PROFILE WHICH WE ARE VIEWING HAS SENT US THE REQUSET OR NOT AND DETECT IF IT IS FRIENDS WITH US OR NOT -------------------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrentState=2;//1=request Received
                                mProfileSendReqBtn.setText("Accept Friend Request");
                            }
                            else if (req_type.equals("sent")) {
                                mCurrentState=1;//1=request sent
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                            }

                            mProgressDialog.dismiss();
                        }
                        else{// runs when already friend

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrentState=3;//3=friends
                                        mProfileSendReqBtn.setText("Unfriend this person");

                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    mProgressDialog.dismiss();
                                }
                            });



                        }




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mProgressDialog.dismiss();
                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgressDialog.dismiss();

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBtn.setEnabled(false);

                //--------------------NOT FRIENDS STATE-------------------------
                if(mCurrentState==0){//0=not friends i.e. if not friends then we have to make them friends
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            
                            if(task.isSuccessful()){
                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mCurrentState=1;//1=request sent
                                        mProfileSendReqBtn.setText("Cancel Friend Request");
                                        Toast.makeText(ProfileActivity.this, "Friend Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                }

                //---------------------CANCEL REQUEST STATE------------
                if(mCurrentState==1){//1=request sent i.e. if request is sent then we have to cancel the request

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState=0;//0=not friends
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    Toast.makeText(ProfileActivity.this, "Friend Request Cancelled", Toast.LENGTH_SHORT).show();


                                }
                            });
                        }
                    });
                }


                //-------------------REQUEST RECEIVED STATE-----------
                if(mCurrentState==2){//2=request received

                    final String current_date= DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrentState=3;//3=friends
                                                    mProfileSendReqBtn.setText("Unfriend this person");
                                                    Toast.makeText(ProfileActivity.this, "You are now friends", Toast.LENGTH_SHORT).show();


                                                }
                                            });
                                        }
                                    });


                                }
                            });
                        }
                    });

                }

                //-------------------ALREADY FRIENDS STATE------------------------
                if(mCurrentState==3){//3=friends if already friends the we unfriend them

                    mFriendDatabase.child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState=0;//0=not friends
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    Toast.makeText(ProfileActivity.this, "You are no longer friends", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    });


                }

            }
        });





    }
}
