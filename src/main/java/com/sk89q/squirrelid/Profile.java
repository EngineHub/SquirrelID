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

package com.sk89q.squirrelid;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A pairing of a user's UUID and his or her username.
 *
 * <p>Two profile objects are equal if they have the same UUID.</p>
 */
public final class Profile {

    private final UUID uniqueId;
    private final String name;

    /**
     * Create a new instance.
     *
     * @param uniqueId the user's UUID
     * @param name the user's username
     */
    public Profile(UUID uniqueId, String name) {
        checkNotNull(uniqueId);
        checkNotNull(name);

        this.uniqueId = uniqueId;
        this.name = name;
    }

    /**
     * Get the user's UUID.
     *
     * @return the user's UUID
     */
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Create a copy of this profile but with a new UUID.
     *
     * @param uniqueId the new UUID
     * @return a new profile
     */
    public Profile setUniqueId(UUID uniqueId) {
        return new Profile(uniqueId, name);
    }

    /**
     * Get the user's name.
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Create a copy of this profile but with a new name.
     *
     * @param name the new name
     * @return a new profile
     */
    public Profile setName(String name) {
        return new Profile(uniqueId, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        if (!uniqueId.equals(profile.uniqueId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "Profile{" +
                "uniqueId=" + uniqueId +
                ", name='" + name + '\'' +
                '}';
    }

}
