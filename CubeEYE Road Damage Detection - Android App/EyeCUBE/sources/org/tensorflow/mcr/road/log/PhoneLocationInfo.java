package org.tensorflow.mcr.road.log;

import android.location.Location;
import org.json.JSONException;
import org.json.JSONObject;

public class PhoneLocationInfo implements IJson {
    protected String deviceID;
    protected String groupID = "";
    protected PhoneLocation phoneLocation;
    protected String smartphoneID;
    protected String timestamp;
    protected String userID = "";

    public PhoneLocationInfo(String deviceID2, String smartphoneID2, String timestamp2, Location location) {
        this.deviceID = deviceID2;
        this.smartphoneID = smartphoneID2;
        this.timestamp = timestamp2;
        this.phoneLocation = new PhoneLocation(location.getLatitude(), location.getLongitude());
    }

    public JSONObject getJson() throws JSONException {
        JSONObject object = new JSONObject();
        JSONObject child = new JSONObject();
        child.put("deviceID", this.deviceID);
        child.put("smartphoneID", this.smartphoneID);
        child.put("groupID", this.groupID);
        child.put("userID", this.userID);
        child.put("timestamp", this.timestamp);
        child.put("location", this.phoneLocation.getJson());
        object.put("annotation", child);
        return object;
    }
}
