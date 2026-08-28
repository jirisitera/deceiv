package com.japicraft.command;

import com.japicraft.Deceiv;
import com.japicraft.container.GameContainer;
import com.japicraft.game.ArenaLimits;
import com.japicraft.game.GameInstance;
import com.japicraft.game.Role;
import com.japicraft.game.RoleManager;
import com.japicraft.item.DaggerManager;
import com.japicraft.item.RevolverManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@SuppressWarnings("SameReturnValue")
public class ApexCommand {
    private static final String ARGUMENT_IDENTIFIER = "gameId";
    private static final String ARGUMENT_PLAYERS = "targets";
    private static final int MAX_ROLE_PLAYERS = 64;
    private final GameContainer gameContainer;

    public ApexCommand(GameContainer gameContainer) {
        this.gameContainer = gameContainer;
    }

    public LiteralCommandNode<CommandSourceStack> build() {

        return Commands.literal(Deceiv.PLUGIN_ID)
            .then(Commands.literal("items")
                .executes(this::items)
            )
            .then(Commands.literal("clearChances")
                .then(Commands.argument(ApexCommand.ARGUMENT_PLAYERS, ArgumentTypes.players())
                    .executes(this::clearChances)
                )
            )
            .then(Commands.literal("create")
                .then(Commands.argument(ApexCommand.ARGUMENT_IDENTIFIER, ArgumentTypes.key())
                    .then(buildCreateSubcommandTree())
                )
            )
            .then(Commands.literal("delete")
                .then(Commands.argument(ApexCommand.ARGUMENT_IDENTIFIER, ArgumentTypes.key())
                    .suggests(this::suggestInstances)
                    .executes(this::delete)
                )
            )
            .then(Commands.literal("join")
                .then(Commands.argument(ApexCommand.ARGUMENT_PLAYERS, ArgumentTypes.players())
                    .then(Commands.argument(ApexCommand.ARGUMENT_IDENTIFIER, ArgumentTypes.key())
                        .suggests(this::suggestInstances)
                        .executes(this::join)
                    )
                )
            )
            .then(Commands.literal("leave")
                .then(Commands.argument(ApexCommand.ARGUMENT_PLAYERS, ArgumentTypes.players())
                    .then(Commands.argument(ApexCommand.ARGUMENT_IDENTIFIER, ArgumentTypes.key())
                        .suggests(this::suggestInstances)
                        .executes(this::leave)
                    )
                )
            )
            .then(Commands.literal("start")
                .then(Commands.argument(ApexCommand.ARGUMENT_IDENTIFIER, ArgumentTypes.key())
                    .suggests(this::suggestInstances)
                    .executes(this::start)
                )
            )
            .then(Commands.literal("end")
                .then(Commands.argument(ApexCommand.ARGUMENT_IDENTIFIER, ArgumentTypes.key())
                    .suggests(this::suggestInstances)
                    .executes(this::end)
                )
            )
            .build();
    }

