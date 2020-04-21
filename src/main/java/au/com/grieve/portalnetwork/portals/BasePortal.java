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

package au.com.grieve.portalnetwork.portals;

import au.com.grieve.portalnetwork.PortalManager;
import au.com.grieve.portalnetwork.PortalNetwork;
import com.google.common.collect.Streams;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public class BasePortal {

    static public final NamespacedKey PortalTypeKey = new NamespacedKey(PortalNetwork.getInstance(), "portal_type");

    // Portal Manager
    @Getter
    final PortalManager manager;

    // Location of Portal Block
    final Location location;

    static final List<Material> WOOL_MAPPINGS = List.of(
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

    // Network (16^2)
    @Getter
    Integer network;

    // Address (16)
    @Getter
    Integer address;
    static final List<Material> GLASS_MAPPINGS = List.of(
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

    @Getter
    boolean valid = false;
    // Size Vectors
    @Getter
    BlockVector left, right;
    // Dialed
    @Getter
    BasePortal dialledPortal;

    public BasePortal(PortalManager manager, Location location) {
        this.manager = manager;
        this.location = location;
        update();
    }

    public Location getLocation() {
        return location.clone();
    }

    /**
     * Update Portal
     */
    public void update() {
        // Check that wool only appears on 3 sides
        List<Location> blocks = Arrays.asList(
                location.clone().add(1, 0, 0),
                location.clone().add(0, 0, 1),
                location.clone().add(-1, 0, 0),
                location.clone().add(0, 0, -1)
        );

        int count = 0;
        int non_idx = -1;

        for (int idx = 0; idx < blocks.size(); idx++) {
            if (Tag.WOOL.isTagged(blocks.get(idx).getBlock().getType())) {
                count += 1;
            } else {
                non_idx = idx;
            }
        }

        if (count != 3 || non_idx == -1) {
            if (valid) {
                dial(null);
                valid = false;
            }

            return;
        }

        valid = true;

        // Determine address block. It should be opposite non_idx
        Location address_block = blocks.get((non_idx + 2) % 4);
        address = WOOL_MAPPINGS.indexOf(address_block.getBlock().getType());
        location.setDirection(location.toVector().subtract(address_block.toVector()));

        // Net block is previous and next to non_idx
        Location left_block = blocks.get((non_idx - 1) % 4);
        Location right_block = blocks.get((non_idx + 1) % 4);
        network = (WOOL_MAPPINGS.indexOf(left_block.getBlock().getType()) << 4) + WOOL_MAPPINGS.indexOf(right_block.getBlock().getType());

        // Get Width of portal by counting obsidian blocks to a max of 10 each direction
        Vector left_unit_vector = left_block.toVector().subtract(location.toVector()).normalize();
        left = left_unit_vector.toBlockVector();
        for (int i = 0; i < 10; i++) {
            Vector test_left = left.clone().add(left_unit_vector);
            if (location.clone().add(test_left).getBlock().getType() != Material.OBSIDIAN) {
                break;
            }
            left = test_left.toBlockVector();
        }

        Vector right_unit_vector = right_block.toVector().subtract(location.toVector()).normalize();
        right = right_unit_vector.toBlockVector();
        for (int i = 0; i < 10; i++) {
            Vector test_right = right.clone().add(right_unit_vector);
            if (location.clone().add(test_right).getBlock().getType() != Material.OBSIDIAN) {
                break;
            }
            right = test_right.toBlockVector();
        }
    }

    // Return portal width
    public int getWidth() {
        if (!valid) {
            return 1;
        }
        return (int) left.distance(right);
    }

    // Return Portal height
    public int getHeight() {
        if (!valid) {
            return 0;
        }
        return getWidth() + 1;
    }

    public boolean dial(Integer address) {
        if (address == null) {
            return dial(null, null);
        }

        if (!valid) {
            return false;
        }

        BasePortal portal = manager.find(network, address);
        if (portal == null) {
            return false;
        }

        return dial(portal, null);
    }

    public boolean dial(BasePortal portal, BasePortal from) {
        if (portal == null) {
            if (dialledPortal == null) {
                return true;
            }

            // If we are not connected to from we will get our dialed to undial
            if (from != dialledPortal) {
                dialledPortal.dial(null, this);
            }

            dialledPortal = null;
            deactivate();
            return true;
        }

        // Dialing

        // Already Dialed to portal?
        if (dialledPortal == portal) {
            return true;
        }

        // If we are not connected to from we will get our dialed to undial
        if (dialledPortal != null && from != dialledPortal) {
            dialledPortal.dial(null, this);
        }

        // If portal is not from we will get it to dial us
        if (portal != from) {
            portal.dial(this, this);
        }

        dialledPortal = portal;
//        manager.save();
        activate();

        return true;
    }

    /**
     * Dial next available address, otherwise we deactivate.
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean dialNext() {
        if (!valid) {
            return false;
        }

        int startAddress = dialledPortal == null ? 0 : dialledPortal.getAddress();

        for (int i = 1; i < 17; i++) {
            int checkAddress = (startAddress + i) % 17;

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
                    if (height <= maxHeight - 1) {
                        Location check = location.clone().add(left).add(right.clone().normalize().multiply(width));

                        // If this location is blocked we continue
                        if (check.clone().add(new Vector(0, 1, 0).multiply(height)).getBlock().getType() == Material.OBSIDIAN) {
                            break;
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
     * Return an iterator over the portal base
     */
    public Iterator<Location> getPortalBaseIterator() {

        final int maxWidth = isValid() ? (int) new Vector(0, 0, 0).distance(right.clone().subtract(left)) : 0;
        final int maxHeight = 5;

        return new Iterator<>() {
            int width = 0;
            Location next;

            private Location getNext() throws NoSuchElementException {
                if (!isValid()) {
                    // A non-valid portal just returns its own location
                    if (width == 0) {
                        width++;
                        return location;
                    }
                    throw new NoSuchElementException();
                }

                if (width <= maxWidth) {
                    Location ret = location.clone().add(left).add(right.clone().normalize().multiply(width));
                    width++;
                    return ret;
                }

                // Address block.
                if (width == maxWidth + 1) {
                    Location ret = location.clone().add(location.getDirection());
                    width++;
                    return ret;
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
                            break;
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
    public void activate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Deactivate Portal
     */
    public void deactivate() {
        throw new UnsupportedOperationException();
    }

    public void handlePlayerInteract(PlayerInteractEvent event) {
        // If its not our base we are not interested
        //noinspection UnstableApiUsage
        if (!Streams.stream(getPortalFrameIterator()).anyMatch(l -> event.getClickedBlock().getLocation().equals(l))) {
            return;
        }

        event.setCancelled(true);

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Player has right clicked portal so lets dial next address if any, else deactivate
        if (valid) {
            dialNext();
        }
    }

    public void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (getDialledPortal() == null || event.getTo() == null) {
            return;
        }

        // teleport to relative portal position

        Location fromPortalLocation = getLocation().add(new Vector(0.5, 0, 0.5));
        Location toPortalLocation = dialledPortal.getLocation().add(new Vector(0.5, 0, 0.5));
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

        player.setVelocity(newVelocity);
        event.setTo(destination);
    }

    public void handleBlockBreak(BlockBreakEvent event) {
        // If it's the frame we cancel drops
        //noinspection UnstableApiUsage
        if (Streams.stream(getPortalFrameIterator()).anyMatch(l -> event.getBlock().getLocation().equals(l))) {
            event.setDropItems(false);
        }


        dial(null);

        System.err.println("HereE - " + location.toVector().toBlockVector() + " - " + event.getBlock().getLocation().toVector().toBlockVector());
        if (event.getBlock().getLocation().toVector().toBlockVector().equals(location.toVector().toBlockVector())) {
            System.err.println("Remove");
            event.setDropItems(false);
            remove();
        }


        update();
    }

    public void handleBlockPlace(BlockPlaceEvent event) {
        dial(null);
        update();
    }

    /**
     * Remove portal cleanly
     */
    public void remove() {
        deactivate();
        location.getBlock().setType(Material.AIR);
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
                "dialled=" + (dialledPortal == null ? "[disconnected]" : dialledPortal.getAddress()) + ")";
    }
}
