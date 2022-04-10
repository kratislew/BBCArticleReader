package com.example.bbcarticlereader;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Date;

public class DetailsFragment extends Fragment {

    //create local variables
    private Bundle dataFromActivity;
    private String title;
    private String aDate;
    private String link;
    private String desc;
    private AppCompatActivity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //initialize local variables from passed data, based on which article was clicked on
        dataFromActivity = getArguments();
        title = dataFromActivity.getString(BBCFeed.ART_TITLE);
        aDate = dataFromActivity.getString(BBCFeed.ART_DATE);
        link = "<a href='" + dataFromActivity.getString(BBCFeed.ART_LINK) + "'>"+ dataFromActivity.getString(BBCFeed.ART_LINK) + "</a>";
        desc = dataFromActivity.getString(BBCFeed.ART_DESC);

        //create and set views to show information
        View result = inflater.inflate(R.layout.fragment_details, container, false);
        TextView titleView = (TextView)result.findViewById(R.id.titleFill);
        TextView dateView = (TextView)result.findViewById(R.id.dateFill);
        TextView linkView = (TextView)result.findViewById(R.id.linkFill);
        TextView descriptionView = (TextView)result.findViewById(R.id.descriptionFill);

        titleView.setText(title);
        dateView.setText(aDate);
        linkView.setClickable(true);
        linkView.setMovementMethod(LinkMovementMethod.getInstance());
        linkView.setText(Html.fromHtml(link));
        descriptionView.setText(desc);
        return result;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        parentActivity = (AppCompatActivity)context;
    }
}