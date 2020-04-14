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

package au.com.grieve.bcf.api;

import au.com.grieve.bcf.api.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.api.exceptions.ParserNoResultException;
import au.com.grieve.bcf.api.exceptions.ParserRequiredArgumentException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Parser {
    // Data
    @Getter
    protected CommandManager manager;
    @Getter
    protected ParserNode node;
    @Getter
    protected ParserContext context;
    @Getter
    protected Map<String, String> defaultParameters = new HashMap<>();

    @Getter
    protected boolean parsed = false;

    // Cache
    protected List<String> completions;
    protected Object result;

    public Parser(CommandManager manager, ParserNode node, ParserContext context) {
        this.manager = manager;
        this.node = node;
        this.context = context;
    }

    public List<String> getCompletions() {
        if (completions == null) {
            completions = complete();
        }
        return completions;
    }

    public Object getResult() throws ParserInvalidResultException, ParserNoResultException {
        if (result == null) {
            result = result();
        }

        return result;
    }

    @SuppressWarnings("unused")
    public String getParameter(String key) {
        return getParameter(key, null);
    }

    public String getParameter(String key, String def) {
        if (node.getData() != null) {
            return node.getData().getParameters().getOrDefault(key, defaultParameters.getOrDefault(key, def));
        }

        return defaultParameters.getOrDefault(key, def);
    }

    // default methods

    protected List<String> complete() {
        return new ArrayList<>();
    }

    // abstract methods
    protected abstract Object result() throws ParserInvalidResultException, ParserNoResultException;

    /**
     * Take input and return the unused data
     */
    public String parse(String input) throws ParserRequiredArgumentException {
        parsed = true;
        return input;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(node=" + node + ", " +
                "context=" + context + ", " +
                "defaultParameters=" + defaultParameters + ", " +
                "parsed=" + parsed + ")";
    }


}
