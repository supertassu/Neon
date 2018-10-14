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

package me.tassu.neon.bungee;

import com.google.inject.Inject;
import me.tassu.neon.common.plugin.Platform;
import me.tassu.neon.common.scheduler.Scheduler;
import me.tassu.neon.common.scheduler.Task;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public class NBungeeScheduler implements Scheduler {

    @Inject private Platform platform;
    @Inject private NBungeeBootstrap plugin;

    private TaskScheduler scheduler;

    public NBungeeScheduler() {
        this.scheduler = ProxyServer.getInstance().getScheduler();
    }

    @Override
    public void boot() {}

    @Override
    public void shutdown() {
        scheduler.cancel(plugin);
    }

    @Override
    public boolean isScheduled(Task task) {
        return false; // TODO
    }

    @Override
    public void schedule(Task task) {
        if (task.getTaskId() != -1) {
            throw new IllegalArgumentException("already scheduled");
        }

        task.setTaskId(scheduler.schedule(plugin, task, task.getDelay() * 50,
                        task.getRepeat() * 50, TimeUnit.MILLISECONDS).getId());

    }

    @Override
    public void unschedule(Task task) {
        scheduler.cancel(task.getTaskId());
        task.setTaskId(-1);
    }

    @Override
    public void sync(Runnable runnable) {
        scheduler.runAsync(plugin, runnable);
    }

    @Override
    public void async(Runnable runnable) {
        scheduler.runAsync(plugin, runnable);
    }

    @Override
    public void delay(int ticks, Runnable runnable) {
        scheduler.schedule(plugin, runnable, 50 * ticks, TimeUnit.MILLISECONDS);
    }
}
