package com.speedautosystems.firebaseproject;

import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    EditText userName,email;
    Button save;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    TextView usernameText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName=(EditText)findViewById(R.id.userName);
        email=(EditText)findViewById(R.id.email);
        save=(Button) findViewById(R.id.save);
        usernameText=(TextView)findViewById(R.id.usertxt);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");





        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            // if(!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(userName.getText().toString()))

              //   createUser();

            }
        });
        valueUpdateListener();
    }

    public void createUser()
    {
        String username=userName.getText().toString();
        String uemail=email.getText().toString();
        String loc="64.99,34.78";
        String id=randomString(14);
        User user=new User(username,uemail,loc);
        mFirebaseDatabase.child(id).setValue(user);
    }


    public void valueUpdateListener()
    {
       mFirebaseDatabase.child("jfgvlZJyhyHVg4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                if(user!=null)
                usernameText.setText(user.getName());

         }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(getClass().getName(), "Failed to read value.", error.toException());
            }
        });
    }






    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
