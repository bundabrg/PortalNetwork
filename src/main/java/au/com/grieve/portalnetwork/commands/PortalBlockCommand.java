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

import au.com.grieve.bcf.annotations.Arg;
import au.com.grieve.bcf.annotations.Default;
import au.com.grieve.bcf.annotations.Description;
import au.com.grieve.portalnetwork.Portal;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

@Arg("portalblock|pb")
public class PortalBlockCommand extends MainCommand {

    @Default
    public void onMissing(CommandSender sender) {
        sender.spigot().sendMessage(
                new ComponentBuilder("=== [ HERE ] ===").color(ChatColor.AQUA).create()
        );

        // Show list of child commands
    }

    @Arg("give|g @portaltype(switch=type|t, default=NETHER) @player(required=true, default=%self, mode=online)")
    @Description("Give player a portal block")
    public void onGive(CommandSender sender, Portal.PortalType portalType, Player player) {
        // Create a Portal Block
        ItemStack item = new ItemStack(Material.BEACON, 1);
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        meta.setDisplayName("Portal Block");
        meta.getPersistentDataContainer().set(Portal.PortalTypeKey, PersistentDataType.STRING, portalType.toString());
        item.setItemMeta(meta);
        player.getInventory().addItem(Portal.CreatePortalBlock(portalType));

        sender.spigot().sendMessage(
                new ComponentBuilder("Giving " + player.getName() + " a " + portalType + " portal block.").create()
        );

        if (!sender.equals(player)) {
            player.spigot().sendMessage(
                    new ComponentBuilder("You have received a Portal Block.").create()
            );
        }
    }


}
