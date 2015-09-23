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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private Request() {}
    public List<String> lines = new ArrayList<>();
    
    public void writeTo(OutputStream out) throws IOException {
        /*System.out.println("[REQUEST]");
        for (String line : lines) System.out.println(line);
        System.out.println("[END OF REQUEST]");*/
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String line : lines) {
            baos.write(line.getBytes("UTF-8"));
            baos.write(CRLF);
        }
        baos.write(CRLF);
        baos.writeTo(out);
    }
    
    public static Request read(InputStream is) throws IOException {
        Request request = new Request();
        String s = readLine(is);
        while (s != null && s.length() > 0) {
            request.lines.add(s);
            s = readLine(is);
        }
        return request;
    }
    
    private static String readLine(InputStream is) throws IOException {
        StringBuilder line = new StringBuilder();
        int i;
        char c=0x00;
        i = is.read();
        if (i == -1) return null;
        while (i > -1 && i != 10 && i != 13) {
            c = (char)(i & 0xFF);
            line = line.append(c);
            i = is.read();
        }
        if (i == 13) {
            i = is.read();
        }
        return line.toString();
    }
    
}
