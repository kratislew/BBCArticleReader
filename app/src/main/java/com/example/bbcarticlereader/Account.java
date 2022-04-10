package com.example.bbcarticlereader;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

public class Account extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_layout);

        //For toolbar:
        Toolbar tBar = findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        //For NavigationDrawer:
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.yes, R.string.no);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //retrieve data sent from login page
        Intent dataSent = getIntent();
        String username = dataSent.getStringExtra("username");
        Button logOut = findViewById(R.id.logOutButton);
        //set greeting to username from login page
        TextView welcome = findViewById(R.id.accountGreet);
        welcome.setText(getResources().getString(R.string.hello) + " " + username + "!");

        //if user clicks log out, delete username from shared folder and return to login page
        //otherwise, username is stored in shared folder until logout is clicked
        logOut.setOnClickListener( click -> {
                Intent log = new Intent(this, AccountLogin.class);
                SharedPreferences acc = getSharedPreferences("StoredAcc", Context.MODE_PRIVATE);
                acc.edit().remove("ReserveAcc").commit();
                startActivity(log);
            });
        }

}