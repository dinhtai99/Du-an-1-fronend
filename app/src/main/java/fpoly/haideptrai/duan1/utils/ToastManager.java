package fpoly.haideptrai.duan1.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Utility class để quản lý Toast messages, tránh spam và hiển thị quá nhiều Toast cùng lúc
 */
public class ToastManager {
    private static Toast currentToast;
    private static long lastToastTime = 0;
    private static final long TOAST_DELAY = 2000; // 2 seconds between toasts
    
    /**
     * Hiển thị Toast với quản lý tốt hơn
     * - Cancel toast cũ nếu có
     * - Tránh spam toast (chỉ hiển thị nếu đã qua 2 giây)
     */
    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }
    
    /**
     * Hiển thị Toast với duration tùy chỉnh
     */
    public static void showToast(Context context, String message, int duration) {
        long currentTime = System.currentTimeMillis();
        
        // Cancel toast cũ nếu có
        if (currentToast != null) {
            currentToast.cancel();
        }
        
        // Chỉ hiển thị toast mới nếu đã qua delay time
        if (currentTime - lastToastTime >= TOAST_DELAY) {
            currentToast = Toast.makeText(context.getApplicationContext(), message, duration);
            currentToast.show();
            lastToastTime = currentTime;
        }
    }
    
    /**
     * Cancel toast hiện tại nếu có
     */
    public static void cancelToast() {
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }
}

