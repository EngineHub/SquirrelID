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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.sk89q.squirrelid.Profile;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * An abstract implementation for services that only work with one request
 * at a time.
 */
abstract class SingleRequestService implements ProfileService {

    @Override
    public final ImmutableList<Profile> findAllByName(Iterable<String> names) throws IOException, InterruptedException {
        Builder<Profile> builder = ImmutableList.builder();
        for (String name : names) {
            Profile profile = findByName(name);
            if (profile != null) {
                builder.add(profile);
            }
        }
        return builder.build();
    }

    @Override
    public final void findAllByName(Iterable<String> names, Predicate<Profile> consumer) throws IOException, InterruptedException {
        for (String name : names) {
            Profile profile = findByName(name);
            if (profile != null) {
                consumer.test(profile);
            }
        }
    }

    @Override
    public ImmutableList<Profile> findAllByUuid(Iterable<UUID> uuids) throws IOException, InterruptedException {
        Builder<Profile> builder = ImmutableList.builder();
        for (UUID uuid : uuids) {
            Profile profile = findByUuid(uuid);
            if (profile != null) {
                builder.add(profile);
            }
        }
        return builder.build();
    }

    @Override
    public final void findAllByUuid(Iterable<UUID> uuids, Predicate<Profile> consumer) throws IOException, InterruptedException {
        for (UUID uuid : uuids) {
            Profile profile = findByUuid(uuid);
            if (profile != null) {
                consumer.test(profile);
            }
        }
    }
}
