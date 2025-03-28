package org.wallentines.databridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.wallentines.databridge.impl.CommandDefinition;
import org.wallentines.databridge.impl.DataBridgeRegistries;
import org.wallentines.databridge.impl.JavaFunctionDefinition;
import org.wallentines.databridge.impl.StateObject;

import java.util.List;
import java.util.stream.Stream;

@Mixin(RegistryDataLoader.class)
public class MixinDataLoader {

    @Shadow @Final @Mutable
    public static List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES;

    @WrapOperation(method="<clinit>", at=@At(value="FIELD", opcode=Opcodes.PUTSTATIC, target="Lnet/minecraft/resources/RegistryDataLoader;WORLDGEN_REGISTRIES:Ljava/util/List;"))
    private static void redirectWorldgenRegistries(List<RegistryDataLoader.RegistryData<?>> value, Operation<Void> original) {
        original.call(Stream.concat(value.stream(), Stream.of(
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.STATE_OBJECT, StateObject.CODEC, false),
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.COMMAND, CommandDefinition.CODEC, false),
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.FUNCTION, JavaFunctionDefinition.CODEC, false)
        )).toList());
    }

    @WrapOperation(method="loadContentsFromManager", at=@At(value="INVOKE", target="Lnet/minecraft/tags/TagLoader;loadTagsForRegistry(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/WritableRegistry;)V"))
    private static <T> void onLoadTags(ResourceManager resourceManager, WritableRegistry<T> writableRegistry, Operation<Void> original) {
        if(!(writableRegistry.key().equals(DataBridgeRegistries.FUNCTION))) {
            original.call(resourceManager, writableRegistry);
        }
    }


}
