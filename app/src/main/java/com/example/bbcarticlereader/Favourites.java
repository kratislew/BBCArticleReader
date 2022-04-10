package com.example.bbcarticlereader;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class Favourites extends BaseActivity {
    private ArrayList<FavouritesItem> favouritesList = new ArrayList<>();
    private MyListAdapter myAdapter;

    //initialize default values for local variables
    public static final String ART_TITLE = "TITLE";
    public static final String ART_DATE = "DATE";
    public static final String ART_LINK = "LINK";
    public static final String ART_DESC = "DESCRIPTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites_layout);

        //initialize listview
        ListView favouritesView = (ListView) findViewById(R.id.favouritesFeed);
        favouritesView.setAdapter(myAdapter = new MyListAdapter());

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

        //Load saved favourites from database
        loadDataFromDataBase();

        //log database to ensure proper transfer
        printCursor(fdb.rawQuery("SELECT * FROM " + MyOpener.TABLE_NAME, null));

        //set short press listener to view article information
        favouritesView.setOnItemClickListener((list, item, position, id) -> {
            //Create a bundle to pass data to the new fragment
            Bundle dataToPass = new Bundle();
            dataToPass.putString(ART_TITLE, favouritesList.get(position).getArticleTitle() );
            dataToPass.putString(ART_DATE, favouritesList.get(position).getArticleDate());
            dataToPass.putString(ART_LINK, favouritesList.get(position).getArticleLink());
            dataToPass.putString(ART_DESC, favouritesList.get(position).getArticleDesc());

            Intent nextActivity = new Intent(Favourites.this, MyGeneration.class);
            nextActivity.putExtras(dataToPass); //send data to next activity
            startActivity(nextActivity); //make the transition

        });

        //set up on long press listener so user can delete article from favourites
        favouritesView.setOnItemLongClickListener( (p, b, pos, id) -> {
            AlertDialog.Builder deleteWarning = new AlertDialog.Builder(this);
            deleteWarning.setTitle(R.string.delete_prompt)

                    .setMessage(getString(R.string.deleteText) + " " + favouritesList.get(pos).getArticleTitle())

                    .setPositiveButton(R.string.deleteIt, (click, arg) -> {
                        //on delete, create temporary value in case user wants to undo delete
                        FavouritesItem tempItem = favouritesList.get(pos);
                        //remove item from database and arraylist
                        deleteItem(favouritesList.get(pos));
                        favouritesList.remove(pos);
                        myAdapter.notifyDataSetChanged(); //update view

                        //option to undo delete
                        Snackbar.make(favouritesView, getResources().getString(R.string.onDelete), Snackbar.LENGTH_LONG)
                                .setAction(getResources().getString(R.string.undo), onclick -> {
                                    //if user wants to undo delete, replace item in same position in arraylist and re-add it to the db
                                    favouritesList.add(pos, tempItem);
                                    ContentValues restoreFavouriteValues = new ContentValues();
                                    restoreFavouriteValues.put(MyOpener.COL_TITLE, tempItem.getArticleTitle());
                                    restoreFavouriteValues.put(MyOpener.COL_PUBDATE, tempItem.getArticleDate());
                                    restoreFavouriteValues.put(MyOpener.COL_LINK, tempItem.getArticleLink());
                                    restoreFavouriteValues.put(MyOpener.COL_DESC, tempItem.getArticleDesc());
                                    long newID = fdb.insert(MyOpener.TABLE_NAME, null, restoreFavouriteValues);
                                    //make sure item id gets updated
                                    favouritesList.get(pos).setArtID(newID);
                                    myAdapter.notifyDataSetChanged(); //update view
                                })

                                .show();
                    })

                    .setNegativeButton(R.string.saveIt, (click, arg) -> {})

                    .create().show();
            return true;
        });
    }

    //call to load data from favourites db
    private void loadDataFromDataBase(){
        //open db
        fdb = dbOpen.getWritableDatabase();

        //initialize array to hold database info and cursor to query db
        String [] columns = {MyOpener.COL_TITLE, MyOpener.COL_PUBDATE, MyOpener.COL_LINK, MyOpener.COL_DESC, MyOpener.COL_ID};
        Cursor results = fdb.query(false, MyOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        //retrieve starting index for each db column
        int titleColIndex = results.getColumnIndex(MyOpener.COL_TITLE);
        int pubDateColIndex = results.getColumnIndex(MyOpener.COL_PUBDATE);
        int linkColIndex = results.getColumnIndex(MyOpener.COL_LINK);
        int descColIndex = results.getColumnIndex(MyOpener.COL_DESC);
        int idColIndex = results.getColumnIndex(MyOpener.COL_ID);

        //parse db query results, creating objects with data and adding those objects to favourites arraylist
        while(results.moveToNext()){
            String title = results.getString(titleColIndex);
            String pubDate = results.getString(pubDateColIndex);
            String link = results.getString(linkColIndex);
            String desc = results.getString(descColIndex);
            long id = results.getLong(idColIndex);
            favouritesList.add(new FavouritesItem(title, pubDate, link, desc, id));
        }
        results.close();

    }

    //will remove input item from database based on itemid
    protected void deleteItem(FavouritesItem it){
        fdb.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + "= ?", new String[] {Long.toString(it.getID())});
    }

    //creates favourites feed from favourites arraylist
    private class MyListAdapter extends BaseAdapter {
        public int getCount() {return favouritesList.size();}

        public FavouritesItem getItem(int i) {return favouritesList.get(i);}

        public long getItemId(int i) {return 0;}

        public View getView(int position, View old, ViewGroup parent)
        {
            View newView = old;
            LayoutInflater inflater = getLayoutInflater();

            //make a new row:
            if(newView == null) {
                newView = inflater.inflate(R.layout.favourites_feed_layout, parent, false);
            }
            Log.i("RESULT", ""+favouritesList.get(position).getArticleTitle());

            TextView tView = newView.findViewById(R.id.favouritesFeedItems);
            tView.setText(favouritesList.get(position).getArticleTitle());

            return newView;
        }
    }

    //object class for articles within favourites, includes an ID unlike main feed to allow for db management
    private class FavouritesItem{
        String title;
        String aDate;
        String link;
        String desc;
        long artID;

        //default constructors
        FavouritesItem(){
            title = "Something Happened!";
            aDate = "Jan 1st, 2022";
            link = "https://bbci.co.uk";
            desc = "This is an article about a man who woke in a parallel world";
            artID = 0;
        }

        FavouritesItem(String t, String da, String l, String des, long id){
            title = t;
            aDate = da;
            link = l;
            desc = des;
            artID = id;

        }
        //setters and getters
        public void setArticleTitle(String t){title = t;}
        public void setArticleDate(String d){aDate = d;}
        public void setArticleLink(String l){link = l;}
        public void setArticleDesc(String d){desc = d;}
        public void setArtID(Long id){artID = id;}

        public String getArticleTitle(){return title;}
        public String getArticleDate(){return aDate;}
        public String getArticleLink(){return link;}
        public String getArticleDesc(){return desc;}
        public Long getID(){return artID;}

    }
    //method to log database information for verification.
    private void printCursor(Cursor c) {
        System.out.println("Database Version " + "" + fdb.getVersion());
        Log.d("Number of Columns ", "" + c.getColumnCount());
        String[] colNames = c.getColumnNames();
        for (int i = 0; i < colNames.length; i++) {
            Log.d("Column " + i + " Name ", "" + colNames[i]);
        }
        Log.d("Number of results ", "" + c.getCount());
        while (c.moveToNext()) {
            int count = 0;
            Log.d("Row " + count + " results: ", "Id: " + c.getLong(0) + " To Do: " + c.getString(1) + " Is Urgent: " + c.getInt(2));
            count++;
        }
        c.close();
    }

}