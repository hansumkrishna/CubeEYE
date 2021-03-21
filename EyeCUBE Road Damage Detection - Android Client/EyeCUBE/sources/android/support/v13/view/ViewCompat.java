package android.support.v13.view;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.support.annotation.RequiresApi;
import android.support.p000v4.p002os.BuildCompat;
import android.view.View;

@TargetApi(13)
@RequiresApi(13)
public class ViewCompat extends android.support.p000v4.view.ViewCompat {
    static ViewCompatImpl IMPL;

    interface ViewCompatImpl {
        void cancelDragAndDrop(View view);

        boolean startDragAndDrop(View view, ClipData clipData, View.DragShadowBuilder dragShadowBuilder, Object obj, int i);

        void updateDragShadow(View view, View.DragShadowBuilder dragShadowBuilder);
    }

    private static class BaseViewCompatImpl implements ViewCompatImpl {
        BaseViewCompatImpl() {
        }

        public boolean startDragAndDrop(View v, ClipData data, View.DragShadowBuilder shadowBuilder, Object localState, int flags) {
            return v.startDrag(data, shadowBuilder, localState, flags);
        }

        public void cancelDragAndDrop(View v) {
        }

        public void updateDragShadow(View v, View.DragShadowBuilder shadowBuilder) {
        }
    }

    private static class Api24ViewCompatImpl implements ViewCompatImpl {
        Api24ViewCompatImpl() {
        }

        public boolean startDragAndDrop(View v, ClipData data, View.DragShadowBuilder shadowBuilder, Object localState, int flags) {
            return ViewCompatApi24.startDragAndDrop(v, data, shadowBuilder, localState, flags);
        }

        public void cancelDragAndDrop(View v) {
            ViewCompatApi24.cancelDragAndDrop(v);
        }

        public void updateDragShadow(View v, View.DragShadowBuilder shadowBuilder) {
            ViewCompatApi24.updateDragShadow(v, shadowBuilder);
        }
    }

    static {
        if (BuildCompat.isAtLeastN()) {
            IMPL = new Api24ViewCompatImpl();
        } else {
            IMPL = new BaseViewCompatImpl();
        }
    }

    public static boolean startDragAndDrop(View v, ClipData data, View.DragShadowBuilder shadowBuilder, Object localState, int flags) {
        return IMPL.startDragAndDrop(v, data, shadowBuilder, localState, flags);
    }

    public static void cancelDragAndDrop(View v) {
        IMPL.cancelDragAndDrop(v);
    }

    public static void updateDragShadow(View v, View.DragShadowBuilder shadowBuilder) {
        IMPL.updateDragShadow(v, shadowBuilder);
    }

    private ViewCompat() {
    }
}
