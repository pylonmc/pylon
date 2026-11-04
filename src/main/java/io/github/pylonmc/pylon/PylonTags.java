package io.github.pylonmc.pylon;

import io.github.pylonmc.rebar.block.RebarBlockTag;
import io.github.pylonmc.rebar.registry.RebarRegistry;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class PylonTags {

    public static final RebarBlockTag CAPACITORS = new RebarBlockTag(pylonKey("capacitors"));

    static {
        PylonTags.CAPACITORS.add(PylonKeys.CAPACITOR_1_KJ);
        RebarRegistry.BLOCK_TAGS.register(CAPACITORS);
    }
}
