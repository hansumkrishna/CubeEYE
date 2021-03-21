package org.tensorflow;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Tensors {
    private Tensors() {
    }

    public static Tensor<String> create(String data) {
        return Tensor.create((Object) data.getBytes(StandardCharsets.UTF_8), String.class);
    }

    public static Tensor<String> create(String data, Charset charset) {
        return Tensor.create((Object) data.getBytes(charset), String.class);
    }

    public static Tensor<Float> create(float data) {
        return Tensor.create((Object) Float.valueOf(data), Float.class);
    }

    public static Tensor<Float> create(float[] data) {
        return Tensor.create((Object) data, Float.class);
    }

    public static Tensor<Float> create(float[][] data) {
        return Tensor.create((Object) data, Float.class);
    }

    public static Tensor<Float> create(float[][][] data) {
        return Tensor.create((Object) data, Float.class);
    }

    public static Tensor<Float> create(float[][][][] data) {
        return Tensor.create((Object) data, Float.class);
    }

    public static Tensor<Float> create(float[][][][][] data) {
        return Tensor.create((Object) data, Float.class);
    }

    public static Tensor<Float> create(float[][][][][][] data) {
        return Tensor.create((Object) data, Float.class);
    }

    public static Tensor<Double> create(double data) {
        return Tensor.create((Object) Double.valueOf(data), Double.class);
    }

    public static Tensor<Double> create(double[] data) {
        return Tensor.create((Object) data, Double.class);
    }

    public static Tensor<Double> create(double[][] data) {
        return Tensor.create((Object) data, Double.class);
    }

    public static Tensor<Double> create(double[][][] data) {
        return Tensor.create((Object) data, Double.class);
    }

    public static Tensor<Double> create(double[][][][] data) {
        return Tensor.create((Object) data, Double.class);
    }

    public static Tensor<Double> create(double[][][][][] data) {
        return Tensor.create((Object) data, Double.class);
    }

    public static Tensor<Double> create(double[][][][][][] data) {
        return Tensor.create((Object) data, Double.class);
    }

    public static Tensor<Integer> create(int data) {
        return Tensor.create((Object) Integer.valueOf(data), Integer.class);
    }

    public static Tensor<Integer> create(int[] data) {
        return Tensor.create((Object) data, Integer.class);
    }

    public static Tensor<Integer> create(int[][] data) {
        return Tensor.create((Object) data, Integer.class);
    }

    public static Tensor<Integer> create(int[][][] data) {
        return Tensor.create((Object) data, Integer.class);
    }

    public static Tensor<Integer> create(int[][][][] data) {
        return Tensor.create((Object) data, Integer.class);
    }

    public static Tensor<Integer> create(int[][][][][] data) {
        return Tensor.create((Object) data, Integer.class);
    }

    public static Tensor<Integer> create(int[][][][][][] data) {
        return Tensor.create((Object) data, Integer.class);
    }

    public static Tensor<String> create(byte[] data) {
        return Tensor.create((Object) data, String.class);
    }

    public static Tensor<String> create(byte[][] data) {
        return Tensor.create((Object) data, String.class);
    }

    public static Tensor<String> create(byte[][][] data) {
        return Tensor.create((Object) data, String.class);
    }

    public static Tensor<String> create(byte[][][][] data) {
        return Tensor.create((Object) data, String.class);
    }

    public static Tensor<String> create(byte[][][][][] data) {
        return Tensor.create((Object) data, String.class);
    }

    public static Tensor<String> create(byte[][][][][][] data) {
        return Tensor.create((Object) data, String.class);
    }

    public static Tensor<Long> create(long data) {
        return Tensor.create((Object) Long.valueOf(data), Long.class);
    }

    public static Tensor<Long> create(long[] data) {
        return Tensor.create((Object) data, Long.class);
    }

    public static Tensor<Long> create(long[][] data) {
        return Tensor.create((Object) data, Long.class);
    }

    public static Tensor<Long> create(long[][][] data) {
        return Tensor.create((Object) data, Long.class);
    }

    public static Tensor<Long> create(long[][][][] data) {
        return Tensor.create((Object) data, Long.class);
    }

    public static Tensor<Long> create(long[][][][][] data) {
        return Tensor.create((Object) data, Long.class);
    }

    public static Tensor<Long> create(long[][][][][][] data) {
        return Tensor.create((Object) data, Long.class);
    }

    public static Tensor<Boolean> create(boolean data) {
        return Tensor.create((Object) Boolean.valueOf(data), Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[] data) {
        return Tensor.create((Object) data, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][] data) {
        return Tensor.create((Object) data, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][] data) {
        return Tensor.create((Object) data, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][][] data) {
        return Tensor.create((Object) data, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][][][] data) {
        return Tensor.create((Object) data, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][][][][] data) {
        return Tensor.create((Object) data, Boolean.class);
    }
}
