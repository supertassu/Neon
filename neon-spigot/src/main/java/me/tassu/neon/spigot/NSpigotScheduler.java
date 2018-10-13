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

package me.tassu.neon.spigot;

import com.google.inject.Inject;
import me.tassu.neon.common.plugin.Platform;
import me.tassu.neon.common.scheduler.Scheduler;
import me.tassu.neon.common.scheduler.Task;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.Map;

public class NSpigotScheduler implements Scheduler {

    @Inject private NSpigotBootstrap plugin;
    @Inject private Platform platform;

    @Inject private BukkitScheduler scheduler;
    private BukkitScheduler asyncScheduler;

    @Override
    public void boot() {} // NO-OP

    @Override
    public void shutdown() {
        scheduler.cancelTasks(plugin);
    }

    @Override
    public void schedule(Task task) {
        if (task.getTaskId() != -1 && getTask(task.getTaskId(), task.isAsync()) != null) {
            throw new IllegalArgumentException("already scheduled");
        }

        BukkitTask bukkit;
        if (task.isAsync()) {
            bukkit = scheduler.runTaskTimerAsynchronously(plugin, task, task.getDelay(), task.getRepeat());
        } else {
            bukkit = scheduler.runTaskTimer(plugin, task, task.getDelay(), task.getRepeat());
        }

        task.setTaskId(bukkit.getTaskId());
    }

    @Override
    public void unschedule(Task task) {
        if (task.getTaskId() == -1 || getTask(task.getTaskId(), task.isAsync()) == null) {
            throw new IllegalArgumentException("not scheduled");
        }

        scheduler.cancelTask(task.getTaskId());
    }

    @Override
    public void async(Runnable runnable) {
        if (platform.isAsync()) {
            runnable.run();
            return;
        }

        scheduler.runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void sync(Runnable runnable) {
        if (!platform.isAsync()) {
            runnable.run();
            return;
        }

        scheduler.runTask(plugin, runnable);
    }

    @Override
    public boolean isScheduled(Task task) {
        return task.getTaskId() != -1 && getTask(task.getTaskId(), task.isAsync()) != null;
    }

    private Field runnerField;

    private BukkitTask getTask(int taskId, boolean isAsync) {
        try {
            if (runnerField == null) {
                runnerField = scheduler.getClass().getDeclaredField("runners");
                runnerField.setAccessible(true);
            }

            if (asyncScheduler == null) {
                asyncScheduler = (BukkitScheduler) scheduler.getClass()
                        .getDeclaredField("asyncScheduler").get(scheduler);
            }

            if (isAsync) {
                //noinspection unchecked
                return ((Map<Integer, BukkitTask>) runnerField.get(asyncScheduler)).get(taskId);
            } else {
                //noinspection unchecked
                return ((Map<Integer, BukkitTask>) runnerField.get(scheduler)).get(taskId);
            }
        } catch (Exception e) {
            throw new RuntimeException("unable to retrieve task by id", e);
        }
    }
}
