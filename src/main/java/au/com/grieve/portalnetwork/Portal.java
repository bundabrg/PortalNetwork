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

package au.com.grieve.portalnetwork;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public class Portal {

    static public final NamespacedKey PortalTypeKey = new NamespacedKey(PortalNetwork.getInstance(), "portal_type");

    // Portal Manager
    @Getter
    final PortalManager manager;

    // Type of Portal
    @Getter
    final PortalType portalType;


    // Location of Portal Block
    @Getter
    final Location location;

    // Size Vectors
    @Getter
    Vector left, right;

    // Network (16^2)
    @Getter
    Integer network;

    // Address (16)
    @Getter
    Integer address;

    // Dialed
    @Getter
    Portal dialed;

    @Getter
    boolean valid = false;

    public Portal(PortalManager manager, Location location, PortalType portalType) {
        this.manager = manager;
        this.location = location;
        this.portalType = portalType;
    }

//    public Portal(Location location, Vector left, Vector right, Integer network, Integer address, PortalType type) {
//        this.location = location;
//        this.left = left;
//        this.right = right;
//        this.network = network;
//        this.address = address;
//        this.portalType = type;
//    }

    /**
     * Create portal block item
     */
    public static ItemStack CreatePortalBlock(PortalType portalType) {
        // Create a Portal Block
        ItemStack item = new ItemStack(Material.BEACON, 1);
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        meta.setDisplayName("Portal Block");
        meta.getPersistentDataContainer().set(Portal.PortalTypeKey, PersistentDataType.STRING, portalType.toString());
        item.setItemMeta(meta);
        return item;
    }

    private static final List<Material> WOOL_MAPPINGS = List.of(
            Material.WHITE_WOOL,
            Material.ORANGE_WOOL,
            Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PINK_WOOL,
            Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL,
            Material.PURPLE_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.GREEN_WOOL,
            Material.RED_WOOL,
            Material.BLACK_WOOL
    );

    private static final List<Material> GLASS_MAPPINGS = List.of(
            Material.WHITE_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS,
            Material.MAGENTA_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS,
            Material.LIME_STAINED_GLASS,
            Material.PINK_STAINED_GLASS,
            Material.GRAY_STAINED_GLASS,
            Material.LIGHT_GRAY_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS,
            Material.PURPLE_STAINED_GLASS,
            Material.BLUE_STAINED_GLASS,
            Material.BROWN_STAINED_GLASS,
            Material.GREEN_STAINED_GLASS,
            Material.RED_STAINED_GLASS,
            Material.BLACK_STAINED_GLASS
    );

    /**
     * Update Portal
     * <p>
     * Update portal settings. Returns true if any changes
     */
    public void update() {
        // Check that wool only appears on 3 sides
        List<Block> blocks = Arrays.asList(
                location.clone().add(1, 0, 0).getBlock(),
                location.clone().add(0, 0, 1).getBlock(),
                location.clone().add(-1, 0, 0).getBlock(),
                location.clone().add(0, 0, -1).getBlock()
        );

        int count = 0;
        int non_idx = -1;

        for (int idx = 0; idx < blocks.size(); idx++) {
            if (Tag.WOOL.isTagged(blocks.get(idx).getType())) {
                count += 1;
            } else {
                non_idx = idx;
            }
        }

        if (count != 3 || non_idx == -1) {
            valid = false;

            // Set portal block just in case
            location.getBlock().setType(Material.BEACON);

            return;
        }

        valid = true;

        // Determine address block. It should be opposite non_idx
        Block address_block = blocks.get((non_idx + 2) % 4);
        address = WOOL_MAPPINGS.indexOf(address_block.getType());

        // Net block is previous and next to non_idx
        Block left_block = blocks.get((non_idx - 1) % 4);
        Block right_block = blocks.get((non_idx + 1) % 4);
        network = (WOOL_MAPPINGS.indexOf(left_block.getType()) << 4) + WOOL_MAPPINGS.indexOf(right_block.getType());

        // Get Width of portal by counting obsidian blocks to a max of 10 each direction
        Vector left_unit_vector = left_block.getLocation().toVector().subtract(location.toVector()).normalize();
        left = left_unit_vector.clone();
        for (int i = 0; i < 10; i++) {
            Vector test_left = left.clone().add(left_unit_vector);
            if (location.clone().add(test_left).getBlock().getType() != Material.OBSIDIAN) {
                break;
            }
            left = test_left;
        }

        Vector right_unit_vector = right_block.getLocation().toVector().subtract(location.toVector()).normalize();
        right = right_unit_vector.clone();
        for (int i = 0; i < 10; i++) {
            Vector test_right = right.clone().add(right_unit_vector);
            if (location.clone().add(test_right).getBlock().getType() != Material.OBSIDIAN) {
                break;
            }
            right = test_right;
        }
    }

    public boolean dial(Integer address) {
        if (address == null) {
            return dial(null, null);
        }

        if (!valid) {
            return false;
        }

        Portal portal = manager.find(network, address);
        if (portal == null) {
            return false;
        }

        return dial(portal, null);
    }

    public boolean dial(Portal portal, Portal from) {
        // Undialing
        if (portal == null) {
            if (dialed == null) {
                // Already undialed
                return true;
            }

            // If we are not connected to from we will get our dialed to undial
            if (from != dialed) {
                dialed.dial(null, this);
            }

            dialed = null;
            deactivate();
            return true;
        }

        // Dialing

        // Already Dialed to portal?
        if (dialed == portal) {
            return true;
        }

        // If we are not connected to from we will get our dialed to undial
        if (dialed != null && from != dialed) {
            dialed.dial(null, this);
        }

        // If portal is not from we will get it to dial us
        if (portal != from) {
            portal.dial(this, this);
        }

        dialed = portal;
        manager.save();
        activate();

        return true;
    }

    /**
     * Dial next available address, otherwise we deactivate.
     */
    public boolean dialNext() {
        if (!valid) {
            return false;
        }

        int startAddress = dialed == null ? 0 : dialed.getAddress();

        for (int i = 1; i < 17; i++) {
            int checkAddress = (startAddress + i) % 17;

            System.err.println("Check addr: " + checkAddress);

            if (checkAddress == address) {
                continue;
            }

            // Address 16 will be considered deactivate
            if (checkAddress == 16) {
                break;
            }

            System.err.println("Dialing " + checkAddress);
            if (dial(checkAddress)) {
                System.err.println("Success");
                return true;
            }
        }

        // Deactivate
        System.err.println("Deactivate");
        dial(null);
        return true;
    }

    /**
     * Return an iterator over the portal part of the portal
     */
    public Iterator<Location> getPortalIterator() {

        final int maxWidth = isValid() ? (int) new Vector(0, 0, 0).distance(right.clone().subtract(left)) : 0;
        final int maxHeight = 5;

        return new Iterator<>() {
            int width = 1;
            int height = 1;
            Location next;

            private Location getNext() throws NoSuchElementException {
                if (!isValid()) {
                    throw new NoSuchElementException();
                }

                while (width <= maxWidth - 1) {
                    while (height <= maxHeight - 1) {
                        Location check = location.clone().add(left).add(right.clone().normalize().multiply(width));

                        // If this location is blocked we continue
                        if (check.clone().add(new Vector(0, 1, 0).multiply(height)).getBlock().getType() == Material.OBSIDIAN) {
                            continue;
                        }

                        Location ret = check.clone().add(new Vector(0, 1, 0).multiply(height));

                        // Something blocking above us? We will reset next round
                        if (check.clone().add(new Vector(0, 1, 0).multiply(height + 1)).getBlock().getType() == Material.OBSIDIAN) {
                            height = 1;
                            width++;
                        } else {
                            height++;
                        }

                        return ret;
                    }
                    width++;
                    height = 1;
                }
                throw new NoSuchElementException();
            }


            @Override
            public boolean hasNext() {
                if (next == null) {
                    try {
                        next = getNext();
                    } catch (NoSuchElementException e) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public Location next() {
                if (hasNext()) {
                    Location ret = next;
                    next = null;
                    return ret;
                }

                throw new NoSuchElementException();
            }
        };
    }

    /**
     * Return an iterator over the portal frame
     */
    public Iterator<Location> getPortalFrameIterator() {

        final int maxWidth = isValid() ? (int) new Vector(0, 0, 0).distance(right.clone().subtract(left)) : 0;
        final int maxHeight = 5;

        return new Iterator<Location>() {
            int width = 0;
            int height = 1;
            Location next;

            private Location getNext() throws NoSuchElementException {
                if (!isValid()) {
                    throw new NoSuchElementException();
                }

                while (width <= maxWidth) {
                    while (height <= maxHeight) {
                        Location check = location.clone().add(left).add(right.clone().normalize().multiply(width));

                        // If this location is blocked we continue
                        if (check.clone().add(new Vector(0, 1, 0).multiply(height)).getBlock().getType() == Material.OBSIDIAN) {
                            continue;
                        }

                        // If we are on either end of the portal, then every height is part of the frame unless blocked
                        if (width == 0 || width == maxWidth) {
                            Location ret = check.clone().add(new Vector(0, 1, 0).multiply(height));
                            if (check.clone().add(new Vector(0, 1, 0).multiply(height + 1)).getBlock().getType() == Material.OBSIDIAN) {
                                height = 1;
                                width++;
                            } else {
                                height++;
                            }
                            return ret;
                        }

                        // Max height is frame
                        if (height == maxHeight) {
                            Location ret = check.clone().add(new Vector(0, 1, 0).multiply(height));
                            height = 1;
                            width++;
                            return ret;
                        }

                        // Something blocking above us? We don't draw frame
                        if (check.clone().add(new Vector(0, 1, 0).multiply(height + 1)).getBlock().getType() == Material.OBSIDIAN) {
                            break;
                        }

                        // Else
                        height++;
                    }
                    width++;
                    height = 1;
                }
                throw new NoSuchElementException();
            }


            @Override
            public boolean hasNext() {
                if (next == null) {
                    try {
                        next = getNext();
                    } catch (NoSuchElementException e) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public Location next() {
                if (hasNext()) {
                    Location ret = next;
                    next = null;
                    return ret;
                }

                throw new NoSuchElementException();
            }
        };
    }


    /**
     * Activate Portal using type of portal as to what is seen/heard
     */
    public boolean activate() {
        if (!valid || dialed == null || location.getWorld() == null) {
            return false;
        }

        // Set Portal Block Colour
        location.getWorld().getBlockAt(location).setType(GLASS_MAPPINGS.get(dialed.getAddress()));

        // Draw frame
        for (Iterator<Location> it = getPortalFrameIterator(); it.hasNext(); ) {
            Location loc = it.next();
            Block block = loc.getBlock();
            if (block.getType() != Material.AIR && !GLASS_MAPPINGS.contains(block.getType())) {
                continue;
            }

            block.setType(GLASS_MAPPINGS.get(dialed.getAddress()));
        }

        for (Iterator<Location> it = getPortalIterator(); it.hasNext(); ) {
            Location loc = it.next();
            Block block = loc.getBlock();
            if (block.getType() != Material.AIR) {
                continue;
            }

            block.setType(Material.NETHER_PORTAL);
            Orientable bd = (Orientable) block.getBlockData();
            if (left.getX() == 0) {
                bd.setAxis(Axis.Z);
            } else {
                bd.setAxis(Axis.X);
            }
            block.setBlockData(bd);
        }


        // Play portal sound
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 100, 1);

        return true;

    }

    /**
     * Deactivate Portal
     */
    public boolean deactivate() {
        if (location.getWorld() == null) {
            return false;
        }

        for (Iterator<Location> it = getPortalIterator(); it.hasNext(); ) {
            Location loc = it.next();
            Block block = loc.getBlock();
            if (block.getType() != Material.NETHER_PORTAL) {
                continue;
            }

            block.setType(Material.AIR);
        }

        // Remove frame
        for (Iterator<Location> it = getPortalFrameIterator(); it.hasNext(); ) {
            Location loc = it.next();
            Block block = loc.getBlock();
            if (!GLASS_MAPPINGS.contains(block.getType())) {
                continue;
            }

            block.setType(Material.AIR);
        }

        // Set back to beacon block
        location.getWorld().getBlockAt(location).setType(Material.BEACON);

        // Play portal sound
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 100, 1);

        return true;
    }

    /**
     * Remove portal cleanly
     */
    public void remove() {
        manager.removePortal(this);
        destroy();
    }

    /**
     * Destroy portal
     */
    public void destroy() {
        // Clean up portal
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" +
                "location=" + location + ", " +
                "left=" + left + ", " +
                "right=" + right + ", " +
                "network=" + network + ", " +
                "address=" + address + ", " +
                "type=" + portalType + ", " +
                "dialled=" + (dialed == null ? "[disconnected]" : dialed.getAddress()) + ")";
    }

    /**
     * Create PortalBlock
     */
    public ItemStack createPortalBlock() {
        return CreatePortalBlock(portalType);
    }

    // Portal Types
    public enum PortalType {
        NETHER,
        END,
        HIDDEN,
    }


}
