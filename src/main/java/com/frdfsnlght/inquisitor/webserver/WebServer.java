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

import com.frdfsnlght.inquisitor.Config;
import com.frdfsnlght.inquisitor.Context;
import com.frdfsnlght.inquisitor.Global;
import com.frdfsnlght.inquisitor.Options;
import com.frdfsnlght.inquisitor.OptionsException;
import com.frdfsnlght.inquisitor.OptionsListener;
import com.frdfsnlght.inquisitor.PlayerStats;
import com.frdfsnlght.inquisitor.exceptions.PermissionsException;
import com.frdfsnlght.inquisitor.PlayerStats.PlayerStatsListener;
import com.frdfsnlght.inquisitor.Statistic;
import com.frdfsnlght.inquisitor.StatisticsManager;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.handlers.FileHandler;
import com.frdfsnlght.inquisitor.handlers.PlayerHandler;
import com.frdfsnlght.inquisitor.handlers.PlayerSearchHandler;
import com.frdfsnlght.inquisitor.handlers.PlayersHandler;
import com.frdfsnlght.inquisitor.handlers.RedirectHandler;
import com.frdfsnlght.inquisitor.handlers.RouteHandler;
import com.frdfsnlght.inquisitor.handlers.SkinHandler;
import com.frdfsnlght.inquisitor.handlers.TemplateHandler;
import com.frdfsnlght.inquisitor.handlers.api.FindPlayersHandler;
import com.frdfsnlght.inquisitor.handlers.api.stats.AllStats;
import com.frdfsnlght.inquisitor.handlers.api.stats.Groups;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class WebServer {

    private static final Set<String> OPTIONS = new HashSet<String>();
    private static final Set<String> RESTART_OPTIONS = new HashSet<String>();
    private static final Options options;

    private static final long WORKER_TIMEOUT = 10000;

    public static final List<WebRoute> ROUTES = new ArrayList<WebRoute>();

    private static final String[] DEFAULT_PLAYERSCOLUMNS = new String[] {
            "level",
            "deaths",
            "totalPlayersKilled",
            "totalMobsKilled",
            "totalBlocksBroken",
            "totalBlocksPlaced",
            "totalItemsDropped",
            "totalItemsPickedUp",
            "totalItemsCrafted",
            "totalDistanceTraveled",
            "totalTime",
            "online"
    };

    private static final String[] DEFAULT_RESTRICTCOLUMNS = new String[] {
            "address",
            "coords",
            "bedServer",
            "bedWorld",
            "bedCoords",
    };

    public static final String DEFAULT_SORTCOLUMN = "name";
    public static final String DEFAULT_SORTDIR = "ASC";

    private static boolean started = false;
    private static ServerSocket serverSocket = null;
    private static Thread serverThread = null;
    private static final List<WebWorker> workerThreads = new ArrayList<WebWorker>();

    public static File webRoot;

    static {
        OPTIONS.add("port");
        OPTIONS.add("address");
        OPTIONS.add("workers");
        OPTIONS.add("upgradeWebRoot");
        OPTIONS.add("playersColumns");
        OPTIONS.add("playersSortColumn");
        OPTIONS.add("playersSortDir");
        OPTIONS.add("playersRestrictColumns");
        OPTIONS.add("playersPageSize");

        RESTART_OPTIONS.add("port");
        RESTART_OPTIONS.add("address");
        RESTART_OPTIONS.add("workers");

        options = new Options(WebServer.class, OPTIONS, "inq.webserver", new OptionsListener() {
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.sendLog("web server option '%s' set to '%s'", name, value);
                if (RESTART_OPTIONS.contains(name)) {
                    Config.save(ctx);
                    stop();
                    start();
                }
            }
            public String getOptionPermission(Context ctx, String name) {
                return name;
            }
        });

        PlayerStats.addListener(new PlayerStatsListener() {
            public void onPlayerStatsStarted() {
                start();
            }
            public void onPlayerStatsStopping() {
                stop();
            }
        });
    }

    public static void init() {}

    public static boolean isStarted() {
        return started;
    }

    public static void start() {
        if (! getEnabled()) return;
        if (started) return;

        try {
            if (! StatisticsManager.isStarted())
                throw new Exception("statisticsManager is not started");

            webRoot = new File(Global.plugin.getDataFolder(), "web-root");

            // copy web-root
            if (! webRoot.isDirectory()) {
                if (Utils.installManifest("/resources/web_root/manifest", webRoot, false))
                    Utils.info("installed default web root");
            } else if (getUpgradeWebRoot()) {
                if (Utils.installManifest("/resources/web_root/manifest", webRoot, false))
                    Utils.info("upgraded web root");
            }

            if (ROUTES.isEmpty()) {
                RouteHandler api = new RouteHandler();
                api.add(new WebRoute(new FindPlayersHandler(), "/findPlayers"));
                
                //Samistine Route
                RouteHandler apiStats = new RouteHandler();
                apiStats.add(new WebRoute(new Groups(), "/stats/groups/(.*)", "playerName"));
                apiStats.add(new WebRoute(new AllStats(), "/stats/([^/]*)", "playerName"));
                api.add(new WebRoute(apiStats, "/stats/(.*)", "playerName"));

                // add api routes

                ROUTES.add(new WebRoute(api, "^/api(/.+)", "routePath"));
                ROUTES.add(new WebRoute(new PlayerHandler(), "^/player/(.*)", "playerName"));
                ROUTES.add(new WebRoute(new PlayerSearchHandler(), "^/playerSearch/(.*)", "playerName"));
                ROUTES.add(new WebRoute(new PlayersHandler(), "^/players/?$"));
                ROUTES.add(new WebRoute(new SkinHandler(), "^/skin/(.*)", "playerName"));
                ROUTES.add(new WebRoute(new RedirectHandler("/", 303), "^/resources/"));
                TemplateHandler th = new TemplateHandler();
                ROUTES.add(new WebRoute(th, "^.*\\.html$"));
                ROUTES.add(new WebRoute(th, "^.*/$"));
                ROUTES.add(new WebRoute(new FileHandler(), ".*"));
                
            }

            String address = getAddress();
            if ((address == null) || (address.equals("0.0.0.0")))
                serverSocket = new ServerSocket(getPort());
            else {
                InetAddress addr = InetAddress.getByName(getAddress());
                serverSocket = new ServerSocket(getPort(), 50, addr);
            }

        } catch (Exception e) {
            Utils.warning("web server cannot be started: %s", e.getMessage());
            return;
        }

        if (workerThreads.isEmpty())
            for (int i = 0; i < getWorkers(); i++) {
                WebWorker worker = new WebWorker();
                Thread w = new Thread(worker, "web server worker " + i);
                w.setDaemon(true);
                w.start();
                workerThreads.add(worker);
            }

        serverThread = new Thread(new Runnable() {
            public void run() {
                Socket socket;
                while (started) {
                    try {
                        socket = serverSocket.accept();
                        synchronized (workerThreads) {
                            long expireTime = System.currentTimeMillis() + WORKER_TIMEOUT;
                            while (workerThreads.isEmpty() && (System.currentTimeMillis() < expireTime)) {
                                if (! started) break;
                                try {
                                    workerThreads.wait(WORKER_TIMEOUT);
                                } catch (InterruptedException ie) {}
                            }
                            if (started) {
                                if (System.currentTimeMillis() >= expireTime) {
                                    Utils.warning("web server unable to aquire worker to process request!");
                                    socket.close();
                                } else {
                                    WebWorker worker = workerThreads.remove(0);
                                    Utils.debug("web server accepted connection from %s", socket.getInetAddress().toString());
                                    worker.setSocket(socket);
                                }
                            }
                        }
                    } catch (IOException ie) {}
                }
            }
        }, "web server listener");
        started = true;
        serverThread.setDaemon(true);
        serverThread.start();
        Utils.info("web server started on %s:%s", (getAddress() == null) ? "*" : getAddress(), getPort());
    }

    public static void stop() {
        if (! started) return;
        started = false;
        serverThread.interrupt();
        serverThread = null;
        synchronized (workerThreads) {
            for (WebWorker worker : workerThreads)
                worker.stop();
            workerThreads.clear();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioe) {}
            serverSocket = null;
        }
        Utils.info("web server stopped");
    }

    public static void rejoin(WebWorker worker) {
        synchronized (workerThreads) {
            workerThreads.add(worker);
            workerThreads.notify();
        }
    }

    public static boolean getEnabled() {
        return Config.getBooleanDirect("webServer.enabled", false);
    }

    public static void setEnabled(boolean b) {
        Config.setPropertyDirect("webServer.enabled", b);
        stop();
        if (b) start();
    }

    /* Begin options */

    public static int getPort() {
        return Config.getIntDirect("webServer.port", 8080);
    }

    public static void setPort(int i) {
        if (i < 1) i = 1;
        if (i > 65535) i = 65535;
        Config.setPropertyDirect("webServer.port", i);
    }

    public static String getAddress() {
        return Config.getStringDirect("webServer.address", null);
    }

    public static void setAddress(String address) {
        if ((address != null) && (address.equals("*") || address.equals("-")))
            address = null;
        Config.setPropertyDirect("webServer.address", address);
    }

    public static int getWorkers() {
        return Config.getIntDirect("webServer.workers", 5);
    }

    public static void setWorkers(int i) {
        if (i < 1) i = 1;
        if (i > 1000) i = 1000;
        Config.setPropertyDirect("webServer.workers", i);
    }

    public static boolean getUpgradeWebRoot() {
        return Config.getBooleanDirect("webServer.upgradeWebRoot", true);
    }

    public static void setUpgradeWebRoot(boolean b) {
        Config.setPropertyDirect("webServer.upgradeWebRoot", b);
    }

    public static String[] getPlayersColumns() {
        List<String> v = getPlayersColumnsAsList();
        if (v == null) return null;
        return v.toArray(new String[] {});
    }

    public static void setPlayersColumns(String[] s) {
        if ((s != null) && (s.length > 0)) {
            if (s[0].equals("-")) s = new String[] {};
            else if (s[0].equals("*")) s = null;
        }
        if (s == null)
            setPlayersColumnsFromList(null);
        else {
            List<String> list = new ArrayList<String>(Arrays.asList(s));
            setPlayersColumnsFromList(list);
        }
    }

    public static void addPlayersColumns(String s) {
        if (s == null) return;
        List<String> list = getPlayersColumnsAsList();
        if (list == null)
            list = new ArrayList<String>();
        if (list.contains(s)) return;
        list.add(s);
        Collections.sort(list);
        setPlayersColumnsFromList(list);
    }

    public static void removePlayersColumns(String s) {
        if (s == null) return;
        List<String> list = getPlayersColumnsAsList();
        if (list == null) return;
        if (! list.contains(s)) return;
        list.remove(s);
        setPlayersColumnsFromList(list);
    }

    private static List<String> getPlayersColumnsAsList() {
        String v = Config.getStringDirect("webServer.playersColumns");
        if (v == null) return new ArrayList<String>(Arrays.asList(DEFAULT_PLAYERSCOLUMNS));
        List<String> list = new ArrayList<String>();
        for (StringTokenizer st = new StringTokenizer(v, ","); st.hasMoreTokens(); )
            list.add(st.nextToken());
        return list;
    }

    private static void setPlayersColumnsFromList(List<String> list) {
        if (list == null)
            Config.setPropertyDirect("webServer.playersColumns", null);
        else {
            StringBuilder sb = new StringBuilder();
            Set<String> allColumns = getPlayerColumns(true);
            for (String s : list) {
                if (! allColumns.contains(s)) continue;
                if (sb.length() > 0) sb.append(',');
                sb.append(s);
            }
            Config.setPropertyDirect("webServer.playersColumns", sb.toString());
        }
    }

    public static String getPlayersSortColumn() {
        return Config.getStringDirect("webServer.playersSortColumn", DEFAULT_SORTCOLUMN);
    }

    public static void setPlayersSortColumn(String s) {
        if (s != null) {
            if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("webServer.playersSortColumn", s);
    }

    public static String getPlayersSortDir() {
        return Config.getStringDirect("webServer.playersSortDir", DEFAULT_SORTDIR);
    }

    public static void setPlayersSortDir(String s) {
        if (s != null) {
            if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("webServer.playersSortDir", s);
    }

    public static String[] getPlayersRestrictColumns() {
        List<String> v = getPlayersRestrictColumnsAsList();
        if (v == null) return null;
        return v.toArray(new String[] {});
    }

    public static void setPlayersRestrictColumns(String[] s) {
        if ((s != null) && (s.length > 0)) {
            if (s[0].equals("-")) s = new String[] {};
            else if (s[0].equals("*")) s = null;
        }
        if (s == null)
            setPlayersRestrictColumnsFromList(null);
        else {
            List<String> list = new ArrayList<String>(Arrays.asList(s));
            setPlayersRestrictColumnsFromList(list);
        }
    }

    public static void addPlayersRestrictColumns(String s) {
        if (s == null) return;
        List<String> list = getPlayersRestrictColumnsAsList();
        if (list == null)
            list = new ArrayList<String>();
        if (list.contains(s)) return;
        list.add(s);
        Collections.sort(list);
        setPlayersRestrictColumnsFromList(list);
    }

    public static void removePlayersRestrictColumns(String s) {
        if (s == null) return;
        List<String> list = getPlayersRestrictColumnsAsList();
        if (list == null) return;
        if (! list.contains(s)) return;
        list.remove(s);
        setPlayersRestrictColumnsFromList(list);
    }

    public static List<String> getPlayersRestrictColumnsAsList() {
        String v = Config.getStringDirect("webServer.playersRestrictColumns");
        if (v == null) return new ArrayList<String>(Arrays.asList(DEFAULT_RESTRICTCOLUMNS));
        List<String> list = new ArrayList<String>();
        for (StringTokenizer st = new StringTokenizer(v, ","); st.hasMoreTokens(); )
            list.add(st.nextToken());
        return list;
    }

    private static void setPlayersRestrictColumnsFromList(List<String> list) {
        if (list == null)
            Config.setPropertyDirect("webServer.playersRestrictColumns", null);
        else {
            Collections.sort(list);
            StringBuilder sb = new StringBuilder();
            Set<String> allColumns = getPlayerColumns(true);
            for (String s : list) {
                if (! allColumns.contains(s)) continue;
                if (sb.length() > 0) sb.append(',');
                sb.append(s);
            }
            Config.setPropertyDirect("webServer.playersRestrictColumns", sb.toString());
        }
    }

    public static int getPlayersPageSize() {
        return Config.getIntDirect("webServer.playersPageSize", 50);
    }

    public static void setPlayersPageSize(int i) {
        if (i < 1) i = 1;
        Config.setPropertyDirect("webServer.playersPageSize", i);
    }

    public static void getOptions(Context ctx, String name) throws OptionsException, PermissionsException {
        options.getOptions(ctx, name);
    }

    public static String getOption(Context ctx, String name) throws OptionsException, PermissionsException {
        return options.getOption(ctx, name);
    }

    public static void setOption(Context ctx, String name, String value) throws OptionsException, PermissionsException {
        options.setOption(ctx, name, value);
    }

    public static void addOption(Context ctx, String name, String value) throws OptionsException, PermissionsException {
        options.addOption(ctx, name, value);
    }

    public static void removeOption(Context ctx, String name, String value) throws OptionsException, PermissionsException {
        options.removeOption(ctx, name, value);
    }

    /* End options */

    public static Set<String> getPlayerColumns(boolean restricted) {
        Set<String> columns = new HashSet<String>();
        List<String> restrictedColumns = getPlayersRestrictColumnsAsList();
        for (String statName : PlayerStats.group.getStatisticsNames()) {
            Statistic stat = PlayerStats.group.getStatistic(statName);
            if (stat.isMapped()) continue;
            switch (stat.getType()) {
                case STRING:
                case BOOLEAN:
                case TIMESTAMP:
                case ELAPSED_TIME:
                case DISTANCE:
                case INTEGER:
                case LONG:
                case FLOAT:
                case DOUBLE:
                    if (restricted || (! restrictedColumns.contains(statName)))
                        columns.add(statName);
                    break;
            }
        }
        return columns;
    }

}
