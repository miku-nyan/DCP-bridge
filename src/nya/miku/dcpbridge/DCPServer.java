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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ListIterator;

public class DCPServer extends AbstractServer {
    private DCPSocketFactory dcpSocketFactory;
    private DCPHandler dcpHandler;
    
    public DCPServer(int port) throws IOException {
        this(port, null, null);
    }
    
    public DCPServer(int port, DCPSocketFactory dcpSocketFactory, DCPHandler dcpHandler) throws IOException {
        super(port);
        this.dcpSocketFactory = dcpSocketFactory == null ? DCPSocketFactory.getHttpSocketFactory() : dcpSocketFactory;
        this.dcpHandler = dcpHandler == null ? DCPHandler.getDefault() : dcpHandler;
    }
    
    private class Handler {
        private volatile Socket proxySocket;
        
        protected void handle(InputStream clientIn, OutputStream clientOut) throws IOException {
            clientIn = new BufferedInputStream(clientIn);
            
            InputStream proxyIn = null;
            OutputStream proxyOut = null;
            try {
                while (true) {
                    Request request = Request.read(clientIn);
                    if (request.lines.size() == 0) return;
                    if (request.lines.get(0).startsWith("CONNECT ")) {
                        String host = request.lines.get(0).substring(7).trim();
                        if (host.indexOf(' ') >= 0) host = host.substring(0, host.lastIndexOf(' '));
                        int portIndex = host.lastIndexOf(':');
                        int port = 443;
                        try {
                            port = Integer.parseInt(host.substring(portIndex + 1));
                            host = host.substring(0, portIndex);
                        } catch (Exception e) {}
                        
                        final InputStream httpsIn;
                        final OutputStream httpsOut;
                        Socket httpsSocket;
                        try {
                            httpsSocket = new Socket(host, port);
                            httpsIn = httpsSocket.getInputStream();
                            httpsOut = httpsSocket.getOutputStream();
                            clientOut.write(("HTTP/1.0 200 Connection established\r\n\r\n").getBytes());
                            clientOut.flush();
                        } catch (IOException e) {
                            clientOut.write(("Proxy server could not connect to " + host + ":" + port + "\n").getBytes("UTF-8"));
                            clientOut.flush();
                            return;
                        }
                        
                        Thread c2s = new Passthrough(clientIn, httpsOut);
                        Thread s2c = new Passthrough(httpsIn, clientOut);
                        c2s.start(); s2c.start();
                        try { c2s.join(); s2c.join(); } catch (InterruptedException e) {}
                        try {
                            httpsOut.flush();
                            httpsSocket.close();
                        } catch (IOException e) {}
                        
                    } else if (request.lines.get(0).startsWith("GET http://iichan.hk/cgi-bin/captcha")) {
                        getFromIichan(request, clientOut);
                    } else if (request.lines.get(0).startsWith("POST http://iichan.hk/cgi-bin/wakaba.pl")) {
                        postToIichan(request, clientIn, clientOut);
                    } else {
                        int connectLength = -1;
                        for (String header : request.lines)
                            if (header.contains("Content-Length:"))
                                try {
                                    connectLength = Integer.parseInt(header.substring(header.indexOf("Content-Length:") + 15).trim());
                                } catch (Exception e) {}
                        
                        dcpHandler.handleRequest(request);
                        if (proxySocket == null || proxySocket.isClosed()) {
                            proxySocket = dcpSocketFactory.getSocket();
                            proxyIn = proxySocket.getInputStream();
                            proxyOut = proxySocket.getOutputStream();
                            new Passthrough(proxyIn, clientOut).setOnClosed(new Runnable() {
                                @Override
                                public void run() {
                                    Logger.log("Socket stream end");
                                    try { proxySocket.close(); } catch (IOException e) { Logger.log(e); }
                                    proxySocket = null;
                                }
                            }).start();
                        }
                        
                        request.writeTo(proxyOut);
                        if (connectLength != -1) {
                            int totalCount = 0;
                            byte[] buf = new byte[2048];
                            while (totalCount < connectLength || clientIn.available() > 0) {
                                int count = clientIn.read(buf);
                                proxyOut.write(buf, 0, count);
                                totalCount += count;
                            }
                        }
                        proxyOut.flush();
                    }
                }
            } finally {
                if (proxySocket != null && proxySocket.isClosed()) proxySocket.close();
            }
        }
    }
    
    private static void getFromIichan(Request request, OutputStream stream) throws IOException {
        request.lines.set(0, request.lines.get(0).replace("GET http://iichan.hk/", "GET /"));
        for (ListIterator<String> it = request.lines.listIterator(); it.hasNext();) {
            String current = it.next().trim();
            if (current.startsWith("Connection:") || current.startsWith("Proxy-Connection:")) it.remove();
        }
        request.lines.add("Connection: close");
        final Socket iichanSocket = new Socket("iichan.hk", 80);
        InputStream iichanIn = iichanSocket.getInputStream();
        OutputStream iichanOut = iichanSocket.getOutputStream();
        request.writeTo(iichanOut);
        iichanOut.flush();
        new Passthrough(iichanIn, stream).setOnClosed(new Runnable() {
            @Override
            public void run() {
                try { iichanSocket.close(); } catch (Exception e) {}
            }
        }).start();
    }
    
    private static void postToIichan(Request request, InputStream istream, OutputStream ostream) throws IOException {
        request.lines.set(0, request.lines.get(0).replace("POST http://iichan.hk/", "POST /"));
        for (ListIterator<String> it = request.lines.listIterator(); it.hasNext();) {
            String current = it.next().trim();
            if (current.startsWith("Connection:") || current.startsWith("Proxy-Connection:")) it.remove();
        }
        request.lines.add("Connection: close");
        int connectLength = -1;
        for (String header : request.lines)
            if (header.contains("Content-Length:"))
                try {
                    connectLength = Integer.parseInt(header.substring(header.indexOf("Content-Length:") + 15).trim());
                } catch (Exception e) {}
        if (connectLength == -1) throw new IOException("bad POST request");
        
        final Socket iichanSocket = new Socket("iichan.hk", 80);
        InputStream iichanIn = iichanSocket.getInputStream();
        OutputStream iichanOut = iichanSocket.getOutputStream();
        request.writeTo(iichanOut);
        int totalCount = 0;
        byte[] buf = new byte[2048];
        while (totalCount < connectLength || istream.available() > 0) {
            int count = istream.read(buf);
            iichanOut.write(buf, 0, count);
            totalCount += count;
        }
        iichanOut.flush();
        new Passthrough(iichanIn, ostream).setOnClosed(new Runnable() {
            @Override
            public void run() {
                try { iichanSocket.close(); } catch (Exception e) {}
            }
        }).start();
    }
    
    private static class Passthrough extends Thread {
        private InputStream from;
        private OutputStream to;
        private Runnable onClosed;
        public Passthrough(InputStream from, OutputStream to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public void run() {
            byte[] buffer = new byte[2048];
            int bytes_read;
            try {
                while((bytes_read=from.read(buffer))!=-1) {
                    to.write(buffer, 0, bytes_read);
                    to.flush();
                }
            } catch (IOException e) {
            }
            if (onClosed != null) onClosed.run();
        }
        public Passthrough setOnClosed(Runnable onClosed) {
            this.onClosed = onClosed;
            return this;
        }
    }
    
    @Override
    protected void handle(InputStream clientIn, OutputStream clientOut) throws IOException {
        new Handler().handle(clientIn, clientOut);
    }
    
}
