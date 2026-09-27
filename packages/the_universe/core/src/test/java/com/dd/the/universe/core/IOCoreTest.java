package com.dd.the.universe.core;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IOCoreTest {
    @Test public void customRulesDoNotDependOnGlobalTrie() {
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("/data/game", "/virtual/user1/game");
        rules.put("/data/game/lib", "/virtual/lib");
        assertEquals("/virtual/lib/a.so", IOCore.get().redirectPath("/data/game/lib/a.so", rules));
        assertEquals("/data/game2/a", IOCore.get().redirectPath("/data/game2/a", rules));
        assertEquals("/virtual/user1/game/data/game/a", IOCore.get().redirectPath("/data/game/data/game/a", rules));
    }
}
