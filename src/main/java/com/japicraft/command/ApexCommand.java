package com.japicraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class ApexCommand {
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("deceiv")
                .then(Commands.literal("test")
                        .executes(ApexCommand::test)
                );
        return command.build();
    }

    private static int test(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getExecutor();
        if (player == null) {
            return 0;
        }
        player.sendMessage("Hello world!");
        return 1;
    }
}
