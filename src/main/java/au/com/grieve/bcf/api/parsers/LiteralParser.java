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
import au.com.grieve.bcf.api.exceptions.ParserNoResultException;

import java.util.ArrayList;
import java.util.List;

/**
 * Literal is provided as follows:
 * string1[|string2][|*]
 * <p>
 * Returns a String Type
 * Consumes 1 argument
 * If * is provided then it will accept any input
 * Will use the first matching alias as an alternative for partials
 */
public class LiteralParser extends SingleParser {


    public LiteralParser(CommandManager manager, ParserNode node, ParserContext context) {
        super(manager, node, context);
        defaultParameters.put("suppress", "true");
    }

    @Override
    protected List<String> complete() {
        List<String> result = new ArrayList<>();

        for (String alias : node.getData().getName().split("\\|")) {
            if (alias.equals("*")) {
                result.add(getInput());
                return result;
            }

            if (alias.startsWith(getInput())) {
                result.add(alias);
                return result;
            }
        }

        return result;
    }

    @Override
    protected Object result() throws ParserNoResultException {
        for (String alias : node.getData().getName().split("\\|")) {
            if (alias.equals("*")) {
                return getInput();
            }

            if (alias.equals(getInput())) {
                return getInput();
            }
        }

        throw new ParserNoResultException();
    }
}
