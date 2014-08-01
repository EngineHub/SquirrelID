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
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.sk89q.squirrelid.Profile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves profiles with several parallel threads using another resolver.
 */
public class ParallelProfileService implements ProfileService {

    private final ProfileService resolver;
    private final ExecutorService executorService;
    private int profilesPerJob = 100;

    /**
     * Create a new parallel resolver.
     *
     * @param resolver the resolver to use
     * @param executorService the executor service to schedule jobs in
     */
    public ParallelProfileService(ProfileService resolver, ExecutorService executorService) {
        checkNotNull(resolver);
        checkNotNull(executorService);

        this.resolver = resolver;
        this.executorService = executorService;
    }

    /**
     * Create a new parallel resolver.
     *
     * @param resolver the resolver to use
     * @param nThreads the number of threads to resolve profiles in
     */
    public ParallelProfileService(ProfileService resolver, int nThreads) {
        this(resolver, Executors.newFixedThreadPool(nThreads));
    }

    /**
     * Get the upper bound number of profiles to find per thread.
     *
     * @return a number of profiles
     */
    public int getProfilesPerJob() {
        return profilesPerJob;
    }

    /**
     * Set the upper bound number of profiles to find per thread.
     *
     * @param profilesPerJob a number of profiles
     */
    public void setProfilesPerJob(int profilesPerJob) {
        checkArgument(profilesPerJob >= 1, "profilesPerJob must be >= 1");
        this.profilesPerJob = profilesPerJob;
    }

    @Override
    public int getIdealRequestLimit() {
        return resolver.getIdealRequestLimit();
    }

    /**
     * Get the number or profiles to execute per job.
     *
     * @return the number of profiles per job
     */
    protected int getEffectiveProfilesPerJob() {
        return Math.min(profilesPerJob, resolver.getIdealRequestLimit());
    }

    @Nullable
    @Override
    public Profile findByName(String name) throws IOException, InterruptedException {
        return resolver.findByName(name);
    }

    @Override
    public ImmutableList<Profile> findAllByName(Iterable<String> names) throws IOException, InterruptedException {
        CompletionService<List<Profile>> completion = new ExecutorCompletionService<List<Profile>>(executorService);
        int count = 0;
        for (final List<String> partition : Iterables.partition(names, getEffectiveProfilesPerJob())) {
            count++;
            completion.submit(new Callable<List<Profile>>() {
                @Override
                public List<Profile> call() throws Exception {
                    return resolver.findAllByName(partition);
                }
            });
        }

        Builder<Profile> builder = ImmutableList.builder();
        for (int i = 0; i < count; i++) {
            try {
                builder.addAll(completion.take().get());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else {
                    throw new RuntimeException("Error occurred during the operation", e);
                }
            }
        }
        return builder.build();
    }

    @Override
    public void findAllByName(Iterable<String> names, final Predicate<Profile> consumer) throws IOException, InterruptedException {
        CompletionService<Object> completion = new ExecutorCompletionService<Object>(executorService);
        int count = 0;
        for (final List<String> partition : Iterables.partition(names, getEffectiveProfilesPerJob())) {
            count++;
            completion.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    resolver.findAllByName(partition, consumer);
                    return null;
                }
            });
        }

        Throwable throwable = null;
        for (int i = 0; i < count; i++) {
            try {
                completion.take().get();
            } catch (ExecutionException e) {
                throwable = e.getCause();
            }
        }

        if (throwable != null) {
            if (throwable instanceof IOException) {
                throw (IOException) throwable;
            } else {
                throw new RuntimeException("Error occurred during the operation", throwable);
            }
        }
    }

}
