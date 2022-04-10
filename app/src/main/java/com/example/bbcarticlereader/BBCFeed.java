package com.example.bbcarticlereader;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Log;
import android.util.Xml;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

public class BBCFeed extends BaseActivity {

    //create arraylists for news items
    ArrayList<NewsItem> newsList = new ArrayList<NewsItem>();
    ArrayList<String> titles;
    ArrayList<String> descs;
    ArrayList<String> links;
    ArrayList<String> pubDates;
    private MyListAdapter myAdapter;
    public static final String ART_TITLE = "TITLE";
    public static final String ART_DATE = "DATE";
    public static final String ART_LINK = "LINK";
    public static final String ART_DESC = "DESCRIPTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_feed_layout);

        //initialize refresher
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);

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

        //initialize feed
        ListView currentNews = (ListView) findViewById(R.id.newsFeed);
        currentNews.setAdapter(myAdapter = new MyListAdapter());

        //initialize arraylists
        titles = new ArrayList<String>();
        descs = new ArrayList<String>();
        links = new ArrayList<String>();
        pubDates = new ArrayList<String>();

        //request and load feed from BBC rss feed
        getNewsFeed req = new getNewsFeed();
        req.execute("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");

        //Allow news feed to be refreshed by pulling down on the screen
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewsFeed ref = new getNewsFeed();
                ref.execute("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");
                pullToRefresh.setRefreshing(false);
            }
        });

        //Set short click to view detailed article information
        currentNews.setOnItemClickListener((list, item, position, id) -> {
            //Create a bundle to pass data to the new fragment
            Bundle dataToPass = new Bundle();
            dataToPass.putString(ART_TITLE, newsList.get(position).getArticleTitle() );
            dataToPass.putString(ART_DATE, newsList.get(position).getArticleDate());
            dataToPass.putString(ART_LINK, newsList.get(position).getArticleLink());
            dataToPass.putString(ART_DESC, newsList.get(position).getArticleDesc());

            Intent nextActivity = new Intent(BBCFeed.this, MyGeneration.class);
            nextActivity.putExtras(dataToPass); //send data to next activity
            startActivity(nextActivity); //make the transition

        });

        //set long click to add article to favourites database
        currentNews.setOnItemLongClickListener( (list, item, pos, id) -> {
            //open database
            fdb = dbOpen.getWritableDatabase();

            //create alert to ensure user wishes to proceed
            AlertDialog.Builder setFavouriteConfirmation = new AlertDialog.Builder(this);
            setFavouriteConfirmation.setTitle(newsList.get(pos).getArticleTitle())

                    .setMessage(getString(R.string.fav_ques))

                    .setPositiveButton(R.string.yes, (click, arg) -> {
                        //if user wishes to add article, create ContentValues which will hold database row
                        ContentValues newFavouriteValues = new ContentValues();
                        newFavouriteValues.put(MyOpener.COL_TITLE, newsList.get(pos).getArticleTitle());
                        newFavouriteValues.put(MyOpener.COL_PUBDATE, newsList.get(pos).getArticleDate());
                        newFavouriteValues.put(MyOpener.COL_LINK, newsList.get(pos).getArticleLink());
                        newFavouriteValues.put(MyOpener.COL_DESC, newsList.get(pos).getArticleDesc());
                        long newID = fdb.insert(MyOpener.TABLE_NAME, null, newFavouriteValues); //add object to db

                        Toast.makeText(BBCFeed.this, getResources().getString(R.string.fav_added), Toast.LENGTH_LONG).show(); //inform user item has been added to favourites

                    })

                    .setNegativeButton(R.string.no, (click, arg) -> {})

                    .create().show();
            return true;
        });


    }

    //AsyncTask to retrieve data from BBC feed and place into arraylist
    public class getNewsFeed extends AsyncTask<String, Void, String>{
        //progressbar for importing articles
        ProgressDialog dataGetProgress = new ProgressDialog(BBCFeed.this);

        @Override
        protected void onPreExecute() {
            dataGetProgress.setMessage("Loading BBC RSS feed ...");
            dataGetProgress.show();
        }
        @Override
        protected String doInBackground(String ... args) {
            try{
                //open connection
                URL url = new URL(args[0]);

                HttpURLConnection uCon = (HttpURLConnection) url.openConnection();

                //get response and create XmlPullParser factory
                InputStream response = uCon.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(response, "UTF_8");

                boolean inItem = false;

                int eventType = xpp.getEventType();

                //parse through xml file and add to respective individual arraylists until end of document
                //use tags in order to retrieve specified variables
                while (eventType != XmlPullParser.END_DOCUMENT){
                    if(eventType == XmlPullParser.START_TAG){
                        if(xpp.getName().equalsIgnoreCase("item")){
                            inItem = true;
                        }
                        else if (xpp.getName().equalsIgnoreCase("title")){
                            if (inItem){
                                titles.add(xpp.nextText());

                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("description")){
                            if (inItem){
                                descs.add(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link")){
                            if (inItem){
                                links.add(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("pubDate")){
                            if (inItem){
                                pubDates.add(xpp.nextText());
                            }
                        }

                    }
                    else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        inItem = false;
                    }
                    eventType = xpp.next();
                }

                return null;

            } catch (Exception e) {
                Log.i("NOT WORKING", ""+ e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result){

            //create items from individual arraylists and add them to newsList to be displayed in feed
            try {
                for (int i = 0; i < titles.size(); i++) {
                    NewsItem x = new NewsItem();
                    x.setArticleTitle(titles.get(i));
                    x.setArticleDate(pubDates.get(i));
                    x.setArticleLink(links.get(i));
                    x.setArticleDesc(descs.get(i));
                    newsList.add(x);
                    myAdapter.notifyDataSetChanged();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            //end progressbar
            dataGetProgress.dismiss();
        }
    }
    //create feed from arraylist
    private class MyListAdapter extends BaseAdapter {
        public int getCount() {return newsList.size();}

        public NewsItem getItem(int i) {return newsList.get(i);}

        public long getItemId(int i) {return 0;}

        public View getView(int position, View old, ViewGroup parent)
        {
            View newView = old;
            LayoutInflater inflater = getLayoutInflater();

            //make a new row:
            if(newView == null) {
                newView = inflater.inflate(R.layout.feed_layout, parent, false);
            }
            Log.i("RESULT", ""+newsList.get(position).getArticleTitle());

            TextView tView = newView.findViewById(R.id.newsFeedItems);
            tView.setText(newsList.get(position).getArticleTitle());

            return newView;
        }
    }
    //object to hold news articles, contains title, date, link and description of news article
    private class NewsItem{
        String title;
        String aDate;
        String link;
        String desc;

        //default constructor
        NewsItem(){
            title = "Something Happened!";
            aDate = "Jan 1st, 2022";
            link = "https://bbci.co.uk";
            desc = "This is an article about a man who woke in a parallel world";
        }

        //default constructor provided information
        NewsItem(String t, String da, String l, String des){
            title = t;
            aDate = da;
            link = l;
            desc = des;
        }

        //getters and setters
        public void setArticleTitle(String t){title = t;}
        public void setArticleDate(String d){aDate = d;}
        public void setArticleLink(String l){link = l;}
        public void setArticleDesc(String d){desc = d;}

        public String getArticleTitle(){return title;}
        public String getArticleDate(){return aDate;}
        public String getArticleLink(){return link;}
        public String getArticleDesc(){return desc;}

    }

}