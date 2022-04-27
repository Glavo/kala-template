package kala.template;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateEngineTest {
    @Test
    public void defaultEngineTest() {
        TemplateEngine engine = TemplateEngine.getDefault();

        var markers = Map.of(
                "a", "<text a>",
                "b", "<text b>",
                "abc", "<text abc>"
        );

        assertEquals("test", engine.process("test", markers));
        assertEquals("test$0", engine.process("test$0", markers));

        assertThrows(TemplateProcessException.class, () -> engine.process("test${", markers));
        assertThrows(TemplateProcessException.class, () -> engine.process("test${unknown}", markers));

        assertEquals("test<text a> <text b>", engine.process("test${a} ${b}", markers));
    }
}
