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
import au.com.grieve.portalnetwork.portals.BasePortal;
import com.google.common.collect.Streams;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;


public class PortalEvents implements Listener {

    Map<Player, BlockVector> ignore = new HashMap<>();

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getBlock().getLocation());

        if (portal == null) {
            return;
        }

        // If it's the frame we cancel drops
        //noinspection UnstableApiUsage
        if (Streams.stream(portal.getPortalFrameIterator()).anyMatch(l -> event.getBlock().getLocation().equals(l))) {
            event.setDropItems(false);
        }

        portal.dial(null);
        new BukkitRunnable() {

            @Override
            public void run() {
                portal.update();
            }
        }.runTaskLater(PortalNetwork.getInstance(), 1);

        // If it was the portal block we remove the portal as well
        if (portal.getLocation().equals(event.getBlock().getLocation())) {
            event.setDropItems(false);
            portal.remove();
//            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
//                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), portal.createPortalBlock());
//            }
        }
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
        System.err.println(event);
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getClickedBlock().getLocation());

        // If its not the portal base we are not interested
        if (portal == null || event.getClickedBlock().getLocation().equals(portal.getLocation()) || !Streams.stream(portal.getPortalBaseIterator()).anyMatch(l -> event.getClickedBlock().getLocation().equals(l))) {
            return;
        }

        event.setCancelled(true);

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Player has right clicked portal so lets dial next address if any, else deactivate
        if (portal.isValid()) {
            portal.dialNext();
        }
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

        // If player has not moved, ignore
        if (event.getFrom().toVector().toBlockVector() == event.getTo().toVector().toBlockVector()) {
            return;
        }

        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getTo());
        Player player = event.getPlayer();

        if (portal == null || portal.getDialledPortal() == null) {
            ignore.remove(player);
            return;
        }

        // teleport to relative portal position

        Location fromPortalLocation = portal.getLocation().clone().setDirection(portal.getDirection()).add(new Vector(0.5, 0, 0.5));
        ;
        Location toPortalLocation = portal.getDialledPortal().getLocation().clone().setDirection(portal.getDialledPortal().getDirection()).add(new Vector(0.5, 0, 0.5));
        float yawDiff = fromPortalLocation.getYaw() - toPortalLocation.getYaw();

        System.err.println("fromPortal: " + fromPortalLocation);
        System.err.println("toPortal: " + toPortalLocation);


        Vector playerRelativePosition = event.getTo().toVector().subtract(fromPortalLocation.toVector());
        System.err.println("playerRelativePositionToFrom: " + playerRelativePosition);
        playerRelativePosition.rotateAroundY(Math.toRadians(yawDiff));

        Location destination = toPortalLocation.clone().add(playerRelativePosition);
        System.err.println("playerRelativePositionToTo: " + playerRelativePosition);
        System.err.println("playerCurr: " + player.getLocation());
        System.err.println("playerDest: " + destination);

        destination.setYaw(player.getLocation().getYaw() - yawDiff);
        destination.setPitch(player.getLocation().getPitch());

        Vector oldVelocity = player.getVelocity();
        Vector newVelocity = oldVelocity.clone().rotateAroundY(Math.toRadians(yawDiff));

        System.err.println("oldVelocity: " + oldVelocity);
        System.err.println("newVelocity: " + newVelocity);

        ignore.put(player, destination.toVector().toBlockVector());
        player.setVelocity(newVelocity);
        event.setTo(destination);
        //event.getPlayer().teleport(destination);
    }

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
            System.err.println("No portal found");
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

        // Check if block placed is anywhere in a portal
        PortalManager manager = PortalNetwork.getInstance().getPortalManager();
        BasePortal portal = manager.find(event.getBlock().getLocation());
        if (portal != null) {
            portal.dial(null);
            portal.update();
        }

        // Check if player is trying to place a portal block
        ItemMeta meta = event.getItemInHand().getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer();
            if (meta.getPersistentDataContainer().has(BasePortal.PortalTypeKey, PersistentDataType.STRING)) {
                BasePortal.PortalType portalType = BasePortal.PortalType.valueOf(meta.getPersistentDataContainer().get(BasePortal.PortalTypeKey, PersistentDataType.STRING));
                PortalNetwork.getInstance().getPortalManager().createPortal(event.getBlockPlaced().getLocation(), portalType);
            }
        }
    }

}
