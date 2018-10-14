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

package me.tassu.neon.api.punishment;

public enum SimplePunishmentType implements PunishmentType {

    BAN(true, false, true),
    MUTE(false, true, true),
    KICK(false, false, false)

    ;

    SimplePunishmentType(boolean shouldPreventJoin, boolean shouldPreventChat, boolean isRemovable) {
        this.shouldPreventJoin = shouldPreventJoin;
        this.shouldPreventChat = shouldPreventChat;
        this.isRemovable = isRemovable;
    }

    private boolean shouldPreventJoin;
    private boolean shouldPreventChat;
    private boolean isRemovable;

    @Override
    public String getId() {
        return name();
    }

    @Override
    public boolean shouldPreventJoin() {
        return shouldPreventJoin;
    }

    @Override
    public boolean shouldPreventChat() {
        return shouldPreventChat;
    }

    @Override
    public boolean isRemovable() {
        return isRemovable;
    }
}