    private int items(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getExecutor();
        if (player != null) {
            DaggerManager.give(player);
            RevolverManager.give(player);
            ctx.getSource().getSender().sendMessage(Component.text("Gave ").append(player.name()).append(Component.text(" all special " + Deceiv.PLUGIN_ID + " items.")));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int clearChances(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        // clear all specified player's role chances
        for (Player player : ctx.getArgument(ApexCommand.ARGUMENT_PLAYERS, PlayerSelectorArgumentResolver.class).resolve(ctx.getSource())) {
            RoleManager.clearChances(player);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int create(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (gameContainer.createGame(ctx.getArgument(ApexCommand.ARGUMENT_IDENTIFIER, Key.class), buildArenaLimits(ctx))) {
            sender.sendMessage(Component.text("Game instance created successfully."));
        } else {
            sender.sendMessage(Component.text("Game already exists."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int delete(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (gameContainer.deleteGame(ctx.getArgument(ApexCommand.ARGUMENT_IDENTIFIER, Key.class))) {
            sender.sendMessage(Component.text("Game instance deleted successfully."));
        } else {
            sender.sendMessage(Component.text("Game does not exist."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int join(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return move(ctx, "Added", relation -> gameContainer.addPlayerToGame(relation.player(), relation.instance()));
    }

    private int leave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return move(ctx, "Removed", relation -> gameContainer.removePlayerFromGame(relation.player(), relation.instance()));
    }

    private int move(CommandContext<CommandSourceStack> ctx, String actionName, Function<PlayerInstanceRelation, Boolean> action) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        // remove every specified player from the specified game (if possible)
        GameInstance instance = gameContainer.getGameInstance(ctx.getArgument(ApexCommand.ARGUMENT_IDENTIFIER, Key.class));
        if (instance == null) {
            sender.sendMessage(Component.text("Game does not exist."));
            return Command.SINGLE_SUCCESS;
        }
        int count = 0;
        List<Player> players = ctx.getArgument(ApexCommand.ARGUMENT_PLAYERS, PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
        for (Player player : players) {
            if (action.apply(new PlayerInstanceRelation(player, instance))) count++;
        }
        instance.recalculateArenaChances();
        sender.sendMessage(Component.text(actionName + " " + count + " players from the specified game."));
        return Command.SINGLE_SUCCESS;
    }

    private int start(CommandContext<CommandSourceStack> ctx) {
        GameInstance instance = gameContainer.getGameInstance(ctx.getArgument(ApexCommand.ARGUMENT_IDENTIFIER, Key.class));
        CommandSender sender = ctx.getSource().getSender();
        if (instance == null) {
            sender.sendMessage(Component.text("Specified game does not exist."));
            return Command.SINGLE_SUCCESS;
        }
        if (instance.isRoundInProgress()) {
            sender.sendMessage(Component.text("Specified game already has a round in progress."));
            return Command.SINGLE_SUCCESS;
        }
        if (instance.startRound()) {
            sender.sendMessage(Component.text("Specified game round started successfully."));
        } else {
            sender.sendMessage(Component.text("A minimum of " + instance.getRequiredPlayerCount() + " players is required for the game to begin."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int end(CommandContext<CommandSourceStack> ctx) {
        GameInstance instance = gameContainer.getGameInstance(ctx.getArgument(ApexCommand.ARGUMENT_IDENTIFIER, Key.class));
        CommandSender sender = ctx.getSource().getSender();
        if (instance == null) {
            sender.sendMessage(Component.text("Specified game does not exist."));
            return Command.SINGLE_SUCCESS;
        }
        if (instance.endRound()) {
            sender.sendMessage(Component.text("Specified game terminated successfully."));
        } else {
            sender.sendMessage(Component.text("Specified game does not have a round in progress."));
        }
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Suggestions> suggestInstances(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        for (Key key : gameContainer.getInstanceKeys()) {
            suggestionsBuilder.suggest(key.asString());
        }
        return suggestionsBuilder.buildFuture();
    }

    private String buildRoleArgumentName(String roleName) {
        return "max" + roleName + "Count";
    }

    private RequiredArgumentBuilder<CommandSourceStack, Integer> buildCreateSubcommandTree() {
        Role[] roles = Role.values();
        int roleCount = roles.length;
        if (roleCount < 2) {
            throw new RuntimeException("At least two Game Roles must be defined!");
        }
        IntegerArgumentType type = IntegerArgumentType.integer(1, ApexCommand.MAX_ROLE_PLAYERS);
        var subcommand = Commands.argument(buildRoleArgumentName(roles[roleCount - 1].getName()), type).executes(this::create);
        for (int i = 2; i <= roleCount; i++) {
            subcommand = Commands.argument(buildRoleArgumentName(roles[roleCount - i].getName()), type).then(subcommand);
        }
        return subcommand;
    }

    private ArenaLimits buildArenaLimits(CommandContext<CommandSourceStack> ctx) {
        Map<Role, Integer> roleLimits = new HashMap<>();
        int maxPlayerCount = 0;
        for (Role role : Role.values()) {
            int roleLimit = ctx.getArgument(buildRoleArgumentName(role.getName()), Integer.class);
            roleLimits.put(role, roleLimit);
            maxPlayerCount += roleLimit;
        }
        return new ArenaLimits(Role.values().length, maxPlayerCount, roleLimits);
    }
}
