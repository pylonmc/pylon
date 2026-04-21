package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.util.NumberInputButton;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarElectricBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.electricity.ElectricityManager;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import java.util.UUID;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Transformer extends RebarBlock implements
        RebarDirectionalBlock,
        RebarElectricBlock,
        RebarGuiBlock {

    private static final NamespacedKey VOLTAGE_KEY = pylonKey("voltage");
    private static final NamespacedKey INPUT_NODE_KEY = pylonKey("input_node");
    private static final NamespacedKey OUTPUT_NODE_KEY = pylonKey("output_node");

    private ElectricNode.Connector inputNode;
    private ElectricNode.Connector outputNode;

    @Getter
    private double voltage;

    @SuppressWarnings("unused")
    public Transformer(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addEntity("core", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":core"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.20009678623931632F, 0.20032039188618334F, 0.6004731961191524F)
                        .translateLocal(-0.4F, 0F, 0F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("core_1", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":core"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.1997668812131523F, 0.19966703461153537F, 0.6003585166957504F)
                        .translateLocal(0.4F, 0F, 0F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("rung", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":rung"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.1996251290519708F, 0.19976789546029616F, 1.0003100350848213F)
                        .translateLocal(0.4F, 0F, 0F)
                        .rotateLocalY(1.5707963267948966F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("rung_1", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":rung"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.19970989558988675F, 0.20049819786540016F, 1.0000264976327606F)
                        .translateLocal(0.4F, 0F, 0F)
                        .rotateLocalY(-1.5707963267948966F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("coil", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("copper_block")))
                        .addCustomModelDataString(getKey() + ":coil"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.3995463833191263F, 0.4001898742525297F, 0.0997048554794333F)
                        .translateLocal(-0.4F, 0F, -0.1875F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("coil_1", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("copper_block")))
                        .addCustomModelDataString(getKey() + ":coil"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.3999329168670776F, 0.40013877682813603F, 0.10002070272683829F)
                        .translateLocal(-0.4F, 0F, -0.0625F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("coil_2", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("copper_block")))
                        .addCustomModelDataString(getKey() + ":coil"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.3995519473196249F, 0.40019206234199983F, 0.0996939243442167F)
                        .translateLocal(-0.4F, 0F, 0.1875F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("coil_3", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("copper_block")))
                        .addCustomModelDataString(getKey() + ":coil"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.4001911460388317F, 0.3997099305044037F, 0.10003237057314601F)
                        .translateLocal(-0.4F, 0F, 0.0625F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("coil_4", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("copper_block")))
                        .addCustomModelDataString(getKey() + ":coil"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.4002971305276804F, 0.3996788757583065F, 0.10002218158359033F)
                        .translateLocal(0.4F, 0F, -0.0625F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("coil_5", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("copper_block")))
                        .addCustomModelDataString(getKey() + ":coil"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.40047277836438155F, 0.400447837292574F, 0.10040968619996378F)
                        .translateLocal(0.4F, 0F, 0.0625F)
                        .rotateLocal(new Quaternionf().lookAlong(context.getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0)))
                )
                .build(block.getLocation().toCenterLocation())
        );

        voltage = 0;

        inputNode = addElectricPort(getFacing(), new ElectricNode.Connector(new BlockPosition(block)));
        outputNode = addElectricPort(getFacing().getOppositeFace(), new ElectricNode.Connector(new BlockPosition(block)));
        inputNode.connect(outputNode);
        ElectricityManager.setTransformerEdge(inputNode, outputNode, voltage);
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public Transformer(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        voltage = pdc.get(VOLTAGE_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    protected void postLoad(@NotNull PersistentDataContainer pdc) {
        UUID inputNodeId = pdc.get(INPUT_NODE_KEY, RebarSerializers.UUID);
        UUID outputNodeId = pdc.get(OUTPUT_NODE_KEY, RebarSerializers.UUID);
        inputNode = (ElectricNode.Connector) getElectricNodes().stream()
                .filter(node -> node.getId().equals(inputNodeId))
                .findFirst()
                .orElseThrow();
        outputNode = (ElectricNode.Connector) getElectricNodes().stream()
                .filter(node -> node.getId().equals(outputNodeId))
                .findFirst()
                .orElseThrow();
        ElectricityManager.setTransformerEdge(inputNode, outputNode, voltage);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(VOLTAGE_KEY, RebarSerializers.DOUBLE, voltage);
        pdc.set(INPUT_NODE_KEY, RebarSerializers.UUID, inputNode.getId());
        pdc.set(OUTPUT_NODE_KEY, RebarSerializers.UUID, outputNode.getId());
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # v # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('v', NumberInputButton.builder()
                        .material(Material.REDSTONE)
                        .name(Component.translatable("pylon.gui.voltage"))
                        .increment(1)
                        .shiftIncrement(10)
                        .min(0)
                        .valueGetter(() -> (int) getVoltage())
                        .valueSetter(this::setVoltage)
                        .valueFormatter(UnitFormat.VOLTS::format)
                        .reopenWindow(this::openWindow)
                        .build())
                .build();
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
        ElectricityManager.setTransformerEdge(inputNode, outputNode, voltage);
    }
}
