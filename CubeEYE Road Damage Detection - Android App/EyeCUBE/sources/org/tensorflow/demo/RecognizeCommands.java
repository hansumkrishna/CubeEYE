package org.tensorflow.demo;

import android.util.Pair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RecognizeCommands {
    private static final long MINIMUM_TIME_FRACTION = 4;
    private static final String SILENCE_LABEL = "_silence_";
    private long averageWindowDurationMs;
    private float detectionThreshold;
    private List<String> labels = new ArrayList();
    private int labelsCount;
    private int minimumCount;
    private long minimumTimeBetweenSamplesMs;
    private Deque<Pair<Long, float[]>> previousResults = new ArrayDeque();
    private String previousTopLabel;
    private float previousTopLabelScore;
    private long previousTopLabelTime;
    private int suppressionMs;

    public RecognizeCommands(List<String> inLabels, long inAverageWindowDurationMs, float inDetectionThreshold, int inSuppressionMS, int inMinimumCount, long inMinimumTimeBetweenSamplesMS) {
        this.labels = inLabels;
        this.averageWindowDurationMs = inAverageWindowDurationMs;
        this.detectionThreshold = inDetectionThreshold;
        this.suppressionMs = inSuppressionMS;
        this.minimumCount = inMinimumCount;
        this.labelsCount = inLabels.size();
        this.previousTopLabel = SILENCE_LABEL;
        this.previousTopLabelTime = Long.MIN_VALUE;
        this.previousTopLabelScore = 0.0f;
        this.minimumTimeBetweenSamplesMs = inMinimumTimeBetweenSamplesMS;
    }

    public static class RecognitionResult {
        public final String foundCommand;
        public final boolean isNewCommand;
        public final float score;

        public RecognitionResult(String inFoundCommand, float inScore, boolean inIsNewCommand) {
            this.foundCommand = inFoundCommand;
            this.score = inScore;
            this.isNewCommand = inIsNewCommand;
        }
    }

    private static class ScoreForSorting implements Comparable<ScoreForSorting> {
        public final int index;
        public final float score;

        public ScoreForSorting(float inScore, int inIndex) {
            this.score = inScore;
            this.index = inIndex;
        }

        public int compareTo(ScoreForSorting other) {
            if (this.score > other.score) {
                return -1;
            }
            if (this.score < other.score) {
                return 1;
            }
            return 0;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v64, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v0, resolved type: float[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.tensorflow.demo.RecognizeCommands.RecognitionResult processLatestResults(float[] r29, long r30) {
        /*
            r28 = this;
            r0 = r29
            int r0 = r0.length
            r24 = r0
            r0 = r28
            int r0 = r0.labelsCount
            r25 = r0
            r0 = r24
            r1 = r25
            if (r0 == r1) goto L_0x003f
            java.lang.RuntimeException r24 = new java.lang.RuntimeException
            java.lang.StringBuilder r25 = new java.lang.StringBuilder
            r25.<init>()
            java.lang.String r26 = "The results for recognition should contain "
            java.lang.StringBuilder r25 = r25.append(r26)
            r0 = r28
            int r0 = r0.labelsCount
            r26 = r0
            java.lang.StringBuilder r25 = r25.append(r26)
            java.lang.String r26 = " elements, but there are "
            java.lang.StringBuilder r25 = r25.append(r26)
            r0 = r29
            int r0 = r0.length
            r26 = r0
            java.lang.StringBuilder r25 = r25.append(r26)
            java.lang.String r25 = r25.toString()
            r24.<init>(r25)
            throw r24
        L_0x003f:
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            boolean r24 = r24.isEmpty()
            if (r24 != 0) goto L_0x00b0
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            java.lang.Object r24 = r24.getFirst()
            android.util.Pair r24 = (android.util.Pair) r24
            r0 = r24
            java.lang.Object r0 = r0.first
            r24 = r0
            java.lang.Long r24 = (java.lang.Long) r24
            long r24 = r24.longValue()
            int r24 = (r30 > r24 ? 1 : (r30 == r24 ? 0 : -1))
            if (r24 >= 0) goto L_0x00b0
            java.lang.RuntimeException r25 = new java.lang.RuntimeException
            java.lang.StringBuilder r24 = new java.lang.StringBuilder
            r24.<init>()
            java.lang.String r26 = "You must feed results in increasing time order, but received a timestamp of "
            r0 = r24
            r1 = r26
            java.lang.StringBuilder r24 = r0.append(r1)
            r0 = r24
            r1 = r30
            java.lang.StringBuilder r24 = r0.append(r1)
            java.lang.String r26 = " that was earlier than the previous one of "
            r0 = r24
            r1 = r26
            java.lang.StringBuilder r26 = r0.append(r1)
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            java.lang.Object r24 = r24.getFirst()
            android.util.Pair r24 = (android.util.Pair) r24
            r0 = r24
            java.lang.Object r0 = r0.first
            r24 = r0
            r0 = r26
            r1 = r24
            java.lang.StringBuilder r24 = r0.append(r1)
            java.lang.String r24 = r24.toString()
            r0 = r25
            r1 = r24
            r0.<init>(r1)
            throw r25
        L_0x00b0:
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            int r10 = r24.size()
            r24 = 1
            r0 = r24
            if (r10 <= r0) goto L_0x00f8
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            java.lang.Object r24 = r24.getLast()
            android.util.Pair r24 = (android.util.Pair) r24
            r0 = r24
            java.lang.Object r0 = r0.first
            r24 = r0
            java.lang.Long r24 = (java.lang.Long) r24
            long r24 = r24.longValue()
            long r22 = r30 - r24
            r0 = r28
            long r0 = r0.minimumTimeBetweenSamplesMs
            r24 = r0
            int r24 = (r22 > r24 ? 1 : (r22 == r24 ? 0 : -1))
            if (r24 >= 0) goto L_0x00f8
            org.tensorflow.demo.RecognizeCommands$RecognitionResult r24 = new org.tensorflow.demo.RecognizeCommands$RecognitionResult
            r0 = r28
            java.lang.String r0 = r0.previousTopLabel
            r25 = r0
            r0 = r28
            float r0 = r0.previousTopLabelScore
            r26 = r0
            r27 = 0
            r24.<init>(r25, r26, r27)
        L_0x00f7:
            return r24
        L_0x00f8:
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            android.util.Pair r25 = new android.util.Pair
            java.lang.Long r26 = java.lang.Long.valueOf(r30)
            r0 = r25
            r1 = r26
            r2 = r29
            r0.<init>(r1, r2)
            r24.addLast(r25)
            r0 = r28
            long r0 = r0.averageWindowDurationMs
            r24 = r0
            long r18 = r30 - r24
        L_0x0118:
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            java.lang.Object r24 = r24.getFirst()
            android.util.Pair r24 = (android.util.Pair) r24
            r0 = r24
            java.lang.Object r0 = r0.first
            r24 = r0
            java.lang.Long r24 = (java.lang.Long) r24
            long r24 = r24.longValue()
            int r24 = (r24 > r18 ? 1 : (r24 == r18 ? 0 : -1))
            if (r24 >= 0) goto L_0x013e
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            r24.removeFirst()
            goto L_0x0118
        L_0x013e:
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            java.lang.Object r24 = r24.getFirst()
            android.util.Pair r24 = (android.util.Pair) r24
            r0 = r24
            java.lang.Object r0 = r0.first
            r24 = r0
            java.lang.Long r24 = (java.lang.Long) r24
            long r8 = r24.longValue()
            long r14 = r30 - r8
            r0 = r28
            int r0 = r0.minimumCount
            r24 = r0
            r0 = r24
            if (r10 < r0) goto L_0x0170
            r0 = r28
            long r0 = r0.averageWindowDurationMs
            r24 = r0
            r26 = 4
            long r24 = r24 / r26
            int r24 = (r14 > r24 ? 1 : (r14 == r24 ? 0 : -1))
            if (r24 >= 0) goto L_0x0188
        L_0x0170:
            java.lang.String r24 = "RecognizeResult"
            java.lang.String r25 = "Too few results"
            android.util.Log.v(r24, r25)
            org.tensorflow.demo.RecognizeCommands$RecognitionResult r24 = new org.tensorflow.demo.RecognizeCommands$RecognitionResult
            r0 = r28
            java.lang.String r0 = r0.previousTopLabel
            r25 = r0
            r26 = 0
            r27 = 0
            r24.<init>(r25, r26, r27)
            goto L_0x00f7
        L_0x0188:
            r0 = r28
            int r0 = r0.labelsCount
            r24 = r0
            r0 = r24
            float[] r4 = new float[r0]
            r0 = r28
            java.util.Deque<android.util.Pair<java.lang.Long, float[]>> r0 = r0.previousResults
            r24 = r0
            java.util.Iterator r24 = r24.iterator()
        L_0x019c:
            boolean r25 = r24.hasNext()
            if (r25 == 0) goto L_0x01c8
            java.lang.Object r13 = r24.next()
            android.util.Pair r13 = (android.util.Pair) r13
            java.lang.Object r0 = r13.second
            r16 = r0
            float[] r16 = (float[]) r16
            r11 = 0
        L_0x01af:
            r0 = r16
            int r0 = r0.length
            r25 = r0
            r0 = r25
            if (r11 >= r0) goto L_0x019c
            r25 = r4[r11]
            r26 = r16[r11]
            float r0 = (float) r10
            r27 = r0
            float r26 = r26 / r27
            float r25 = r25 + r26
            r4[r11] = r25
            int r11 = r11 + 1
            goto L_0x01af
        L_0x01c8:
            r0 = r28
            int r0 = r0.labelsCount
            r24 = r0
            r0 = r24
            org.tensorflow.demo.RecognizeCommands$ScoreForSorting[] r0 = new org.tensorflow.demo.RecognizeCommands.ScoreForSorting[r0]
            r17 = r0
            r11 = 0
        L_0x01d5:
            r0 = r28
            int r0 = r0.labelsCount
            r24 = r0
            r0 = r24
            if (r11 >= r0) goto L_0x01ef
            org.tensorflow.demo.RecognizeCommands$ScoreForSorting r24 = new org.tensorflow.demo.RecognizeCommands$ScoreForSorting
            r25 = r4[r11]
            r0 = r24
            r1 = r25
            r0.<init>(r1, r11)
            r17[r11] = r24
            int r11 = r11 + 1
            goto L_0x01d5
        L_0x01ef:
            java.util.Arrays.sort(r17)
            r24 = 0
            r24 = r17[r24]
            r0 = r24
            int r5 = r0.index
            r0 = r28
            java.util.List<java.lang.String> r0 = r0.labels
            r24 = r0
            r0 = r24
            java.lang.Object r6 = r0.get(r5)
            java.lang.String r6 = (java.lang.String) r6
            r24 = 0
            r24 = r17[r24]
            r0 = r24
            float r7 = r0.score
            r0 = r28
            java.lang.String r0 = r0.previousTopLabel
            r24 = r0
            java.lang.String r25 = "_silence_"
            boolean r24 = r24.equals(r25)
            if (r24 != 0) goto L_0x022a
            r0 = r28
            long r0 = r0.previousTopLabelTime
            r24 = r0
            r26 = -9223372036854775808
            int r24 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1))
            if (r24 != 0) goto L_0x0260
        L_0x022a:
            r20 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
        L_0x022f:
            r0 = r28
            float r0 = r0.detectionThreshold
            r24 = r0
            int r24 = (r7 > r24 ? 1 : (r7 == r24 ? 0 : -1))
            if (r24 <= 0) goto L_0x0269
            r0 = r28
            int r0 = r0.suppressionMs
            r24 = r0
            r0 = r24
            long r0 = (long) r0
            r24 = r0
            int r24 = (r20 > r24 ? 1 : (r20 == r24 ? 0 : -1))
            if (r24 <= 0) goto L_0x0269
            r0 = r28
            r0.previousTopLabel = r6
            r0 = r30
            r2 = r28
            r2.previousTopLabelTime = r0
            r0 = r28
            r0.previousTopLabelScore = r7
            r12 = 1
        L_0x0257:
            org.tensorflow.demo.RecognizeCommands$RecognitionResult r24 = new org.tensorflow.demo.RecognizeCommands$RecognitionResult
            r0 = r24
            r0.<init>(r6, r7, r12)
            goto L_0x00f7
        L_0x0260:
            r0 = r28
            long r0 = r0.previousTopLabelTime
            r24 = r0
            long r20 = r30 - r24
            goto L_0x022f
        L_0x0269:
            r12 = 0
            goto L_0x0257
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.demo.RecognizeCommands.processLatestResults(float[], long):org.tensorflow.demo.RecognizeCommands$RecognitionResult");
    }
}
