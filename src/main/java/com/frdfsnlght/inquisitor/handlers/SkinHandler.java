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
package com.frdfsnlght.inquisitor.handlers;

import com.frdfsnlght.inquisitor.Global;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.WebRequest;
import com.frdfsnlght.inquisitor.WebResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class SkinHandler extends TemplateHandler {

    private static final String CACHE_DIR = "web-cache";
    private static final long CACHE_REFRESH = 3600 * 3 * 1000;
    private static final String SKIN_URL = "http://s3.amazonaws.com/MinecraftSkins/";
    private static final String DEFAULT_SKIN = "char";

    private static File cacheDir = null;

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String playerName = req.getParameter("playerName", null);
        if (playerName == null) {
            res.setStatus(400, "Bad Request");
            return;
        }

        File cacheFile = cacheSkin(playerName);
        if (cacheFile == null)
            cacheFile = cacheSkin(DEFAULT_SKIN);
        if (cacheFile == null) {
            res.setStatus(500, "Server error");
            Utils.warning("Unable to download default skin from %s%s.png!", SKIN_URL, DEFAULT_SKIN);
            return;
        }

        Date checkDate = req.getHeaderDate("if-modified-since");
        if (checkDate != null) {
            if ((checkDate.getTime() / 1000) >= (cacheFile.lastModified() / 1000)) {
                res.setStatus(304, "Not modified");
                return;
            }
        }

        FileChannel ic = new FileInputStream(cacheFile).getChannel();
        ByteBuffer buf = ByteBuffer.allocate((int)cacheFile.length());
        while (buf.hasRemaining()) ic.read(buf);
        ic.close();
        buf.rewind();
        byte[] buffer = new byte[buf.capacity()];
        buf.get(buffer, 0, buffer.length);

        res.setContentType("image/png");
        res.setContentLength(buffer.length);
        res.setLastModified(new Date(cacheFile.lastModified()));
        res.flushHeaders();
        if (req.getMethod().equals("HEAD")) return;
        res.getPrintStream().write(buffer);
    }

    private File cacheSkin(String name) throws IOException {
        if (cacheDir == null) {
            cacheDir = new File(Global.plugin.getDataFolder(), CACHE_DIR);
            if (! cacheDir.exists())
                cacheDir.mkdirs();
        }
        File file = new File(cacheDir, URLEncoder.encode(name, "US-ASCII") + ".png");
        if (file.exists() && ((System.currentTimeMillis() - file.lastModified()) <= CACHE_REFRESH)) return file;
        URL url = new URL(SKIN_URL + URLEncoder.encode(name, "US-ASCII") + ".png");
        if (Utils.downloadFile(url, file, false)) return file;
        return null;
    }

}
