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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ParserMethod {
    @Getter
    BaseCommand command;
    @Getter
    Method method;

    public ParserMethod(BaseCommand command, Method method) {
        this.command = command;
        this.method = method;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Object invoke(List<Object> args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return invoke(args.toArray());
    }

    @SuppressWarnings("UnusedReturnValue")
    public Object invoke(Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(command, args);
    }
}
