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

import au.com.grieve.portalnetwork.exceptions.InvalidPortalException;
import au.com.grieve.portalnetwork.portals.BasePortal;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

public class PortalManager {
    @Getter
    private final Map<String, Class<? extends BasePortal>> portalClasses = new HashMap<>();

    private final JavaPlugin plugin;

    // Portals
    @Getter
    private final List<BasePortal> portals = new ArrayList<>();

    // Location Maps
    private final Hashtable<BlockVector, BasePortal> indexFrames = new Hashtable<>();
    private final Hashtable<BlockVector, BasePortal> indexPortals = new Hashtable<>();
    private final Hashtable<BlockVector, BasePortal> indexBases = new Hashtable<>();
    private final Hashtable<BlockVector, BasePortal> indexPortalBlocks = new Hashtable<>();

    public PortalManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a new Portal Class
     *
     * @param name        Name of Portal Type
     * @param portalClass Class of Portal
     */
    public void registerPortalClass(String name, Class<? extends BasePortal> portalClass) {
        portalClasses.put(name, portalClass);
    }

//    public void load() {
//        // Portal Data
//        Config portalConfig = new Config(plugin.getDataFolder() + "/" + "portal-data.yml");
//        try {
//            portalConfig.load();
//        } catch (IOException | InvalidConfigurationException ignored) {
//        }
//
//        // Initialize all portals
//        Map<BasePortal, Integer> dialed = new HashMap<>();
//        List<BasePortal> invalid = new ArrayList<>();
//
//        ConfigurationSection portalsData = portalConfig.getConfigurationSection("portals");
//        if (portalsData != null) {
//            for (String key : portalsData.getKeys(false)) {
//                ConfigurationSection portalData = portalsData.getConfigurationSection(key);
//                if (portalData == null) {
//                    continue;
//                }
//
//                BasePortal portal = new BasePortal(
//                        this,
//                        portalData.getLocation("location"),
//                        BasePortal.PortalType.valueOf(portalData.getString("portal_type"))
//                );
//
//                // Update valid portals, ignore invalid so we don't accidentally dial them later
//                if (portalData.getBoolean("valid")) {
//                    portal.update();
//                } else {
//                    invalid.add(portal);
//                }
//
//                if (portalData.contains("dialed")) {
//                    dialed.put(portal, portalData.getInt("dialed"));
//                }
//
//                portals.add(portal);
//                reindexPortal(portal);
//            }
//        }
//
//        // Dial Portals
//        for (Map.Entry<BasePortal, Integer> dialedPortal : dialed.entrySet()) {
//            dialedPortal.getKey().dial(dialedPortal.getValue());
//        }
//
//        // Update invalids
//        for (BasePortal portal : invalid) {
//            portal.update();
//        }
//    }

//    public void reload() {
//        for (BasePortal portal : portals) {
//            portal.destroy();
//        }
//        portals.clear();
//        indexLocation.clear();
//
//        // Load Data
//        load();
//    }

//    public void save() {
//        Config portalConfig = new Config(plugin.getDataFolder() + "/" + "portal-data.yml");
//        ConfigurationSection portalsData = portalConfig.createSection("portals");
//        for (int i = 0; i < portals.size(); i++) {
//            BasePortal portal = portals.get(i);
//            ConfigurationSection portalData = portalsData.createSection(Integer.toString(i));
//
//            if (portal.getDialledPortal() != null) {
//                portalData.set("dialed", portal.getDialledPortal().getAddress());
//            }
//            portalData.set("portal_type", portal.getPortalType().toString());
//            portalData.set("location", portal.getLocation());
//            portalData.set("valid", portal.isValid());
//        }
//
//        portalConfig.save();
//    }

    /**
     * Create a new portal
     */
    @SuppressWarnings("UnusedReturnValue")
    public BasePortal createPortal(String portalType, Location location) throws InvalidPortalException {
        if (!portalClasses.containsKey(portalType)) {
            throw new InvalidPortalException("No such portal type");
        }

        BasePortal portal;

        try {
            Constructor<? extends BasePortal> c = portalClasses.get(portalType).getConstructor(PortalManager.class, Location.class);
            portal = c.newInstance(this, location);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new InvalidPortalException("Unable to create portal");
        }

        portals.add(portal);
        reindexPortal(portal);
        return portal;
    }

