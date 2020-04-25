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

package au.com.grieve.portalnetwork.parsers;

import au.com.grieve.bcf.ArgNode;
import au.com.grieve.bcf.CommandContext;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.parsers.SingleParser;
import au.com.grieve.portalnetwork.PortalNetwork;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Type of Portal
 */
public class PortalTypeParser extends SingleParser {

    public PortalTypeParser(CommandManager manager, ArgNode argNode, CommandContext context) {
        super(manager, argNode, context);
    }

    @Override
    protected String result() throws ParserInvalidResultException {
        if (PortalNetwork.getInstance().getPortalManager().getPortalClasses().containsKey(getInput().toLowerCase())) {
            return getInput().toLowerCase();
        }
        throw new ParserInvalidResultException(this, "Invalid Portal Type: " + getInput());
    }

    @Override
    protected List<String> complete() {
        return PortalNetwork.getInstance().getPortalManager().getPortalClasses().keySet().stream()
                .filter(p -> p.startsWith(getInput().toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
    }
}
