package com.speedautosystems.firebaseproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Yasir on 2/28/2017.
 */
public class SelectionActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        findViewById(R.id.send_loc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(SelectionActivity.this,LocationUpdateActivity.class);
                intent.putExtra("IS_TRACKING",false);
                startActivity(intent);
            }
        });

        findViewById(R.id.track_loc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectionActivity.this,LocationUpdateActivity.class);
                intent.putExtra("IS_TRACKING",true);
                startActivity(intent);
            }
        });

        findViewById(R.id.set_loc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectionActivity.this,PlacePickerActivity.class);
                startActivity(intent);
            }
        });

    }
}
