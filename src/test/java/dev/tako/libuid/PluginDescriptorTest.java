package dev.tako.libuid;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDescriptorTest {
    @Test
    void pluginDescriptorDeclaresFoliaSupport() throws Exception {
        String descriptor;
        try (InputStream in = PluginDescriptorTest.class.getResourceAsStream("/paper-plugin.yml")) {
            assertNotNull(in, "paper-plugin.yml is missing from the plugin resources");
            descriptor = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(
                descriptor.lines().anyMatch(line -> line.trim().equals("folia-supported: true")),
                "paper-plugin.yml must declare folia-supported: true so Folia loads Libuid");
    }
}
