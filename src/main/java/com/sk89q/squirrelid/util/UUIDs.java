/*
 * SquirrelID, a UUID library for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) SquirrelID team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.squirrelid.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for UUIDs.
 */
public final class UUIDs {

    private static final Pattern DASHLESS_PATTERN = Pattern.compile("^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");

    private UUIDs() {
    }

    /**
     * Add dashes to a UUID.
     *
     * <p>If dashes already exist, the same UUID will be returned.</p>
     *
     * @param uuid the UUID
     * @return a UUID with dashes
     * @throws IllegalArgumentException thrown if the given input is not actually a UUID
     */
    public static String addDashes(String uuid) {
        uuid = uuid.replace("-", ""); // Remove dashes
        Matcher matcher = DASHLESS_PATTERN.matcher(uuid);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid UUID format");
        }
        return matcher.replaceAll("$1-$2-$3-$4-$5");
    }

    /**
     * Strip dashes from a UUID.
     *
     * <p>
     *     If dashes have already been stripped, the same UUID will be returned.
     * </p>
     *
     * @param uuid the UUID
     * @return a UUID without dashes
     * @throws IllegalArgumentException thrown if the given input is not actually a UUID
     */
    public static String stripDashes(String uuid) {
        uuid = uuid.replace("-", ""); // Remove dashes
        Matcher matcher = DASHLESS_PATTERN.matcher(uuid);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid UUID format");
        }
        return uuid;
    }
}
