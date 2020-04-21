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

import au.com.grieve.bcf.BukkitCommandManager;
import au.com.grieve.portalnetwork.commands.MainCommand;
import au.com.grieve.portalnetwork.commands.PortalBlockCommand;
import au.com.grieve.portalnetwork.listeners.PortalEvents;
import au.com.grieve.portalnetwork.parsers.PortalTypeParser;
import au.com.grieve.portalnetwork.portals.Nether;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortalNetwork extends JavaPlugin {

    @Getter
    private static PortalNetwork instance;

    @Getter
    private BukkitCommandManager bcf;

    @Getter
    private PortalManager portalManager;

    public PortalNetwork() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Setup Command Manager
        bcf = new BukkitCommandManager(this);
        bcf.registerParser("portaltype", PortalTypeParser.class);

        // Register Commands
        bcf.registerCommand(new MainCommand());
        bcf.registerCommand(new PortalBlockCommand());

        // Initialize Configs
        initConfig();

        // Load Portal Manager
        portalManager = new PortalManager(this);
        portalManager.registerPortalClass("nether", Nether.class);

        //portalManager.load();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PortalEvents(), this);

        // Test1
//        Portal portal1 = Portal.Create(new Location(getServer().getWorld("world"), 356, 3, -313));
//        System.err.println("Portal1: " + portal1);
//
//        Portal portal2 = Portal.Create(new Location(getServer().getWorld("world"), 353, 4, -308));
//        System.err.println("Portal2: " + portal2);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initConfig() {
        // Main Config
        saveDefaultConfig();
    }

    public void reload() {
        // Reload Config
        reloadConfig();

        // Reload Portals
        //portalManager.reload();
    }

}
