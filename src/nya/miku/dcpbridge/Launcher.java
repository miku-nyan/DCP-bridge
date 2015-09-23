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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Launcher {
    private static final String HELP = "Data Compression Proxy bridge <https://github.com/miku-nyan/DCP-bridge>\n" +
            "The local proxy server sends all HTTP (but not HTTPS) traffic through Chrome Data Compression Proxy\n\n" +
            "Usage: <launcher class> <port> [options]\n" +
            "Options:\n" +
            "    https            use HTTPS during connection to Data Compression Proxy server\n" +
            "    replaceaccept    replace 'Accept' HTTP-header and request images in webp format";
    
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            int port = Integer.parseInt(args[0]);
            boolean https = false;
            boolean replaceAccept = false;
            for (int i=1; i<args.length; ++i) {
                if (args[i].equalsIgnoreCase("https")) https = true;
                else if (args[i].equalsIgnoreCase("replaceaccept")) replaceAccept = true;
            }
            
            Logger.setLogger(new Logger.ILogger() {
                private DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ", Locale.US);
                @Override
                public void log(Throwable throwable) {
                    log("");
                    throwable.printStackTrace(System.out);
                }
                @Override
                public void log(String message) {
                    System.out.println(dateFormat.format(System.currentTimeMillis()) + message);
                }
            });
            new DCPServer(port,
                    https ? DCPSocketFactory.getHttpsSocketFactory() : DCPSocketFactory.getHttpSocketFactory(),
                    replaceAccept ? DCPHandler.getReplacingAccept() : DCPHandler.getDefault()).start();
        } else {
            System.out.println(HELP);
        }
    }
}
