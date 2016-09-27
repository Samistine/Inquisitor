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

import com.frdfsnlght.inquisitor.webserver.WebServer;
import com.frdfsnlght.inquisitor.exceptions.InquisitorException;
import com.frdfsnlght.inquisitor.api.API;
import com.frdfsnlght.inquisitor.command.CommandException;
import com.frdfsnlght.inquisitor.command.CommandProcessor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.Metrics;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class Inquisitor extends JavaPlugin {

    private BlockListenerImpl blockListener;
    private PlayerListenerImpl playerListener;
    private EntityListenerImpl entityListener;

    private API api = null;

    @Override
    public void onEnable() {
        Global.plugin = this;
        PluginDescriptionFile pdf = getDescription();
        Global.pluginName = pdf.getName();
        Global.pluginVersion = pdf.getVersion();
        Global.started = false;

        Global.enabled = true;

        final Context ctx = new Context(getServer().getConsoleSender());

        // install/update resources
        File dataFolder = Global.getDataFolder();

        if (!dataFolder.exists()) {
            ctx.sendLog("creating data folder");
            dataFolder.mkdirs();
        }
        Utils.copyFileFromJar("/resources/LICENSE.txt", dataFolder, true);
        Utils.copyFileFromJar("/resources/README.txt", dataFolder, true);

        if (Utils.copyFileFromJar("/resources/config.yml", dataFolder, false)) {
            ctx.sendLog("installed default configuration");
        }

        // copy FreeMarker
        if (Utils.copyFileFromJar("/resources/freemarker.jar", dataFolder, false)) {
            ctx.sendLog("installed FreeMarker");
        }

        // add FreeMarker to class path
        // add FreeMarker to class path
        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();

        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            try {
                URL u = (new File(Global.getDataFolder(), "freemarker.jar")).toURI().toURL();
                method.invoke(classLoader, u);
            } catch (IllegalAccessException | InvocationTargetException | MalformedURLException ex) {
                ex.printStackTrace();
            }

        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        }

        Config.load(ctx);

        Utils.checkVersion();

        blockListener = new BlockListenerImpl();
        playerListener = new PlayerListenerImpl();
        entityListener = new EntityListenerImpl();

        DB.init();
        StatisticsManager.init();
        PlayerStats.init();
        WebServer.init();

        DB.start();

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(blockListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(entityListener, this);

        Global.started = true;

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            ctx.warn("unable to start metrics: %s", e.getMessage());
        }

        ctx.sendLog("ready on server '%s'", getServer().getServerName());

//		int x = 0;
//		while (x < 100) {
//			Material m = Material.getMaterial(x);
//			ctx.sendLog(m.getId() + ":" + m.toString());
//			x++;
//		}
    }

    @Override
    public void onDisable() {
        if (!Global.enabled) {
            return;
        }
        Global.enabled = false;
        Context ctx = new Context(getServer().getConsoleSender());
        Config.save(ctx);
        DB.stop();
        ctx.sendLog("disabled");
        Global.plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] rawArgs) {
        // Rebuild quoted arguments
        List<String> args = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder argBuffer = null;
        for (String arg : rawArgs) {
            if (arg.isEmpty()) {
                continue;
            }
            if (inQuotes) {
                argBuffer.append(" ");
                argBuffer.append(arg);
                if (arg.endsWith("\"")) {
                    argBuffer.deleteCharAt(argBuffer.length() - 1);
                    inQuotes = false;
                    args.add(argBuffer.toString());
                    argBuffer = null;
                }
            } else if (arg.startsWith("\"")) {
                argBuffer = new StringBuilder(arg);
                argBuffer.deleteCharAt(0);
                if ((arg.length() > 1) && arg.endsWith("\"")) {
                    argBuffer.deleteCharAt(argBuffer.length() - 1);
                    args.add(argBuffer.toString());
                    argBuffer = null;
                } else {
                    inQuotes = true;
                }
            } else {
                args.add(arg);
            }
        }
        if (argBuffer != null) {
            args.add(argBuffer.toString());
        }

        Context ctx = new Context(sender);

        if (args.isEmpty()) {
            ctx.send("this is v%s", Global.pluginVersion);
            return true;
        }

        // Find the matching commands
        List<CommandProcessor> cps = Global.commands.stream()
                .filter(cp -> cp.matches(ctx, cmd, args))
                .collect(Collectors.toList());

        // Execute the matching command
        try {
            if (cps.isEmpty()) {
                throw new CommandException("huh? try %sinq help",
                        (ctx.isPlayer() ? "/" : ""));
            }
            if (cps.size() > 1) {
                throw new CommandException("ambiguous command; try %sinq help",
                        (ctx.isPlayer() ? "/" : ""));
            }
            cps.get(0).process(ctx, cmd, args);
            return true;
        } catch (InquisitorException te) {
            ctx.warn(te.getMessage());
            return true;
        }
    }

    public API getAPI() {
        if (api == null) {
            api = new API();
        }
        return api;
    }

}
