/*
 * PortalNetwork - Portals for Players
 * Copyright (C) 2022 PortalNetwork Developers
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

package au.com.grieve.portalnetwork.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.io.IOException;

public class Converter {
    public static class MaterialDeserializer extends JsonDeserializer<Material> {

        @Override
        public Material deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Material result = Material.getMaterial(p.getValueAsString());
            if (result == null) {
                throw new IllegalArgumentException("Invalid material: " + p.getValueAsString());
            }
            return result;
        }
    }

    public static class SoundDeserializer extends JsonDeserializer<Sound> {

        @Override
        public Sound deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            try {
                return Sound.valueOf(p.getValueAsString());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid sound: " + p.getValueAsString());
            }
        }
    }
}
