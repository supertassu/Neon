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

package me.tassu.neon.spigot.sync;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import me.tassu.neon.common.sync.ISynchronizer;
import me.tassu.neon.spigot.NSpigotBootstrap;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.tassu.neon.common.util.ChatColor.color;

public class SpigotBungeeSynchronizer implements ISynchronizer {

    private static final String CHANNEL = "neon:update";

    @Inject private Logger logger;
    @Inject private NSpigotBootstrap plugin;

    @Override
    public void open() {
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, CHANNEL);
        //this.plugin.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, CHANNEL, this);
    }

    @Override
    public void close() {
        //this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin, CHANNEL);
        this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin, CHANNEL);

        if (!queue.isEmpty()) {
            while (!queue.isEmpty() && !Bukkit.getOnlinePlayers().isEmpty()) {
                val message = queue.remove(0);
                send(message);
            }

            if (!queue.isEmpty()) {
                logger.warn(queue.size() + " BungeeSync packets were discarded because there are no players online.");
                queue.clear();
            }
        }
    }
    
    private List<OutgoingMessage> queue = new ArrayList<>();

    @Override
    public void sync(UUID uuid) {
        send("Sync", uuid.toString());
    }

    @Override
    public void broadcast(long id) {
        send("Broadcast", String.valueOf(id));
    }

    @Override
    public void kick(long id) {
        send("Kick", String.valueOf(id));
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    private static class OutgoingMessage {
        private byte[] bytes;
    }

    public void clearQueue() {
        if (!queue.isEmpty() && !Bukkit.getOnlinePlayers().isEmpty()) {
            val message = queue.remove(0);
            send(message);
        }
    }

    private void send(String... message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        for (String string : message) {
            out.writeUTF(string);
        }

        send(out.toByteArray());
    }

    private void send(byte[] bytes) {
        send(OutgoingMessage.of(bytes));
    }

    private void send(OutgoingMessage message) {
        val player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) {
            queue.add(message);
            logger.warn("No players online - a BungeeSync packet was queued.");
            return;
        }

        player.sendPluginMessage(plugin, CHANNEL, message.getBytes());
    }

    @Override
    public String getImplementationName() {
        return "BungeeCord";
    }

}
