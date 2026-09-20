package com.skittlq.endernium.client.render;

import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.trim.ModTrimMaterials;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Adds trim item layers at runtime instead of enumerating trim materials in item JSON.
 * This keeps Endernium armor compatible with trim materials registered by other mods and
 * makes the Endernium trim visible on standard armor items supplied by other mods.
 */
public final class FabricTrimItemModels {
    private static final ModelDebugName DEBUG_NAME = () -> "Endernium dynamic armor trim";
    private static final Set<Item> ENDERNIUM_ARMOR = Set.of(
            ModItems.ENDERNIUM_HELMET,
            ModItems.ENDERNIUM_CHESTPLATE,
            ModItems.ENDERNIUM_LEGGINGS,
            ModItems.ENDERNIUM_BOOTS
    );

    private FabricTrimItemModels() {
    }

    public static void register() {
        ModelLoadingPlugin.register(context -> context.modifyItemModelAfterBake().register(
                ModelModifier.WRAP_PHASE,
                (model, bakeContext) -> new DynamicTrimModel(
                        model,
                        bakeContext.bakingContext(),
                        bakeContext.transformation()
                )
        ));
    }

    @Nullable
    private static Identifier trimTexture(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Identifier.withDefaultNamespace("trims/items/helmet_trim");
            case CHEST -> Identifier.withDefaultNamespace("trims/items/chestplate_trim");
            case LEGS -> Identifier.withDefaultNamespace("trims/items/leggings_trim");
            case FEET -> Identifier.withDefaultNamespace("trims/items/boots_trim");
            default -> null;
        };
    }

    private static final class DynamicTrimModel extends WrapperBakedItemModel {
        private final Object2ObjectMap<TrimLayerKey, ItemModel> trimLayers = new Object2ObjectOpenHashMap<>();
        private final ItemModel.BakingContext bakingContext;
        private final Matrix4fc transformation;
        private final ItemTransforms itemTransforms;

        private DynamicTrimModel(
                ItemModel wrapped,
                ItemModel.BakingContext bakingContext,
                Matrix4fc transformation
        ) {
            super(wrapped);
            this.bakingContext = bakingContext;
            this.transformation = transformation;
            this.itemTransforms = bakingContext.blockModelBaker()
                    .getModel(Identifier.withDefaultNamespace("item/generated"))
                    .getTopTransforms();
        }

        @Override
        public void update(
                ItemStackRenderState state,
                ItemStack stack,
                ItemModelResolver resolver,
                ItemDisplayContext displayContext,
                @Nullable ClientLevel level,
                @Nullable ItemOwner owner,
                int seed
        ) {
            super.update(state, stack, resolver, displayContext, level, owner, seed);

            ArmorTrim trim = stack.get(DataComponents.TRIM);
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            Identifier baseTrimTexture = equippable == null ? null : trimTexture(equippable.slot());
            if (trim == null || equippable == null || baseTrimTexture == null || equippable.assetId().isEmpty()) {
                return;
            }

            Identifier paletteId = trim.material().value().paletteId();
            boolean enderniumTrim = trim.material().unwrapKey()
                    .map(ModTrimMaterials.ENDERNIUM::equals)
                    .orElse(false)
                    || paletteId.equals(ModTrimMaterials.ENDERNIUM_PALETTE);
            if (!ENDERNIUM_ARMOR.contains(stack.getItem()) && !enderniumTrim) {
                return;
            }

            String palettePath = paletteId.getPath();
            String suffix = palettePath.substring(palettePath.lastIndexOf('/') + 1);

            boolean sameMaterial = trim.material().unwrapKey()
                    .map(material -> material.identifier().equals(equippable.assetId().get().identifier()))
                    .orElse(false)
                    || enderniumTrim && equippable.assetId().get().identifier()
                            .equals(ModTrimMaterials.ENDERNIUM.identifier());
            if (sameMaterial) {
                suffix += "_darker";
            }

            this.trimLayers.computeIfAbsent(new TrimLayerKey(baseTrimTexture, suffix), this::createTrimLayer)
                    .update(state, stack, resolver, displayContext, level, owner, seed);
        }

        private ItemModel createTrimLayer(TrimLayerKey key) {
            ModelBaker baker = this.bakingContext.blockModelBaker();
            MaterialBaker materials = baker.materials();
            Material.Baked overlayMaterial = materials.get(
                    new Material(key.baseTexture().withSuffix("_" + key.suffix())),
                    DEBUG_NAME
            );
            ModelRenderProperties properties = new ModelRenderProperties(false, overlayMaterial, this.itemTransforms);
            QuadCollection quads = baker.compute(
                    new ItemModelGenerator.ItemLayerKey(overlayMaterial, BlockModelRotation.IDENTITY, 0)
            );
            return new CuboidItemModelWrapper(List.of(), quads, properties, this.transformation);
        }
    }

    private record TrimLayerKey(Identifier baseTexture, String suffix) {
    }
}
