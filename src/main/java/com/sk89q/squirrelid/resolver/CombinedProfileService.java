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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Combines several {@code ProfileService}s together and checks them from
 * first to last, stopping when there are no more services left to query or
 * all profiles were found.
 */
public class CombinedProfileService implements ProfileService {

    private final List<ProfileService> services;

    /**
     * Create a new instance.
     *
     * @param services a list of services
     */
    public CombinedProfileService(List<ProfileService> services) {
        checkNotNull(services);
        this.services = ImmutableList.copyOf(services);
    }

    /**
     * Create a new instance.
     *
     * @param services an array of services
     */
    public CombinedProfileService(ProfileService... services) {
        checkNotNull(services);
        this.services = ImmutableList.copyOf(services);
    }

    @Override
    public int getIdealRequestLimit() {
        int ideal = Integer.MAX_VALUE;
        for (ProfileService service : services) {
            ideal = Math.min(service.getIdealRequestLimit(), ideal);
        }
        return ideal;
    }

    @Nullable
    @Override
    public Profile findByName(String name) throws IOException, InterruptedException {
        for (ProfileService service : services) {
            Profile profile = service.findByName(name);
            if (profile != null) {
                return profile;
            }
        }
        return null;
    }

    @Override
    public ImmutableList<Profile> findAllByName(Iterable<String> names) throws IOException, InterruptedException {
        List<String> missing = new ArrayList<String>();
        List<Profile> totalResults = new ArrayList<Profile>();

        for (String name : names) {
            missing.add(name.toLowerCase());
        }

        for (ProfileService service : services) {
            ImmutableList<Profile> results = service.findAllByName(missing);

            for (Profile profile : results) {
                String nameLower = profile.getName().toLowerCase();
                if (missing.contains(nameLower)) {
                    missing.remove(nameLower);
                }
                totalResults.add(profile);
            }

            if (missing.isEmpty()) {
                break;
            }
        }

        return ImmutableList.copyOf(totalResults);
    }

    @Override
    public void findAllByName(Iterable<String> names, final Predicate<Profile> consumer) throws IOException, InterruptedException {
        final List<String> missing = Collections.synchronizedList(new ArrayList<String>());

        Predicate<Profile> forwardingConsumer = new Predicate<Profile>() {
            @Override
            public boolean apply(Profile profile) {
                missing.remove(profile.getName().toLowerCase());
                return consumer.apply(profile);
            }
        };

        for (String name : names) {
            missing.add(name.toLowerCase());
        }

        for (ProfileService service : services) {
            service.findAllByName(new ArrayList<String>(missing), forwardingConsumer);

            if (missing.isEmpty()) {
                break;
            }
        }
    }

}
