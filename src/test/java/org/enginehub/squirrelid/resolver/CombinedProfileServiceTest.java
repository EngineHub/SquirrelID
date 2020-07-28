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

import com.google.common.collect.Lists;
import org.enginehub.squirrelid.Profile;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class CombinedProfileServiceTest {

    @Test
    public void testFindAllByName() throws Exception {
        HashMapService staticResolver = new HashMapService();
        ProfileService realResolver = HttpRepositoryService.forMinecraft();
        ProfileService resolver = new CombinedProfileService(staticResolver, realResolver);

        UUID notchUuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
        UUID fakeNotchUuid = UUID.fromString("38fe93b6-c412-44f7-a1e2-2217a08154d8");
        Profile notchProfile = new Profile(notchUuid, "Notch");
        Profile fakeNotchProfile = new Profile(fakeNotchUuid, "Notch");

        assertThat(
                resolver.findByName("Notch"),
                equalTo(notchProfile));

        MatcherAssert.assertThat(
                resolver.findAllByName(Lists.newArrayList("Notch")),
                allOf(
                        Matchers.<Profile>hasSize(1),
                        containsInAnyOrder(notchProfile)));

        staticResolver.put(notchProfile);

        assertThat(
                resolver.findByName("Notch"),
                equalTo(notchProfile));

        MatcherAssert.assertThat(
                resolver.findAllByName(Lists.newArrayList("Notch")),
                allOf(
                        Matchers.<Profile>hasSize(1),
                        containsInAnyOrder(notchProfile)));

        staticResolver.put(fakeNotchProfile);

        assertThat(
                resolver.findByName("Notch"),
                equalTo(fakeNotchProfile));

        MatcherAssert.assertThat(
                resolver.findAllByName(Lists.newArrayList("Notch")),
                allOf(
                        Matchers.<Profile>hasSize(1),
                        containsInAnyOrder(fakeNotchProfile)));
    }

    @Test
    public void testFindAllByUuid() throws Exception {
        HashMapService staticResolver = new HashMapService();
        ProfileService realResolver = HttpRepositoryService.forMinecraft();
        ProfileService resolver = new CombinedProfileService(staticResolver, realResolver);

        UUID notchUuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
        Profile notchProfile = new Profile(notchUuid, "Notch");

        assertThat(
            resolver.findByUuid(notchUuid),
            equalTo(notchProfile));

        MatcherAssert.assertThat(
            resolver.findAllByUuid(Lists.newArrayList(notchUuid)),
            allOf(
                Matchers.<Profile>hasSize(1),
                containsInAnyOrder(notchProfile)));

        staticResolver.put(notchProfile);

        assertThat(
            resolver.findByUuid(notchUuid),
            equalTo(notchProfile));

        MatcherAssert.assertThat(
            resolver.findAllByUuid(Lists.newArrayList(notchUuid)),
            allOf(
                Matchers.<Profile>hasSize(1),
                containsInAnyOrder(notchProfile)));
    }

}