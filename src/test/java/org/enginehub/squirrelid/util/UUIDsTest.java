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

package org.enginehub.squirrelid.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UUIDsTest {

    @Test
    public void testAddDashes() throws Exception {
        assertThat(UUIDs.addDashes("a8fb55e5-8438-4bbc-8d08-633cce6078f8"), equalTo("a8fb55e5-8438-4bbc-8d08-633cce6078f8"));
        assertThat(UUIDs.addDashes("a8fb55e584384bbc8d08633cce6078f8"), equalTo("a8fb55e5-8438-4bbc-8d08-633cce6078f8"));
    }

    @Test
    public void testStripDashes() throws Exception {
        assertThat(UUIDs.stripDashes("a8fb55e5-8438-4bbc-8d08-633cce6078f8"), equalTo("a8fb55e584384bbc8d08633cce6078f8"));
        assertThat(UUIDs.stripDashes("a8fb55e584384bbc8d08633cce6078f8"), equalTo("a8fb55e584384bbc8d08633cce6078f8"));
    }

}