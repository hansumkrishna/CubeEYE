package org.tensorflow.mcr.road;

import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class RoadImage {
    private static final int IMAGE_QUARITY = 100;

    public static String getString(Bitmap image) {
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream);
        return Base64.encodeToString(byteArrayBitmapStream.toByteArray(), 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0024, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0025, code lost:
        r5 = r3;
        r3 = r2;
        r2 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0037, code lost:
        r2 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int save(android.graphics.Bitmap r6, java.io.File r7) {
        /*
            java.io.FileOutputStream r1 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x0019 }
            r1.<init>(r7)     // Catch:{ Exception -> 0x0019 }
            r3 = 0
            android.graphics.Bitmap$CompressFormat r2 = android.graphics.Bitmap.CompressFormat.JPEG     // Catch:{ Throwable -> 0x0022, all -> 0x0037 }
            r4 = 100
            r6.compress(r2, r4, r1)     // Catch:{ Throwable -> 0x0022, all -> 0x0037 }
            if (r3 == 0) goto L_0x001e
            r1.close()     // Catch:{ Throwable -> 0x0014 }
        L_0x0012:
            r2 = 0
            return r2
        L_0x0014:
            r2 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r3, r2)     // Catch:{ Exception -> 0x0019 }
            goto L_0x0012
        L_0x0019:
            r0 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r0)
            goto L_0x0012
        L_0x001e:
            r1.close()     // Catch:{ Exception -> 0x0019 }
            goto L_0x0012
        L_0x0022:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r3 = move-exception
            r5 = r3
            r3 = r2
            r2 = r5
        L_0x0028:
            if (r3 == 0) goto L_0x0033
            r1.close()     // Catch:{ Throwable -> 0x002e }
        L_0x002d:
            throw r2     // Catch:{ Exception -> 0x0019 }
        L_0x002e:
            r4 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.addSuppressed(r3, r4)     // Catch:{ Exception -> 0x0019 }
            goto L_0x002d
        L_0x0033:
            r1.close()     // Catch:{ Exception -> 0x0019 }
            goto L_0x002d
        L_0x0037:
            r2 = move-exception
            goto L_0x0028
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.mcr.road.RoadImage.save(android.graphics.Bitmap, java.io.File):int");
    }
}
