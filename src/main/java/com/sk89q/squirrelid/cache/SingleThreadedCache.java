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

package com.sk89q.squirrelid.cache;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation that forces all requests to go to one thread.
 */
abstract class SingleThreadedCache extends AbstractUUIDCache {

    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(createExecutorService());

    /**
     * Create the executor service.
     *
     * @return the executor service
     */
    protected static ExecutorService createExecutorService() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                0, 1,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    @Override
    public final ImmutableMap<UUID, String> getAllPresent(final Iterable<UUID> uuids) throws CacheException {
        checkNotNull(uuids);

        try {
            return executorService.submit(new Callable<ImmutableMap<UUID, String>>() {
                @Override
                public ImmutableMap<UUID, String> call() throws Exception {
                    return executeGet(uuids);
                }
            }).get();
        } catch (InterruptedException e) {
            throw new CacheException("Failed to fetch UUIDs", e);
        } catch (ExecutionException e) {
            throw new CacheException("Failed to fetch UUIDs", e);
        }
    }

    @Override
    public final void putAll(Map<UUID, String> entries) throws CacheException {
        checkNotNull(entries);

        final Map<UUID, String> immutableEntries = ImmutableMap.copyOf(entries);
        try {
            executorService.submit(new Callable<Map<UUID, String>>() {
                @Override
                public Map<UUID, String> call() throws Exception {
                    executePut(immutableEntries);
                    return null;
                }
            }).get();
        } catch (InterruptedException e) {
            throw new CacheException("Failed to put UUIDs", e);
        } catch (ExecutionException e) {
            throw new CacheException("Failed to put UUIDs", e);
        }
    }

    /**
     * Execute the fetch operation.
     *
     * @param uuids a collection of UUIDs
     * @return the results
     * @throws Exception thrown on an error
     */
    protected abstract ImmutableMap<UUID, String> executeGet(Iterable<UUID> uuids) throws Exception;

    /**
     * Execute the put operation.
     *
     * @param entries a map of entries
     * @throws Exception thrown on an error
     */
    protected abstract void executePut(Map<UUID, String> entries) throws Exception;


}
