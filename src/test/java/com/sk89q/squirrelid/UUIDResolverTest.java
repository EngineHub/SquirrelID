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

import com.sk89q.squirrelid.util.ExtraMatchers;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;

public class UUIDResolverTest extends TestCase {

    public void testGetAllPresent() throws Exception {
        UUIDResolver resolver = UUIDResolver.forMinecraft();

        UUID notchUuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
        UUID jebUuid = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6");

        assertThat(
                resolver.getAllPresent(Arrays.asList("Notch")),
                allOf(
                        ExtraMatchers.<String, UUID>hasSize(1),
                        hasEntry("Notch", notchUuid)));

        assertThat(
                resolver.getAllPresent(Arrays.asList("Notch", "jeb_")),
                allOf(
                        ExtraMatchers.<String, UUID>hasSize(2),
                        hasEntry("Notch", notchUuid),
                        hasEntry("jeb_", jebUuid)));

        assertThat(
                resolver.getAllPresent(Arrays.asList("!__@#%*@#^(@6__NOBODY____", "jeb_")),
                allOf(
                        ExtraMatchers.<String, UUID>hasSize(1),
                        hasEntry("jeb_", jebUuid)));
    }

}