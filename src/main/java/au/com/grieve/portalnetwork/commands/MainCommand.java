/*
 * PortalNetwork - Portals for Players
 * Copyright (C) 2020 PortalNetwork Developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.com.grieve.portalnetwork.commands;

import au.com.grieve.bcf.BaseCommand;
import au.com.grieve.bcf.annotations.Error;
import au.com.grieve.bcf.annotations.*;
import au.com.grieve.portalnetwork.PortalNetwork;
import au.com.grieve.portalnetwork.exceptions.InvalidPortalException;
import au.com.grieve.portalnetwork.portals.BasePortal;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


@Command("portalnetwork|pn")
public class MainCommand extends BaseCommand {

    @Default
    public void onDefault(CommandSender sender) {
        sender.spigot().sendMessage(
                new ComponentBuilder("========= [ PortalNetwork Help ] =========").color(ChatColor.AQUA).create()
        );

        sender.spigot().sendMessage(
                new ComponentBuilder("/pn <command> help").color(ChatColor.DARK_AQUA)
                        .append(" - Show help about command").color(ChatColor.GRAY).create()
        );

        // Show list of child commands
    }

    @Error
    public void onError(CommandSender sender, String message) {
        sender.spigot().sendMessage(
                new ComponentBuilder(message).color(ChatColor.RED).create()
        );
    }

    @Arg("reload")
    public void onReload(CommandSender sender) {
        // Read main config
        PortalNetwork.getInstance().reload();

        sender.spigot().sendMessage(
                new ComponentBuilder("Reloaded PortalNetwork").color(ChatColor.YELLOW).create()
        );
    }

    @Arg("list")
    public void onList(CommandSender sender) {
        sender.spigot().sendMessage(
                new ComponentBuilder("========= [ List of Portals ] =========").color(ChatColor.AQUA).create()
        );

        for (BasePortal portal : PortalNetwork.getInstance().getPortalManager().getPortals()) {
            @SuppressWarnings("ConstantConditions") ComponentBuilder msg = new ComponentBuilder(
                    "[" + portal.getLocation().getX() + ";" +
                            portal.getLocation().getY() + ";" +
                            portal.getLocation().getZ() + ";" +
                            portal.getLocation().getWorld().getName() + "] ").color(ChatColor.GREEN)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + sender.getName() + " " +
                                    portal.getLocation().getX() + " " +
                                    (portal.getLocation().getY() + 1) + " " +
                                    portal.getLocation().getZ()
                    ))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to Portal").create()))
                    .append("").event((HoverEvent) null).event((ClickEvent) null);


            if (!portal.isValid()) {
                msg.append("[invalid]").color(ChatColor.RED);
            } else {
                msg.append(portal.getNetwork() + ":" + portal.getAddress() + " ").color(ChatColor.YELLOW);
                if (portal.getDialledPortal() == null) {
                    msg.append("[disconnected]").color(ChatColor.RED);
                } else {
                    msg.append("connected:" + portal.getDialledPortal().getAddress());
                }
            }

            sender.spigot().sendMessage(msg.create());
        }

        sender.spigot().sendMessage(
                new ComponentBuilder("=====================================").color(ChatColor.AQUA).create()
        );
    }

    @Arg("give|g @portaltype(switch=type|t, default=NETHER) @player(required=true, default=%self, mode=online)")
    @Description("Give player a portal block")
    public void onGive(CommandSender sender, String portalType, Player player) {
        ItemStack item;

        try {
            item = PortalNetwork.getInstance().getPortalManager().createPortalBlock(portalType);
            player.getInventory().addItem(item);

            sender.spigot().sendMessage(
                    new ComponentBuilder("Giving " + player.getName() + " a portal block.").create()
            );

            if (!sender.equals(player)) {
                player.spigot().sendMessage(
                        new ComponentBuilder("You have received a Portal Block.").create()
                );
            }
        } catch (InvalidPortalException e) {
            sender.spigot().sendMessage(
                    new ComponentBuilder("Unable to create block.").color(ChatColor.RED).create()
            );
        }
    }


}
