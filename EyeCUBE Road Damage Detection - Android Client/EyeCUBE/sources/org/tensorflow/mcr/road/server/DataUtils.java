package org.tensorflow.mcr.road.server;

import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DataUtils {
    public static void eraseFiles(List<File> files) {
        for (File file : files) {
            try {
                file.delete();
            } catch (Exception e) {
                ThrowableExtension.printStackTrace(e);
            }
        }
    }

    public static void eraseDirctory(File dir) {
        for (File e : dir.listFiles()) {
            if (e.isDirectory() && e.listFiles().length == 0) {
                e.delete();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003e, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003f, code lost:
        r10 = r8;
        r8 = r7;
        r7 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        r7 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static org.json.JSONArray loadDamages(java.util.List<java.io.File> r11) {
        /*
            org.json.JSONArray r1 = new org.json.JSONArray
            r1.<init>()
            java.util.Iterator r9 = r11.iterator()
        L_0x0009:
            boolean r7 = r9.hasNext()
            if (r7 == 0) goto L_0x0046
            java.lang.Object r3 = r9.next()
            java.io.File r3 = (java.io.File) r3
            java.io.FileInputStream r4 = new java.io.FileInputStream     // Catch:{ Exception -> 0x0037 }
            r4.<init>(r3)     // Catch:{ Exception -> 0x0037 }
            r8 = 0
            int r6 = r4.available()     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            byte[] r0 = new byte[r6]     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            r4.read(r0)     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            java.lang.String r5 = new java.lang.String     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            java.lang.String r7 = "UTF-8"
            r5.<init>(r0, r7)     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            org.json.JSONObject r7 = new org.json.JSONObject     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            r7.<init>(r5)     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            r1.put(r7)     // Catch:{ Throwable -> 0x003c, all -> 0x0047 }
            $closeResource((java.lang.Throwable) r8, (java.io.FileInputStream) r4)     // Catch:{ Exception -> 0x0037 }
            goto L_0x0009
        L_0x0037:
            r2 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r2)
            goto L_0x0009
        L_0x003c:
            r7 = move-exception
            throw r7     // Catch:{ all -> 0x003e }
        L_0x003e:
            r8 = move-exception
            r10 = r8
            r8 = r7
            r7 = r10
        L_0x0042:
            $closeResource((java.lang.Throwable) r8, (java.io.InputStream) r4)     // Catch:{ Exception -> 0x0037 }
            throw r7     // Catch:{ Exception -> 0x0037 }
        L_0x0046:
            return r1
        L_0x0047:
            r7 = move-exception
            goto L_0x0042
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.mcr.road.server.DataUtils.loadDamages(java.util.List):org.json.JSONArray");
    }

    private static /* synthetic */ void $closeResource(Throwable x0, BufferedReader x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                ThrowableExtension.addSuppressed(x0, th);
            }
        } else {
            x1.close();
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, FileInputStream x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                ThrowableExtension.addSuppressed(x0, th);
            }
        } else {
            x1.close();
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, InputStream x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                ThrowableExtension.addSuppressed(x0, th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004c, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004d, code lost:
        r12 = r10;
        r10 = r9;
        r9 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0055, code lost:
        r9 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static org.json.JSONArray loadLocations(java.util.List<java.io.File> r13) {
        /*
            org.json.JSONArray r2 = new org.json.JSONArray
            r2.<init>()
            java.util.Iterator r11 = r13.iterator()
        L_0x0009:
            boolean r9 = r11.hasNext()
            if (r9 == 0) goto L_0x0054
            java.lang.Object r4 = r11.next()
            java.io.File r4 = (java.io.File) r4
            java.io.FileInputStream r6 = new java.io.FileInputStream     // Catch:{ Exception -> 0x0045 }
            r6.<init>(r4)     // Catch:{ Exception -> 0x0045 }
            r10 = 0
            int r8 = r6.available()     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            byte[] r1 = new byte[r8]     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            r6.read(r1)     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            java.lang.String r7 = new java.lang.String     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            java.lang.String r9 = "UTF-8"
            r7.<init>(r1, r9)     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            org.json.JSONArray r0 = new org.json.JSONArray     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            r0.<init>(r7)     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            r5 = 0
        L_0x0031:
            int r9 = r0.length()     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            if (r5 >= r9) goto L_0x0041
            java.lang.Object r9 = r0.get(r5)     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            r2.put(r9)     // Catch:{ Throwable -> 0x004a, all -> 0x0055 }
            int r5 = r5 + 1
            goto L_0x0031
        L_0x0041:
            $closeResource((java.lang.Throwable) r10, (java.io.InputStream) r6)     // Catch:{ Exception -> 0x0045 }
            goto L_0x0009
        L_0x0045:
            r3 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r3)
            goto L_0x0009
        L_0x004a:
            r9 = move-exception
            throw r9     // Catch:{ all -> 0x004c }
        L_0x004c:
            r10 = move-exception
            r12 = r10
            r10 = r9
            r9 = r12
        L_0x0050:
            $closeResource((java.lang.Throwable) r10, (java.io.InputStream) r6)     // Catch:{ Exception -> 0x0045 }
            throw r9     // Catch:{ Exception -> 0x0045 }
        L_0x0054:
            return r2
        L_0x0055:
            r9 = move-exception
            goto L_0x0050
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.mcr.road.server.DataUtils.loadLocations(java.util.List):org.json.JSONArray");
    }

    public static List<File> getListDamageFiles(File dir, String prefix) {
        List<File> list = new ArrayList<>();
        Pattern patter = Pattern.compile(String.format("^%s.*json", new Object[]{prefix}));
        if (dir.exists()) {
            for (File e : dir.listFiles()) {
                if (e.isFile()) {
                    if (patter.matcher(e.getName()).matches() && e.canRead()) {
                        list.add(e);
                    }
                } else if (e.isDirectory()) {
                    list.addAll(getListDamageFiles(e, prefix));
                }
            }
        }
        return list;
    }

    public static List<File> getListLocationFiles(File dir, String prefix) {
        List<File> list = new ArrayList<>();
        Pattern patter = Pattern.compile(String.format("^%s.*json", new Object[]{prefix}));
        if (dir.exists()) {
            for (File e : dir.listFiles()) {
                if (e.isFile()) {
                    if (patter.matcher(e.getName()).matches() && e.canRead()) {
                        list.add(e);
                    }
                } else if (e.isDirectory()) {
                    list.addAll(getListLocationFiles(e, prefix));
                }
            }
        }
        return list;
    }

    /* JADX WARNING: type inference failed for: r7v3, types: [java.net.URLConnection] */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0049, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004a, code lost:
        r9 = r8;
        r8 = r7;
        r7 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        $closeResource((java.lang.Throwable) null, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0074, code lost:
        r7 = th;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String downloadString(java.lang.String r10) {
        /*
            r2 = 0
            java.lang.StringBuffer r6 = new java.lang.StringBuffer
            r6.<init>()
            java.net.URL r7 = new java.net.URL     // Catch:{ Exception -> 0x006a }
            r7.<init>(r10)     // Catch:{ Exception -> 0x006a }
            java.net.URLConnection r7 = r7.openConnection()     // Catch:{ Exception -> 0x006a }
            r0 = r7
            java.net.HttpURLConnection r0 = (java.net.HttpURLConnection) r0     // Catch:{ Exception -> 0x006a }
            r2 = r0
            java.lang.String r7 = "GET"
            r2.setRequestMethod(r7)     // Catch:{ Exception -> 0x006a }
            r7 = 3000(0xbb8, float:4.204E-42)
            r2.setConnectTimeout(r7)     // Catch:{ Exception -> 0x006a }
            r7 = 3000(0xbb8, float:4.204E-42)
            r2.setReadTimeout(r7)     // Catch:{ Exception -> 0x006a }
            r2.connect()     // Catch:{ Exception -> 0x006a }
            int r5 = r2.getResponseCode()     // Catch:{ Exception -> 0x006a }
            r7 = 200(0xc8, float:2.8E-43)
            if (r5 != r7) goto L_0x0055
            java.io.BufferedReader r1 = new java.io.BufferedReader     // Catch:{ Exception -> 0x0051 }
            java.io.InputStreamReader r7 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x0051 }
            java.io.InputStream r8 = r2.getInputStream()     // Catch:{ Exception -> 0x0051 }
            r7.<init>(r8)     // Catch:{ Exception -> 0x0051 }
            r1.<init>(r7)     // Catch:{ Exception -> 0x0051 }
            r8 = 0
            r4 = 0
        L_0x003d:
            java.lang.String r4 = r1.readLine()     // Catch:{ Throwable -> 0x0047, all -> 0x0074 }
            if (r4 == 0) goto L_0x005f
            r6.append(r4)     // Catch:{ Throwable -> 0x0047, all -> 0x0074 }
            goto L_0x003d
        L_0x0047:
            r7 = move-exception
            throw r7     // Catch:{ all -> 0x0049 }
        L_0x0049:
            r8 = move-exception
            r9 = r8
            r8 = r7
            r7 = r9
        L_0x004d:
            $closeResource((java.lang.Throwable) r8, (java.io.BufferedReader) r1)     // Catch:{ Exception -> 0x0051 }
            throw r7     // Catch:{ Exception -> 0x0051 }
        L_0x0051:
            r3 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r3)     // Catch:{ Exception -> 0x006a }
        L_0x0055:
            if (r2 == 0) goto L_0x005a
            r2.disconnect()
        L_0x005a:
            java.lang.String r7 = r6.toString()
            return r7
        L_0x005f:
            $closeResource((java.lang.Throwable) r8, (java.io.BufferedReader) r1)     // Catch:{ Exception -> 0x0051 }
            goto L_0x0055
        L_0x0063:
            r7 = move-exception
            if (r2 == 0) goto L_0x0069
            r2.disconnect()
        L_0x0069:
            throw r7
        L_0x006a:
            r3 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r3)     // Catch:{ all -> 0x0063 }
            if (r2 == 0) goto L_0x005a
            r2.disconnect()
            goto L_0x005a
        L_0x0074:
            r7 = move-exception
            goto L_0x004d
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.mcr.road.server.DataUtils.downloadString(java.lang.String):java.lang.String");
    }

    /* JADX WARNING: type inference failed for: r7v4, types: [java.net.URLConnection] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean uploadJson(java.lang.String r9, java.lang.String r10) {
        /*
            r1 = 0
            r4 = 0
            java.net.URL r7 = new java.net.URL     // Catch:{ Exception -> 0x0070 }
            r7.<init>(r9)     // Catch:{ Exception -> 0x0070 }
            java.net.URLConnection r7 = r7.openConnection()     // Catch:{ Exception -> 0x0070 }
            r0 = r7
            java.net.HttpURLConnection r0 = (java.net.HttpURLConnection) r0     // Catch:{ Exception -> 0x0070 }
            r1 = r0
            if (r1 == 0) goto L_0x0060
            r7 = 1
            r1.setDoOutput(r7)     // Catch:{ Exception -> 0x0070 }
            r7 = 1
            r1.setDoInput(r7)     // Catch:{ Exception -> 0x0070 }
            java.lang.String r7 = "POST"
            r1.setRequestMethod(r7)     // Catch:{ Exception -> 0x0070 }
            r7 = 20000(0x4e20, float:2.8026E-41)
            r1.setConnectTimeout(r7)     // Catch:{ Exception -> 0x0070 }
            java.lang.String r7 = "Content-Type"
            java.lang.String r8 = "application/json"
            r1.setRequestProperty(r7, r8)     // Catch:{ Exception -> 0x0070 }
            r1.connect()     // Catch:{ Exception -> 0x0070 }
            java.io.OutputStream r3 = r1.getOutputStream()     // Catch:{ Exception -> 0x0070 }
            java.io.BufferedWriter r6 = new java.io.BufferedWriter     // Catch:{ Exception -> 0x0070 }
            java.io.OutputStreamWriter r7 = new java.io.OutputStreamWriter     // Catch:{ Exception -> 0x0070 }
            java.lang.String r8 = "UTF-8"
            r7.<init>(r3, r8)     // Catch:{ Exception -> 0x0070 }
            r6.<init>(r7)     // Catch:{ Exception -> 0x0070 }
            r6.write(r10)     // Catch:{ IOException -> 0x0066 }
            r6.flush()     // Catch:{ IOException -> 0x0066 }
            if (r6 == 0) goto L_0x0048
            r6.close()     // Catch:{ Exception -> 0x0070 }
        L_0x0048:
            int r5 = r1.getResponseCode()     // Catch:{ Exception -> 0x0070 }
            r7 = 200(0xc8, float:2.8E-43)
            if (r5 != r7) goto L_0x0051
            r4 = 1
        L_0x0051:
            java.lang.String r7 = "JSONRESPONSE"
            java.lang.String r8 = r1.getResponseMessage()     // Catch:{ Exception -> 0x0070 }
            android.util.Log.e(r7, r8)     // Catch:{ Exception -> 0x0070 }
            r6.close()     // Catch:{ Exception -> 0x0070 }
            r3.close()     // Catch:{ Exception -> 0x0070 }
        L_0x0060:
            if (r1 == 0) goto L_0x0065
            r1.disconnect()     // Catch:{ Exception -> 0x008f }
        L_0x0065:
            return r4
        L_0x0066:
            r2 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r2)     // Catch:{ all -> 0x0081 }
            if (r6 == 0) goto L_0x0048
            r6.close()     // Catch:{ Exception -> 0x0070 }
            goto L_0x0048
        L_0x0070:
            r2 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r2)     // Catch:{ all -> 0x0088 }
            java.lang.String r7 = "Error"
            android.util.Log.e(r7, r9)     // Catch:{ all -> 0x0088 }
            if (r1 == 0) goto L_0x0065
            r1.disconnect()     // Catch:{ Exception -> 0x007f }
            goto L_0x0065
        L_0x007f:
            r7 = move-exception
            goto L_0x0065
        L_0x0081:
            r7 = move-exception
            if (r6 == 0) goto L_0x0087
            r6.close()     // Catch:{ Exception -> 0x0070 }
        L_0x0087:
            throw r7     // Catch:{ Exception -> 0x0070 }
        L_0x0088:
            r7 = move-exception
            if (r1 == 0) goto L_0x008e
            r1.disconnect()     // Catch:{ Exception -> 0x0091 }
        L_0x008e:
            throw r7
        L_0x008f:
            r7 = move-exception
            goto L_0x0065
        L_0x0091:
            r8 = move-exception
            goto L_0x008e
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.mcr.road.server.DataUtils.uploadJson(java.lang.String, java.lang.String):boolean");
    }
}
