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

package nya.miku.dcpbridge;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

public abstract class DCPHandler {
    private static final short[] KEY_1 = new short[] {
            0x5d, 0x30, 0x13, 0x5a, 0xb3, 0x25, 0xce, 0x44, 0xc5, 0xde, 0x8a, 0x80, 0xd2, 0x06, 0x0f, 0xd8, 0x38, 0x5d, 0x8a, 0x0c
    };
    
    private static final short[] KEY_2 = new short[] {
            0xf1, 0x75, 0x13, 0x87, 0x88, 0x50, 0xb7, 0x5c, 0xa9, 0xc5, 0x8c, 0xa0, 0xb3, 0x49, 0xd4, 0xc7, 0x45, 0x3c, 0x73, 0x48
    };
    
    // https://code.google.com/p/datacompressionproxy/source/browse/background.js
    private static final String AUTH_VALUE;
    
    static {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<KEY_1.length; ++i) {
            String b = Integer.toHexString(KEY_1[i] ^ KEY_2[i]);
            if (b.length() == 1) builder.append('0');
            builder.append(b);
        }
        AUTH_VALUE = builder.toString();
    }
    
    private static final String ACCEPT_HEADER_REPLACEMENT = "Accept: image/webp,*/*;q=0.8";
    private static final Pattern ACCEPT_HEADER_PATTERN = Pattern.compile("(?i)\\s*accept\\s*:(.*)");
    private static void replaceAccept(List<String> headers) {
        boolean replaced = false;
        for (ListIterator<String> it = headers.listIterator(); it.hasNext();) {
            String line = it.next();
            if (ACCEPT_HEADER_PATTERN.matcher(line).matches()) {
                if (!replaced) {
                    it.set(ACCEPT_HEADER_REPLACEMENT);
                    replaced = true;
                } else {
                    it.remove();
                }
            }
        }
        if (!replaced) headers.add(ACCEPT_HEADER_REPLACEMENT);
    }
    
    private static void handleRequest(Request request, boolean replaceAccept) {
        String timestamp = Long.toString(System.currentTimeMillis());
        if (timestamp.length() > 10) timestamp = timestamp.substring(0, 10);
        String sid = CryptoUtils.computeMD5(timestamp + AUTH_VALUE + timestamp);
        String key = "ps=" + timestamp + "-" + getRandom() + "-" + getRandom() + "-" + getRandom() + ", sid=" + sid + ", b=2228, p=0, c=win";
        
        List<String> headers = request.lines;
        if (replaceAccept) replaceAccept(headers);
        headers.add("Chrome-Proxy: " + key);
    }
    
    private static String getRandom() {
        return Long.toString((long)Math.floor(Math.random() * 1000000000));
    }
    
    public abstract void handleRequest(Request request);
    
    public static DCPHandler getDefault() {
        return new DCPHandler() {
            @Override
            public void handleRequest(Request request) {
                DCPHandler.handleRequest(request, false);
            }
        };
    }
    
    public static DCPHandler getReplacingAccept() {
        return new DCPHandler() {
            @Override
            public void handleRequest(Request request) {
                DCPHandler.handleRequest(request, true);
            }
        };
    }
}
