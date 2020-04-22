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

import lombok.Getter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParserNodeData {
    @Getter
    final String name;
    @Getter
    final Map<String, String> parameters = new HashMap<>();


    public ParserNodeData(String name) {
        this.name = name;
    }

    public ParserNodeData(String name, Map<String, String> parameters) {
        this(name);
        this.parameters.putAll(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ParserNodeData)) {
            return false;
        }

        ParserNodeData data = (ParserNodeData) obj;

        return data.getName().equals(name);
    }

    /**
     * Parse a string and return new Data Nodes
     */
    public static List<ParserNodeData> parse(StringReader reader) {
        List<ParserNodeData> result = new ArrayList<>();

        State state = State.NAME;
        StringBuilder name = new StringBuilder();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        Map<String, String> parameters = new HashMap<>();

        int i;
        char quote = ' ';

        do {
            try {
                i = reader.read();
            } catch (IOException e) {
                break;
            }

            if (i < 0) {
                break;
            }

            char c = (char) i;

            switch (state) {
                case NAME:
                    switch (" (,".indexOf(c)) {
                        case 0:
                            if (name.length() > 0) {
                                result.add(new ParserNodeData(name.toString()));
                                return result;
                            }
                            break;
                        case 1:
                            state = State.PARAM_KEY;
                            parameters = new HashMap<>();
                            key = new StringBuilder();
                            break;
                        case 2:
                            result.add(new ParserNodeData(name.toString()));
                            name = new StringBuilder();
                            break;
                        default:
                            name.append(c);
                    }
                    break;
                case PARAM_KEY:
                    //noinspection SwitchStatementWithTooFewBranches
                    switch ("=".indexOf(c)) {
                        case 0:
                            state = State.PARAM_VALUE;
                            value = new StringBuilder();
                            break;
                        default:
                            key.append(c);
                    }
                    break;
                case PARAM_VALUE:
                    switch (",)\"'".indexOf(c)) {
                        case 0:
                            parameters.put(key.toString().trim(), value.toString().trim());
                            key = new StringBuilder();
                            state = State.PARAM_KEY;
                            break;
                        case 1:
                            parameters.put(key.toString().trim(), value.toString().trim());
                            result.add(new ParserNodeData(name.toString(), parameters));
                            state = State.PARAM_END;
                            break;
                        case 2:
                        case 3:
                            if (value.length() == 0) {
                                quote = c;
                                state = State.PARAM_VALUE_QUOTE;
                                break;
                            }
                            break;
                        default:
                            value.append(c);
                    }
                    break;
                case PARAM_VALUE_QUOTE:
                    switch ("\"'\\".indexOf(c)) {
                        case 0:
                        case 1:
                            if (c == quote) {
                                parameters.put(key.toString().trim(), value.toString().trim());
                                key = new StringBuilder();
                                state = State.PARAM_VALUE_QUOTE_END;
                            } else {
                                value.append(c);
                            }
                            break;
                        case 2:
                            value.append(c);
                            try {
                                i = reader.read();
                            } catch (IOException e) {
                                break;
                            }
                            if (i < 0) {
                                break;
                            }
                            value.append((char) i);
                            break;
                        default:
                            value.append(c);
                    }
                    break;
                case PARAM_VALUE_QUOTE_END:
                    switch (",)".indexOf(c)) {
                        case 0:
                            state = State.PARAM_KEY;
                            break;
                        case 1:
                            state = State.PARAM_END;
                            break;
                    }
                    break;
                case PARAM_END:
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (" ".indexOf(c)) {
                        case 0:
                            return result;
                    }
                    break;
            }

        } while (true);

        if (state == State.NAME && name.length() > 0) {
            result.add(new ParserNodeData(name.toString()));
        }

        return result;
    }

    @Override
    public String toString() {
        return name + "(" + parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ")) + ")";
    }

    enum State {
        NAME,
        PARAM_KEY,
        PARAM_VALUE,
        PARAM_VALUE_QUOTE,
        PARAM_VALUE_QUOTE_END,
        PARAM_END
    }

}
