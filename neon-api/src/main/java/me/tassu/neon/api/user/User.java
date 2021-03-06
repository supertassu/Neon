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

package me.tassu.neon.api.user;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Represents an user who may apply punishments to other users.
 */
public interface User {

    /**
     * Gets the users unique ID
     *
     * @return the users Mojang assigned unique id
     */
    @NonNull
    UUID getUuid();

    /**
     * Gets the users username
     *
     * <p>Returns null if no username is known for the user.</p>
     *
     * @return the users username
     */
    @Nullable
    String getName();

    /**
     * @return True if a real player, false otherwise
     */
    boolean isRealPlayer();

    /**
     * Checks if this user has a permission.
     *
     * @param permission Permission to check.
     * @return True if user has permission, false otherwise
     */
    boolean hasPermission(String permission);

    /**
     * Sends a message to this user, if possible.
     * @param message message to send.
     */
    void sendMessage(String message);
}
