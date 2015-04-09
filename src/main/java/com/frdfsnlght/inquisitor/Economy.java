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
	private static net.milkbowl.vault.economy.Economy vaultPlugin = null;

	public static boolean vaultAvailable() {
		if ((vaultPlugin != null) && vaultPlugin.isEnabled())
			return true;
		if (vaultChecked)
			return false;
		vaultChecked = true;
		Plugin p = Global.plugin.getServer().getPluginManager()
				.getPlugin("Vault");
		if (p == null)
			return false;
		if (!p.isEnabled())
			return false;
		RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = Global.plugin
				.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (rsp == null) {
			Utils.warning("Vault didn't return a service provider for economy!");
			return false;
		}
		net.milkbowl.vault.economy.Economy plugin = rsp.getProvider();
		if (plugin == null) {
			Utils.warning("Vault didn't return an economy provider!");
			return false;
		}
		vaultPlugin = plugin;
		Utils.info("Initialized Vault for Economy");
		return true;
	}

	/*
	 * public static boolean canGetBalance() { return vaultAvailable(); }
	 */

	public static double getBalanace(Player player) {
		try {
			if (vaultAvailable()) {
				return vaultPlugin.getBalance(player.getName());
			}
		} catch (Exception ex) {
			Utils.warning(
					"Vault or your Vault compatible economy plugin threw an exception getting player balance: %s",
					ex.getMessage());
		}
		return 0.0d;
	}

}
