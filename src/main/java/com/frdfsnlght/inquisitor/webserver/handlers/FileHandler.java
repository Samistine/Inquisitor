/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor.webserver.handlers;

import com.frdfsnlght.inquisitor.webserver.WebHandler;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import com.frdfsnlght.inquisitor.webserver.WebServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class FileHandler implements WebHandler {

    public static final Map<String,String> MIME_TYPES = new HashMap<>();
    
    private static final long CACHE_REFRESH = 3600 * 72; //Cache static files for 72 hours

    static {
        MIME_TYPES.put("css", "text/css; charset=utf-8");
        MIME_TYPES.put("txt", "text/plain; charset=utf-8");
        MIME_TYPES.put("js", "application/x-javascript");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
    }

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String realPath = req.getPath();
        if (realPath.startsWith("/"))
            realPath = realPath.substring(1);
        int extensionPos = realPath.lastIndexOf(".");
        String extension = realPath.substring(extensionPos + 1).toLowerCase();
        realPath = realPath.replace("/", File.separator);
        File realFile = new File(WebServer.webRoot, realPath);
        if (!realFile.getParentFile().getAbsolutePath().startsWith(WebServer.webRoot.getAbsolutePath())) {
            res.setStatus(403, "Forbidden");
            return;
        }
        if (!realFile.isFile()) {
            res.setStatus(404, "Not found");
            return;
        }
        if (!realFile.canRead()) {
            res.setStatus(403, "Forbidden");
            return;
        }

        //If client already has the same file we tell them to use it
        Date checkDate = req.getHeaderDate("if-modified-since");
        if (checkDate != null) {
            if ((checkDate.getTime() / 1000) >= (realFile.lastModified() / 1000)) {
                res.setStatus(304, "Not modified");
                return;
            }
        }

        FileChannel ic = new FileInputStream(realFile).getChannel();
        ByteBuffer buf = ByteBuffer.allocate((int)realFile.length());
        while (buf.hasRemaining()) ic.read(buf);
        ic.close();
        buf.rewind();
        byte[] buffer = new byte[buf.capacity()];
        buf.get(buffer, 0, buffer.length);

        String contentType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

        res.setContentType(contentType);
        res.setContentLength(buffer.length);
        res.setLastModified(new Date(realFile.lastModified()));
        res.setHeader("Cache-Control", "max-age=" + CACHE_REFRESH);
        res.flushHeaders();
        if (req.getMethod().equals("HEAD")) return;
        res.getPrintStream().write(buffer);
    }

}
