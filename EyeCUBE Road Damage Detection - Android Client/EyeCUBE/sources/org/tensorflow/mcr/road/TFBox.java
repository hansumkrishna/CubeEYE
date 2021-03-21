package org.tensorflow.mcr.road;

import org.tensorflow.demo.Classifier;

public class TFBox {
    public Classifier detector;
    public ConvImage image;

    public TFBox(ConvImage image2, Classifier detector2) {
        this.image = image2;
        this.detector = detector2;
    }
}
