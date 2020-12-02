package com.example.waterlevelcontroller;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.john.waveview.WaveView;

import org.json.JSONException;
import org.json.JSONObject;

public class Status extends Fragment
{

    RequestQueue requestQueue;
    Context context;

    TextView undergrounrstatus = null;
    TextView overheadstatus = null;
    TextView pumpstatus = null;

    WaveView undergroundwave = null;
    WaveView overheadwave = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        context = getContext();
        return inflater.inflate(R.layout.fragment_status,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        undergrounrstatus = (TextView) getView().findViewById(R.id.underground_txt);
        overheadstatus = (TextView) getView().findViewById(R.id.overhead_txt);
        pumpstatus = (TextView) getView().findViewById(R.id.pump_status);

        undergroundwave = (WaveView) getView().findViewById(R.id.underground_wave);
        overheadwave = (WaveView) getView().findViewById(R.id.overhead_wave);

        requestQueue = Volley.newRequestQueue(context);

        Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                fetchdata();
                handler.postDelayed(this,5000);
            }
        };



        handler.postDelayed(runnable,5000);
        // fetchdata();

    }



    public void fetchdata()
    {

        String url = "http://192.168.1.5:8000/status" ;
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                System.out.println("********************** Response : "+response);
                try
                {
                    System.out.println("pump Status:"+response.getString("pumpstatus"));
                    System.out.println("over Status:"+response.getString("overheadlevel"));
                    System.out.println("under Status:"+response.getString("undergroundlevel"));

                    updateValues(Integer.parseInt(response.getString("pumpstatus")),Integer.parseInt(response.getString("overheadlevel")),Integer.parseInt(response.getString("undergroundlevel")));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        System.out.println("**********************Error : "+error);
                    }
                }
        );

        requestQueue.add(objectRequest);

    }

    public void updateValues(int pumpstat , int overheadstat , int undergroundstat)
    {
        if (pumpstat == 0)
        {
            pumpstatus.setText("Pump Status : OFF");
        }
        else
        {
            pumpstatus.setText("Pump Status : ON");
        }

        overheadstatus.setText("Over Head Tank : "+33.333*overheadstat+"%");
        undergrounrstatus.setText("Under Ground Tank : "+33.333*undergroundstat+"%");

        overheadwave.setProgress(33*overheadstat);
        undergroundwave.setProgress(33*undergroundstat);
    }
}