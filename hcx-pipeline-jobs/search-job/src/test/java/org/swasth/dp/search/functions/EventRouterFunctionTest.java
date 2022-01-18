package org.swasth.dp.search.functions;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.swasth.dp.search.task.CompositeSearchConfig;

import java.util.Map;

public class EventRouterFunctionTest {

    Config config = ConfigFactory.load("search-test.conf");
    CompositeSearchConfig searchConfig = new CompositeSearchConfig(config,"CompositeSearch-Job");

    @Test
    public void testEventRouteSuccessDispatch() throws Exception {
        EventRouterFunction processFunction = new EventRouterFunction(searchConfig);

    }
}

