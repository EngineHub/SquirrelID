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
import org.bukkit.Bukkit;

import java.io.IOException;
import javax.annotation.Nullable;

public class PaperPlayerService extends SingleRequestService {

    private static final PaperPlayerService INSTANCE = new PaperPlayerService();

    private PaperPlayerService() {
        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PaperPlayerService called on a non-Paper server.");
        }
    }

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

}
