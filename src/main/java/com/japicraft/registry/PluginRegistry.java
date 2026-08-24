package com.japicraft.registry;

import com.japicraft.Deceiv;
import com.japicraft.command.ApexCommand;
import com.japicraft.container.GameContainer;
import com.japicraft.event.AsyncEvents;
import com.japicraft.event.CancelledEvents;
import com.japicraft.event.PlayerEvents;
import com.japicraft.game.InteractManager;
import com.japicraft.hook.Hook;
import com.japicraft.item.ItemManager;
import com.japicraft.player.AnimationManager;
import com.japicraft.ui.DialogManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;

public class PluginRegistry {
    private final Deceiv plugin;

    public PluginRegistry(Deceiv plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        ArrayList<LiteralCommandNode<CommandSourceStack>> commands = new ArrayList<>();
        ArrayList<Listener> events = new ArrayList<>();
        // plugin hooks
        checkHooksAs(events);
        // game systems
        GameContainer gameContainer = new GameContainer(plugin);
        // player systems
        AnimationManager animationManager = new AnimationManager();
        // items
        ItemManager itemManager = new ItemManager(plugin, animationManager);
        // commands
        commands.add(new ApexCommand(gameContainer).build());
        // events
        events.add(new AsyncEvents());
        events.add(new CancelledEvents());
        events.add(new PlayerEvents(gameContainer, animationManager));
        events.add(new DialogManager());
        events.add(new InteractManager(gameContainer, animationManager, itemManager));
        // register everything
        registerCommands(commands);
        registerEvents(events);
    }

    public void checkHooksAs(ArrayList<Listener> events) {
        for (Hook hook : Hook.values()) {
            hook.setAvailable(plugin.getServer().getPluginManager().isPluginEnabled(hook.getName()));
            if (hook.isAvailable()) {
                try {
                    events.add(hook.getListener().getConstructor(Deceiv.class).newInstance(plugin));
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
                plugin.getComponentLogger().atInfo().log(Component.text("Hooked into " + hook.getName() + " successfully. " + hook.getPurpose() + " are now available."));
            } else {
                plugin.getComponentLogger().atWarn().log(Component.text("Could not hook into " + hook.getName() + ", continuing without it. " + hook.getPurpose() + " will not be available."));
            }
        }
    }

    public void registerCommands(Collection<LiteralCommandNode<CommandSourceStack>> commands) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, (ReloadableRegistrarEvent<Commands> event) -> {
            for (LiteralCommandNode<CommandSourceStack> command : commands) {
                event.registrar().register(command);
            }
        });
    }

    public void registerEvents(Collection<Listener> events) {
        for (Listener event : events) {
            plugin.getServer().getPluginManager().registerEvents(event, plugin);
        }
    }
}
