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

package com.sk89q.squirrelid.resolver;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.UUID;

public interface UUIDResolver {

    /**
     * Query the profile server for UUIDs for the given names.
     *
     * @param names an iterable containing names
     * @return a map of results, which may not contain results for names that are not in the database
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     */
    ImmutableMap<String, UUID> getAllPresent(Iterable<String> names) throws IOException, InterruptedException;

}
