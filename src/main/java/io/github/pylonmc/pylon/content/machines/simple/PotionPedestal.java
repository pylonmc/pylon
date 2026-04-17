package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.content.building.Pedestal;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


/**
 * @author balugaq
 */
public class PotionPedestal extends Pedestal {
    public PotionPedestal(@NotNull final Block block, @NotNull final BlockCreateContext context) {
        super(block, context);
    }

    public PotionPedestal(@NotNull final Block block, @NotNull final PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public boolean isIllegalItem(@NotNull final Player player, @NotNull final ItemStack stack) {
        if (!PylonUtils.isPotion(stack.getType())) {
            player.sendMessage(Component.translatable("pylon.message.potion_pedestal.not-potion"));
            return false;
        }

        return true;
    }
}
