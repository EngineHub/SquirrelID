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
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Checks the list of online players in Bukkit to find UUIDs.
 */
public class BukkitPlayerService extends SingleRequestService {

    private static final BukkitPlayerService INSTANCE = new BukkitPlayerService();

    private BukkitPlayerService() {
    }

    @Override
    public int getIdealRequestLimit() {
        return Integer.MAX_VALUE;
    }

    @Nullable
    @Override
    public Profile findByName(String name) throws IOException, InterruptedException {
        Player player = Bukkit.getServer().getPlayerExact(name);
        if (player != null) {
            return new Profile(player.getUniqueId(), player.getName());
        } else {
            return null;
        }
    }

    public static BukkitPlayerService getInstance() {
        return INSTANCE;
    }

}
