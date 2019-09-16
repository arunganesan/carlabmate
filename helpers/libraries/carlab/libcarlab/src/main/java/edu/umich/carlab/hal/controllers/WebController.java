package edu.umich.carlab.hal.controllers;

import android.content.Context;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.hal.JobMaker;
import edu.umich.carlab.net.HttpHandler;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import static edu.umich.carlab.sensors.WebSensors.WINDSPEED;

/**
 * Uses HTTP Handler to make a request and process output.
 *
 * This callable function is invoked inside of a thread using PollController.
 */

public class WebController implements JobMaker {



    final String TAG = this.getClass().getName();

    public WebController (Context ctx, DataMarshal dm) {}

    public boolean validSensor (String sensor) {
        return sensor.equals(WINDSPEED);
    }

    public Callable<Float> createTask (String sensor) {
        return new Callable<Float>() {
            @Override
            public Float call() {
                String query = "https://query.yahooapis.com/v1/public/yql?q=select wind from weather.forecast where woeid in (select woeid from geo.places(1) where text='ann arbor, mi')&format=json";
                HttpHandler sh = new HttpHandler();
                // Making a request to url and getting response
                query = query.replace(" ", "%20");

                Float value = null;

                try {
                    String allResponse = sh.makeServiceCall(query);
                    JSONObject jobj = new JSONObject(allResponse);
                    float windSpeed = (float)jobj.getJSONObject("query")
                            .getJSONObject("results")
                            .getJSONObject("channel")
                            .getJSONObject("wind")
                            .getDouble("speed");
                    value = windSpeed;
                } catch (Exception e) {

                }

                return value;
            }
        };
    }
}
