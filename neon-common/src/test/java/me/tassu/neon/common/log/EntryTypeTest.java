package me.tassu.neon.common.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntryTypeTest {

    @Test
    void sanityCheck() {
        for (EntryType type : EntryType.values()) {
            assertTrue(type.name().length() <= 16, type.name() + " is too long");
        }
    }

}