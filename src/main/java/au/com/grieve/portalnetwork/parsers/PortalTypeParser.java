/*
 * PortalNetwork - Portals for Players
 * Copyright (C) 2023 PortalNetwork Developers
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

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.InvalidOptionError;
import au.com.grieve.bcf.impl.parser.BaseParser;
import au.com.grieve.portalnetwork.PortalNetwork;
import lombok.ToString;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Return the type of portal as a String This technically could just be a literal parser, but we
 * want to support 3rd parties adding their own portal types
 */
@ToString(callSuper = true)
public class PortalTypeParser extends BaseParser<CommandSender, String> {
    public PortalTypeParser(Map<String, String> parameters) {
        super(parameters);
    }

    @Override
    protected String doParse(ParserContext<CommandSender> context, ParsedLine line)
            throws EndOfLineException, ParserSyntaxException {
        String input = line.next();

        if (PortalNetwork.getInstance()
                .getPortalManager()
                .getPortalClasses()
                .containsKey(input.toLowerCase())) {
            return input.toLowerCase();
        }

        throw new ParserSyntaxException(
                line,
                new InvalidOptionError(
                        new ArrayList<>(
                                PortalNetwork.getInstance()
                                        .getPortalManager()
                                        .getPortalClasses()
                                        .keySet())));
    }

    @Override
    protected void doComplete(
            ParserContext<CommandSender> context, ParsedLine line, List<CompletionCandidateGroup> candidates)
            throws EndOfLineException {
        String input = line.next();

        PortalNetwork.getInstance()
                .getPortalManager()
                .getPortalConfig().forEach((name, config) -> {
                    CompletionCandidateGroup group = new StaticCompletionCandidateGroup(input, (config.getDescription() != null && !config.getDescription().isEmpty()) ? config.getDescription() : getDescription());
                    group.getCompletionCandidates().add(new DefaultCompletionCandidate(name));
                    candidates.add(group);
                });
    }
}
