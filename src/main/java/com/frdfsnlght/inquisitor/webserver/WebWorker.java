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

import com.frdfsnlght.inquisitor.Utils;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class WebWorker implements Runnable {

    private static final int READ_TIMEOUT = 5000;

    private boolean run;
    private Socket socket;

    public WebWorker() {
        socket = null;
    }

    public void run() {
        run = true;
        while (run) {
            while (socket == null) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {}
                }
                if (!run) break;
            }
            if (!run) break;
            String client = socket.toString();
            try {
                handleRequest();
            } catch (Throwable t) {
                Utils.severe(t, "web server encountered an error while processing a request from %s:", client);
            }
            WebServer.rejoin(this);
        }
    }

    public void stop() {
        run = false;
        synchronized (this) {
            notify();
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        synchronized (this) {
            notify();
        }
    }

    private void handleRequest() throws IOException {
        WebRequest request;
        WebResponse response = null;
        try {
            socket.setSoTimeout(READ_TIMEOUT);
            socket.setTcpNoDelay(true);
            response = new WebResponse(socket.getOutputStream());
            request = new WebRequest(socket.getInputStream(), response);
            if (response.getStatus() == 200) {
                for (WebRoute route : WebServer.ROUTES) {
                    if (route.matches(request, request.getPath())) {
                        request.getHandler().handleRequest(request, response);
                        return;
                    }
                }
                response.setStatus(404, "Not Found");
            }
        } finally {
            try {
                if (response != null) response.close();
                socket.close();
                socket = null;
            } catch (IOException ie) {}
        }
    }

}
