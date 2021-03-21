package org.tensorflow.mcr.road.log;

import org.json.JSONException;
import org.json.JSONObject;

public class DamageSize implements IJson {
    private int depth;
    private int height;
    private int width;

    public DamageSize(int depth2, int height2, int width2) {
        this.depth = depth2;
        this.height = height2;
        this.width = width2;
    }

    public JSONObject getJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("depth", this.depth);
        object.put("height", this.height);
        object.put("width", this.width);
        return object;
    }
}
