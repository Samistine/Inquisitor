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

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Economy {

    private static boolean vaultChecked = false;
    private static net.milkbowl.vault.economy.Economy vaultService = null;

    /**
     * Check if the Vault Economy API is available.
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

            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = Global
                    .getServer()
                    .getServicesManager()
                    .getRegistration(net.milkbowl.vault.economy.Economy.class);

            if (rsp == null) {
                Utils.warning("Vault didn't return a service provider for economy!");
                return false;
            }

            net.milkbowl.vault.economy.Economy provider = rsp.getProvider();

            if (provider == null) {
                Utils.warning("Vault didn't return an economy provider!");
                return false;
            } else {
                vaultService = provider;
                Utils.info("Initialized Vault for Economy");
                return true;
            }

        } finally {
            vaultChecked = true;
        }
    }

    /**
     * Public method to check if the {@link #getBalanace(org.bukkit.entity.Player) }
     * method will attempt to return accurate results.
     *
     * @return true for economy integration with Vault
     */
    public static boolean canGetBalance() {
        return vaultAvailable();
    }

    /**
     * Get the balance of the player.
     *
     * <br>
     * Returns 0.0 if Vault is not installed,
     * you don't have an economy plugin,
     * your economy plugin doesn't support Vault,
     * or a general error occurs.
     *
     * @param player
     * @return balance
     */
    public static double getBalanace(Player player) {
        try {
            if (vaultAvailable()) {
                return vaultService.getBalance(player);
            }
        } catch (Exception ex) {
            Utils.warning("Vault or your Vault compatible economy plugin threw an exception getting player balance: %s", ex.getMessage());
        }
        return 0.0d;
    }

}
