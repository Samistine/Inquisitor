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
import java.util.HashSet;
import java.util.Set;
import org.bukkit.OfflinePlayer;
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
		RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp = Global.plugin
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

	public static boolean has(Player player, String perm) {
		if (player == null)
			return true;
		try {
			require(player.getWorld().getName(), player.getName(), true, perm);
			return true;
		} catch (PermissionsException e) {
			return false;
		}
	}

	public static void require(Player player, String perm)
			throws PermissionsException {
		if (player == null)
			return;
		require(player.getWorld().getName(), player.getName(), true, perm);
	}

	public static void require(Player player, boolean requireAll,
			String... perms) throws PermissionsException {
		if (player == null)
			return;
		require(player.getWorld().getName(), player.getName(), requireAll,
				perms);
	}

	public static void require(String worldName, String playerName, String perm)
			throws PermissionsException {
		require(worldName, playerName, true, perm);
	}

	private static void require(String worldName, String playerName,
			boolean requireAll, String... perms) throws PermissionsException {
		if (isOp(playerName)) {
			Utils.debug("player '%s' is op", playerName);
			return;
		}

		try {
			if (vaultAvailable()) {
				for (String perm : perms) {
					if (requireAll) {
						if (!vaultPlugin.has(worldName, playerName, perm))
							throw new PermissionsException("not permitted");
					} else {
						if (vaultPlugin.has(worldName, playerName, perm))
							return;
					}
				}
				if ((!requireAll) && (perms.length > 0))
					throw new PermissionsException("not permitted");
				return;
			}
		} catch (Exception ex) {
			Utils.warning(
					"Vault or your Vault compatible permissions plugin threw an exception getting player permissions: %s",
					ex.getMessage());
		}

		Player player = Global.plugin.getServer().getPlayer(playerName);
		if (player != null) {
			for (String perm : perms) {
				if (requireAll) {
					if (!player.hasPermission(perm))
						throw new PermissionsException("not permitted");
				} else {
					if (player.hasPermission(perm))
						return;
				}
			}
			if ((!requireAll) && (perms.length > 0))
				throw new PermissionsException("not permitted");
			return;
		}

		throw new PermissionsException("not permitted");

	}

	public static boolean isOp(Player player) {
		if (player == null)
			return true;
		return player.isOp();
	}

	public static boolean isOp(String playerName) {
		// Set<OfflinePlayer> ops = Global.plugin.getServer().getOperators();
		for (OfflinePlayer p : Global.plugin.getServer().getOperators())
			if (p.getName().equalsIgnoreCase(playerName))
				return true;
		return false;
	}

	/*
	 * public static boolean canGetGroups() { return vaultAvailable(); }
	 */

	public static Set<String> getGroups(Player player) {
		try {
			if (vaultAvailable())
				return new HashSet<String>(Arrays.asList(vaultPlugin
						.getPlayerGroups(player)));
		} catch (Exception ex) {
			Utils.warning("Vault threw an exception getting player groups: %s",
					ex.getMessage());
		}
		return new HashSet<String>();
	}

}
