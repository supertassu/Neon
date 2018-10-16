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