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
package com.frdfsnlght.inquisitor.webserver;

import com.frdfsnlght.inquisitor.Global;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class WebResponse {

    // Sat, 29 Oct 1994 19:43:31 GMT
    public static final SimpleDateFormat HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz");
    static {
        HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public final byte[] EOL = {(byte) '\r', (byte) '\n'};

    private boolean statusSent = false;
    private int status = 200;
    private String statusMessage = "OK";

    private boolean headersSent = false;
    private Map<String,String> headers = new HashMap<>();

    private boolean contentSent = false;
    private String contentType = "text/html";
    private int contentLength = -1;
    private Date lastModified = null;

    private OutputStream os;
    private PrintStream out;

    public WebResponse(OutputStream os) throws IOException {
        this.os = os;
        out = new PrintStream(os);
    }

    public int getStatus() { return status; }

    public void setStatus(int status, String statusMessage) throws IOException {
        if (statusSent)
            throw new IOException("status line has already been sent");
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public boolean hasHeader(String key) {
        return headers.containsKey(key);
    }

    public void setHeader(String key, String value) throws IOException {
        if (headersSent)
            throw new IOException("headers have already been sent");
        headers.put(key, value);
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public PrintStream getPrintStream() {
        return out;
    }

    public void flushHeaders() throws IOException {
        if (! statusSent) sendStatus();
        if (! headersSent) sendHeaders();
    }

    public void close() throws IOException {
        flushHeaders();
        out.flush();
        out.close();
    }

    public void redirect(String url) throws IOException {
        redirect(url, 303);
    }

    public void redirect(String url, int status) throws IOException {
        setStatus(status, "Redirect");
        setHeader("Location", url);
        flushHeaders();
    }

    private void sendStatus() throws IOException {
        out.print("HTTP/1.0 ");
        out.print(status);
        out.print(" ");
        out.print(statusMessage);
        out.write(EOL);
        statusSent = true;
    }

    private void sendHeaders() throws IOException {
        if (! hasHeader("Status"))
            setHeader("Status", status + "");
        if (! hasHeader("Server"))
            setHeader("Server", Global.pluginName + " " + Global.pluginVersion + " Web Server");
        if (! hasHeader("Date"))
            setHeader("Date", ((SimpleDateFormat)HTTP_DATE_FORMAT.clone()).format(new Date()));
        if ((! hasHeader("Content-Type")) && (contentType != null))
            setHeader("Content-Type", contentType);
        if ((! hasHeader("Content-Length")) && (contentLength > 0))
            setHeader("Content-Length", contentLength + "");
        if ((! hasHeader("Last-Modified")) && (lastModified != null))
            setHeader("Last-Modified", ((SimpleDateFormat)HTTP_DATE_FORMAT.clone()).format(lastModified));

        for (String key : headers.keySet()) {
            String value = headers.get(key);
            out.print(key);
            out.print(": ");
            out.print(value);
            out.write(EOL);
        }
        out.write(EOL);
        headersSent = true;
    }

}
