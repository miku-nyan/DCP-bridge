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
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public abstract class DCPSocketFactory {
    private static final String DCP_HTTP_HOST = "compress.googlezip.net";
    private static final String DCP_HTTPS_HOST = "proxy.googlezip.net";
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    
    private static final SocketFactory SSL_SOCKET_FACTORY = SSLSocketFactory.getDefault();
    
    public abstract Socket getSocket() throws IOException;
    
    public static DCPSocketFactory getHttpSocketFactory() {
        return new DCPSocketFactory() {
            @Override
            public Socket getSocket() throws IOException {
                return new Socket(DCP_HTTP_HOST, HTTP_PORT);
            }
        };
    }
    
    public static DCPSocketFactory getHttpsSocketFactory() {
        return new DCPSocketFactory() {
            @Override
            public Socket getSocket() throws IOException {
                return SSL_SOCKET_FACTORY.createSocket(DCP_HTTPS_HOST, HTTPS_PORT);
            }
        };
    }
}
