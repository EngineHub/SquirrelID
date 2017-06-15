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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.squirrelid.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@code MemoryCache} stores entries in a {@link ConcurrentMap}.
 */
public class HashMapCache extends AbstractProfileCache {

    private final BiMap<UUID, String> cache = Maps.synchronizedBiMap(HashBiMap.<UUID, String>create());

    @Override
    public void putAll(Iterable<Profile> profiles) {
        for (Profile profile : profiles) {
            cache.put(profile.getUniqueId(), profile.getName());
        }
    }

    @Override
    public ImmutableMap<UUID, Profile> getAllPresent(Iterable<UUID> uuids) {
        Map<UUID, Profile> results = new HashMap<UUID, Profile>();
        for (UUID uuid : uuids) {
            String name = cache.get(uuid);
            if (name != null) {
                results.put(uuid, new Profile(uuid, name));
            }
        }
        return ImmutableMap.copyOf(results);
    }

    @Override
    public ImmutableMap<String, Profile> getAllPresentByName(Iterable<String> names) {
        Map<String, Profile> results = new HashMap<String, Profile>();
        for (String name : names) {
            UUID uuid = cache.inverse().get(name);
            if (uuid != null) {
                results.put(name, new Profile(uuid, name));
            }
        }
        return ImmutableMap.copyOf(results);
    }

}
