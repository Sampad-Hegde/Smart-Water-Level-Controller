package com.example.waterlevelcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(nvlistener);
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,new Status()).addToBackStack(null).commit();

    }
    private BottomNavigationView.OnNavigationItemSelectedListener nvlistener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            Fragment selected = null;

            switch (item.getItemId())
            {
                case R.id.status: selected = new Status();
                                    break;
                case R.id.analytics: selected = new Analytics();
                    break;
                case R.id.control: selected = new Controller();
                    break;
                case R.id.developer: selected = new Developers();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.framelayout,selected).addToBackStack(null).commit();
            return true;
        }
    };

}