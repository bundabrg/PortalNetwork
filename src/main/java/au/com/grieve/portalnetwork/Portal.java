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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;


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
    Integer dialed;

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

    /**
     * Return integer for color of wool
     */
    public static int ColorToNumber(Material material) {
        switch (material) {
            case WHITE_WOOL:
                return 0;
            case ORANGE_WOOL:
                return 1;
            case MAGENTA_WOOL:
                return 2;
            case LIGHT_BLUE_WOOL:
                return 3;
            case YELLOW_WOOL:
                return 4;
            case LIME_WOOL:
                return 5;
            case PINK_WOOL:
                return 6;
            case GRAY_WOOL:
                return 7;
            case LIGHT_GRAY_WOOL:
                return 8;
            case CYAN_WOOL:
                return 9;
            case PURPLE_WOOL:
                return 10;
            case BLUE_WOOL:
                return 11;
            case BROWN_WOOL:
                return 12;
            case GREEN_WOOL:
                return 13;
            case RED_WOOL:
                return 14;
            case BLACK_WOOL:
                return 15;
        }
        throw new IndexOutOfBoundsException("Invalid Material");
    }

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
            return;
        }

        valid = true;

        // Determine address block. It should be opposite non_idx
        Block address_block = blocks.get((non_idx + 2) % 4);
        address = ColorToNumber(address_block.getType());

        // Net block is previous and next to non_idx
        Block left_block = blocks.get((non_idx - 1) % 4);
        Block right_block = blocks.get((non_idx + 1) % 4);
        network = (ColorToNumber(left_block.getType()) << 4) + ColorToNumber(right_block.getType());

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

    @SuppressWarnings("UnusedReturnValue")
    public boolean dial(Integer address) {
        if (!valid) {
            return false;
        }

        // Find an active portal with this address
        Portal portal = manager.find(network, address);

        if (portal == null) {
            return false;
        }

        dialed = address;
        return true;
    }

    /**
     * Remove portal
     */
    public void remove() {
        manager.removePortal(this);
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
                "dialled=" + dialed + ")";
    }

    /**
     * Make Portal
     */
    public void make() {
        if (location.getWorld() == null) {
            return;
        }

        BlockIterator blockIterator = new BlockIterator(
                location.getWorld(),
                location.toVector().add(left),
                location.toVector().add(right),
                0, 10);

        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            System.err.println("Loc: " + block.getLocation() + " - " + block.getType().toString());
        }
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
