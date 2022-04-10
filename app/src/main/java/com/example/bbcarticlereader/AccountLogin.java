package com.example.bbcarticlereader;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;

public class AccountLogin extends BaseActivity {

    //initialize sharedpreferences folder
    SharedPreferences accName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

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

        //check if user is already saved (logged in)
        accName = getSharedPreferences("StoredAcc", Context.MODE_PRIVATE);
        String savedString = accName.getString("ReserveAcc", "reserve not found");

        //initialize intent
        Intent nextPage = new Intent(this, Account.class);

        //if user is found to be logged in, skip login page and send retrieved username to Account activity
        if(!savedString.equals("reserve not found")){
            nextPage.putExtra("username", savedString);
            startActivity(nextPage);
        }

        //initialize login button and username edittext
        Button login = findViewById(R.id.loginButton);
        EditText username = findViewById(R.id.usernameEntry);

        //if login button is clicked, retrieve entered text, save text to sharedfolder, then send text to Account activity
        login.setOnClickListener( click -> {
            nextPage.putExtra("username", username.getText().toString());
            SharedPreferences.Editor edit = accName.edit();
            edit.putString("ReserveAcc", username.getText().toString());
            edit.commit();
            startActivity(nextPage);
        });

    }
}