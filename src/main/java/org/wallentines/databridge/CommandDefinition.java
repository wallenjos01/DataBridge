package org.wallentines.databridge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public record CommandDefinition(String name, Type type, String value, int permissionLevel, Optional<String> permissionNode, Optional<Holder<StateObject<?>>> state) {

    private static final CommandExceptionType EXCEPTION = new CommandExceptionType() {};

    public static final Codec<CommandDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(CommandDefinition::name),
            Type.CODEC.optionalFieldOf("type", Type.ALIAS).forGetter(CommandDefinition::type),
            Codec.STRING.fieldOf("value").forGetter(CommandDefinition::value),
            Codec.INT.optionalFieldOf("permission_level", 0).forGetter(CommandDefinition::permissionLevel),
            Codec.STRING.optionalFieldOf("permission_node").forGetter(CommandDefinition::permissionNode),
            RegistryFixedCodec.create(DataBridgeRegistries.STATE_OBJECT).optionalFieldOf("state_object").forGetter(CommandDefinition::state)
    ).apply(instance, CommandDefinition::new));

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        LiteralArgumentBuilder<CommandSourceStack> out = switch (type) {
            case ALIAS -> Commands.literal(name).executes(ctx -> {

                MinecraftServer server = ctx.getSource().getServer();
                CommandSourceStack source = ctx.getSource().withPermission(server.getFunctionCompilationLevel());

                server.getCommands().performPrefixedCommand(source, value);
                return 1;
            });
            case METHOD -> {
                try {
                    StateObject<?> obj = state.isPresent() ? state.get().value() : StateObject.EMPTY;
                    Method method = Utils.findMethod(value, CommandContext.class, obj.type());
                    if(method.getReturnType() != int.class) {
                        throw new IllegalArgumentException("Expected a method which returns an int!");
                    }
                    yield Commands.literal(name).executes(ctx -> {
                        try {
                            return (int) method.invoke(null, ctx, obj.value());
                        } catch (Exception e) {
                            throw new CommandSyntaxException(EXCEPTION, () -> "Unable to execute command " + name + "! " + e.getMessage());
                        }
                    });
                } catch (ClassNotFoundException | NoSuchMethodException ex) {
                    throw new RuntimeException(ex);
                }
            }
            case BUILDER -> fromBuilder();
        };

        if(permissionLevel > 0) {
            if(permissionNode.isPresent()) {
                out = out.requires(Permissions.require(permissionNode.orElseThrow(), permissionLevel));
            } else {
                out = out.requires((ctx) -> ctx.hasPermission(permissionLevel));
            }
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    private LiteralArgumentBuilder<CommandSourceStack> fromBuilder() {
        try {

            StateObject<?> obj = state.isPresent() ? state.get().value() : StateObject.EMPTY;

            Method method = Utils.findMethod(value, String.class, LiteralArgumentBuilder.class, obj.type());
            if (method.getReturnType() != LiteralArgumentBuilder.class) {
                throw new IllegalArgumentException("Expected a method which returns a LiteralArgumentBuilder!");
            }
            return (LiteralArgumentBuilder<CommandSourceStack>) method.invoke(null, name, Commands.literal(name), obj.value());

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    enum Type {
        ALIAS("alias"),
        METHOD("method"),
        BUILDER("builder");

        private final String id;
        Type(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Type byId(String id) {
            for (Type t : Type.values()) {
                if (t.id.equals(id)) {
                    return t;
                }
            }
            return null;
        }

        public static final Codec<Type> CODEC = Codec.STRING.xmap(Type::byId, Type::getId);
    }

}
