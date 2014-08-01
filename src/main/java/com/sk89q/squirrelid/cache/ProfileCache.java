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
import com.sk89q.squirrelid.Profile;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Stores a "last known" mapping of UUIDs to names.
 */
public interface ProfileCache {

    /**
     * Store the given name as the last known name for the given UUID.
     *
     * <p>If the operation fails, an error will be logged but no exception
     * will be thrown.</p>
     *
     * @param profile a profile
     */
    void put(Profile profile);

    /**
     * Store a list of zero or more names.
     *
     * <p>If the operation fails, an error will be logged but no exception
     * will be thrown.</p>
     *
     * @param profiles an iterable of profiles
     */
    void putAll(Iterable<Profile> profiles);

    /**
     * Query the cache for the name for a given UUID.
     *
     * <p>If the operation fails, an error will be logged but no exception
     * will be thrown.</p>
     *
     * @param uuid the UUID
     * @return the name or {@code null} if it is not known
     */
    @Nullable
    Profile getIfPresent(UUID uuid);

    /**
     * Query the cache for the names of the given UUIDs.
     *
     * <p>If the operation fails, an error will be logged but no exception
     * will be thrown.</p>
     *
     * @param ids a list of UUIDs to query
     * @return a map of results, which may not have a key for every given UUID
     */
    ImmutableMap<UUID, Profile> getAllPresent(Iterable<UUID> ids);

}
