package net.slimediamond.espial.commands.subsystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.util.Format;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommand implements CommandExecutor {

    private final String permission;
    private final Component description;
    private final List<AbstractCommand> children = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private final Map<Flag, Component> flags = new LinkedHashMap<>();
    private final List<Parameter> parameters = new LinkedList<>();
    private boolean showInHelp = true;
    private Command.Parameterized command;

    /**
     * Constructor for a command
     *
     * @param permission  Command permission
     * @param description Command description
     */
    public AbstractCommand(@Nullable String permission, @NonNull Component description) {
        this.permission = permission;
        this.description = description;

        if (!(this instanceof HelpCommand)) {
            addChild(new HelpCommand(this));
        }
    }

    public String getPermission() {
        return permission;
    }

    public Component getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<AbstractCommand> getChildren() {
        return children;
    }

    public Map<Flag, Component> getFlags() {
        return flags;
    }

    protected void addChild(AbstractCommand child) {
        children.add(child);
    }

    protected void addAlias(String alias) {
        aliases.add(alias);
    }

    protected void addFlag(Flag flag, Component description) {
        flags.put(flag, description);
    }

    protected void addFlags(Map<Flag, Component> flags) {
        this.flags.putAll(flags);
    }

    protected void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    protected void showInHelp(boolean showInHelp) {
        this.showInHelp = showInHelp;
    }

    public void sendHelpCommand(CommandCause cause) {
        PaginationList.builder()
                .title(formatTitle())
                .header(formatHeader())
                .contents(formatCommandList(cause))
                .padding(Format.PADDING)
                .build()
                .sendTo(cause.audience());
    }

    private Component formatTitle() {
        return Format.title("Command Help ")
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(Component.join(
                        JoinConfiguration.separator(Component.text(", ")),
                        getAliases().stream().map(Component::text).toList()
                ))
                .append(Component.text("]").color(NamedTextColor.GRAY));
    }

    private Component formatHeader() {
        Component header = Component.text("Desc: ")
                .color(NamedTextColor.AQUA)
                .append(description.color(NamedTextColor.YELLOW));

        if (!flags.isEmpty()) {
            header = header.append(Component.newline())
                    .append(Component.text("Flags: ").color(NamedTextColor.AQUA))
                    .append(formatFlags());
        }

        return header;
    }

    private Component formatFlags() {
        return Component.join(
                JoinConfiguration.separator(Component.text(" ")),
                flags.entrySet().stream().map(this::formatFlag).toList()
        );
    }

    private Component formatFlag(Map.Entry<Flag, Component> entry) {
        Flag flag = entry.getKey();
        boolean isValueFlag = flag.associatedParameter().map(p -> !p.isOptional()).orElse(false);
        NamedTextColor color = isValueFlag ? NamedTextColor.GREEN : NamedTextColor.GOLD;

        return Format.chip(Component.text(flag.aliases().stream()
                                .min(Comparator.comparingInt(String::length))
                                .orElseThrow(() -> new RuntimeException("Flag has no shortest alias")))
                        .color(color),
                Component.text("Flag" + (isValueFlag ? " - Requires Value" : ""))
                        .color(color)
                        .append(Component.newline())
                        .append(entry.getValue())
        );
    }

    private List<Component> formatCommandList(CommandCause cause) {
        return getChildren().stream()
                .filter(command -> command.permission == null || cause.hasPermission(command.permission))
                .filter(command -> command.showInHelp)
                .map(this::formatCommand)
                .toList();
    }

    private Component formatCommand(AbstractCommand child) {
        return Component.text()
                .append(Component.text(child.aliases.getFirst()).color(NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Aliases: ")
                                .append(Component.text(String.join(", ", child.getAliases()))
                                        .color(NamedTextColor.GRAY)))))
                .clickEvent(SpongeComponents.executeCallback(child::sendHelpCommand))
                .append(Component.text(" - ").color(NamedTextColor.GRAY))
                .append(child.description.color(NamedTextColor.WHITE))
                .build();
    }


    public Command.Parameterized build() {
        if (command == null) {
            if (aliases.isEmpty()) {
                throw new RuntimeException("Cannot build a command with no aliases");
            }
            Command.Parameterized.Builder builder = Command.builder()
                    .executor(this)
                    .permission(permission)
                    .addFlags(flags.keySet())
                    .addParameters(parameters);

            if (!children.isEmpty()) {
                children.forEach(child -> builder.addChild(child.build(), child.aliases));
            }

            command = builder.build();
        }
        return command;
    }
}
