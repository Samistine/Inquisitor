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

import com.frdfsnlght.inquisitor.Global;
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.WebHandler;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import com.frdfsnlght.inquisitor.webserver.WebServer;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class TemplateHandler extends WebHandler {

    protected static final String VIEW_EXTENSION = "html";

    protected static Configuration freeMarker = null;

    public TemplateHandler() {
    }

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String realPath = req.getPath();
        if (realPath.startsWith("/") && (realPath.length() > 1))
            realPath = realPath.substring(1);
        if (realPath.endsWith("/")) realPath += "index." + VIEW_EXTENSION;
        if (! realPath.endsWith("." + VIEW_EXTENSION))
            realPath += "." + VIEW_EXTENSION;
        realPath = realPath.replace("/", File.separator);
        renderTemplate(req, res, new File(WebServer.webRoot, realPath));
    }

    protected void renderTemplate(WebRequest req, WebResponse res, String tempPath) throws IOException {
        renderTemplate(req, res, tempPath, new TypeMap());
    }

    protected void renderTemplate(WebRequest req, WebResponse res, String tempPath, TypeMap data) throws IOException {
        File tempFile = new File(WebServer.webRoot, tempPath.replace('/', File.separatorChar));
        renderTemplate(req, res, tempFile, data);
    }

    protected void renderTemplate(WebRequest req, WebResponse res, File tempFile) throws IOException {
        renderTemplate(req, res, tempFile, new TypeMap());
    }

    protected void renderTemplate(WebRequest req, WebResponse res, File tempFile, TypeMap data) throws IOException {
        if (! tempFile.getParentFile().getAbsolutePath().startsWith(WebServer.webRoot.getAbsolutePath())) {
            res.setStatus(403, "Forbidden");
            return;
        }
        String relPath = tempFile.getAbsolutePath().substring(WebServer.webRoot.getAbsolutePath().length());
        if (! tempFile.isFile()) {
            res.setStatus(404, "Not found");
            return;
        }
        if (! tempFile.canRead()) {
            res.setStatus(403, "Forbidden");
            return;
        }
        Template template;

        if (freeMarker == null) {
            freeMarker = new Configuration();
            freeMarker.setDirectoryForTemplateLoading(WebServer.webRoot);
            freeMarker.setObjectWrapper(new DefaultObjectWrapper());
            freeMarker.setURLEscapingCharset("US-ASCII");
        }

        try {
            template = freeMarker.getTemplate(relPath);
        } catch (Throwable t) {
            res.setStatus(500, "Server error");
            Utils.severe(t, "Exception while loading web server template %s:", tempFile.getAbsolutePath());
            return;
        }

        data.set("pluginName", Global.pluginName);
        data.set("pluginVersion", Global.pluginVersion);
        data.set("now", new Date());

        // render the view
        StringWriter out = new StringWriter();
        byte[] content;
        try {
            template.process(data, out);
            content = out.toString().getBytes("UTF-8");
        } catch (Exception e) {
            res.setStatus(500, "Server error");
            Utils.severe(e, "Exception while processing web server view %s:", tempFile.getAbsolutePath());
            return;
        }

        res.setContentType("text/html; charset=utf-8");
        res.setContentLength(content.length);
        res.setLastModified(new Date(tempFile.lastModified()));
        res.flushHeaders();
        if (req.getMethod().equals("HEAD")) return;
        res.getPrintStream().write(content);
    }

}
