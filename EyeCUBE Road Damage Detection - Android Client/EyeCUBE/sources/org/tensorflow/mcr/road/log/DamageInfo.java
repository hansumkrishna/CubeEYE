package org.tensorflow.mcr.road.log;

import android.location.Location;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DamageInfo extends PhoneLocationInfo implements IJson {
    private String filename;
    private String image;
    private List<DamageObject> listDamage;
    private DamageSize size;

    public DamageInfo(String deviceID, String smartphoneID, String timestamp, Location location, String filename2, DamageSize size2, List<DamageObject> listDamage2, String image2) {
        super(deviceID, smartphoneID, timestamp, location);
        this.filename = filename2;
        this.size = size2;
        this.listDamage = listDamage2;
        this.image = image2;
    }

    public JSONObject getJson() throws JSONException {
        JSONObject object = new JSONObject();
        JSONObject child = new JSONObject();
        child.put("filename", this.filename);
        child.put("deviceID", this.deviceID);
        child.put("smartphoneID", this.smartphoneID);
        child.put("timestamp", this.timestamp);
        child.put("location", this.phoneLocation.getJson());
        child.put("size", this.size.getJson());
        JSONArray damages = new JSONArray();
        for (DamageObject damage : this.listDamage) {
            damages.put(damage.getJson());
        }
        child.put("object", damages);
        child.put("image", this.image);
        object.put("annotation", child);
        return object;
    }
}
