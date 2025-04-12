package org.wallentines.databridge.api;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.wallentines.databridge.impl.CommandSourceStackExtension;
import org.wallentines.databridge.impl.Utils;

import java.util.function.Function;

public interface ServerFunctionUtil {

    static void executeFunction(MinecraftServer server, ResourceLocation id, @Nullable CompoundTag with, @Nullable Function<CommandSourceStack, CommandSourceStack> sourceTransformer, CommandResultCallback callback) {
        Utils.executeFunction(server, id, with, sourceTransformer, callback);
    }

    static void executeFunctionTag(MinecraftServer server, ResourceLocation tag, @Nullable CompoundTag with, @Nullable Function<CommandSourceStack, CommandSourceStack> sourceTransformer) {
        Utils.executeFunctionTag(server, tag, with, sourceTransformer);
    }

    static Entity getTriggerEntity(CommandSourceStack commandSourceStack) {
        return ((CommandSourceStackExtension) commandSourceStack).getTriggerEntity();
    }

}
