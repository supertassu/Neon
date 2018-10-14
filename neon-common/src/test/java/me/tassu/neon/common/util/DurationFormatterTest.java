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

package me.tassu.neon.common.util;

import org.junit.jupiter.api.Test;

import static me.tassu.neon.common.util.DurationFormatter.getRemaining;
import static org.junit.jupiter.api.Assertions.*;

class DurationFormatterTest {

    @Test
    void testGetRemaining() {
        assertEquals("37.0sec", getRemaining(37000));
        assertEquals("37.0sec", getRemaining(37020));
        assertEquals("37.1sec", getRemaining(37100));
        assertEquals("1min 0sec", getRemaining(60000));
        assertEquals("1min 0sec", getRemaining(60000));
        assertEquals("5min 37sec", getRemaining(337000));
        assertEquals("5min 37sec", getRemaining(337900));
        assertEquals("59min 59sec", getRemaining(3599999));
        assertEquals("1h 0min 0sec", getRemaining(3600000));
        assertEquals("1h 5min 37sec", getRemaining(3937900));
    }
}