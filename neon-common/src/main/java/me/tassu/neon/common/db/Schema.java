/*
 * This file is part of Neon, licensed under the MIT License.
 *
 * Copyright (c) 2018 Tassu <hello@tassu.me>
 * Copyright (c) 2018 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.tassu.neon.common.db;

public class Schema {

    public static final String SELECT_USER_BY_UUID = "SELECT uuid, username FROM {prefix}_players WHERE uuid=? LIMIT 1;";
    public static final String SELECT_USER_BY_NAME = "SELECT uuid, username FROM {prefix}_players WHERE username=? LIMIT 1;";
    public static final String SELECT_USER_BY_ANY = "SELECT uuid, username FROM {prefix}_players WHERE username=? OR uuid=? LIMIT 1;";

    public static final String ADD_OR_UPDATE_USER =
            "INSERT INTO {prefix}_players (uuid, username) VALUES (?, ?) ON DUPLICATE KEY UPDATE username=?;";

    public static final String ADD_PUNISHMENT =
            "INSERT INTO {prefix}_punishments (actor_uuid, target_uuid, type, reason, given, expiration) VALUES (?, ?, ?, ?, ?, ?);";

    public static final String SELECT_ALL_PUNISHMENTS_FOR_USER =
            "SELECT id, target_uuid, actor_uuid, type, reason, given, expiration, revoked FROM {prefix}_punishments WHERE target_uuid=?";
    public static final String SELECT_PUNISHMENT_BY_ID =
            "SELECT id, target_uuid, actor_uuid, type, reason, given, expiration, revoked FROM {prefix}_punishments WHERE id=?";

    private Schema() {}

}
