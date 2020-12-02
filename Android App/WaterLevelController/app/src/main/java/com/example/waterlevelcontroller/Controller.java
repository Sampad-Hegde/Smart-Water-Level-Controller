package com.example.waterlevelcontroller;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Controller extends Fragment
{

    ImageView emergencystop = null;
    Button pumpswitch = null;

    RequestQueue requestQueue;
    Context context;

    int current_pump = 0;

    int togglestate = 0;
    int emebtnstate = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        context = getContext();
        requestQueue = Volley.newRequestQueue(context);

        return inflater.inflate(R.layout.fragment_controller,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        emergencystop = (ImageView) getView().findViewById(R.id.emergency);
        pumpswitch = (Button) getView().findViewById(R.id.pump_switch);

        emergencystop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                System.out.println("*** Emergency Button Pressed ***");
                emebtnstate = 1;
                sendDataToServeer();
            }
        });

        pumpswitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(current_pump == 1)
                {
                    togglestate = 0;
                    pumpswitch.setText("Turn On");
                }
                else
                {
                    togglestate = 1;
                    pumpswitch.setText("Turn Off");
                }
                sendDataToServeer();
            }
        });
    }

    public void sendDataToServeer()
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("emergencystatus",emebtnstate);
            jsonObject.put("forcestop",togglestate);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        final String requestbody = jsonObject.toString();

        String url = "http://192.168.1.5:8000/control" ;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                System.out.println("****************** Response : "+response);

                try
                {
                    JSONObject jsonobj = new JSONObject(response);
                    if (jsonobj.getInt("pumpstatus") == 1)
                    {
                        pumpswitch.setText("Turn Off");
                        current_pump = 1;
                    }
                    else
                    {
                        pumpswitch.setText("Turn On");
                        current_pump = 1;
                    }

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                if (emebtnstate == 1)
                {
                    emebtnstate = 0;
                }
            }
        },
        new Response.ErrorListener()
        {
        @Override
        public void onErrorResponse(VolleyError error)
        {
            System.out.println("****************** Error : "+error);
        }
        })
        {
            @Override
            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError
            {
                try
                {
                    return requestbody == null ? null : requestbody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee)
                {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestbody, "utf-8");
                    return null;
                }
            }
        };


        requestQueue.add(stringRequest);
    }
}

