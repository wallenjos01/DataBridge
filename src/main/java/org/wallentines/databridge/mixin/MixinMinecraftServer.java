package org.wallentines.databridge.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.databridge.DataBridgeRegistries;
import org.wallentines.databridge.StateObject;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow public abstract RegistryAccess.Frozen registryAccess();

    @Inject(method="stopServer", at=@At("TAIL"))
    private void onStop(CallbackInfo ci) {
        registryAccess().lookup(DataBridgeRegistries.STATE_OBJECT).ifPresent(state -> {
            state.forEach(StateObject::unload);
        });
    }

}
