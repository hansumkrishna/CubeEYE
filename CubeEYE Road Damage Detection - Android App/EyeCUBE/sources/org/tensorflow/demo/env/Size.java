package org.tensorflow.demo.env;

import android.graphics.Bitmap;
import android.text.TextUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Size implements Comparable<Size>, Serializable {
    public static final long serialVersionUID = 7689808733290872361L;
    public final int height;
    public final int width;

    public Size(int width2, int height2) {
        this.width = width2;
        this.height = height2;
    }

    public Size(Bitmap bmp) {
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
    }

    public static Size getRotatedSize(Size size, int rotation) {
        if (rotation % 180 != 0) {
            return new Size(size.height, size.width);
        }
        return size;
    }

    public static Size parseFromString(String sizeString) {
        if (TextUtils.isEmpty(sizeString)) {
            return null;
        }
        String[] components = sizeString.trim().split("x");
        if (components.length != 2) {
            return null;
        }
        try {
            return new Size(Integer.parseInt(components[0]), Integer.parseInt(components[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<Size> sizeStringToList(String sizes) {
        List<Size> sizeList = new ArrayList<>();
        if (sizes != null) {
            for (String pair : sizes.split(",")) {
                Size size = parseFromString(pair);
                if (size != null) {
                    sizeList.add(size);
                }
            }
        }
        return sizeList;
    }

    public static String sizeListToString(List<Size> sizes) {
        String sizesString = "";
        if (sizes != null && sizes.size() > 0) {
            sizesString = sizes.get(0).toString();
            for (int i = 1; i < sizes.size(); i++) {
                sizesString = sizesString + "," + sizes.get(i).toString();
            }
        }
        return sizesString;
    }

    public final float aspectRatio() {
        return ((float) this.width) / ((float) this.height);
    }

    public int compareTo(Size other) {
        return (this.width * this.height) - (other.width * other.height);
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Size)) {
            return false;
        }
        Size otherSize = (Size) other;
        if (this.width == otherSize.width && this.height == otherSize.height) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.width * 32713) + this.height;
    }

    public String toString() {
        return dimensionsAsString(this.width, this.height);
    }

    public static final String dimensionsAsString(int width2, int height2) {
        return width2 + "x" + height2;
    }
}
