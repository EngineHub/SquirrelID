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

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Resolves names into UUIDs.
 */
public interface ProfileService {

    /**
     * Get the optimal maximum number of profiles that can be found
     * with one {@link #findAllByName(Iterable)} call.
     *
     * <p>{@link #findAllByName(Iterable)} (and similar) methods may split up
     * requests into smaller ones to fit within one request. This method
     * returns the ideal maximum number.</p>
     *
     * @return the number of profiles
     */
    int getIdealRequestLimit();

    /**
     * Query the profile server for the UUID of a name.
     *
     * @param name a name
     * @return the profile of the user, otherwise {@code null}
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     */
    @Nullable
    Profile findByName(String name) throws IOException, InterruptedException;

    /**
     * Query the profile server for UUIDs for the given names.
     *
     * @param names an iterable containing names to search
     * @return a list of found profiles
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     */
    ImmutableList<Profile> findAllByName(Iterable<String> names) throws IOException, InterruptedException;

    /**
     * Query the profile server for UUIDs for the given names.
     *
     * @param names an iterable containing names to search
     * @param consumer a consumer function that will receive discovered profiles
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     */
    void findAllByName(Iterable<String> names, Predicate<Profile> consumer) throws IOException, InterruptedException;

}
