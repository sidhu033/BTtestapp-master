package gamsystech.bttestapp.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toolbar;

import gamsystech.bttestapp.R;
import gamsystech.bttestapp.app.App;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener
{
    android.support.v7.widget.Toolbar tolbarmain;
    ImageView startimg;
    String start = "s";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tolbarmain = findViewById(R.id.tolbarmain);
        setSupportActionBar(tolbarmain);

        startimg = findViewById(R.id.startimg);


        startimg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Bpreading.class));
            }
        });

    }


    @Override
    public void onBackStackChanged()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }
}
