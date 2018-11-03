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

import com.google.common.base.MoreObjects;
import lombok.val;
import me.tassu.neon.api.user.RealUser;
import me.tassu.neon.common.db.Schema;
import me.tassu.neon.common.db.StorageConnector;
import me.tassu.neon.common.scheduler.Scheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.SQLException;
import java.util.UUID;

public abstract class AbstractRealUser implements RealUser {

    protected Scheduler scheduler;
    private StorageConnector connector;

    private final UUID uuid;
    private String name = null;

    private void updateName(String name) {
        scheduler.async(() -> {
            try (val connection = connector.getFactory().getConnection()) {
                try (val statement = connection.prepareStatement(connector.getStatementProcessor().apply(Schema.ADD_OR_UPDATE_USER))) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, name);
                    statement.setString(3, name);
                    statement.execute();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected abstract void kick(String reason);

    public AbstractRealUser(StorageConnector connector, Scheduler scheduler, @NonNull UUID uuid, @Nullable String name) {
        this.connector = connector;
        this.scheduler = scheduler;
        this.uuid = uuid;

        if (name != null) {
            this.name = name;
            updateName(name);
        } else {
            scheduler.async(() -> {
                try (val connection = connector.getFactory().getConnection()) {
                    try (val statement = connection.prepareStatement(connector.getStatementProcessor().apply(Schema.SELECT_USER_BY_UUID))) {
                        statement.setString(1, uuid.toString());
                        try (val result = statement.executeQuery()) {
                            if (result.next()) {
                                this.name = result.getString("username");
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @NonNull
    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public final void disconnect(String reason) {
        if (isOnline()) {
            kick(reason);
        }
    }

    public void setName(@NonNull String name) {
        if (!name.equals(this.name)) {
            this.name = name;
            updateName(name);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", getUuid())
                .add("name", getName())
                .add("isRealPlayer", isRealPlayer())
                .toString();
    }

}
