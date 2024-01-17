package com.example.weatheranalysis.classes.singleton;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
//Singleton pattern για request Queue
public class MySingletonVolley {
    private static MySingletonVolley instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private MySingletonVolley(Context context){
        ctx = context;
        requestQueue = getRequestQueue();
    }
    public static synchronized MySingletonVolley getInstance(Context context) {
        if( instance == null){
            instance = new MySingletonVolley(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }
}
