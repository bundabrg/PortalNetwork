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
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortalNetwork extends JavaPlugin {

    @Getter
    private static PortalNetwork instance;

    @Getter
    private BukkitCommandManager bcf;

    public PortalNetwork() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Setup Command Manager
        bcf = new BukkitCommandManager(this);

        // Register Commands
        bcf.registerCommand(new MainCommand());
        bcf.registerCommand(new PortalBlockCommand());

        // Initialize Config
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
