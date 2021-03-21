package org.tensorflow.mcr.road.log;

import org.json.JSONException;
import org.json.JSONObject;

public class DamageObject implements IJson {
    private double confidence;
    private String name;
    private float xmax;
    private float xmin;
    private float ymax;
    private float ymin;

    public DamageObject(float xmax2, float xmin2, float ymax2, float ymin2, double confidence2, String name2) {
        this.xmax = xmax2;
        this.xmin = xmin2;
        this.ymax = ymax2;
        this.ymin = ymin2;
        this.confidence = confidence2;
        this.name = name2;
    }

    public JSONObject getJson() throws JSONException {
        JSONObject object = new JSONObject();
        JSONObject child = new JSONObject();
        child.put("xmax", (double) this.xmax);
        child.put("xmin", (double) this.xmin);
        child.put("ymax", (double) this.ymax);
        child.put("ymin", (double) this.ymin);
        object.put("bndbox", child);
        object.put("confidence", this.confidence);
        object.put("name", this.name);
        return object;
    }
}
