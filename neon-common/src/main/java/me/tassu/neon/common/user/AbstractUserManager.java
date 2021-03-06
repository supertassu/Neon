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

package me.tassu.neon.common.user;

import lombok.val;
import me.tassu.neon.api.user.User;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.db.Schema;
import me.tassu.neon.common.db.StorageConnector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.SQLException;
import java.util.UUID;

public abstract class AbstractUserManager implements UserManager {

    private StorageConnector connector;

    public AbstractUserManager(StorageConnector connector) {
        this.connector = connector;
    }

    @Override
    public @Nullable User getUser(@NonNull String name) {
        String uuid = null;

        try (val connection = connector.getFactory().getConnection()) {
            try (val statement = connection.prepareStatement(connector.getStatementProcessor().apply(Schema.SELECT_USER_BY_ANY))) {
                statement.setString(1, name);
                statement.setString(2, name);
                try (val result = statement.executeQuery()) {
                    if (result.next()) {
                        uuid = result.getString("uuid");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (uuid != null) {
            return getUser(UUID.fromString(uuid));
        }

        return null;
    }

}
