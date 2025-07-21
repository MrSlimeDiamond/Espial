package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Queue;

public class QueueCommand extends AbstractCommand {

    public QueueCommand() {
        super(Permissions.QUEUE, Component.text("View the records in the recording queue"));

        addAlias("queue");
        addChild(new QueueFlushCommand());
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        final Queue<? extends EspialRecord> queue = Espial.getInstance().getRecordingQueue().getQueue();

        if (queue.isEmpty()) {
            context.sendMessage(Format.text("The recording queue is empty"));
        } else {
            PaginationList.builder()
                    .header(Format.accent(String.valueOf(queue.size()))
                            .append(Component.text(" items are in the queue").color(Format.TEXT_COLOR)))
                    .contents(queue.stream().map(RecordFormatter::format).toList())
                    .padding(Format.PADDING)
                    .sendTo(context.cause().audience());
        }
        return CommandResult.success();
    }

    private static class QueueFlushCommand extends AbstractCommand {

        public QueueFlushCommand() {
            super(Permissions.QUEUE_FLUSH, Component.text("Flush the recording queue"));

            addAlias("flush");
            addFlag(Flags.YES, Component.text("Confirm the flush"));
        }

        @Override
        public CommandResult execute(final CommandContext context) throws CommandException {
            if (!context.hasFlag(Flags.YES)) {
                return CommandResult.error(Format.error("WARNING: This will remove logs which have yet to " +
                        "be inserted into the database, which means data loss. To confirm, add the --yes flag"));
            }
            // actually do the flush
            final Queue<? extends EspialRecord> queue = Espial.getInstance().getRecordingQueue().getQueue();
            if (queue.isEmpty()) {
                return CommandResult.error(Format.text("The recording queue is empty"));
            }
            queue.clear();
            context.sendMessage(Format.text("Recording queue cleared"));
            return CommandResult.success();
        }

    }

}
