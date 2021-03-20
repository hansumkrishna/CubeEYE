package android.support.v13.view.inputmethod;

import android.annotation.TargetApi;
import android.content.ClipDescription;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.p000v4.p002os.BuildCompat;
import android.support.v13.view.inputmethod.InputConnectionCompatApi25;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

@TargetApi(13)
@RequiresApi(13)
public final class InputConnectionCompat {
    private static final InputConnectionCompatImpl IMPL;
    public static int INPUT_CONTENT_GRANT_READ_URI_PERMISSION = 1;

    private interface InputConnectionCompatImpl {
        boolean commitContent(@NonNull InputConnection inputConnection, @NonNull InputContentInfoCompat inputContentInfoCompat, int i, @Nullable Bundle bundle);

        @NonNull
        InputConnection createWrapper(@NonNull InputConnection inputConnection, @NonNull EditorInfo editorInfo, @NonNull OnCommitContentListener onCommitContentListener);
    }

    public interface OnCommitContentListener {
        boolean onCommitContent(InputContentInfoCompat inputContentInfoCompat, int i, Bundle bundle);
    }

    static final class BaseInputContentInfoCompatImpl implements InputConnectionCompatImpl {
        private static String COMMIT_CONTENT_ACTION = "android.support.v13.view.inputmethod.InputConnectionCompat.COMMIT_CONTENT";
        private static String COMMIT_CONTENT_CONTENT_URI_KEY = "android.support.v13.view.inputmethod.InputConnectionCompat.CONTENT_URI";
        private static String COMMIT_CONTENT_DESCRIPTION_KEY = "android.support.v13.view.inputmethod.InputConnectionCompat.CONTENT_DESCRIPTION";
        private static String COMMIT_CONTENT_FLAGS_KEY = "android.support.v13.view.inputmethod.InputConnectionCompat.CONTENT_FLAGS";
        private static String COMMIT_CONTENT_LINK_URI_KEY = "android.support.v13.view.inputmethod.InputConnectionCompat.CONTENT_LINK_URI";
        private static String COMMIT_CONTENT_OPTS_KEY = "android.support.v13.view.inputmethod.InputConnectionCompat.CONTENT_OPTS";
        private static String COMMIT_CONTENT_RESULT_RECEIVER = "android.support.v13.view.inputmethod.InputConnectionCompat.CONTENT_RESULT_RECEIVER";

        BaseInputContentInfoCompatImpl() {
        }

        public boolean commitContent(@NonNull InputConnection inputConnection, @NonNull InputContentInfoCompat inputContentInfo, int flags, @Nullable Bundle opts) {
            Bundle params = new Bundle();
            params.putParcelable(COMMIT_CONTENT_CONTENT_URI_KEY, inputContentInfo.getContentUri());
            params.putParcelable(COMMIT_CONTENT_DESCRIPTION_KEY, inputContentInfo.getDescription());
            params.putParcelable(COMMIT_CONTENT_LINK_URI_KEY, inputContentInfo.getLinkUri());
            params.putInt(COMMIT_CONTENT_FLAGS_KEY, flags);
            params.putParcelable(COMMIT_CONTENT_OPTS_KEY, opts);
            return inputConnection.performPrivateCommand(COMMIT_CONTENT_ACTION, params);
        }

        @NonNull
        public InputConnection createWrapper(@NonNull InputConnection ic, @NonNull EditorInfo editorInfo, @NonNull OnCommitContentListener onCommitContentListener) {
            if (EditorInfoCompat.getContentMimeTypes(editorInfo).length == 0) {
                return ic;
            }
            final OnCommitContentListener listener = onCommitContentListener;
            return new InputConnectionWrapper(ic, false) {
                public boolean performPrivateCommand(String action, Bundle data) {
                    if (BaseInputContentInfoCompatImpl.handlePerformPrivateCommand(action, data, listener)) {
                        return true;
                    }
                    return super.performPrivateCommand(action, data);
                }
            };
        }

