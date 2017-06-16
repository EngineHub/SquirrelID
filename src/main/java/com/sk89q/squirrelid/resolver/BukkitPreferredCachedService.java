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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.cache.ProfileCache;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves UUIDs by first checking Bukkit's online player profile.
 * If unavailble (e.g. offline player), checks a cache, and if not cached,
 * finally falls back to a resolver.
 */
public class BukkitPreferredCachedService implements ProfileService {

    private final ProfileService resolver;
    private final ProfileCache cache;

    /**
     * Create a new instance.
     *
     * @param resolver the resolver to use
     * @param cache the cache to use
     */
    public BukkitPreferredCachedService(ProfileService resolver, ProfileCache cache) {
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
        Profile profile = BukkitPlayerService.getInstance().findByName(name);
        if (profile == null) {
            Profile cached = cache.getIfPresentByName(name);
            if (cached == null) {
                profile = resolver.findByName(name);
                if (profile != null) {
                    cache.put(profile);
                }
                return profile;
            } else {
                return cached;
            }
        }
        cache.put(profile);
        return profile;
    }

    @Override
    public ImmutableList<Profile> findAllByName(Iterable<String> names) throws IOException, InterruptedException {
        int size = Iterables.size(names);
        // check bukkit online players first
        ImmutableList<Profile> online = BukkitPlayerService.getInstance().findAllByName(names);
        cache.putAll(online);
        if (online.size() == size) {
            // all names were found
            return online;
        }
        // check which names weren't found
        List<String> checkCache = Lists.newLinkedList(names); // marginally better than an array list for removals
        for (Profile profile : online) {
            checkCache.remove(profile.getName());
        }
        // check cache for offline names
        ImmutableMap<String, Profile> cached = cache.getAllPresentByName(checkCache);
        if (online.size() + cached.size() == size) {
            // found everything, return online + cached names
            return ImmutableList.<Profile>builder().addAll(online).addAll(cached.values()).build();
        }
        // otherwise, need to do a lookup with the resolver for the remaining names
        List<String> missingNames = Lists.newLinkedList(checkCache);
        for (Profile profile : cached.values()) {
            missingNames.remove(profile.getName());
        }

        // find via resolver
        ImmutableList<Profile> lookup = resolver.findAllByName(missingNames);
        // make sure we update cache with our results
        cache.putAll(lookup);

        // return the online + cached + looked-up names together
        return ImmutableList.<Profile>builder().addAll(online).addAll(cached.values()).addAll(lookup).build();
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
