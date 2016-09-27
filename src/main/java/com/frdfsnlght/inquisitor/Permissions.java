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

import com.frdfsnlght.inquisitor.exceptions.PermissionsException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.World;
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

    static void require(World world, Player player, boolean requireAll, String... perms) throws PermissionsException {
        String worldName = world.getName();

        if (player == null) throw new PermissionsException("not permitted");

        if (player.isOp()) {
            Utils.debug("player '%s' is op", player.getName());
            return;
        }

        try {
            if (vaultAvailable()) {
                for (String perm : perms) {
                    if (requireAll) {
                        if (!vaultPlugin.playerHas(worldName, player, perm)) {
                            throw new PermissionsException("not permitted");
                        }
                    } else {
                        if (vaultPlugin.playerHas(worldName, player, perm)) {
                            return;
                        }
                    }
                }
                if ((!requireAll) && (perms.length > 0)) {
                    throw new PermissionsException("not permitted");
                }
                return;
            }
        } catch (Exception ex) {
            Utils.warning(
                    "Vault or your Vault compatible permissions plugin threw an exception getting player permissions: %s",
                    ex.getMessage());
        }

        for (String perm : perms) {
            if (requireAll) {
                if (!player.hasPermission(perm)) {
                    throw new PermissionsException("not permitted");
                }
            } else {
                if (player.hasPermission(perm)) {
                    return;
                }
            }
        }
        if ((!requireAll) && (perms.length > 0)) {
            throw new PermissionsException("not permitted");
        }
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
        return new HashSet<>();
    }

}
