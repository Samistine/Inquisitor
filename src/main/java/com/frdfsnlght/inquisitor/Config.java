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

import com.frdfsnlght.inquisitor.exceptions.OptionsException;
import com.frdfsnlght.inquisitor.exceptions.PermissionsException;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Config {

    private static final Set<String> OPTIONS = new HashSet<String>();
    private static final Options options;
    private static TypeMap config = null;

    static {
        OPTIONS.add("debug");

        options = new Options(Config.class, OPTIONS, "inq", new OptionsListener() {
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.sendLog("global option '%s' set to '%s'", name, value);
            }
            public String getOptionPermission(Context ctx, String name) {
                return name;
            }
        });
    }

    public static File getConfigFile() {
        File dataFolder = Global.plugin.getDataFolder();
        return new File(dataFolder, "config.yml");
    }

    public static void load(Context ctx) {
        File confFile = getConfigFile();
        config = new TypeMap(confFile);
        config.load();
        ctx.sendLog("loaded configuration");
    }

    public static void save(Context ctx) {
        if (config == null) return;
        File configDir = Global.plugin.getDataFolder();
        if (! configDir.exists()) configDir.mkdirs();
        config.save();
        if (ctx != null)
            ctx.sendLog("saved configuration");
    }

    public static String getStringDirect(String path) {
        return config.getString(path, null);
    }

    public static String getStringDirect(String path, String def) {
        return config.getString(path, def);
    }

    public static int getIntDirect(String path, int def) {
        return config.getInt(path, def);
    }

    public static boolean getBooleanDirect(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public static List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public static List<TypeMap> getMapList(String path) {
        return config.getMapList(path);
    }

    public static TypeMap getMap(String path) {
        return config.getMap(path);
    }

    public static void setPropertyDirect(String path, Object v) {
        if (config == null) return;
        if (v == null)
            config.remove(path);
        else
            config.set(path, v);
    }



    /* Begin options */

    public static boolean getDebug() {
        return config.getBoolean("global.debug", false);
    }

    public static void setDebug(boolean b) {
        config.set("global.debug", b);
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

    /* End options */

}
