package org.wallentines.databridge.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.databridge.api.ServerFunctionUtil;
import org.wallentines.databridge.impl.DatabridgeFunctionTags;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void onJoin(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie,
            CallbackInfo ci) {
        CompoundTag data = new CompoundTag();
        data.putString("username", serverPlayer.getGameProfile().getName());
        data.putString("uuid", serverPlayer.getUUID().toString());
        ServerFunctionUtil.executeFunctionTag(server, DatabridgeFunctionTags.JOIN, data,
                source -> source.withEntity(serverPlayer));
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "TAIL"))
    private void postJoin(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie,
            CallbackInfo ci) {
        CompoundTag data = new CompoundTag();
        data.putString("username", serverPlayer.getGameProfile().getName());
        data.putString("uuid", serverPlayer.getUUID().toString());
        ServerFunctionUtil.executeFunctionTag(server, DatabridgeFunctionTags.JOINED, data,
                source -> source.withEntity(serverPlayer));
    }

    @Inject(method = "remove", at = @At(value = "HEAD"))
    private void onLeave(ServerPlayer serverPlayer, CallbackInfo ci) {
        CompoundTag data = new CompoundTag();
        data.putString("username", serverPlayer.getGameProfile().getName());
        data.putString("uuid", serverPlayer.getUUID().toString());
        ServerFunctionUtil.executeFunctionTag(server, DatabridgeFunctionTags.LEAVE, data,
                source -> source.withEntity(serverPlayer));
    }

}
