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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Permissions {

    private static boolean vaultChecked = false;
    private static net.milkbowl.vault.permission.Permission vaultPlugin = null;

    public static boolean vaultAvailable() {
        if (vaultPlugin != null && vaultPlugin.isEnabled()) {
            return true;
        }
        if (vaultChecked) {
            return false;
        }
        vaultChecked = true;
        Plugin p = Global.getServer().getPluginManager().getPlugin("Vault");
        if (p == null || !p.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp = Global
                .getServer()
                .getServicesManager()
                .getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (rsp == null) {
            Utils.warning("Vault didn't return a service provider for permissions!");
            return false;
        }
        net.milkbowl.vault.permission.Permission plugin = rsp.getProvider();
        if (plugin == null) {
            Utils.warning("Vault didn't return a permissions provider!");
            return false;
        }
        vaultPlugin = plugin;
        Utils.info("Initialized Vault for Permissions");
        return true;
    }

    /*
     * public static boolean canGetGroups() { return vaultAvailable(); }
     */
    public static Set<String> getGroups(Player player) {
        try {
            if (vaultAvailable()) {
                return new HashSet<>(Arrays.asList(vaultPlugin.getPlayerGroups(player)));
            }
        } catch (Exception ex) {
            Utils.warning("Vault threw an exception getting player groups: %s", ex.getMessage());
        }
        return Collections.emptySet();
    }

}
