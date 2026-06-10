package net.zalduaxa.backend.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SlugUtilsTest {

    @ParameterizedTest
    @MethodSource("slugCases")
    void slugify_returnsExpectedValue(String input, String expected) {
        assertEquals(expected, SlugUtils.slugify(input));
    }

    static Stream<Arguments> slugCases() {
        return Stream.of(
            Arguments.of(null, "untitled"),
            Arguments.of("", "untitled"),
            Arguments.of("   ", "untitled"),
            Arguments.of("Hello World", "hello-world"),
            Arguments.of("HELLO", "hello"),
            Arguments.of("Café", "cafe"),
            Arguments.of("ñ", "n"),
            Arguments.of("hello_world", "hello_world"),
            Arguments.of("hello-world", "hello-world"),
            Arguments.of("hello   world", "hello-world"),
            Arguments.of("hello___world", "hello___world"),
            Arguments.of("hello---world", "hello---world"),
            Arguments.of("hello - world", "hello---world"),
            Arguments.of("hello@world", "hello-world"),
            Arguments.of("hello!world", "hello-world"),
            Arguments.of("  --hello--  ", "hello"),
            Arguments.of("a b c", "a-b-c"),
            Arguments.of("  a  ", "a"),
            Arguments.of("°", "untitled")
        );
    }

    @Test
    void constructor_throwsUnsupportedOperationException() throws Exception {
        var constructor = SlugUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(
            UnsupportedOperationException.class,
            () -> {
                try {
                    constructor.newInstance();
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }
}