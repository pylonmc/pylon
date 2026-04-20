package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transformer extends RebarBlock implements
        RebarDirectionalBlock,
        RebarEntityHolderBlock {

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
    }

    @SuppressWarnings("unused")
    public Transformer(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
}
