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

package au.com.grieve.bcf;

import au.com.grieve.bcf.api.CommandManager;
import au.com.grieve.bcf.api.ParserNode;
import au.com.grieve.bcf.api.RootCommand;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class BukkitRootCommand extends Command implements RootCommand {
    private final ParserNode node = new ParserNode();

    @Getter
    private final CommandManager manager;

    protected BukkitRootCommand(CommandManager manager, String name) {
        super(name);
        this.manager = manager;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        BukkitParserContext context = new BukkitParserContext(manager, sender);
        manager.execute(node, String.join(" ", args), context, Collections.singletonList(sender));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        BukkitParserContext context = new BukkitParserContext(manager, sender);
        return manager.getComplete(node, String.join(" ", args), context);
    }

    @Override
    public ParserNode getNode() {
        return node;
    }
}
