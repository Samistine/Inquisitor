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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Context {

    private static final ChatColor HL_ON = ChatColor.DARK_PURPLE;
    private static final ChatColor HL_OFF = ChatColor.WHITE;

    private CommandSender sender = null;

    public Context() {}

    public Context(CommandSender sender) {
        this.sender = sender;
    }

    public CommandSender getSender() {
        return sender;
    }

    /**
     * Send a message to this context. If this context is null then we log it to console
     *
     * @param msg message to send
     * @param args arguments
     */
    public void send(String msg, Object ... args) {
        if (args.length > 0) msg = String.format(msg, args);
        if (msg.isEmpty()) return;

        if (sender != null)
            sender.sendMessage(HL_ON + "[" + Global.pluginName + "] " + HL_OFF + msg);
        else
            Utils.info(msg);
    }

    /**
     * Send a message to this context & additionally log the context and message
     *
     * @param msg message to send
     * @param args arguments
     */
    public void sendLog(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        if (msg.isEmpty()) return;
        send(msg);

        //In addition we log player actions to console.
        if (isPlayer()) {
            Utils.info("->[%s] %s", sender.getName(), msg);
        }
    }

    /**
     * Send a warning to this context. If this context is null then we log it to console
     *
     * @param msg message to send
     * @param args arguments
     */
    public void warn(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        if (msg.isEmpty()) return;
        if (sender == null)
            Utils.warning(msg);
        else
            sender.sendMessage(HL_ON + "[" + Global.pluginName + "] " + ChatColor.RED + msg);
    }

    /**
     * Send a warning to this context & additionally log the context's name and warning
     *
     * @param msg message to send
     * @param args arguments
     */
    public void warnLog(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        if (msg.isEmpty()) return;
        warn(msg);

        //In addition we log player actions to console.
        if (isPlayer()) {
            Utils.info("->[%s] %s", sender.getName(), msg);
        }
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    public boolean isSystem() {
        return sender == null;
    }

    public boolean isOp() {
        return sender.isOp();
    }

    public Player getPlayer() {
        if (! isPlayer()) return null;
        return (Player)sender;
    }

    public boolean has(String perm) {
        Player player = getPlayer();
        if (player == null) return true;
        return sender.hasPermission(perm);
    }

    public void requirePermission(String perm) throws PermissionsException {
        if (!has(perm)) {
            throw new PermissionsException("not permitted");
        }
    }

}
