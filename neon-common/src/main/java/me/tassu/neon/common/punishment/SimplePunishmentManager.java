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

import com.google.inject.Inject;
import lombok.val;
import me.tassu.neon.api.punishment.Punishment;
import me.tassu.neon.api.punishment.PunishmentManager;
import me.tassu.neon.api.punishment.PunishmentType;
import me.tassu.neon.api.punishment.SimplePunishmentType;
import me.tassu.neon.api.user.RealUser;
import me.tassu.neon.api.user.User;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.db.Schema;
import me.tassu.neon.common.db.StorageConnector;
import me.tassu.neon.common.plugin.Platform;
import me.tassu.neon.common.sync.Synchronizer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimplePunishmentManager implements PunishmentManager {

    @Inject private PunishmentHandler handler;
    @Inject private StorageConnector connector;
    @Inject private Synchronizer synchronizer;
    @Inject private UserManager manager;
    @Inject private Platform platform;

    @Override
    public @Nullable Punishment getPunishmentById(long id) {
        try (val connection = connector.getFactory().getConnection()) {
            try (val statement = connection.prepareStatement(connector.getStatementProcessor().apply(Schema.SELECT_PUNISHMENT_BY_ID))) {
                statement.setLong(1, id);
                try (val result = statement.executeQuery()) {
                    if (result.next()) {
                        return new SimplePunishment(
                                manager, UUID.fromString(result.getString("target_uuid")), UUID.fromString(result.getString("actor_uuid")),
                                getTypeById(result.getString("type")), result.getLong("given"), result.getLong("expiration"),
                                result.getString("reason")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Set<Punishment> getActivePunishments(User user) {
        val data = new HashSet<Punishment>();
        try (val connection = connector.getFactory().getConnection()) {
                try (val statement = connection.prepareStatement(connector.getStatementProcessor().apply(Schema.SELECT_ALL_PUNISHMENTS_FOR_USER))) {
                    statement.setString(1, user.getUuid().toString());
                    try (val result = statement.executeQuery()) {
                        while (result.next()) {
                            val punishment = new SimplePunishment(
                                    manager, user.getUuid(), UUID.fromString(result.getString("actor_uuid")), getTypeById(result.getString("type")),
                                    result.getLong("given"), result.getLong("expiration"), result.getString("reason")
                            );

                            if (!punishment.hasExpired()) data.add(punishment);
                        }
                    }
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return data;
    }

    @Override
    public Punishment createPunishment(RealUser target, User actor, long expiry, String reason, PunishmentType type) {
        try (val connection = connector.getFactory().getConnection()) {
            try (val statement = connection.prepareStatement(connector.getStatementProcessor().apply(Schema.ADD_PUNISHMENT), Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, actor.getUuid().toString());
                statement.setString(2, target.getUuid().toString());
                statement.setString(3, type.getId());
                statement.setString(4, reason);
                statement.setLong(5, System.currentTimeMillis());
                statement.setLong(6, expiry);
                if (statement.executeUpdate() == 0) {
                    throw new SQLException("No rows affected.");
                }

                val punishment = new SimplePunishment(target, actor, type, System.currentTimeMillis(), expiry, reason);

                try (val generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        val id = generatedKeys.getLong(1);

                        broadcast(punishment, id);

                        if (type.shouldKick() || (type.shouldPreventJoin() && target.isOnline())) {
                            target.disconnect(handler.getKickMessage(punishment));
                        } else if (type.shouldPreventJoin()) {
                            synchronizer.sync(target.getUuid());
                        }
                    }
                    else {
                        throw new SQLException("Creating punishment failed failed, no ID obtained.");
                    }
                }

                return punishment;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcast(Punishment punishment, long id) {
        if (synchronizer.isAvailable()) {
            synchronizer.broadcast(id);
            return;
        }

        val broadcast = handler.getBroadcast(punishment);
        for (String message : broadcast) {
            platform.broadcast(message);
        }
    }

    public PunishmentType getTypeById(String id) {
        try {
            return SimplePunishmentType.valueOf(id.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
