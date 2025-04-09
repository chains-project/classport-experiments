package com.graphhopper.application;

import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    void testMain() throws Exception {
        GraphHopperApplication app = new GraphHopperApplication();
        app.run(new String[]{"server", "src/test/resources/config.yml"});
    }
}
