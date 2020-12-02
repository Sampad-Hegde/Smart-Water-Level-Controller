package com.example.waterlevelcontroller;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Analytics extends Fragment
{

    RequestQueue requestQueue;
    ArrayList<Entry> arrayList = new ArrayList<>();

    Context context;
    LineChart graph;
    TextView monthly_usage = null;
    TextView daily_usage = null;
    TextView unit_usage = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        context = getContext();
        return inflater.inflate(R.layout.fragment_analytics,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);
        graph = (LineChart) getView().findViewById(R.id.graph);
        daily_usage = (TextView) getView().findViewById(R.id.dailyusage);
        monthly_usage = (TextView) getView().findViewById(R.id.monthlyusage);
        unit_usage = (TextView) getView().findViewById(R.id.unitusage);

//        graph.setOnChartGestureListener((OnChartGestureListener) context);
//        graph.setOnChartGestureListener((OnChartGestureListener) context);
        graph.setDragEnabled(true);
        graph.setScaleEnabled(true);
        graph.animate();
        graph.getSolidColor();



        requestQueue = Volley.newRequestQueue(context);

        getData();

    }
    public void updateDailyUsage(int value)
    {
        if (daily_usage != null)
        {
            daily_usage.setText("Daily Pump on Time Average : "+value+" Hrs");
        }
    }
    public void updatemMonthlyUsage(int value)
    {
        if (monthly_usage != null)
        {
            monthly_usage.setText("Monthly Pump on Time Average : "+value+" Hrs");
        }
    }
    public void updateUnitUsage(int value)
    {
        if (unit_usage != null)
        {
            unit_usage.setText("Expected Unit Usage : "+value+" Units");
        }
    }

    public void getData()
    {
        String url = "http://192.168.1.5:8000/analytics";

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                System.out.println("************************  Volley Analytics Response : "+response);
                try
                {
                    updateData(response.getDouble("daytotal"),response.getDouble("monthtotal"),response.getDouble("totalunits"),response.getJSONArray("datapoints"));
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
                        System.out.println("************************  Volley Error : "+error);
                    }
        });
        requestQueue.add(objectRequest);
    }

    void updateData(Double dtotal, Double mtotal, Double unitusage, JSONArray points)
    {
//        System.out.println("Day Total : "+dtotal);
//        System.out.println("Month Total : "+mtotal);
//        System.out.println("Total Units : "+unitusage);
//        System.out.println("Data Points : "+points);
        daily_usage.setText(String.format("%.2f",dtotal)+" Hrs");
        monthly_usage.setText(String.format("%.2f",mtotal)+" Hrs");
        unit_usage.setText(String.format("%.2f",unitusage)+" Units");

        int i;
        for(i=0 ; i<points.length() ; i++)
        {
            try
            {
                System.out.println("< " + i + " , " + points.get(i) + ">");
                arrayList.add(new Entry(i, (float) points.getDouble(i)));
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
//        arrayList.add(new Entry(0,0));
//        arrayList.add(new Entry(1,2));
//        arrayList.add(new Entry(2,4));
//        arrayList.add(new Entry(3,6));
//        arrayList.add(new Entry(4,8));
        //System.out.println("Array List : "+arrayList);
        LineDataSet set = new LineDataSet(arrayList,"Time-Usage");
        set.setFillAlpha(110);
        ArrayList<ILineDataSet> ds = new ArrayList<>();
        ds.add(set);
        LineData lineData = new LineData(ds);

        graph.setData(lineData);
        graph.invalidate();

    }
}

