package net.slimediamond.espial.sponge.commands.subsystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.sponge.permission.Permission;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommand implements CommandExecutor {

    private final Permission permission;
    private final Component description;
    private final List<AbstractCommand> children = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private final Map<Flag, Component> flags = new LinkedHashMap<>();
    private final List<Parameter> parameters = new LinkedList<>();
    protected boolean showInHelp = true;
    private Command.Parameterized command;

    /**
     * Constructor for a command
     *
     * @param permission  Command permission
     * @param description Command description
     */
    public AbstractCommand(@Nullable final Permission permission, @NotNull final Component description) {
        this.permission = permission;
        this.description = description;

        if (!(this instanceof HelpCommand)) {
            addChild(new HelpCommand(this));
        }
    }

    public Permission getPermission() {
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

    private Component formatTitle() {
        return Format.title("Command Help ")
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(Component.join(
                        JoinConfiguration.separator(Component.text(", ")),
                        getAliases().stream().map(Component::text).toList()
                ))
                .append(Component.text("]").color(NamedTextColor.GRAY));
    }


    public Command.Parameterized build() {
        if (this.command == null) {
            if (this.aliases.isEmpty()) {
                throw new RuntimeException("Cannot build a command with no aliases");
            }
            Command.Parameterized.Builder builder = Command.builder()
                    .executor(this)
                    .addFlags(this.flags.keySet())
                    .addParameters(this.parameters);

            if (this.permission != null) {
                builder.permission(this.permission.get());
            }

            if (!this.children.isEmpty()) {
                this.children.forEach(child -> builder.addChild(child.build(), child.aliases));
            }

            this.command = builder.build();
        }
        return this.command;
    }
}
