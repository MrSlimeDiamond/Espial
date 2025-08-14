package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class RootCommand extends AbstractCommand {

    public RootCommand() {
        super(null, Component.text("Root Espial command"));

        addAlias("espial");
        addAlias("es");
        addChild(new LookupCommand());
        addChild(new RollbackCommand(false));
        addChild(new RestoreCommand(false));
        addChild(new NearCommand());
        addChild(new UndoCommand());
        addChild(new RedoCommand());
        addChild(new InteractiveCommand());
        addChild(new NearbySignsCommand());
        addChild(new QueueCommand());
        addChild(new WandCommand());
        addChild(new ReloadCommand());
        addChild(new PurgeCommand());
        addChild(new PreviewCommand());
        addChild(new EventsCommand());
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        context.sendMessage(Component.text()
                .append(Component.text("=".repeat(30)).color(Format.PADDING_COLOR))
                .appendNewline()
                .append(Component.text("Espial version ").color(Format.THEME_COLOR))
                .append(Component.text(Espial.getInstance().getContainer().metadata().version().toString()).color(Format.ACCENT_COLOR))
                .appendNewline()
                .append(Component.text("Developers: ").color(Format.THEME_COLOR))
                .append(Component.text("SlimeDiamond").color(Format.ACCENT_COLOR))
                .appendNewline()
                .appendNewline()
                .append(Component.text("Espial is a plugin for querying and rolling back grief")
                        .color(Format.TEXT_COLOR))
                .appendNewline()
                .appendNewline()
                .append(Component.text("Check out the ").color(Format.TEXT_COLOR)
                        .append(Format.link("source code", "https://github.com/MrSlimeDiamond/Espial/")))
                .appendNewline()
                .appendNewline()
                .append(Component.text("Try the ").color(Format.TEXT_COLOR))
                .append(Format.commandHint("/espial help"))
                .append(Component.text(" command").color(Format.TEXT_COLOR))
                .build());
        return CommandResult.success();
    }

}
