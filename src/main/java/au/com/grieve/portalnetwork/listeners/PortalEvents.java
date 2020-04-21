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

import au.com.grieve.portalnetwork.PortalManager;
import au.com.grieve.portalnetwork.PortalNetwork;
import au.com.grieve.portalnetwork.exceptions.InvalidPortalException;
import au.com.grieve.portalnetwork.portals.BasePortal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;

import java.util.HashMap;
import java.util.Map;


public class PortalEvents implements Listener {

    Map<Player, BlockVector> ignore = new HashMap<>();

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        PortalManager manager = PortalNetwork.getInstance().getPortalManager();

        // Check if player is breaking a portal block
        BasePortal portal = manager.getPortal(event.getBlock().getLocation());
        if (portal == null) {
            // Check the rest of the portal
            portal = manager.find(event.getBlock().getLocation());
        }

        if (portal == null) {
            return;
        }

        portal.handleBlockBreak(event);
    }

//    @EventHandler
//    public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
//        System.err.println("Physics: " + event + " - " + event.getBlock().getType() + " - " + event.getBlock().getLocation());
//        // We cancel any physics events in a portal
//        if (event.getBlock().getType() != Material.NETHER_PORTAL) {
//            return;
//        }
//
//        event.setCancelled(true);
//
////        Portal portal = PortalNetwork.getInstance().getPortalManager().find(event.getBlock().getLocation());
////        if (portal != null) {
////            event.setCancelled(true);
////        }
//    }


    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getClickedBlock().getLocation());

        if (portal == null) {
            return;
        }

        portal.handlePlayerInteract(event);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        ignore.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        // If ignored player has moved enough we stop ignoring
        if (ignore.containsKey(event.getPlayer())) {
            if (ignore.get(event.getPlayer()).distance(event.getPlayer().getLocation().toVector()) > 2) {
                ignore.remove(event.getPlayer());
            }
            return;
        }

        // If player has not actually moved, ignore
        if (event.getTo() == null || event.getFrom().toVector().toBlockVector() == event.getTo().toVector().toBlockVector()) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getTo());

        if (portal == null) {
            return;
        }

        portal.handlePlayerMove(event);

        ignore.put(event.getPlayer(), event.getTo().toVector().toBlockVector());
    }

    // Probably should move this inside nether portal class
    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent event) {

        // Only interested if its one of the 3 portal types
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL &&
                event.getCause() != PlayerTeleportEvent.TeleportCause.END_GATEWAY &&
                event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getFrom(), 1);

        if (portal == null) {
            return;
        }

        // Make sure portal is dialled
        if (portal.getDialledPortal() == null) {
            return;
        }

        // Cancel event
        event.setCancelled(true);
    }


    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        // Check if player is trying to place a portal block
        ItemMeta meta = event.getItemInHand().getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer();
            if (meta.getPersistentDataContainer().has(BasePortal.PortalTypeKey, PersistentDataType.STRING)) {
                String portalType = meta.getPersistentDataContainer().get(BasePortal.PortalTypeKey, PersistentDataType.STRING);
                try {
                    PortalNetwork.getInstance().getPortalManager().createPortal(portalType, event.getBlockPlaced().getLocation());
                } catch (InvalidPortalException e) {
                    e.printStackTrace();
                }
            }
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getBlock().getLocation());

        if (portal == null) {
            return;
        }

        portal.handleBlockPlace(event);
    }

}
