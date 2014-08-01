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

package com.sk89q.squirrelid.cache;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * Stores a "last known" mapping of UUIDs to names.
 */
public interface UUIDCache {

    /**
     * Store the given name as the last known name for the given UUID.
     *
     * @param uuid the given UUID
     * @param name the name
     * @throws CacheException thrown on a error occurring
     */
    void put(UUID uuid, String name) throws CacheException;

    /**
     * Store a list of zero or more names.
     *
     * @param entries a map of UUIDs to names
     * @throws CacheException thrown on a error occurring
     */
    void putAll(Map<UUID, String> entries) throws CacheException;

    /**
     * Query the cache for the name for a given UUID.
     *
     * @param uuid the UUID
     * @return the name or {@code null} if it is not known
     * @throws CacheException thrown on a error occurring
     */
    @Nullable
    String getIfPresent(UUID uuid) throws CacheException;

    /**
     * Query the cache for the names of the given UUIDs.
     *
     * @param uuids a list of UUIDs to query
     * @return a map of results, which may not have a key for every given UUID
     * @throws CacheException thrown on a error occurring
     */
    ImmutableMap<UUID, String> getAllPresent(Iterable<UUID> uuids) throws CacheException;

}