    public void removePortal(BasePortal portal) {
        portals.remove(portal);
        indexFrames.values().removeIf(v -> v.equals(portal));
        indexPortals.values().removeIf(v -> v.equals(portal));
        indexBases.values().removeIf(v -> v.equals(portal));
        indexPortalBlocks.values().removeIf(v -> v.equals(portal));
    }

    public ItemStack createPortalBlock(String portalType) throws InvalidPortalException {
        if (!portalClasses.containsKey(portalType)) {
            throw new InvalidPortalException("No such portal type");
        }

        // Create a Portal Block
        ItemStack item = new ItemStack(Material.GOLD_BLOCK, 1);
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        meta.setDisplayName("Portal Block (" + portalType + ")");
        meta.getPersistentDataContainer().set(BasePortal.PortalTypeKey, PersistentDataType.STRING, portalType);
        item.setItemMeta(meta);
        return item;
    }

    public void reindexPortal(BasePortal portal) {
        indexPortalBlocks.values().removeIf(v -> v.equals(portal));
        indexPortalBlocks.put(portal.getLocation().toVector().toBlockVector(), portal);

        indexFrames.values().removeIf(v -> v.equals(portal));
        for (Iterator<Location> it = portal.getPortalFrameIterator(); it.hasNext(); ) {
            Location loc = it.next();
            indexFrames.put(loc.toVector().toBlockVector(), portal);
        }

        indexPortals.values().removeIf(v -> v.equals(portal));
        for (Iterator<Location> it = portal.getPortalIterator(); it.hasNext(); ) {
            Location loc = it.next();
            indexPortals.put(loc.toVector().toBlockVector(), portal);
        }

        indexBases.values().removeIf(v -> v.equals(portal));
        for (Iterator<Location> it = portal.getPortalBaseIterator(); it.hasNext(); ) {
            Location loc = it.next();
            indexBases.put(loc.toVector().toBlockVector(), portal);
        }
    }

    /**
     * Find a portal
     */
    public BasePortal find(Integer network, Integer address, Boolean valid) {
        for (BasePortal portal : portals) {
            if (valid != null && portal.isValid() != valid) {
                continue;
            }

            if (!Objects.equals(portal.getNetwork(), network)) {
                continue;
            }

            if (!Objects.equals(portal.getAddress(), address)) {
                continue;
            }

            return portal;
        }
        return null;
    }

    public BasePortal find(Integer network, Integer address) {
        return find(network, address, null);
    }

    /**
     * Get a portal at location
     */
    public BasePortal find(@NonNull BlockVector search, Boolean valid) {
        BasePortal portal = Stream.concat(
                indexFrames.entrySet().stream(),
                Stream.concat(
                        indexPortals.entrySet().stream(),
                        indexBases.entrySet().stream()
                )
        )
                .filter(e -> e.getKey().equals(search))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if (portal != null) {
            if (valid == null || valid == portal.isValid()) {
                return portal;
            }
        }
        return null;
    }

    /**
     * Get a portal at location
     */
    public BasePortal find(@NonNull Location location, Boolean valid, int distance) {
        BlockVector search = location.toVector().toBlockVector();
        BasePortal portal;

        // Check exact match
        portal = find(search, valid);

        if (portal != null) {
            return portal;
        }

        for (int x = -distance; x < distance; x++) {
            for (int y = -distance; y < distance; y++) {
                for (int z = -distance; z < distance; z++) {
                    portal = find(search.clone().add(new Vector(x, y, z)).toBlockVector(), valid);
                    if (portal != null) {
                        return portal;
                    }
                }
            }
        }

        return null;
    }

    public BasePortal find(@NonNull Location location) {
        return find(location, null, 0);
    }

    public BasePortal find(@NonNull Location location, int distance) {
        return find(location, null, distance);
    }

    public BasePortal getPortal(@NonNull Location location) {
        return indexPortalBlocks.get(location.toVector().toBlockVector());
    }

}
