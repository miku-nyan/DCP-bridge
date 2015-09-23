/*
 * Data Compression Proxy bridge for Overchan
 * Copyright (C) 2014-2015  miku-nyan <https://github.com/miku-nyan>
 *     
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nya.miku.dcpbridge.android;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import nya.miku.dcpbridge.Logger;
import nya.miku.dcpbridge.android.R;

public class NotifService extends Service {
    public static final int NOTIFICATION_ID = 100;
    private NotificationManager notificationManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    
    @SuppressLint("InlinedApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return Service.START_STICKY;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        
        Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.notification_title), System.currentTimeMillis());
        notification.setLatestEventInfo(this, getString(R.string.notification_title),
                getString(R.string.notification_content_port, AndroidApplication.instance.getPreferencess().port),
                PendingIntent.getActivity(this, 0, new Intent(NotifService.this, LauncherActivity.class),
                        PendingIntent.FLAG_CANCEL_CURRENT));
        
        if (AndroidApplication.SDK < 5) {
            try {
                getClass().getMethod("setForeground", new Class[] { boolean.class }).invoke(this, Boolean.TRUE);
            } catch (Exception e) {
                Logger.log("cannot invoke setForeground(true)");
                Logger.log(e);
            }
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            ForegroundCompat.startForeground(this, NOTIFICATION_ID, notification);
        }
    }
    
    @Override
    public void onDestroy() {
        if (AndroidApplication.SDK < 5) {
            notificationManager.cancel(NOTIFICATION_ID);
            try {
                getClass().getMethod("setForeground", new Class[] { boolean.class }).invoke(this, Boolean.FALSE);
            } catch (Exception e) {
                Logger.log("cannot invoke setForeground(false)");
                Logger.log(e);
            }
        } else {
            ForegroundCompat.stopForeground(this);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @SuppressLint("NewApi")
    private static class ForegroundCompat {
        static void startForeground(Service service, int id, Notification notification) {
            service.startForeground(id, notification);
        }
        static void stopForeground(Service service) {
            service.stopForeground(true);
        }
    }
    
}
