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

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import nya.miku.dcpbridge.DCPServer;
import nya.miku.dcpbridge.Logger;

public class AndroidApplication extends Application {
    @SuppressWarnings("deprecation")
    public static final int SDK = Integer.parseInt(Build.VERSION.SDK);
    
    private static final int DEFAULT_PORT = 8888;
    private static final boolean DEFAULT_HTTPS = false;
    private static final boolean DEFAULT_REPLACE = SDK >= 14;
    
    public static AndroidApplication instance;
    public DCPServer server;
    private SharedPreferences prefs;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setLogger(AndroidLogger.INSTANCE);
        SSLCompatibility.fixSSLs(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        instance = this;
    }
    
    public void savePreferences(Preferences p) {
        prefs.edit().putInt("port", p.port).putBoolean("https", p.https).putBoolean("replace", p.replaceAccept).commit();
    }
    
    public Preferences getPreferencess() {
        SharedPreferences p = prefs;
        return new Preferences(p.getInt("port", DEFAULT_PORT), p.getBoolean("https", DEFAULT_HTTPS), p.getBoolean("replace", DEFAULT_REPLACE));
    }
}
