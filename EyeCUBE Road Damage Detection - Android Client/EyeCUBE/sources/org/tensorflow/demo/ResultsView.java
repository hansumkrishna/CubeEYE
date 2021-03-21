package org.tensorflow.demo;

import java.util.List;
import org.tensorflow.demo.Classifier;

public interface ResultsView {
    void setResults(List<Classifier.Recognition> list);
}
