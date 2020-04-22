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

package au.com.grieve.bcf.parsers;

import au.com.grieve.bcf.BukkitParserContext;
import au.com.grieve.bcf.api.CommandManager;
import au.com.grieve.bcf.api.ParserContext;
import au.com.grieve.bcf.api.ParserNode;
import au.com.grieve.bcf.api.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.api.parsers.SingleParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Name of a player
 * <p>
 * Parameters:
 * mode:
 * any - (default) Any player
 * online - Only online players
 */
public class PlayerParser extends SingleParser {

    public PlayerParser(CommandManager manager, ParserNode node, ParserContext context) {
        super(manager, node, context);
    }

    @Override
    protected Object result() throws ParserInvalidResultException {
        switch (getNode().getData().getParameters().getOrDefault("mode", "offline")) {
            case "online":
                if (getInput().equals("%self")) {
                    CommandSender sender = ((BukkitParserContext) context).getSender();
                    if (sender instanceof ConsoleCommandSender) {
                        throw new ParserInvalidResultException("When console a player name is required");
                    }
                    return sender;
                }

                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getName().toLowerCase().equals(getInput().toLowerCase()))
                        .findFirst()
                        .orElseThrow(() -> new ParserInvalidResultException("No such player can be found online"));
            case "offline":
                if (getInput().equals("%self")) {
                    CommandSender sender = ((BukkitParserContext) context).getSender();
                    if (sender instanceof ConsoleCommandSender) {
                        throw new ParserInvalidResultException("When console a player name is required");
                    }

                    return Bukkit.getOfflinePlayer(((Player) ((BukkitParserContext) context).getSender()).getUniqueId());
                }

                return Arrays.stream(Bukkit.getOfflinePlayers())
                        .filter(p -> p.getName() != null)
                        .filter(p -> Objects.equals(p.getName().toLowerCase(), getInput().toLowerCase()))
                        .findFirst()
                        .orElseThrow(() -> new ParserInvalidResultException("No such player can be found"));
        }

        throw new ParserInvalidResultException("Invalid mode: " + getNode().getData().getParameters().get("mode"));
    }

    @Override
    protected List<String> complete() {
        switch (getNode().getData().getParameters().getOrDefault("mode", "offline")) {
            case "online":
                return Bukkit.getOnlinePlayers().stream()
                        .map(HumanEntity::getName)
                        .filter(s -> s.startsWith(getInput()))
                        .limit(20)
                        .collect(Collectors.toList());
            case "offline":
                return Arrays.stream(Bukkit.getOfflinePlayers())
                        .map(OfflinePlayer::getName).filter(Objects::nonNull)
                        .filter(s -> s.startsWith(getInput()))
                        .limit(20)
                        .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
