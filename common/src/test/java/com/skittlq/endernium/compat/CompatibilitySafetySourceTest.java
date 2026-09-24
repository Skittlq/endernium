package com.skittlq.endernium.compat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilitySafetySourceTest {
    private static final Path REPOSITORY_ROOT = findRepositoryRoot();

    @Test
    void autoCollectionUsesTheEntityPickupPath() throws Exception {
        String source = read("common/src/main/java/com/skittlq/endernium/util/EnderniumUtils.java");
        assertTrue(source.contains("drop.playerTouch(player)"));
        assertFalse(source.contains("getInventory().add("));
        assertFalse(source.contains("collectNearbyDrops"));
    }

    @Test
    void furnaceHookChainsAndHasNoThreadLocalState() throws Exception {
        String source = read("common/src/main/java/com/skittlq/endernium/mixin/AbstractFurnaceBlockEntityMixin.java");
        assertTrue(source.contains("@WrapOperation"));
        assertTrue(source.contains("original.call(stackToShrink, consumed)"));
        assertFalse(source.contains("@Redirect"));
        assertFalse(source.contains("ThreadLocal"));
    }

    @Test
    void legacySessionDataIsNeverScannedOrDeleted() throws Exception {
        String source = read("common/src/main/java/com/skittlq/endernium/util/EnderniumUtils.java");
        assertFalse(source.contains("VeinMiningSessionId"));
        assertFalse(source.contains("clearLegacyOperationTag"));
        assertFalse(source.contains("getContainerSize()"));
    }

    @Test
    void loaderHooksAreNarrowedToAttributedDropsAndArmor() throws Exception {
        String fabricEvents = read("fabric/src/main/java/com/skittlq/endernium/util/EnderniumUtilsEvents.java");
        String neoEvents = read("neoforge/src/main/java/com/skittlq/endernium/util/EnderniumUtilsEvents.java");
        String neoMetadata = read("neoforge/src/main/templates/META-INF/neoforge.mods.toml");
        String fabricModels = read("fabric/src/main/java/com/skittlq/endernium/client/render/FabricTrimItemModels.java");
        String fabricLoot = read("fabric/src/main/java/com/skittlq/endernium/loot/ModLootModifiers.java");

        assertTrue(fabricEvents.contains("PlayerBlockBreakEvents.BEFORE"));
        assertTrue(fabricEvents.contains("existingDropIds"));
        assertTrue(fabricEvents.contains("EnderniumTickScheduler.schedule"));
        assertTrue(fabricEvents.contains("PlayerBlockBreakEvents.CANCELED"));
        assertTrue(neoEvents.contains("BlockDropsEvent"));
        assertTrue(neoEvents.contains("event.getDrops()"));
        assertFalse(neoMetadata.contains("${mod_id}.neoforge.mixins.json"));
        assertTrue(fabricModels.contains("(model, bakeContext) -> new DynamicTrimModel"));
        assertTrue(fabricModels.contains("this.trimLayers == null"));
        assertTrue(fabricModels.contains("EnderniumTrimRendering.shouldHandle(stack, trim)"));
        assertFalse(fabricLoot.contains("resourceManager.listResources("));
        assertTrue(fabricLoot.contains("chests/end_city_treasure"));
        assertFalse(fabricLoot.contains("global_loot_modifiers.json"));
    }

    private static String read(String relativePath) throws Exception {
        return Files.readString(REPOSITORY_ROOT.resolve(relativePath));
    }

    private static Path findRepositoryRoot() {
        Path candidate = Path.of("").toAbsolutePath().normalize();
        while (candidate != null) {
            if (Files.isDirectory(candidate.resolve("common/src/main"))
                    && Files.isDirectory(candidate.resolve("fabric/src/main"))
                    && Files.isDirectory(candidate.resolve("neoforge/src/main"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new IllegalStateException("Could not locate Endernium repository root");
    }
}
