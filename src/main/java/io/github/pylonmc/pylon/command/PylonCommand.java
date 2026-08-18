package io.github.pylonmc.pylon.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pylonmc.pylon.content.machines.fluid.PortableFluidTank;
import io.github.pylonmc.pylon.content.science.Loupe;
import io.github.pylonmc.rebar.command.RegistryCommandArgument;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings({"UnstableApiUsage", "SameReturnValue"})
@UtilityClass
public class PylonCommand {

    public final LiteralCommandNode<CommandSourceStack> ROOT = Commands.literal("pylon")
            .then(Commands.literal("fillfluid")
                    .requires(source ->
                            source.getSender().hasPermission("pylon.command.fillfluid") && source.getSender() instanceof Player
                    )
                    .then(Commands.argument("fluid", new RegistryCommandArgument<>(RebarRegistry.FLUIDS))
                            .executes(PylonCommand::fillFluid)
                    )
            )
            .then(Commands.literal("resetloupedata")
                    .requires(source -> source.getSender().hasPermission("pylon.command.reset_loupe"))
                    .executes(ctx -> resetLoupe(ctx, null))
                    .then(Commands.argument("player", ArgumentTypes.player())
                            .executes(ctx -> resetLoupe(ctx, ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()))
                    )
            )
            .build();

    private int fillFluid(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>You must be a player to use this command");
            return Command.SINGLE_SUCCESS;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        RebarItem item = RebarItem.fromStack(itemStack);
        if (
                itemStack.getAmount() > 1 ||
                !(item instanceof PortableFluidTank.Item tank && tank.getAmount() == 0)
        ) {
            sender.sendRichMessage("<red>You must be holding one empty portable fluid tank to use this command");
            return Command.SINGLE_SUCCESS;
        }

        RebarFluid fluid = ctx.getArgument("fluid", RebarFluid.class);
        tank.setFluid(fluid);
        tank.setAmount(tank.getCapacity());

        return Command.SINGLE_SUCCESS;
    }

    private int resetLoupe(CommandContext<CommandSourceStack> ctx, Player target) {
        CommandSender sender = ctx.getSource().getSender();
        if (target == null) {
            if (!(sender instanceof Player player)) {
                sender.sendRichMessage("<red>You must be a player to use this command");
                return Command.SINGLE_SUCCESS;
            }
            target = player;
        }

        target.getPersistentDataContainer().remove(Loupe.CONSUMED_KEY);
        sender.sendRichMessage("<green>Reset loupe data for <target>",
                Placeholder.unparsed("target", target.getName()));
        return Command.SINGLE_SUCCESS;
    }
}
