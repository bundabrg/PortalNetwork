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

package au.com.grieve.bcf.api.parsers;

import au.com.grieve.bcf.api.CommandManager;
import au.com.grieve.bcf.api.ParserContext;
import au.com.grieve.bcf.api.ParserNode;
import au.com.grieve.bcf.api.exceptions.ParserInvalidResultException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntegerParser extends SingleParser {

    public IntegerParser(CommandManager manager, ParserNode node, ParserContext context) {
        super(manager, node, context);
    }

    @Override
    protected List<String> complete() {
        Map<String, String> parameters = getNode().getData().getParameters();

        if (parameters.containsKey("max")) {
            int min;
            int max;

            try {
                max = Integer.parseInt(parameters.get("max"));
            } catch (NumberFormatException e) {
                return super.complete();
            }

            try {
                min = Integer.parseInt(parameters.getOrDefault("min", "0"));
            } catch (NumberFormatException e) {
                min = 0;
            }

            return IntStream.rangeClosed(min, max)
                    .mapToObj(String::valueOf)
                    .filter(s -> s.startsWith(getInput()))
                    .limit(20)
                    .collect(Collectors.toList());
        }

        return super.complete();
    }

    @Override
    protected Object result() throws ParserInvalidResultException {
        int result;

        try {
            result = Integer.parseInt(getInput());

            if (getNode().getData().getParameters().containsKey("min")) {
                if (result < Integer.parseInt(getNode().getData().getParameters().get("min"))) {
                    throw new ParserInvalidResultException();
                }
            }

            if (getNode().getData().getParameters().containsKey("max")) {
                if (result > Integer.parseInt(getNode().getData().getParameters().get("max"))) {
                    throw new ParserInvalidResultException();
                }
            }

        } catch (NumberFormatException e) {
            throw new ParserInvalidResultException();
        }

        return result;
    }
}
