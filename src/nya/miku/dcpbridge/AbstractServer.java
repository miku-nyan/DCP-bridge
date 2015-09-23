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
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractServer extends Thread {
    private final ServerSocket listen;
    private Set<Connection> connections = new HashSet<>();
    private volatile boolean stop = false;
    public AbstractServer(int port) throws IOException {
        listen = new ServerSocket(port);
        listen.setSoTimeout(30 * 000);
    }
    
    @Override
    public void run() {
        Logger.log("Starting server on port " + listen.getLocalPort());
        while(!stop) {
            try {
                new Connection(listen.accept()).start();
            } catch (InterruptedIOException e) {
            } catch (IOException e) {
                Logger.log(e);
            }
        }
    }
    
    public void stopServer() throws IOException {
        stop = true;
        interrupt();
        listen.close();
        for (Connection connection : connections)
            if (connection.client != null && !connection.client.isClosed())
                connection.client.close();
    }
    
    private class Connection extends Thread {
        private final Socket client;
        
        public Connection(Socket client) throws UnknownHostException, IOException {
            this.client = client;
            setDaemon(true);
        }
        
        @Override
        public void run() {
            connections.add(this);
            Logger.log("Connected to " + client.getInetAddress().getHostAddress() + ":" + client.getPort() + " on port " + client.getLocalPort());
            InputStream in = null;
            OutputStream out = null;
            try {
                in = client.getInputStream();
                out = client.getOutputStream();
                handle(in, out);
            } catch (Exception e) {
                Logger.log(e);
            } finally {
                try { if (in != null) in.close(); } catch (IOException e) {}
                try { if (out != null) out.close(); } catch (IOException e) {}
                try { if (!client.isClosed()) client.close(); } catch (IOException e) {}
                connections.remove(this);
                Logger.log("Connection to " + client.getInetAddress().getHostAddress() + ":" + client.getPort() + " closed.");
            }
        }
    }
    
    protected abstract void handle(InputStream in, OutputStream out) throws IOException;
}
