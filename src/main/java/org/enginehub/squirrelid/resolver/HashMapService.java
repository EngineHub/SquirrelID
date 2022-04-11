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

package org.enginehub.squirrelid.resolver;

import org.enginehub.squirrelid.Profile;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

/**
 * A {@code ProfileService} backed by a {@code ConcurrentHashMap}.
 */
public class HashMapService extends SingleRequestService {

    private final ConcurrentHashMap<String, UUID> nameToIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> idToNameMap = new ConcurrentHashMap<>();

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
            this.nameToIdMap.put(entry.getKey(), entry.getValue());
            this.idToNameMap.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Add the given profile to the internal map.
     *
     * @param profile the profile
     */
    public void put(Profile profile) {
        this.nameToIdMap.put(profile.getName(), profile.getUniqueId());
        this.idToNameMap.put(profile.getUniqueId(), profile.getName());
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
        UUID uuid = nameToIdMap.get(name);
        if (uuid != null) {
            return new Profile(uuid, name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Profile findByUuid(UUID uuid) throws IOException, InterruptedException {
        String name = idToNameMap.get(uuid);
        if (name != null) {
            return new Profile(uuid, name);
        } else {
            return null;
        }
    }

}
