package net.yslibrary.android.keyboardvisibilityevent;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

/**
 * Created by yshrsmz on 15/03/17.
 */
public class KeyboardVisibilityEvent {

    private final static int KEYBOARD_VISIBLE_THRESHOLD_DP = 100;

    /**
     * Set keyboard visibility change event listener.
     *
     * @param activity Activity
     * @param listener KeyboardVisibilityEventListener
     */
    public static UnbindKeyboard setEventListener(final Activity activity,
                                        final KeyboardVisibilityEventListener listener) {
        return setEventListener(getActivityRoot(activity), listener);
    }

    /**
     * Set keyboard visibility change event listener.
     *
     * @param rootView View
     * @param listener KeyboardVisibilityEventListener
     */
    public static UnbindKeyboard setEventListener(final View rootView,
                                        final KeyboardVisibilityEventListener listener) {

        if (rootView == null) {
            throw new NullPointerException("Parameter:activity must not be null");
        }

        if (listener == null) {
            throw new NullPointerException("Parameter:listener must not be null");
        }

        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener =
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    private final Rect r = new Rect();

                    private final int visibleThreshold = Math.round(
                            UIUtil.convertDpToPx(rootView.getContext(), KEYBOARD_VISIBLE_THRESHOLD_DP));

                    private boolean wasOpened = false;

                    @Override
                    public void onGlobalLayout() {
                        rootView.getWindowVisibleDisplayFrame(r);

                        int heightDiff = rootView.getRootView().getHeight() - r.height();

                        boolean isOpen = heightDiff > visibleThreshold;

                        if (isOpen == wasOpened) {
                            // keyboard state has not changed
                            return;
                        }

                        wasOpened = isOpen;

                        listener.onVisibilityChanged(isOpen);
                    }
                };

        final UnbindKeyboard unbindKeyboard = new UnbindKeyboard(rootView, onGlobalLayoutListener);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        return unbindKeyboard;
    }

    /**
     * Determine if keyboard is visible
     *
     * @param activity Activity
     * @return Whether keyboard is visible or not
     */
    public static boolean isKeyboardVisible(Activity activity) {
        Rect r = new Rect();

        View activityRoot = getActivityRoot(activity);
        int visibleThreshold = Math
                .round(UIUtil.convertDpToPx(activity, KEYBOARD_VISIBLE_THRESHOLD_DP));

        activityRoot.getWindowVisibleDisplayFrame(r);

        int heightDiff = activityRoot.getRootView().getHeight() - r.height();

        return heightDiff > visibleThreshold;
    }

    private static View getActivityRoot(Activity activity) {
        return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    }

    public static class UnbindKeyboard{
        private View rootView;
        private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

        private UnbindKeyboard(View rootView,
                               ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
            this.rootView = rootView;
            this.onGlobalLayoutListener = onGlobalLayoutListener;
        }

        public void unbind() {
            if(rootView != null && onGlobalLayoutListener != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
                }
                rootView = null;
                onGlobalLayoutListener = null;
            }
        }
    }
}
