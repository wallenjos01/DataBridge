package org.wallentines.databridge.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.wallentines.databridge.impl.StateObject;

import java.util.Optional;

public interface ServerStateObjects {

    static <T> Optional<T> getStateObject(MinecraftServer server, Class<T> state, String id) {
        return getStateObject(server, state, ResourceLocation.tryParse(id));
    }

    static <T> Optional<T> getStateObject(MinecraftServer server, Class<T> type, ResourceLocation id) {
        return StateObject.get(server.registryAccess(), type, id)
                .filter(StateObject::isPresent)
                .map(StateObject::value);
    }

    static <T> Optional<T> getStateObject(RegistryAccess access, Class<T> state, String id) {
        return getStateObject(access, state, ResourceLocation.tryParse(id));
    }

    static <T> Optional<T> getStateObject(RegistryAccess access, Class<T> type, ResourceLocation id) {
        return StateObject.get(access, type, id)
                .filter(StateObject::isPresent)
                .map(StateObject::value);
    }

}
