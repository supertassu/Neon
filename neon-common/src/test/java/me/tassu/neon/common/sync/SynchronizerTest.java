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

package me.tassu.neon.common.sync;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizerTest {

    @Test
    void testNoImplementation() {
        val synchronizer = new Synchronizer();
        assertFalse(synchronizer.isAvailable());
        assertNull(synchronizer.getImplementationName());
        synchronizer.broadcast(0); // no null pointer
    }

    @Test
    void testImplementation() throws Exception {
        val synchronizer = new Synchronizer();
        val impl = new NoOpSynchronizer();

        val field = synchronizer.getClass().getDeclaredField("impl");
        field.setAccessible(true);
        field.set(synchronizer, impl);

        assertTrue(synchronizer.isAvailable());
        assertEquals(synchronizer.getImplementationName(), impl.getImplementationName());

        synchronizer.broadcast(0);
        assertTrue(impl.called.get());
    }

    private class NoOpSynchronizer implements ISynchronizer {

        private AtomicBoolean called = new AtomicBoolean();

        @Override
        public String getImplementationName() {
            return "NoOp";
        }

        @Override
        public void sync(UUID uuid) {}

        @Override
        public void broadcast(long id) {
            called.set(true);
        }
    }

}