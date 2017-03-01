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
    private static net.milkbowl.vault.permission.Permission vaultService = null;

    /**
     * Check if the Vault Permission API is available and that it supports groups.
     * <br>
     * This method is synchronized to prevent two threads from initializing vault simultaneously.
     *
     * @return true if API is available
     */
    private static synchronized boolean vaultAvailable() {
        //Returns true if Vault was already aquired and Vault is still running
        if (vaultService != null && vaultService.isEnabled()) return true;

        //Return false because we already did initialization for Vault
        if (vaultChecked) return false;

        try {
            //Get the Vault Plugin
            Plugin p = Global.getServer().getPluginManager().getPlugin("Vault");

            //If the plugin doesn't exist? Break and return false!
            if (p == null || !p.isEnabled()) return false;

            RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp = Global
                    .getServer()
                    .getServicesManager()
                    .getRegistration(net.milkbowl.vault.permission.Permission.class);

            if (rsp == null) {
                Utils.warning("Vault didn't return a service provider for permissions!");
                return false;
            }

            net.milkbowl.vault.permission.Permission provider = rsp.getProvider();

            if (provider == null) {
                Utils.warning("Vault didn't return a permissions provider!");
                return false;
            } else if (!provider.hasGroupSupport()) {
                Utils.warning("Vault returned false on its permissions provider for hasGroupSupport.");
                return false;
            } else {
                vaultService = provider;
                Utils.info("Initialized Vault for Permissions");
                return true;
            }

        } finally {
            vaultChecked = true;
        }
    }

    /**
     * Public method to check if the {@link #getGroups(org.bukkit.entity.Player) }
     * method will attempt to return accurate results.
     *
     * @return true for permissions integration with Vault
     */
    public static boolean canGetGroups() {
        return vaultAvailable();
    }

    /**
     * Get the permission groups that the player is part of.
     *
     * <br>
     * Return an unmodifiable empty set if Vault is not installed,
     * your permissions permissions plugin doesn't support Vault or groups,
     * or a general error occurs.
     *
     * @param player
     * @return groups
     */
    public static Set<String> getGroups(Player player) {
        try {
            if (vaultAvailable()) {
                return new HashSet<>(Arrays.asList(vaultService.getPlayerGroups(player)));
            }
        } catch (Exception ex) {
            Utils.warning("Vault threw an exception getting player groups: %s", ex.getMessage());
        }
        return Collections.emptySet();
    }

}
