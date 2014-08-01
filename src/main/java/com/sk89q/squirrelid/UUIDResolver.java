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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.sk89q.squirrelid.util.HttpRequest;
import com.sk89q.squirrelid.util.UUIDs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves names in bulk to UUIDs.
 */
public class UUIDResolver {

    private static final Logger log = Logger.getLogger(UUIDResolver.class.getCanonicalName());
    private static final int MAX_NAMES_PER_REQUEST = 100;
    public static final String MINECRAFT_AGENT = "Minecraft";

    private final URL profilesURL;

    /**
     * Create a new resolver.
     *
     * <p>For Minecraft, use the {@link #MINECRAFT_AGENT} constant. The UUID
     * to name mapping is only available if a user owns the game for the
     * provided "agent," so an incorrect agent may return zero results or
     * incorrect results.</p>
     *
     * @param agent the agent (i.e. the game)
     */
    public UUIDResolver(String agent) {
        checkNotNull(agent);
        profilesURL = HttpRequest.url("https://api.mojang.com/profiles/" + agent);
    }

    /**
     * Query the profile server for UUIDs for the given names.
     *
     * @param names an iterable containing names
     * @return a map of results, which may not contain results for names that are not in the database
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     */
    public ImmutableMap<String, UUID> getAllPresent(Iterable<String> names) throws IOException, InterruptedException {
        Map<String, UUID> results = new HashMap<String, UUID>();

        for (List<String> partition : Iterables.partition(names, MAX_NAMES_PER_REQUEST)) {
            Object result = HttpRequest
                    .post(profilesURL)
                    .bodyJson(partition)
                    .execute()
                    .returnContent()
                    .asJson();

            if (result instanceof Iterable) {
                for (Object entry : (Iterable) result) {
                    addResult(results, entry);
                }
            }
        }

        return ImmutableMap.copyOf(results);
    }

    @SuppressWarnings("unchecked")
    private static void addResult(Map<String, UUID> results, Object entry) {
        try {
            if (entry instanceof Map) {
                Map<Object, Object> mapEntry = (Map<Object, Object>) entry;
                Object rawUuid = mapEntry.get("id");
                Object rawName = mapEntry.get("name");

                if (rawUuid != null && rawName != null) {
                    UUID uuid = UUID.fromString(UUIDs.addDashes(String.valueOf(rawUuid)));
                    String name = String.valueOf(rawName);
                    results.put(name, uuid);
                }
            }
        } catch (ClassCastException e) {
            log.log(Level.WARNING, "Got invalid value from UUID lookup service", e);
        } catch (IllegalArgumentException e) {
            log.log(Level.WARNING, "Got invalid value from UUID lookup service", e);
        }
    }

    /**
     * Create a resolver for Minecraft.
     *
     * @return a UUID resolver
     */
    public static UUIDResolver forMinecraft() {
        return new UUIDResolver(MINECRAFT_AGENT);
    }

}
