package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.recipes.GasTurbineRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformUtil;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class GasTurbine extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarTickingBlock {

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public GasTurbine(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());
        setTickInterval(tickInterval);
    }

    @SuppressWarnings("unused")
    public GasTurbine(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        ElectricityOutputHatch electricityOutputHatch = getMultiblockComponentOrThrow(ElectricityOutputHatch.class, ELECTRICITY_OUTPUT_HATCH);
        electricityOutputHatch.setPower(0);

        FluidInputHatch inputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUT_HATCH);
        RebarFluid inputFluid = inputHatch.getFluid();
        if (inputFluid == null) return;
        double inputAmount = inputHatch.getFluidAmount();

        GasTurbineRecipe matchingRecipe = GasTurbineRecipe.RECIPE_TYPE.stream()
                .filter(r -> r.input().contains(inputFluid))
                .findFirst()
                .orElse(null);
        if (matchingRecipe == null) return;

        FluidOutputHatch outputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUT_HATCH);
        RebarFluid outputFluid = matchingRecipe.output().fluid();
        if (outputHatch.getFluid() != null && !outputHatch.getFluid().equals(outputFluid)) return;

        double inputOutputRatio = matchingRecipe.input().amountMillibuckets() / matchingRecipe.output().amount();

        double outputAmount = inputAmount / inputOutputRatio;
        double actualOutputAmount = Math.min(outputAmount, outputHatch.getFluidSpaceRemaining());
        double actualInputAmount = Math.min(inputAmount, outputAmount * inputOutputRatio);

        inputHatch.removeFluid(inputFluid, actualInputAmount);
        outputHatch.addFluid(outputFluid, actualOutputAmount);

        double powerOutput = matchingRecipe.powerProduction() * (actualInputAmount / matchingRecipe.input().amountMillibuckets()) * (20D / tickInterval);
        electricityOutputHatch.setPower(powerOutput);

        Vector3f direction = getFacing().getOppositeFace().getDirection().toVector3f();

        for (String entityId : getHeldEntities().keySet()) {
            if (!entityId.startsWith("turbine_")) continue;

            ItemDisplay display = getHeldEntityOrThrow(ItemDisplay.class, entityId);
            Matrix4f transform = TransformUtil.transformationToMatrix(display.getTransformation());

            int stepsPerTick = 5;
            for (int i = 0; i < stepsPerTick; i++) {
                Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                    transform.rotateLocal(new Quaternionf().rotateAxis((float) (2 * Math.PI / stepsPerTick), direction));
                    if (display.isValid()) {
                        display.setInterpolationDelay(0);
                        display.setInterpolationDuration(tickInterval / stepsPerTick);
                        display.setTransformationMatrix(transform);
                    }
                }, i * (tickInterval / stepsPerTick));
            }
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("power", UnitFormat.WATTS.format(getMultiblockComponentOrThrow(ElectricityOutputHatch.class, ELECTRICITY_OUTPUT_HATCH).getPower()))
        ));
    }

    private static final Vector3i FLUID_INPUT_HATCH = new Vector3i(0, 0, -2);
    private static final Vector3i FLUID_OUTPUT_HATCH = new Vector3i(0, 0, 2);
    private static final Vector3i ELECTRICITY_OUTPUT_HATCH = new Vector3i(0, -1, 2);

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        lineOfThree(0, 1, PylonKeys.REINFORCED_GLASS, components);
        lineOfThree(-1, 0, PylonKeys.REINFORCED_GLASS, components);
        lineOfThree(1, 0, PylonKeys.REINFORCED_GLASS, components);

        lineOfThree(0, -1, PylonKeys.STEEL_SUPPORT_BEAM, components);

        lineOfThree(0, -2, PylonKeys.BRONZE_FOUNDATION, components);
        lineOfThree(-1, -1, PylonKeys.BRONZE_FOUNDATION, components);
        lineOfThree(1, -1, PylonKeys.BRONZE_FOUNDATION, components);

        components.put(new Vector3i(0, -1, -2), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));

        components.put(FLUID_INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(FLUID_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(ELECTRICITY_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.ELECTRICITY_OUTPUT_HATCH));

        return components;
    }

    private static void lineOfThree(int x, int y, NamespacedKey key, Map<Vector3i, MultiblockComponent> components) {
        components.put(new Vector3i(x, y, 0), new RebarMultiblockComponent(key));
        components.put(new Vector3i(x, y, 1), new RebarMultiblockComponent(key));
        components.put(new Vector3i(x, y, -1), new RebarMultiblockComponent(key));
    }

    @Override
    public @Nullable ItemStack getBlockTextureItem() {
        return isFormedAndFullyLoaded() ? null : super.getBlockTextureItem();
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUT_HATCH)
                .setAllowedFluids(GasTurbineRecipe.RECIPE_TYPE.stream().flatMap(r -> r.input().fluids().stream()).collect(Collectors.toSet()));

        if (getHeldEntity("turbine_shaft") == null) {
            getBlock().setType(Material.STRUCTURE_VOID);
            refreshBlockTextureItem();
            setupDisplay();
        }
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        RebarSimpleMultiblock.super.onMultiblockUnformed(partUnloaded);

        if (!partUnloaded) {
            for (String entityId : new ArrayList<>(getHeldEntities().keySet())) {
                if (entityId.startsWith("turbine_")) {
                    tryRemoveEntity(entityId);
                }
            }

            getBlock().setType(Material.IRON_BLOCK);
            refreshBlockTextureItem();
        }

        ElectricityOutputHatch electricityOutputHatch = getMultiblockComponent(ElectricityOutputHatch.class, ELECTRICITY_OUTPUT_HATCH);
        if (electricityOutputHatch != null) {
            electricityOutputHatch.setPower(0);
        }
    }

    private void setupDisplay() {
        addEntity("turbine_shaft", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_shaft"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.20031818431717888F, 0.1998618756962673F, 2.999928431318844F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.7001095104874101F, 0.7003343199606629F, 0.05027297059664036F)
                        .translateLocal(0F, 0F, 0.02499999999999991F)
                        .translateLocal(0F, 0F, -1.2F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk_1", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.5995684032343717F, 0.5998784121385513F, 0.049829445006917716F)
                        .translateLocal(0F, 0F, -0.025000000000000022F)
                        .translateLocal(0F, 0F, -1.1F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk_2", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.400408077237662F, 0.39961631863827995F, 0.049862215651233666F)
                        .translateLocal(0F, 0F, -0.02499999999999991F)
                        .translateLocal(0F, 0F, -1.05F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk_3", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.4998993373616677F, 0.4999216378035786F, 0.050083582762834034F)
                        .translateLocal(0F, 0F, -0.025000000000000022F)
                        .translateLocal(0F, 0F, -0.15000000000000002F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk_4", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.6000367675274685F, 0.6501684628404046F, 0.04992085141408112F)
                        .translateLocal(0F, -0.025000000000000022F, 0.02499999999999991F)
                        .translateLocal(0F, 0F, 0.6000000000000001F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk_5", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.5002561171050549F, 0.5496725403840674F, 0.050218433779457734F)
                        .translateLocal(0F, -0.02499999999999991F, -0.025000000000000133F)
                        .translateLocal(0F, 0F, 0.7F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk_6", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.2999681211206062F, 0.35012340582916684F, 0.049593864090870424F)
                        .translateLocal(0F, -0.025000000000000022F, -0.02499999999999991F)
                        .translateLocal(0F, 0F, 0.75F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("turbine_blisk_7", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("iron_block")))
                        .addCustomModelDataString(getKey() + ":turbine_blisk"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.5002451479182026F, 0.5003304693503907F, 0.049636693814377984F)
                        .translateLocal(0F, 0F, -0.025000000000000133F)
                        .translateLocal(0F, 0F, 1.05F)
                        .rotateLocal(new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
    }
}