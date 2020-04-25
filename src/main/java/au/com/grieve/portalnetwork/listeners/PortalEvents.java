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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;


public class PortalEvents implements Listener {

    final Map<Player, BlockVector> ignore = new HashMap<>();

    // Stop burning portal
    @EventHandler
    public void onBlockBurnEvent(BlockBurnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getBlock().getLocation());
        if (portal != null) {
            portal.handleBlockBurn(event);
        }
    }

    // Stop Exploding
    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getBlock().getLocation());
        if (portal != null) {
            portal.handleBlockExplode(event);
        }
    }

    // Stop ignition
    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getBlock().getLocation());
        if (portal != null) {
            portal.handleBlockIgnite(event);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

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

        // If its the portal block we remove portal and drop the block
        if (event.getBlock().getLocation().toVector().toBlockVector().equals(portal.getLocation().toVector().toBlockVector())) {
            event.setDropItems(false);
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getBlock().getLocation().getWorld() != null) {
                try {
                    event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), PortalNetwork.getInstance().getPortalManager().createPortalBlock(portal));
                } catch (InvalidPortalException ignored) {
                }
            }
            portal.remove();
            return;
        }

        portal.handleBlockBreak(event);


    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

//        if (event.useInteractedBlock().equals(Event.Result.DENY)) {
//            return;
//        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getClickedBlock().getLocation());

        if (portal == null) {
            return;
        }

        portal.handlePlayerInteract(event);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        ignore.remove(event.getPlayer());
    }

    @SuppressWarnings("unused")
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

        Vector velocity = event.getTo().toVector().subtract(event.getFrom().toVector());

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        Location loc = event.getFrom().clone();
        if (velocity.getZ() < 0) {
            loc = loc.add(new Vector(0, 0, 0.0));
        } else {
            loc = loc.add(new Vector(0, 0, -1.0));
        }

        if (velocity.getX() < 0) {
            loc = loc.add(new Vector(0.0, 0, 0));
        } else {
            loc = loc.add(new Vector(-1.0, 0, 0));
        }

        // X and Z to nearest whole number
        loc.setX(Math.round(loc.getX()));
        loc.setZ(Math.round(loc.getZ()));

        BasePortal portal = manager.findByPortal(loc);

        if (portal == null) {
            return;
        }

        portal.handlePlayerMove(event);
        ignore.put(event.getPlayer(), event.getTo().toVector().toBlockVector());
    }

    // Probably should move this inside nether portal class
    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Only interested if its one of the 3 portal types
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL &&
                event.getCause() != PlayerTeleportEvent.TeleportCause.END_GATEWAY &&
                event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getFrom(), 2);

        if (portal == null) {
            return;
        }

//        // Make sure portal is dialled
//        if (portal.getDialledPortal() == null) {
//            return;
//        }

        // Cancel event
        event.setCancelled(true);
    }


    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

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
        BasePortal portal = manager.find(event.getBlock().getLocation(), 2);

        if (portal == null) {
            return;
        }

        portal.handleBlockPlace(event);
    }

}
