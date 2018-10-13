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
import me.tassu.neon.api.user.User;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.db.Schema;
import me.tassu.neon.common.db.StorageConnector;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimplePunishmentManager implements PunishmentManager {

    @Inject private StorageConnector connector;
    @Inject private UserManager manager;

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

                        System.out.println("found: " + punishment);

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
    public Punishment createPunishment(User target, User actor, long expiry, String reason, PunishmentType type) {
        System.out.println(connector);
        try (val connection = connector.getFactory().getConnection()) {
            try (val statement = connection.prepareStatement(connector.getStatementProcessor().apply(Schema.ADD_PUNISHMENT))) {
                statement.setString(1, actor.getUuid().toString());
                statement.setString(2, target.getUuid().toString());
                statement.setString(3, type.getId());
                statement.setString(4, reason);
                statement.setLong(5, System.currentTimeMillis());
                statement.setLong(6, expiry);
                statement.execute();
            }

            return new SimplePunishment(target, actor, type, System.currentTimeMillis(), expiry, reason);
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
