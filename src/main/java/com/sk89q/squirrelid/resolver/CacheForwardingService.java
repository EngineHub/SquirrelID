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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.cache.ProfileCache;

import javax.annotation.Nullable;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves UUIDs using another resolver and stores results to a cache.
 */
public class CacheForwardingService implements ProfileService {

    private final ProfileService resolver;
    private final ProfileCache cache;

    /**
     * Create a new instance.
     *
     * @param resolver the resolver to use
     * @param cache the cache to use
     */
    public CacheForwardingService(ProfileService resolver, ProfileCache cache) {
        checkNotNull(resolver);
        checkNotNull(cache);

        this.resolver = resolver;
        this.cache = cache;
    }

    @Override
    public int getIdealRequestLimit() {
        return resolver.getIdealRequestLimit();
    }

    @Nullable
    @Override
    public Profile findByName(String name) throws IOException, InterruptedException {
        Profile profile = resolver.findByName(name);
        if (profile != null) {
            cache.put(profile);
        }
        return profile;
    }

    @Override
    public ImmutableList<Profile> findAllByName(Iterable<String> names) throws IOException, InterruptedException {
        ImmutableList<Profile> profiles = resolver.findAllByName(names);
        for (Profile profile : profiles) {
            cache.put(profile);
        }
        return profiles;
    }

    @Override
    public void findAllByName(Iterable<String> names, final Predicate<Profile> consumer) throws IOException, InterruptedException {
        resolver.findAllByName(names, new Predicate<Profile>() {
            @Override
            public boolean apply(@Nullable Profile input) {
                cache.put(input);
                return consumer.apply(input);
            }
        });
    }
}
