package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.common.permission.Permissions;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.sponge.data.EspialKeys;
import net.slimediamond.espial.sponge.utils.CommandUtils;
import net.slimediamond.espial.sponge.wand.QueryBuilderCache;
import net.slimediamond.espial.sponge.wand.WandLoreBuilder;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;


public class WandCommand extends AbstractCommand {

    public WandCommand() {
        super(Permissions.WAND, Component.text("Get an inspection wand item"));

        addAlias("wand");
        addAlias("w");
        addParameter(Parameters.WAND_TARGET);
        addParameter(Parameters.OPTIONAL_TRANSACTION_TYPE);
        addFlags(Flags.QUERY_FLAGS);
        addFlag(Flags.MAXIMUM, Component.text("Specify a maximum amount of uses the wand will have"));
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        final ServerPlayer target = context.one(Parameters.WAND_TARGET).orElse(CommandUtils.getServerPlayer(context));
        final EspialQuery.Builder builder = CommandUtils.getQueryBuilder(context);

        final String modeDisplay = context.one(Parameters.OPTIONAL_TRANSACTION_TYPE)
                .map(t -> t.key(EspialRegistryTypes.TRANSACTION_TYPE).value())
                .orElse("inspect");

        // create item stack
        final ItemStack.Builder itemStackBuilder = ItemStack.builder()
                .itemType(ItemTypes.STICK.get())
                .add(EspialKeys.WAND, true)
                .add(EspialKeys.WAND_FILTERS, QueryBuilderCache.add(builder))
                .add(Keys.ITEM_NAME, Component.text("Espial Wand").color(Format.THEME_COLOR)
                        .append(Component.text(" - ").color(Format.TITLE_COLOR))
                        .append(Format.accent(modeDisplay)));

        if (context.hasFlag(Flags.MAXIMUM)) {
            // apply max uses stat. Assume 1 max use as the default if the flag is specified
            final int max = context.one(Parameters.OPTIONAL_MAXIMUM).orElse(1);
            itemStackBuilder.add(EspialKeys.WAND_MAX_USES, max);
            itemStackBuilder.add(EspialKeys.WAND_USES, max);
        }

        if (context.hasAny(Parameters.OPTIONAL_TRANSACTION_TYPE)) {
            itemStackBuilder.add(EspialKeys.WAND_TRANSACTION_TYPE,
                    context.requireOne(Parameters.OPTIONAL_TRANSACTION_TYPE)
                            .key(EspialRegistryTypes.TRANSACTION_TYPE));
        } else {
            itemStackBuilder.add(EspialKeys.WAND_DOES_LOOKUPS, true);
        }

        // temporary build :)
        itemStackBuilder.add(Keys.LORE, WandLoreBuilder.getLore(itemStackBuilder.build(), builder));

        final ItemStack itemStack = itemStackBuilder.build();
        if (!target.inventory().primary().canFit(itemStack)) {
            context.sendMessage(Format.error("Unable to fit the item in the target's inventory"));
        } else {
            target.inventory().primary().offer(itemStack);
            final TextComponent.Builder textBuilder = Component.text().color(Format.TEXT_COLOR)
                    .append(Format.text("Gave "));

            if (context.subject() instanceof ServerPlayer source
                    && source.uniqueId().equals(target.uniqueId())) {
                textBuilder.append(Component.text("you").color(Format.TEXT_COLOR));
            } else {
                textBuilder.append(Format.accent(target.name()));
            }
            textBuilder.appendSpace()
                    .append(Component.text("a tool to "))
                    .append(Format.accent(modeDisplay))
                    .append(Component.text(" grief"));
            context.sendMessage(textBuilder.build());
        }
        return CommandResult.success();
    }

}
