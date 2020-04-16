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

package au.com.grieve.portalnetwork.listeners;

import au.com.grieve.portalnetwork.Portal;
import au.com.grieve.portalnetwork.PortalManager;
import au.com.grieve.portalnetwork.PortalNetwork;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;


public class PortalEvents implements Listener {

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        Portal portal = manager.find(event.getBlock().getLocation());

        if (portal == null) {
            return;
        }

        // If the block broken is the portal block itself, we remove the portal and drop the block
        if (portal.getLocation().equals(event.getBlock().getLocation())) {
            portal.remove();
            event.setDropItems(false);
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), portal.createPortalBlock());
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        System.err.println(event);
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            System.err.println("Action: " + event.getAction() + " - " + event.getClickedBlock());
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        Portal portal = manager.find(event.getClickedBlock().getLocation());

        if (portal == null) {
            return;
        }
        event.setCancelled(true);

        // Player has right clicked portal so lets dial next address if any, else deactivate
        System.err.println("Reached dial");
        if (portal.isValid()) {
            portal.dialNext();
        }
    }

    /**
     * Check if player is trying to place a portal block
     */
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        ItemMeta meta = event.getItemInHand().getItemMeta();
        if (meta == null) {
            return;
        }

        if (!meta.getPersistentDataContainer().has(Portal.PortalTypeKey, PersistentDataType.STRING)) {
            return;
        }

        // It's a portal block so lookup type and try to create it
        Portal.PortalType portalType = Portal.PortalType.valueOf(meta.getPersistentDataContainer().get(Portal.PortalTypeKey, PersistentDataType.STRING));

        PortalNetwork.getInstance().getPortalManager().createPortal(event.getBlockPlaced().getLocation(), portalType);
    }

}
