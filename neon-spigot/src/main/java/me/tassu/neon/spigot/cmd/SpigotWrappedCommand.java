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

package me.tassu.neon.spigot.cmd;

import com.google.common.collect.Lists;
import lombok.val;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.command.meta.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SpigotWrappedCommand extends org.bukkit.command.Command {

    private UserManager manager;
    private Command command;

    protected SpigotWrappedCommand(Command command, UserManager userManager) {
        super(command.names()[0]);
        this.command = command;
        this.manager = userManager;

        this.setAliases(Arrays.asList(command.names()));
    }

    @Override
    public boolean execute(CommandSender bukkitSender, String commandLabel, String[] args) {
        val sender = bukkitSender instanceof Player
                ? manager.getUser(((Player) bukkitSender).getUniqueId())
                : manager.getConsoleUser();

        try {
            command.run(sender, commandLabel, Arrays.asList(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return Lists.newArrayList(); // TODO
    }
}