        /* JADX WARNING: type inference failed for: r9v4, types: [android.os.Parcelable] */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static boolean handlePerformPrivateCommand(@android.support.annotation.Nullable java.lang.String r13, @android.support.annotation.NonNull android.os.Bundle r14, @android.support.annotation.NonNull android.support.v13.view.inputmethod.InputConnectionCompat.OnCommitContentListener r15) {
            /*
                r12 = 0
                r10 = 1
                r11 = 0
                java.lang.String r9 = COMMIT_CONTENT_ACTION
                boolean r9 = android.text.TextUtils.equals(r9, r13)
                if (r9 != 0) goto L_0x000c
            L_0x000b:
                return r11
            L_0x000c:
                if (r14 == 0) goto L_0x000b
                r8 = 0
                r7 = 0
                java.lang.String r9 = COMMIT_CONTENT_RESULT_RECEIVER     // Catch:{ all -> 0x0055 }
                android.os.Parcelable r9 = r14.getParcelable(r9)     // Catch:{ all -> 0x0055 }
                r0 = r9
                android.os.ResultReceiver r0 = (android.os.ResultReceiver) r0     // Catch:{ all -> 0x0055 }
                r8 = r0
                java.lang.String r9 = COMMIT_CONTENT_CONTENT_URI_KEY     // Catch:{ all -> 0x0055 }
                android.os.Parcelable r1 = r14.getParcelable(r9)     // Catch:{ all -> 0x0055 }
                android.net.Uri r1 = (android.net.Uri) r1     // Catch:{ all -> 0x0055 }
                java.lang.String r9 = COMMIT_CONTENT_DESCRIPTION_KEY     // Catch:{ all -> 0x0055 }
                android.os.Parcelable r2 = r14.getParcelable(r9)     // Catch:{ all -> 0x0055 }
                android.content.ClipDescription r2 = (android.content.ClipDescription) r2     // Catch:{ all -> 0x0055 }
                java.lang.String r9 = COMMIT_CONTENT_LINK_URI_KEY     // Catch:{ all -> 0x0055 }
                android.os.Parcelable r5 = r14.getParcelable(r9)     // Catch:{ all -> 0x0055 }
                android.net.Uri r5 = (android.net.Uri) r5     // Catch:{ all -> 0x0055 }
                java.lang.String r9 = COMMIT_CONTENT_FLAGS_KEY     // Catch:{ all -> 0x0055 }
                int r3 = r14.getInt(r9)     // Catch:{ all -> 0x0055 }
                java.lang.String r9 = COMMIT_CONTENT_OPTS_KEY     // Catch:{ all -> 0x0055 }
                android.os.Parcelable r6 = r14.getParcelable(r9)     // Catch:{ all -> 0x0055 }
                android.os.Bundle r6 = (android.os.Bundle) r6     // Catch:{ all -> 0x0055 }
                android.support.v13.view.inputmethod.InputContentInfoCompat r4 = new android.support.v13.view.inputmethod.InputContentInfoCompat     // Catch:{ all -> 0x0055 }
                r4.<init>(r1, r2, r5)     // Catch:{ all -> 0x0055 }
                boolean r7 = r15.onCommitContent(r4, r3, r6)     // Catch:{ all -> 0x0055 }
                if (r8 == 0) goto L_0x0051
                if (r7 == 0) goto L_0x0053
                r9 = r10
            L_0x004e:
                r8.send(r9, r12)
            L_0x0051:
                r11 = r7
                goto L_0x000b
            L_0x0053:
                r9 = r11
                goto L_0x004e
            L_0x0055:
                r9 = move-exception
                if (r8 == 0) goto L_0x005d
                if (r7 == 0) goto L_0x005e
            L_0x005a:
                r8.send(r10, r12)
            L_0x005d:
                throw r9
            L_0x005e:
                r10 = r11
                goto L_0x005a
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v13.view.inputmethod.InputConnectionCompat.BaseInputContentInfoCompatImpl.handlePerformPrivateCommand(java.lang.String, android.os.Bundle, android.support.v13.view.inputmethod.InputConnectionCompat$OnCommitContentListener):boolean");
        }
    }

    private static final class Api25InputContentInfoCompatImpl implements InputConnectionCompatImpl {
        private Api25InputContentInfoCompatImpl() {
        }

        public boolean commitContent(@NonNull InputConnection inputConnection, @NonNull InputContentInfoCompat inputContentInfo, int flags, @Nullable Bundle opts) {
            return InputConnectionCompatApi25.commitContent(inputConnection, inputContentInfo.unwrap(), flags, opts);
        }

        @Nullable
        public InputConnection createWrapper(@Nullable InputConnection inputConnection, @NonNull EditorInfo editorInfo, @Nullable OnCommitContentListener onCommitContentListener) {
            final OnCommitContentListener listener = onCommitContentListener;
            return InputConnectionCompatApi25.createWrapper(inputConnection, new InputConnectionCompatApi25.OnCommitContentListener() {
                public boolean onCommitContent(Object inputContentInfo, int flags, Bundle opts) {
                    return listener.onCommitContent(InputContentInfoCompat.wrap(inputContentInfo), flags, opts);
                }
            });
        }
    }

    static {
        if (BuildCompat.isAtLeastNMR1()) {
            IMPL = new Api25InputContentInfoCompatImpl();
        } else {
            IMPL = new BaseInputContentInfoCompatImpl();
        }
    }

    public static boolean commitContent(@NonNull InputConnection inputConnection, @NonNull EditorInfo editorInfo, @NonNull InputContentInfoCompat inputContentInfo, int flags, @Nullable Bundle opts) {
        ClipDescription description = inputContentInfo.getDescription();
        boolean supported = false;
        String[] contentMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
        int length = contentMimeTypes.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            } else if (description.hasMimeType(contentMimeTypes[i])) {
                supported = true;
                break;
            } else {
                i++;
            }
        }
        if (!supported) {
            return false;
        }
        return IMPL.commitContent(inputConnection, inputContentInfo, flags, opts);
    }

    @NonNull
    public static InputConnection createWrapper(@NonNull InputConnection inputConnection, @NonNull EditorInfo editorInfo, @NonNull OnCommitContentListener onCommitContentListener) {
        if (inputConnection == null) {
            throw new IllegalArgumentException("inputConnection must be non-null");
        } else if (editorInfo == null) {
            throw new IllegalArgumentException("editorInfo must be non-null");
        } else if (onCommitContentListener != null) {
            return IMPL.createWrapper(inputConnection, editorInfo, onCommitContentListener);
        } else {
            throw new IllegalArgumentException("onCommitContentListener must be non-null");
        }
    }
}
