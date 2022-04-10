package com.example.bbcarticlereader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MyGeneration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_generation);

        //handles bundle passing fragment
        Bundle dataToPass = getIntent().getExtras();
        DetailsFragment dFrag = new DetailsFragment();
        dFrag.setArguments( dataToPass );
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.placeHolderFrame, dFrag)
                .commit();
    }
}