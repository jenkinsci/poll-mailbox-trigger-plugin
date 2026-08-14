package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import org.junit.jupiter.api.Test;

import javax.mail.Flags;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.stringify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of the original Groovy {@code StringifyTest}.
 */
class StringifyTest {

    private static final String DELIM = "|";

    private final List<Object> list = Arrays.asList(1, "two", false);
    private final Object[] array = {1, "two", false};

    @Test
    void list() {
        assertEquals("1, two, false", stringify(list));
        assertEquals("1|two|false", stringify(list, DELIM));
    }

    @Test
    void array() {
        assertEquals("1, two, false", stringify(array));
        assertEquals("1|two|false", stringify(array, DELIM));
    }

    @Test
    void set() {
        assertEquals("1, false, two", stringify(new TreeSet<>(Arrays.asList("1", "two", "false"))));
        assertEquals("1|false|two", stringify(new TreeSet<>(Arrays.asList("1", "two", "false")), DELIM));
    }

    @Test
    void map() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("one", 1);
        map.put("two", "two");
        map.put("three", false);
        assertEquals("one=1, two=two, three=false", stringify(map));
        assertEquals("one_1|two_two|three_false", stringify(map, "_", DELIM));
    }

    @Test
    void throwable() {
        assertTrue(stringify(new RuntimeException("foobar"))
                .startsWith("java.lang.RuntimeException: foobar"));
    }

    @Test
    void string() {
        assertEquals("foobar", stringify("foobar"));
    }

    @Test
    void integer() {
        assertEquals("1", stringify(1));
        assertEquals("2", stringify((Object) Integer.valueOf(2)));
    }

    @Test
    void date() {
        Calendar cal = Calendar.getInstance();
        cal.set(2001, Calendar.MARCH, 3, 4, 5, 6);
        assertEquals("2001-03-03T04:05Z", stringify(cal.getTime()));
    }

    @Test
    void flags() {
        Flags flags = new Flags();
        flags.add(Flags.Flag.ANSWERED);
        flags.add(Flags.Flag.SEEN);
        assertEquals("ANSWERED, SEEN", stringify(flags));
    }

    @Test
    void address() throws AddressException {
        String expected = "Sup <foo@bar.com.au>";
        assertEquals(expected, stringify((Object) new InternetAddress(expected)));
    }
}
