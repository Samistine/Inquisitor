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
package com.frdfsnlght.inquisitor.webserver.handlers.api;

import com.frdfsnlght.inquisitor.JSON;
import com.frdfsnlght.inquisitor.TypeMap;
import com.frdfsnlght.inquisitor.webserver.WebHandler;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import java.io.IOException;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public abstract class APIHandler implements WebHandler {

    protected void sendFailure(WebRequest req, WebResponse res, String error) throws IOException {
        TypeMap msg = new TypeMap();
        msg.set("success", false);
        msg.set("error", error);
        sendJSON(req, res, JSON.encode(msg));
    }

    protected void sendSuccess(WebRequest req, WebResponse res, Object obj) throws IOException {
        TypeMap msg = new TypeMap();
        msg.set("success", true);
        msg.set("result", obj);
        sendJSON(req, res, JSON.encode(msg));
    }

    protected void sendJSON(WebRequest req, WebResponse res, String json) throws IOException {
        byte[] buffer = json.getBytes();
        res.setContentType("application/json");
        res.setContentLength(buffer.length);
        res.flushHeaders();
        if (req.getMethod().equals("HEAD")) return;
        res.getPrintStream().write(buffer);
    }

}
