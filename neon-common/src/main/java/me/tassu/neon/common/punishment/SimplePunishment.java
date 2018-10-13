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

package me.tassu.neon.common.punishment;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.NonNull;
import me.tassu.neon.api.punishment.Punishment;
import me.tassu.neon.api.punishment.PunishmentType;
import me.tassu.neon.api.user.User;
import me.tassu.neon.api.user.UserManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class SimplePunishment implements Punishment {

    public SimplePunishment(@NonNull User target, @NonNull User actor,
                            @NonNull PunishmentType type, long startDate, long expiryDate,
                            @Nullable String reason) {
        this.target = target;
        this.actor = actor;
        this.type = type;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.reason = reason;
    }

    public SimplePunishment(@NonNull UserManager userManager, @NonNull UUID target, @NonNull UUID actor,
                            @NonNull PunishmentType type, long startDate, long expiryDate,
                            @Nullable String reason) {
        this(userManager.getUser(target), userManager.getUser(actor), type, startDate, expiryDate, reason);
    }

    @Getter
    private long startDate, expiryDate;

    @Getter
    private User target, actor;

    @Override
    public boolean hasExpired() {
        if (expiryDate == -1) return false;
        return System.currentTimeMillis() - expiryDate > 0;
    }

    @Getter
    private PunishmentType type;

    @Getter
    private String reason;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("startDate", startDate)
                .add("expiryDate", expiryDate)
                .add("target", target)
                .add("actor", actor)
                .add("type", type)
                .add("reason", reason)
                .add("hasExpired", hasExpired())
                .toString();
    }
}
