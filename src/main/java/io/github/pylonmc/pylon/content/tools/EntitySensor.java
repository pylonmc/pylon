package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNodeType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.*;
import java.util.regex.Pattern;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class EntitySensor extends RebarBlock implements
        EntityHolderRebarBlock,
        InteractRebarBlockHandler,
        SimpleElectricRebarBlock,
        DirectionalRebarBlock {

    public static class Item extends RebarItem {


        public final double powerPerBlock = getSettingOrThrow("power-per-block", ConfigAdapter.DOUBLE);

        private static final UnitFormat WATTS_PER_BLOCK_ENCOMPASSED = new UnitFormat(
                Pylon.getInstance(),
                "watts-per-block-encompassed"
        ).withDefaultStyle(UnitFormat.WATTS.getDefaultStyle());

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("power", WATTS_PER_BLOCK_ENCOMPASSED.format(powerPerBlock).decimalPlaces(2).selectPrefixAndRescale())
            );
        }
    }

    public final int maxRange = getSettingOrThrow("max-range", ConfigAdapter.INTEGER);
    public final double powerPerBlock = getSettingOrThrow("power-per-block", ConfigAdapter.DOUBLE);

    private static final NamespacedKey SELECTORS_KEY = pylonKey("selectors");
    private static final PersistentDataType<?, List<String>> SELECTORS_TYPE = RebarSerializers.LIST.listTypeFrom(RebarSerializers.STRING);
    @Getter
    private final List<String> selectors = new ArrayList<>();

    private static final NamespacedKey RANGE_KEY = pylonKey("range");
    @Getter
    private float range;

    public void setRange(float range) {
        // explanation: it's the volume of a sphere dummy
        setRequiredPower(4.0 / 3.0 * Math.PI * range * range * range * powerPerBlock);
        this.range = range;
    }

    @SuppressWarnings("unused")
    public EntitySensor(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createSimpleElectricPort(ElectricNodeType.CONSUMER, getFacing());

        selectors.add("@e");

        // the sender entity is used as the CommandSender for the selectors
        addEntity("sender", block.getWorld().spawnEntity(block.getLocation().toCenterLocation(), EntityType.MARKER));

        setRange(1);
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public EntitySensor(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        selectors.addAll(pdc.get(SELECTORS_KEY, SELECTORS_TYPE));
        range = pdc.get(RANGE_KEY, RebarSerializers.FLOAT);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(SELECTORS_KEY, SELECTORS_TYPE, selectors);
        pdc.set(RANGE_KEY, RebarSerializers.FLOAT, range);
    }

    public @NotNull Set<Entity> getSensedEntities() {
        if (!isPowered()) return Set.of();
        Entity sender = getHeldEntityOrThrow("sender");
        Set<Entity> entities = new HashSet<>();
        for (String selector : selectors) {
            entities.addAll(Bukkit.selectEntities(sender, selector));
        }
        entities.removeIf(e -> e.getLocation().distanceSquared(sender.getLocation()) > range * range);
        entities.remove(sender);
        return entities;
    }

    private static final Pattern PIPE = Pattern.compile("\\s*\\|\\s*");

    private Dialog getDialog() {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(getNameTranslationKey())
                        .canCloseWithEscape(true)
                        .inputs(List.of(
                                DialogInput.text("selectors", Component.translatable("pylon.gui.entity-sensor.selector-hint"))
                                        .initial(String.join(" | ", selectors))
                                        .build(),
                                DialogInput.numberRange(
                                                "range",
                                                Component.translatable("pylon.gui.entity-sensor.range-hint"),
                                                0,
                                                maxRange
                                        )
                                        .initial(range)
                                        .step(0.5f)
                                        .build()
                        ))
                        .build())
                .type(DialogType.confirmation(ActionButton.builder(Component.translatable("pylon.ok"))
                                .action(DialogAction.customClick((view, audience) -> {
                                    String[] candidates = PIPE.split(view.getText("selectors"));
                                    CommandSender sender = getHeldEntityOrThrow("sender");
                                    for (String candidate : candidates) {
                                        try {
                                            Bukkit.selectEntities(sender, candidate);
                                        } catch (IllegalArgumentException e) {
                                            audience.sendMessage(Component.translatable("pylon.gui.entity-sensor.invalid-selector"));
                                            return;
                                        }
                                    }
                                    selectors.clear();
                                    selectors.addAll(Arrays.asList(candidates));
                                    //noinspection DataFlowIssue
                                    setRange(view.getFloat("range"));
                                }, ClickCallback.Options.builder().build()))
                                .build(),
                        ActionButton.builder(Component.translatable("pylon.cancel"))
                                .action(DialogAction.staticAction(ClickEvent.callback(_ -> {
                                })))
                                .build())));
    }

    @Override
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().isSneaking())
            return;
        event.getPlayer().showDialog(getDialog());
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (!isPowered()) {
            display.add(Component.translatable(
                    "pylon.message.no_power",
                    RebarArgument.of("power", UnitFormat.WATTS.format(getRequiredPower()).decimalPlaces(2).selectPrefixAndRescale())
            ));
        }
        return display;
    }
}