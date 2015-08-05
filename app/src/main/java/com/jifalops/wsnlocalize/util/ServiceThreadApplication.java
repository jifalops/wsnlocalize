package com.jifalops.wsnlocalize.util;

import android.app.Application;
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
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * ServiceThreadApplication provides an Application and related Service that can be bound to by using
 * {@link #bindLocalService()} and {@link #unbindLocalService()}. See
 * {@link ServiceThreadApplication.LocalService} for details.
 * Descendants of this class can use a static instance variable initialized in
 * {@link Application#onCreate()} and associated accessor method making it
 * accessible anywhere in the app itself.
 */
public class ServiceThreadApplication extends Application {
    /** This is only used by {@link LocalService#setPersistent(boolean)}. */
    private static ServiceThreadApplication sAppInstance;

    private LocalService mBoundService;
    private boolean mIsBound;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((LocalService.LocalBinder) service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
    }

    public void bindLocalService() {
        bindService(new Intent(ServiceThreadApplication.this, LocalService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void unbindLocalService() {
        if (mIsBound) {
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
     * {@link #bindLocalService()} and {@link #unbindLocalService()}, or can call
     * {@link #setPersistent(boolean)} to keep the service running in the background.
     * The service also manages its own thread and exposes two methods from the thread's Handler,
     * {@link #post(Runnable)} ()} and {@link #postDelayed(Runnable, long)}.
     */
    public static class LocalService extends Service {
        private HandlerThread mHandlerThread;
        private Handler mServiceHandler;
        private final IBinder mBinder = new LocalBinder();
        private boolean mIsPersistent;

        private class LocalBinder extends Binder {
            LocalService getService() {
                return LocalService.this;
            }
        }

        @Override
        public void onCreate() {
            mHandlerThread = new HandlerThread(getClass().getName());
            mServiceHandler = new Handler(mHandlerThread.getLooper());
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
            return START_STICKY;
        }

        @Override
        public boolean stopService(Intent name) {
            mIsPersistent = false;
            return super.stopService(name);
        }

        public boolean isPersistent() {
            return mIsPersistent;
        }

        /**
         * Set whether this service should continue to run in the background.
         * Calls {@link #startService(Intent)} internally.
         */
        public void setPersistent(boolean persist) {
            Intent i = new Intent(sAppInstance, LocalService.class);
            if (persist) {
                startService(i);
            } else {
                stopService(i);
            }
        }

        /** {@link Handler#post(Runnable)} */
        public boolean post(Runnable r) {
            if (mHandlerThread.getState() == Thread.State.NEW) {
                mHandlerThread.start();
            }
            return mServiceHandler.post(r);
        }
        /** {@link Handler#postDelayed(Runnable, long)} */
        public boolean postDelayed(Runnable r, long delayMillis) {
            if (mHandlerThread.getState() == Thread.State.NEW) {
                mHandlerThread.start();
            }
            return mServiceHandler.postDelayed(r, delayMillis);
        }

        public Thread.State getThreadState() {
            return mHandlerThread.getState();
        }
    }
}
