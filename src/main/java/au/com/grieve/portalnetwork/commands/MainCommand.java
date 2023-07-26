/*
 * PortalNetwork - Portals for Players
 * Copyright (C) 2023 PortalNetwork Developers
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

import au.com.grieve.bcf.annotation.Arg;
import au.com.grieve.bcf.annotation.Command;
import au.com.grieve.bcf.annotation.Default;
import au.com.grieve.bcf.platform.minecraft.bukkit.annotation.Permission;
import au.com.grieve.bcf.platform.minecraft.bukkit.impl.command.BukkitAnnotationCommand;
import au.com.grieve.portalnetwork.PortalNetwork;
import au.com.grieve.portalnetwork.exceptions.InvalidPortalException;
import au.com.grieve.portalnetwork.portals.BasePortal;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;


@Command("portalnetwork|pn")
@Permission("portalnetwork.admin")
@Permission("portalnetwork.command.reload")
@Permission("portalnetwork.command.list")
@Permission("portalnetwork.command.give")
public class MainCommand extends BukkitAnnotationCommand {

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

//    @Error
//    @Override
//    public void onError(CommandSender sender, List<ExecutionError> errors) {
//        System.err.println(errors);
//    }

    @Arg("reload(description=Reload Plugin)")
    @Permission("portalnetwork.admin")
    @Permission("portalnetwork.command.reload")
    public void onReload(CommandSender sender) {
        // Read main config
        try {
            PortalNetwork.getInstance().reload();

            sender.spigot().sendMessage(
                    new ComponentBuilder("Reloaded PortalNetwork").color(ChatColor.YELLOW).create())
            ;
        } catch (IOException e) {
            sender.spigot().sendMessage(
                    new ComponentBuilder("Failed to reload PortalNetwork").color(ChatColor.RED).create()
            );
        }
    }

    @Arg("list(description=List placed portals)")
    @Permission("portalnetwork.admin")
    @Permission("portalnetwork.command.list")
    public void onList(CommandSender sender) {
        sender.spigot().sendMessage(
                new ComponentBuilder("========= [ List of Portals ] =========").color(ChatColor.AQUA).create()
        );

        for (BasePortal portal : PortalNetwork.getInstance().getPortalManager().getPortals()) {
            if (portal.getLocation().getWorld() == null) {
                continue;
            }

            String worldName = portal.getLocation().getWorld().getName();
            switch (worldName) {
                case "world":
                    worldName = "overworld";
                    break;
                case "world_nether":
                    worldName = "nether";
                    break;
                case "world_the_end":
                    worldName = "the_end";
                    break;
            }

            ComponentBuilder msg = new ComponentBuilder(
                    "[" + portal.getLocation().getX() + ";" +
                            portal.getLocation().getY() + ";" +
                            portal.getLocation().getZ() + ";" +
                            worldName + "] ").color(ChatColor.GREEN)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/execute in " + worldName + " run tp " + sender.getName() + " " +
                                    portal.getLocation().getX() + " " +
                                    (portal.getLocation().getY() + 1) + " " +
                                    portal.getLocation().getZ()
                    ))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Teleport to Portal").create())))
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

    // @player(required=true, default=%self, mode=online)"
    @Arg("give|g(description=Give player a portal block) @portaltype(switch=type|t, default=NETHER) @player(required=true, default=%self, mode=online)")
    @Permission("portalnetwork.admin")
    @Permission("portalnetwork.command.give")
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
