package org.tensorflow.mcr.road.log;

public class GPS {

    /* renamed from: sb */
    private static StringBuilder f27sb = new StringBuilder(20);

    public static String latitudeRef(double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }

    public static String longitudeRef(double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }

    public static final synchronized String convert(double latitude) {
        String sb;
        synchronized (GPS.class) {
            double latitude2 = Math.abs(latitude);
            int degree = (int) latitude2;
            double latitude3 = (latitude2 * 60.0d) - (((double) degree) * 60.0d);
            int minute = (int) latitude3;
            f27sb.setLength(0);
            f27sb.append(degree);
            f27sb.append("/1,");
            f27sb.append(minute);
            f27sb.append("/1,");
            f27sb.append((int) (1000.0d * ((latitude3 * 60.0d) - (((double) minute) * 60.0d))));
            f27sb.append("/1000,");
            sb = f27sb.toString();
        }
        return sb;
    }
}
