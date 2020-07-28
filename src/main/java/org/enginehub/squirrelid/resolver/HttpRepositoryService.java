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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.util.HttpRequest;
import org.enginehub.squirrelid.util.UUIDs;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves names in bulk to UUIDs using Mojang's profile HTTP API.
 */
public class HttpRepositoryService implements ProfileService {

    public static final String MINECRAFT_AGENT = "Minecraft";

    private static final Logger log = Logger.getLogger(HttpRepositoryService.class.getCanonicalName());
    private static final int MAX_NAMES_PER_REQUEST = 100;

    private final URL profilesURL;
    private final Function<UUID, URL> nameHistoryUrlCreator;
    private int maxRetries = 5;
    private long retryDelay = 50;

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
    public HttpRepositoryService(String agent) {
        checkNotNull(agent);
        profilesURL = HttpRequest.url("https://api.mojang.com/profiles/" + agent);
        nameHistoryUrlCreator = (uuid)
            -> HttpRequest.url("https://api.mojang.com/user/profiles/" + UUIDs.stripDashes(uuid.toString()) + "/names");
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Profile decodeProfileResult(Object entry) {
        try {
            if (entry instanceof Map) {
                Map<Object, Object> mapEntry = (Map<Object, Object>) entry;
                Object rawUuid = mapEntry.get("id");
                Object rawName = mapEntry.get("name");

                if (rawUuid != null && rawName != null) {
                    UUID uuid = UUID.fromString(UUIDs.addDashes(String.valueOf(rawUuid)));
                    String name = String.valueOf(rawName);
                    return new Profile(uuid, name);
                }
            }
        } catch (ClassCastException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Got invalid value from UUID lookup service", e);
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Profile decodeNameHistoryResult(Object entry, UUID uuid) {
        try {
            if (entry instanceof Map) {
                Map<Object, Object> mapEntry = (Map<Object, Object>) entry;
                Object rawName = mapEntry.get("name");

                if (rawName != null) {
                    String name = String.valueOf(rawName);
                    return new Profile(uuid, name);
                }
            }
        } catch (ClassCastException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Got invalid value from Name History lookup service", e);
        }

        return null;
    }

    /**
     * Create a resolver for Minecraft.
     *
     * @return a UUID resolver
     */
    public static ProfileService forMinecraft() {
        return new HttpRepositoryService(MINECRAFT_AGENT);
    }

    /**
     * Get the maximum number of HTTP request retries.
     *
     * @return the maximum number of retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Set the maximum number of HTTP request retries.
     *
     * @param maxRetries the maximum number of retries
     */
    public void setMaxRetries(int maxRetries) {
        checkArgument(maxRetries > 0, "maxRetries must be >= 0");
        this.maxRetries = maxRetries;
    }

    /**
     * Get the number of milliseconds to delay after each failed HTTP request,
     * doubling each time until success or total failure.
     *
     * @return delay in milliseconds
     */
    public long getRetryDelay() {
        return retryDelay;
    }

    /**
     * Set the number of milliseconds to delay after each failed HTTP request,
     * doubling each time until success or total failure.
     *
     * @param retryDelay delay in milliseconds
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    public int getIdealRequestLimit() {
        return MAX_NAMES_PER_REQUEST;
    }

    @Nullable
    @Override
    public Profile findByName(String name) throws IOException, InterruptedException {
        ImmutableList<Profile> profiles = findAllByName(ImmutableList.of(name));
        if (!profiles.isEmpty()) {
            return profiles.get(0);
        } else {
            return null;
        }
    }

    @Override
    public ImmutableList<Profile> findAllByName(Iterable<String> names) throws IOException, InterruptedException {
        Builder<Profile> builder = ImmutableList.builder();
        for (List<String> partition : Iterables.partition(names, MAX_NAMES_PER_REQUEST)) {
            builder.addAll(queryByName(partition));
        }
        return builder.build();
    }

    @Override
    public void findAllByName(Iterable<String> names, Predicate<Profile> consumer) throws IOException, InterruptedException {
        for (List<String> partition : Iterables.partition(names, MAX_NAMES_PER_REQUEST)) {
            for (Profile profile : queryByName(partition)) {
                consumer.test(profile);
            }
        }
    }

    @Nullable
    @Override
    public Profile findByUuid(UUID uuid) throws IOException, InterruptedException {
        ImmutableList<Profile> profiles = findAllByUuid(ImmutableList.of(uuid));
        if (!profiles.isEmpty()) {
            return profiles.get(0);
        } else {
            return null;
        }
    }

    @Override
    public ImmutableList<Profile> findAllByUuid(Iterable<UUID> uuids) throws IOException, InterruptedException {
        return ImmutableList.copyOf(queryByUuid(uuids));
    }

    @Override
    public void findAllByUuid(Iterable<UUID> uuids, Predicate<Profile> consumer) throws IOException, InterruptedException {
        for (Profile profile : queryByUuid(uuids)) {
            consumer.test(profile);
        }
    }

    /**
     * Perform a query for profiles by name without partitioning the queries.
     *
     * @param names an iterable of names
     * @return a list of results
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     */
    protected ImmutableList<Profile> queryByName(Iterable<String> names) throws IOException, InterruptedException {
        List<Profile> profiles = new ArrayList<>();

        Object result;

        int retriesLeft = maxRetries;
        long retryDelay = this.retryDelay;

        while (true) {
            try {
                result = HttpRequest
                    .post(profilesURL)
                    .bodyJson(names)
                    .execute()
                    .returnContent()
                    .asJson();
                break;
            } catch (IOException e) {
                if (retriesLeft == 0) {
                    throw e;
                }

                log.log(Level.WARNING, "Failed to query profile service -- retrying...", e);
                Thread.sleep(retryDelay);
            }

            retryDelay *= 2;
            retriesLeft--;
        }

        if (result instanceof Iterable) {
            for (Object entry : (Iterable) result) {
                Profile profile = decodeProfileResult(entry);
                if (profile != null) {
                    profiles.add(profile);
                }
            }
        }

        return ImmutableList.copyOf(profiles);
    }

    /**
     * Perform a query for profiles by uuid.
     *
     * @param uuids an iterable of uuids
     * @return a list of results
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     */
    protected ImmutableList<Profile> queryByUuid(Iterable<UUID> uuids) throws IOException, InterruptedException {
        List<Profile> profiles = new ArrayList<>();

        Object result;

        int retriesLeft = maxRetries;
        long retryDelay = this.retryDelay;

        for (UUID uuid : uuids) {
            while (true) {
                try {
                    result = HttpRequest
                        .get(nameHistoryUrlCreator.apply(uuid))
                        .execute()
                        .returnContent()
                        .asJson();

                    if (result instanceof Iterable) {
                        Profile lastProfile = null;
                        for (Object entry : (Iterable) result) {
                            Profile profile = decodeNameHistoryResult(entry, uuid);
                            if (profile != null) {
                                lastProfile = profile;
                            }
                        }
                        if (lastProfile != null) {
                            profiles.add(lastProfile);
                        }
                    }

                    break;
                } catch (IOException e) {
                    if (retriesLeft == 0) {
                        throw e;
                    }

                    log.log(Level.WARNING, "Failed to query name history service -- retrying...", e);
                    Thread.sleep(retryDelay);
                }

                retryDelay *= 2;
                retriesLeft--;
            }
        }

        return ImmutableList.copyOf(profiles);
    }

}
