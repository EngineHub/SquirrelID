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
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class HttpRepositoryServiceTest {

    @Test
    public void testFindAllByName() throws Exception {
        ProfileService resolver = HttpRepositoryService.forMinecraft();

        UUID notchUuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
        UUID jebUuid = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6");
        Profile notchProfile = new Profile(notchUuid, "Notch");
        Profile jebProfile = new Profile(jebUuid, "jeb_");

        assertThat(
                resolver.findByName("Notch"),
                equalTo(notchProfile));

        assertThat(
                resolver.findByName("!__@#%*@#^(@6__NOBODY____"),
                equalTo(null));

        assertThat(
                resolver.findAllByName(Lists.newArrayList("Notch")),
                allOf(
                        Matchers.<Profile>hasSize(1),
                        containsInAnyOrder(notchProfile)));

        assertThat(
                resolver.findAllByName(Arrays.asList("Notch", "jeb_")),
                allOf(
                        Matchers.<Profile>hasSize(2),
                        containsInAnyOrder(notchProfile, jebProfile)));

        assertThat(
                resolver.findAllByName(Arrays.asList("VNAYBMIl3xsMjEWk", "jeb_")),
                allOf(
                        Matchers.<Profile>hasSize(1),
                        containsInAnyOrder(jebProfile)));
    }

    @Test
    public void testFindAllByUuid() throws Exception {
        ProfileService resolver = HttpRepositoryService.forMinecraft();

        UUID notchUuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
        UUID jebUuid = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6");
        UUID me4502Uuid = UUID.fromString("1381ed2f-de72-411b-a830-316deec050e7");
        Profile notchProfile = new Profile(notchUuid, "Notch");
        Profile jebProfile = new Profile(jebUuid, "jeb_");
        Profile me4502Profile = new Profile(me4502Uuid, "Me4502"); // I've updated my name

        assertThat(
            resolver.findByUuid(notchUuid),
            equalTo(notchProfile));

        assertThat(
            resolver.findAllByUuid(Lists.newArrayList(notchUuid)),
            allOf(
                Matchers.<Profile>hasSize(1),
                containsInAnyOrder(notchProfile)));

        assertThat(
            resolver.findAllByUuid(Arrays.asList(notchUuid, jebUuid)),
            allOf(
                Matchers.<Profile>hasSize(2),
                containsInAnyOrder(notchProfile, jebProfile)));

        assertThat(
            resolver.findByUuid(me4502Uuid),
            equalTo(me4502Profile));
    }
}
