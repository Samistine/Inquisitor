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
package com.frdfsnlght.inquisitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class WebRequest {

    private SimpleDateFormat[] HTTP_DATE_FORMATS = new SimpleDateFormat[] {
        // RFC 1123: Sat, 29 Oct 1994 19:43:31 GMT
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz"),

        // RFC 850: Sunday, 06-Nov-94 08:49:37 GMT
        new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz"),

        // ANSI C asctime(): Sun Nov  6 08:49:37 1994
        new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy"),

        // Mon Jul 09 16:40:52 EDT 2012
        new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
    };


    private WebResponse response;

    private String request;
    private String method;
    private String path;
    private String queryString;
    private TypeMap parameters = new TypeMap();
    private WebHandler handler;
    private Map<String,String> headers = new HashMap<String,String>();

    public WebRequest(InputStream is, WebResponse response) throws IOException {
        this.response = response;
        readRequest(is);
    }

    public String getRequest() { return request; }

    public void setRequest(String request) { this.request = request; }

    public String getMethod() { return method; }

    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }

    public void setPath(String path) { this.path = path; }

    public String getQueryString() { return queryString; }

    public void setQueryString(String queryString) { this.queryString = queryString; }

    public boolean hasParameter(String name) {
        return parameters.containsKey(name);
    }

    public String getParameter(String name, String def) {
        return parameters.getString(name, def);
    }

    public TypeMap getParameters() { return parameters; }

    public void setParameter(String name, String value) {
        parameters.set(name, value);
    }

    public void setParameters(Map<String,Object> params) {
        parameters.putAll(params);
    }

    public WebHandler getHandler() { return handler; }

    public void setHandler(WebHandler handler) { this.handler = handler; }

    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Date getHeaderDate(String name) {
        String str = getHeader(name);
        if (str == null) return null;
        for (SimpleDateFormat format : HTTP_DATE_FORMATS) {
            try {
                return format.parse(str);
            } catch (Throwable t) {}
        }
        return null;
    }

    private void readRequest(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "US-ASCII"));

        try {
        request = reader.readLine();
        } catch (SocketTimeoutException e) {
            request = null;
        }
        if (request == null) {
            response.setStatus(400, "Bad request");
            return;
        }
        // parse request line

        String r = request;
        int pos = r.indexOf(" ");
        if (pos == -1) {
            response.setStatus(400, "Bad request");
            return;
        }
        method = r.substring(0, pos).toUpperCase();
        r = r.substring(pos + 1);
        pos = r.indexOf(" ");
        if (pos == -1) pos = r.length();
        r = r.substring(0, pos);
        if (r.length() == 0) {
            path = "/";
        } else {
            pos = r.indexOf("?");
            if (pos == -1)
                path = r;
            else {
                path = r.substring(0, pos);
                queryString = r.substring(pos);
                pos = path.indexOf("#");
                if (pos != -1)
                    path = path.substring(0, pos);
                parseParameters(queryString);
            }
        }
        path = path.replaceAll("/\\.\\./", "/");
        path = path.replaceAll("/\\./", "/");

        // parse headers

        String header = null;
        for (;;) {
            String line = reader.readLine();
            if (line != null) {
                if (line.startsWith(" ") || line.startsWith("\t")) {
                    if (header != null) {
                        header += " " + line.trim();
                        continue;
                    } else {
                        response.setStatus(400, "Bad request");
                        return;
                    }
                } else {
                    parseHeader(header);
                    if (line.equals("")) break;
                    header = line;
                    continue;
                }
            } else {
                parseHeader(header);
                return;
            }
        }

        // parse content

        if ((! method.equals("POST")) ||
            (! hasHeader("content-type")) ||
            (! getHeader("content-type").equals("application/x-www-form-urlencoded"))) return;
        String content = reader.readLine();
        parseParameters(content);

    }

    private void parseHeader(String header) {
        if (header == null) return;
        int pos = header.indexOf(":");
        if (pos == -1) return;
        String key = header.substring(0, pos);
        String value = header.substring(pos + 1).trim();
        setHeader(key.toLowerCase(), value);
    }

    private void parseParameters(String query) {
        if (query.startsWith("?")) query = query.substring(1);
        for (String param : query.split("&")) {
            int pos = param.indexOf("=");
            String key;
            String value = null;
            try {
                if (pos == -1) {
                    key = URLDecoder.decode(param, "UTF-8");
                } else {
                    key = URLDecoder.decode(param.substring(0, pos), "UTF-8");
                    value = URLDecoder.decode(param.substring(pos + 1), "UTF-8");
                }
                parameters.put(key, value);
            } catch (UnsupportedEncodingException uee) {}
        }
    }


}
