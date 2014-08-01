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
import com.sk89q.squirrelid.cache.CacheException;
import com.sk89q.squirrelid.cache.UUIDCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves UUIDs using another resolver and stores results to a cache.
 */
public class CachingResolver implements UUIDResolver {

    private static final Logger log = Logger.getLogger(CachingResolver.class.getCanonicalName());
    private final UUIDResolver resolver;
    private final UUIDCache cache;

    /**
     * Create a new instance.
     *
     * @param resolver the resolver to use
     * @param cache the cache to use
     */
    public CachingResolver(UUIDResolver resolver, UUIDCache cache) {
        checkNotNull(resolver);
        checkNotNull(cache);

        this.resolver = resolver;
        this.cache = cache;
    }

    @Override
    public ImmutableMap<String, UUID> getAllPresent(Iterable<String> names) throws IOException, InterruptedException {
        ImmutableMap<String, UUID> results = resolver.getAllPresent(names);

        try {
            // Flip keys and values
            Map<UUID, String> map = new HashMap<UUID, String>();
            for (Map.Entry<String, UUID> entry : results.entrySet()) {
                map.put(entry.getValue(), entry.getKey());
            }

            cache.putAll(map);
        } catch (CacheException e) {
            log.log(Level.WARNING, "Failed to add resolved UUIDs to the cache", e);
        }

        return results;
    }

}
