package net.slimediamond.espial.sponge.commands.subsystem;

import io.leangen.geantyref.TypeToken;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.common.permission.Permissions;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;

import java.time.Duration;
import java.util.UUID;

public class Parameters {

    public static final Parameter.Value<Integer> RANGE = Parameter.integerNumber()
            .key("range")
            .build();

    public static final Parameter.Value<Boolean> SPREAD = Parameter.bool()
            .key("spread")
            .optional()
            .build();

    public static final Parameter.Value<Duration> BEFORE = Parameter.duration()
            .key("before")
            .build();

    public static final Parameter.Value<Duration> AFTER = Parameter.duration()
            .key("after")
            .build();

    public static final Parameter.Value<UUID> USER = Parameter.user()
            .key("user")
            .build();

    public static final Parameter.Value<BlockType> BLOCK_TYPE = Parameter.registryElement(new TypeToken<BlockType>() {},
                    RegistryTypes.BLOCK_TYPE, "minecraft")
            .key("block type")
            .build();
    
    public static final Parameter.Value<EspialEvent> EVENT = Parameter.registryElement(new TypeToken<EspialEvent>() {},
                    EspialRegistryTypes.EVENT, "espial")
            .key("event")
            .build();

    public static final Parameter.Value<Boolean> WORLDEDIT = Parameter.bool()
            .key("use worldedit")
            .optional()
            .build();

    public static final Parameter.Value<Boolean> YES = Parameter.bool()
            .key("confirm")
            .optional()
            .build();

    public static final Parameter.Value<ServerPlayer> WAND_TARGET = Parameter.player()
            .key("target")
            .requiredPermission(Permissions.WAND_OTHER.get())
            .optional()
            .build();

    public static final Parameter.Value<TransactionType> OPTIONAL_TRANSACTION_TYPE = Parameter.registryElement(new TypeToken<TransactionType>() {},
                    EspialRegistryTypes.TRANSACTION_TYPE, "espial")
            .key("transaction type")
            .optional()
            .build();

    public static final Parameter.Value<Integer> OPTIONAL_MAXIMUM = Parameter.integerNumber()
            .key("maximum")
            .optional()
            .build();

}
