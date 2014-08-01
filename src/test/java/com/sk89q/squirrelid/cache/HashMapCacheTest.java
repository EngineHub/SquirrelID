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

import com.sk89q.squirrelid.util.ExtraMatchers;
import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;

public class HashMapCacheTest extends TestCase {

    public void testDatabase() throws Exception {
        UUID testId1 = UUID.randomUUID();
        UUID testId2 = UUID.randomUUID();
        UUID testId3 = UUID.randomUUID();

        HashMapCache cache = new HashMapCache();

        MatcherAssert.assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                ExtraMatchers.<UUID, String>hasSize(0));

        cache.put(testId1, "test1");
        cache.put(testId2, "test2");

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1)),
                allOf(
                        ExtraMatchers.<UUID, String>hasSize(1),
                        hasEntry(testId1, "test1")));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, String>hasSize(2),
                        hasEntry(testId1, "test1"),
                        hasEntry(testId2, "test2")));

        cache.put(testId1, "test1_2");

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, String>hasSize(2),
                        hasEntry(testId1, "test1_2"),
                        hasEntry(testId2, "test2")));

        cache.put(testId3, "test3");

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, String>hasSize(3),
                        hasEntry(testId1, "test1_2"),
                        hasEntry(testId2, "test2"),
                        hasEntry(testId3, "test3")));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId3)),
                allOf(
                        ExtraMatchers.<UUID, String>hasSize(2),
                        hasEntry(testId1, "test1_2"),
                        hasEntry(testId3, "test3")));
    }

}