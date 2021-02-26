/*
 * PortalNetwork - Portals for Players
 * Copyright (C) 2021 PortalNetwork Developers
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

package au.com.grieve.portalnetwork.portals;

import au.com.grieve.portalnetwork.PortalManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BlockVector;

import java.util.Iterator;

public class End extends BasePortal {

    public End(PortalManager manager, Location location) {
        super(manager, location);
    }

    /**
     * Activate Portal using type of portal as to what is seen/heard
     */
    @Override
    public void activate() {
        if (!valid || dialledPortal == null || location.getWorld() == null) {
            return;
        }

        updateBlock();

        // Draw frame
        for (Iterator<BlockVector> it = getPortalFrameIterator(); it.hasNext(); ) {
            BlockVector loc = it.next();
            Block block = loc.toLocation(location.getWorld()).getBlock();
            if (block.getType() != Material.AIR && !GLASS_MAPPINGS.contains(block.getType())) {
                continue;
            }

            block.setType(GLASS_MAPPINGS.get(dialledPortal.getAddress()));
        }

        for (Iterator<BlockVector> it = getPortalIterator(); it.hasNext(); ) {
            BlockVector loc = it.next();
            Block block = loc.toLocation(location.getWorld()).getBlock();


            if (block.getType() != Material.AIR) {
                continue;
            }

            // Ugly hack. If we are in THE END we will display as Nether instead
            if (location.getWorld().getEnvironment() == World.Environment.THE_END) {
                block.setType(Material.NETHER_PORTAL);

                Orientable bd = (Orientable) block.getBlockData();
                if (left.getX() == 0) {
                    bd.setAxis(Axis.Z);
                } else {
                    bd.setAxis(Axis.X);
                }
                block.setBlockData(bd);
            } else {
                block.setType(Material.END_GATEWAY);

                EndGateway eg = (EndGateway) block.getState();
                eg.setAge(-100000000);
                eg.update();
            }
        }

        // Play portal sound
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1);
    }

    /**
     * Deactivate Portal
     */
    @Override
    public void deactivate() {
        if (location.getWorld() == null) {
            return;
        }

        for (Iterator<BlockVector> it = getPortalIterator(); it.hasNext(); ) {
            BlockVector loc = it.next();
            Block block = loc.toLocation(location.getWorld()).getBlock();
            if (block.getType() != Material.END_GATEWAY && block.getType() != Material.NETHER_PORTAL) {
                continue;
            }

            block.setType(Material.AIR);
        }

        // Remove frame
        for (Iterator<BlockVector> it = getPortalFrameIterator(); it.hasNext(); ) {
            BlockVector loc = it.next();
            Block block = loc.toLocation(location.getWorld()).getBlock();
            if (!GLASS_MAPPINGS.contains(block.getType())) {
                continue;
            }

            block.setType(Material.AIR);
        }

        updateBlock();

        // Play portal sound
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1);
    }

}
