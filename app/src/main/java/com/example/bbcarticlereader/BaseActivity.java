package com.example.bbcarticlereader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected static SQLiteDatabase fdb;
    MyOpener dbOpen = new MyOpener(this);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //set options for each menu item clicked
        switch(item.getItemId())
        {
            case R.id.home:
                //clicking home will send user to MainActivity
                startActivity(new Intent(BaseActivity.this, MainActivity.class));
                break;
            case R.id.feed:
                //feed will send user to BBCFeed
                startActivity(new Intent(BaseActivity.this, BBCFeed.class));
                break;
            case R.id.fav:
                //fav will send user to Favourites
                startActivity(new Intent(BaseActivity.this, Favourites.class));
                break;
            case R.id.acc:
                //acc will send user to AccountLogin
                startActivity(new Intent(BaseActivity.this, AccountLogin.class));
                break;
            case R.id.helpButton:
                //help will create popup to detail help info
                startActivity(new Intent(BaseActivity.this, HelpPop.class));
                break;
        }
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //set options for each navigation bar item clicked

        switch(item.getItemId())
        {
            case R.id.home:
                //clicking home will send user to MainActivity
                startActivity(new Intent(BaseActivity.this, MainActivity.class));
                break;
            case R.id.tofeed:
                //tofeed will send user to BBCFeed
                Intent goFeed = new Intent(getApplicationContext(), BBCFeed.class);
                startActivity(goFeed);
                break;
            case R.id.favourites:
                //fav will send user to Favourites
                Intent goFav = new Intent(getApplicationContext(), Favourites.class);
                startActivity(goFav);
                break;
            case R.id.account:
                //acc will send user to AccountLogin
                Intent goAcc = new Intent(getApplicationContext(), AccountLogin.class);
                startActivity(goAcc);
                break;
            case R.id.help:
                //help will create an alert dialogue with usage instructions
                AlertDialog.Builder helpAlert = new AlertDialog.Builder(this);
                helpAlert.setTitle(R.string.help_title)
                        .setMessage(R.string.help_message)
                        .setNeutralButton(R.string.help_confirm, (click, arg) -> {})
                        .create().show();
                break;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }


}