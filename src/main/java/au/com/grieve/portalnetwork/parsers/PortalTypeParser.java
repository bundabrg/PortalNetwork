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

import au.com.grieve.bcf.api.CommandManager;
import au.com.grieve.bcf.api.ParserContext;
import au.com.grieve.bcf.api.ParserNode;
import au.com.grieve.bcf.api.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.api.parsers.SingleParser;
import au.com.grieve.portalnetwork.Portal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Type of Portal
 */
public class PortalTypeParser extends SingleParser {

    public PortalTypeParser(CommandManager manager, ParserNode node, ParserContext context) {
        super(manager, node, context);
    }

    @Override
    protected Object result() throws ParserInvalidResultException {
        try {
            return Portal.PortalType.valueOf(getInput().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParserInvalidResultException("Invalid Portal Type: " + getInput());
        }
    }

    @Override
    protected List<String> complete() {
        return Arrays.stream(Portal.PortalType.values())
                .map(Portal.PortalType::toString)
                .filter(p -> p.startsWith(getInput().toUpperCase()))
                .limit(20)
                .collect(Collectors.toList());
    }
}
