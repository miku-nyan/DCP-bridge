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

import android.util.Log;
import nya.miku.dcpbridge.Logger;
import nya.miku.dcpbridge.Logger.ILogger;
import nya.miku.dcpbridge.android.BuildConfig;

public class AndroidLogger implements Logger.ILogger {
    
    public static final ILogger INSTANCE = new AndroidLogger();
    
    private static final String TAG = "DCP";
    
    private AndroidLogger() {}
    
    @Override
    public void log(Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, throwable.toString(), throwable);
        }
    }
    
    @Override
    public void log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }
    
}
