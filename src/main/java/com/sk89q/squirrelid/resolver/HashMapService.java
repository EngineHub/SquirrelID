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

import com.sk89q.squirrelid.Profile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@code ProfileService} backed by a {@code ConcurrentHashMap}.
 */
public class HashMapService extends SingleRequestService {

    private final ConcurrentHashMap<String, UUID> map = new ConcurrentHashMap<String, UUID>();

    /**
     * Create a new instance.
     */
    public HashMapService() {
    }

    /**
     * Create a new instance and add the entries of the given map
     * to the internal map.
     *
     * @param map a map of entries
     */
    public HashMapService(Map<String, UUID> map) {
        for (Map.Entry<String, UUID> entry : map.entrySet()) {
            this.map.put(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    /**
     * Add the given profile to the internal map.
     *
     * @param profile the profile
     */
    public void put(Profile profile) {
        this.map.put(profile.getName().toLowerCase(), profile.getUniqueId());
    }

    /**
     * Add the given profiles to the internal map.
     *
     * @param profiles a collection of profiles
     */
    public void putAll(Collection<Profile> profiles) {
        for (Profile profile : profiles) {
            put(profile);
        }
    }

    @Override
    public int getIdealRequestLimit() {
        return Integer.MAX_VALUE;
    }

    @Nullable
    @Override
    public Profile findByName(String name) throws IOException, InterruptedException {
        UUID uuid = map.get(name.toLowerCase());
        if (uuid != null) {
            return new Profile(uuid, name);
        } else {
            return null;
        }
    }

}
