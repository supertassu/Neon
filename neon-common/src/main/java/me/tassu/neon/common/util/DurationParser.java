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

import lombok.experimental.var;
import lombok.val;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class DurationParser {

    private static final long DAY = TimeUnit.DAYS.toMillis(1L);
    private static final long HOUR = TimeUnit.HOURS.toMillis(1L);
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1L);
    private static final long SECOND = TimeUnit.SECONDS.toMillis(1L);

    private static final Pattern DATE_PATTERN = Pattern.compile("([0-9]{1,3})(mo|[wdhms])");

    public static long parse(String date) {
        val matcher = DATE_PATTERN.matcher(date);
        if (matcher.groupCount() != 0) {
            var time = 0;
            while (matcher.find()) {
                val amount = matcher.group(1);
                val unit = matcher.group(2);

                time += unitAsMillis(unit) * Integer.valueOf(amount);
            }

            return time;
        }

        return -1;
    }

    private static long unitAsMillis(String unit) {
        switch (unit.toLowerCase()) {
            case "mo":
                return DAY * 30;
            case "w":
                return DAY * 4;
            case "d":
                return DAY;
            case "h":
                return HOUR;
            case "m":
                return MINUTE;
            case "s":
                return SECOND;
            default:
                return 0;
        }
    }

}
