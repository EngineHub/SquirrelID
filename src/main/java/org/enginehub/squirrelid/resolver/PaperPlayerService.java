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
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Uses the shared paper profile cache to lookup profiles.
 */
public class PaperPlayerService extends SingleRequestService {

    private static PaperPlayerService INSTANCE;

    static {
        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            INSTANCE = new PaperPlayerService();
        } catch (ClassNotFoundException e) {
            INSTANCE = null;
        }
    }

    private PaperPlayerService() {
    }

    /**
     * Gets the instance of this service.
     *
     * <p>
     *     This instance will be null if Paper is not
     *     detected
     * </p>
     *
     * @return the instance
     */
    public static PaperPlayerService getInstance() {
        return INSTANCE;
    }

    @Override
    public int getIdealRequestLimit() {
        return Integer.MAX_VALUE;
    }

    @Nullable
    @Override
    public Profile findByName(String name) throws IOException, InterruptedException {
        com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(name);
        if (profile.completeFromCache()) {
            return new Profile(profile.getId(), profile.getName());
        }

        return null;
    }

    @Nullable
    @Override
    public Profile findByUuid(UUID uuid) throws IOException, InterruptedException {
        com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(uuid);
        if (profile.completeFromCache()) {
            return new Profile(profile.getId(), profile.getName());
        }

        return null;
    }

}
