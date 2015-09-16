package com.jifalops.wsnlocalize.util;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jifalops.wsnlocalize.MainActivity;
import com.jifalops.wsnlocalize.R;

import java.util.HashMap;
import java.util.Map;

/**
 * ServiceThreadApplication provides an Application and related Service that can be bound to by using
 * {@link #bindLocalService(Runnable)} and {@link #unbindLocalService(Runnable)}. See
 * {@link ServiceThreadApplication.LocalService} for details.
 * Descendants of this class can use a static instance variable initialized in
 * {@link Application#onCreate()} and associated accessor method making it
 * accessible anywhere in the app itself.
 */
public class ServiceThreadApplication extends Application {
    /** This is only used by {@link LocalService#setPersistent(boolean, Class)}. */
    private static ServiceThreadApplication sAppInstance;

    private LocalService mBoundService;
    private boolean mIsBound;
    private Runnable bindRunnable, unbindRunnable;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((LocalService.LocalBinder) service).getService();
            if (bindRunnable != null) bindRunnable.run();
        }
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            if (unbindRunnable != null) unbindRunnable.run();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
    }

    public void bindLocalService(Runnable onServiceBound) {
        if (!mIsBound) {
            bindRunnable = onServiceBound;
            bindService(new Intent(ServiceThreadApplication.this, LocalService.class),
                    mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    public void unbindLocalService(Runnable onServiceUnbound) {
        if (mIsBound) {
            unbindRunnable = onServiceUnbound;
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Nullable
    public LocalService getService() {
        return mBoundService;
    }

    /**
     * LocalService is a Service that Activities can bind to while active by calling
     * {@link #bindLocalService(Runnable)} and {@link #unbindLocalService(Runnable)}, or can call
     * {@link #setPersistent(boolean, Class)} to keep the service running in the background.
     * The service also manages its own thread and exposes two methods from the thread's Handler,
     * {@link #post(Runnable)} ()} and {@link #postDelayed(Runnable, long)}.
     */
    public static class LocalService extends Service {
        private HandlerThread mHandlerThread;
        private Handler mServiceHandler;
        private final IBinder mBinder = new LocalBinder();
        private boolean mIsPersistent;
        // Activities can use when running in the background.
        private Map<String, Object> cache = new HashMap<>();

        private NotificationManager mNM;

        // Unique Identification Number for the Notification.
        // We use it on Notification start, and to cancel it.
        private int NOTIFICATION = R.string.app_persisting;

        private class LocalBinder extends Binder {
            LocalService getService() {
                return LocalService.this;
            }
        }

        @Override
        public void onCreate() {
            mHandlerThread = new HandlerThread(getClass().getName());
            mHandlerThread.start();
            mServiceHandler = new Handler(mHandlerThread.getLooper());
            mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        }

        @Override
        public void onDestroy() {
            mServiceHandler.removeCallbacksAndMessages(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mHandlerThread.quitSafely();
            } else {
                mHandlerThread.quit();
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i("LocalService", "Received start id " + startId + ": " + intent);
            mIsPersistent = true;
            Intent target = intent.getParcelableExtra("notificationTarget");
            showNotification(target);
            return START_STICKY;
        }

        private void showNotification(Intent target) {
            CharSequence text = getString(R.string.app_persisting, getText(R.string.app_name));

            if (target == null) {
                // Start the main activity for the app when the notification is clicked
                target = getPackageManager().getLaunchIntentForPackage(getPackageName());
            }

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, target, 0);

            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .build();
            mNM.notify(NOTIFICATION, notification);
        }

        @Override
        public boolean stopService(Intent name) {
            mIsPersistent = false;
            mNM.cancel(NOTIFICATION);
            return super.stopService(name);
        }

        public boolean isPersistent() {
            return mIsPersistent;
        }

        /**
         * Set whether this service should continue to run in the background.
         * The target activity should have either android:launchMode="singleInstance" or
         * android:launchMode="singleTask" set to avoid multiple instances.
         * Calls {@link #startService(Intent)} internally.
         */
        public void setPersistent(boolean persist, Class<? extends Activity> notificationTarget) {
            Intent i = new Intent(sAppInstance, LocalService.class);
            i.putExtra("notificationTarget", new Intent(this, notificationTarget));
            if (persist) {
                startService(i);
            } else {
                stopService(i);
            }
        }

        /** {@link Handler#post(Runnable)} */
        public boolean post(Runnable r) {
            return mServiceHandler.post(r);
        }
        /** {@link Handler#postDelayed(Runnable, long)} */
        public boolean postDelayed(Runnable r, long delayMillis) {
            return mServiceHandler.postDelayed(r, delayMillis);
        }

        public Thread.State getThreadState() {
            return mHandlerThread.getState();
        }

        public Object getCachedObject(String key) {
            return cache.get(key);
        }
        /** Returns the displaced object if one exists. */
        public Object setCachedObject(String key, Object obj) {
            return cache.put(key, obj);
        }
    }
}
