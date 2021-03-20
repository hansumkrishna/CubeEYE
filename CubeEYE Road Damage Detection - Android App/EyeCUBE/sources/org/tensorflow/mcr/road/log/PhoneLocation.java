package org.tensorflow.mcr.road.log;

import org.json.JSONException;
import org.json.JSONObject;

public class PhoneLocation implements IJson {
    private double lat;
    private double lon;

    public PhoneLocation(double lat2, double lon2) {
        this.lat = lat2;
        this.lon = lon2;
    }

    public JSONObject getJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("lat", this.lat);
        object.put("lon", this.lon);
        return object;
    }
}
