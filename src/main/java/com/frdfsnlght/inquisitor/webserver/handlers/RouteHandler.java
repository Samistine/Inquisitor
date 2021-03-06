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

import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.webserver.WebHandler;
import com.frdfsnlght.inquisitor.webserver.WebRequest;
import com.frdfsnlght.inquisitor.webserver.WebResponse;
import com.frdfsnlght.inquisitor.webserver.WebRoute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RouteHandler implements WebHandler {

    private final List<WebRoute> routes = new ArrayList<>();

    public void add(WebRoute route) {
        routes.add(route);
    }

    @Override
    public void handleRequest(WebRequest req, WebResponse res) throws IOException {
        String path = req.getParameter("routePath", null);
        if (path == null) {
            res.setStatus(500, "Server Error");
            Utils.warning("No routePath found for request %s", req.getPath());
            return;
        }
        for (WebRoute route : routes) {
            if (route.matches(req, path)) {
                req.getHandler().handleRequest(req, res);
                return;
            }
        }
        res.setStatus(404, "Not Found");
    }

}
