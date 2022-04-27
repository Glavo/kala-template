package kala.template;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateEngineTest {
    private static final Map<String, String> markers = Map.of(
            "a", "<text a>",
            "b", "<text b>",
            "abc", "<text abc>"
    );

    @Test
    public void defaultEngineTest() {
        TemplateEngine engine = TemplateEngine.getDefault();

        assertEquals("test", engine.process("test", markers));
        assertEquals("test$0", engine.process("test$0", markers));

        assertThrows(TemplateProcessException.class, () -> engine.process("test${", markers));
        assertThrows(TemplateProcessException.class, () -> engine.process("test${sometext", markers));
        assertThrows(TemplateProcessException.class, () -> engine.process("test${unknown}", markers));
        assertThrows(TemplateProcessException.class, () -> engine.process("test${unknown}sometext", markers));
        assertThrows(TemplateProcessException.class, () -> engine.process("test${a}${unknown}sometext", markers));

        assertEquals("begin <text a> <text b><text abc> end", engine.process("begin ${a} ${b}${abc} end", markers));
    }

    @Test
    public void customTagTest() {
        TemplateEngine e1 = TemplateEngine.builder()
                .tag("$", "$")
                .build();
        assertEquals("test", e1.process("test", markers));
        assertThrows(TemplateProcessException.class, () -> e1.process("test$", markers));
        assertThrows(TemplateProcessException.class, () -> e1.process("test$sometext", markers));
        assertThrows(TemplateProcessException.class, () -> e1.process("test$unknown$", markers));
        assertThrows(TemplateProcessException.class, () -> e1.process("test$unknown$sometext", markers));
        assertThrows(TemplateProcessException.class, () -> e1.process("test$$a$$$unknown$sometext", markers));
        assertEquals("begin <text a> <text b><text abc> end", e1.process("begin $a$ $b$$abc$ end", markers));


        TemplateEngine e2 = TemplateEngine.builder()
                .tag("{%", "%}")
                .build();

        assertEquals("test", e2.process("test", markers));
        assertThrows(TemplateProcessException.class, () -> e2.process("test{%", markers));
        assertThrows(TemplateProcessException.class, () -> e2.process("test{%sometext", markers));
        assertThrows(TemplateProcessException.class, () -> e2.process("test{%a%", markers));
        assertThrows(TemplateProcessException.class, () -> e2.process("test{%a}", markers));
        assertThrows(TemplateProcessException.class, () -> e2.process("test{%unknown%}", markers));
        assertThrows(TemplateProcessException.class, () -> e2.process("test{%unknown%}sometext", markers));
        assertThrows(TemplateProcessException.class, () -> e2.process("test{%a%}{%unknown%}sometext", markers));
        assertEquals("begin <text a> <text b><text abc> end", e2.process("begin {%a%} {%b%}{%abc%} end", markers));
    }
}
