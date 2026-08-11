package com.japicraft;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import io.papermc.paper.registry.keys.tags.DialogTagKeys;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class Bootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        LifecycleEventManager<BootstrapContext> lifecycleManager = context.getLifecycleManager();
        this.registerDatapacks(lifecycleManager);
        this.registerTags(lifecycleManager);
        this.registerDialogs(lifecycleManager);
    }

    private void registerDatapacks(LifecycleEventManager<BootstrapContext> lifecycleManager) {
        lifecycleManager.registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(event -> {
            try {
                URL datapack = this.getClass().getResource("/datapack");
                if (datapack == null) {
                    return;
                }
                event.registrar().discoverPack(datapack.toURI(), "data");
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private void registerTags(LifecycleEventManager<BootstrapContext> lifecycleManager) {
        lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.DIALOG),
            event -> event.registrar().setTag(
                DialogTagKeys.PAUSE_SCREEN_ADDITIONS, Set.of(
                    TagEntry.valueEntry(DialogKeys.create(Key.key(Deceiv.PLUGIN_ID, "info")))
                )
            )
        );
        lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.DIALOG),
            event -> event.registrar().setTag(
                DialogTagKeys.QUICK_ACTIONS, Set.of(
                    TagEntry.valueEntry(DialogKeys.create(Key.key(Deceiv.PLUGIN_ID, "shop")))
                )
            )
        );
    }

    private void registerDialogs(LifecycleEventManager<BootstrapContext> lifecycleManager) {
        lifecycleManager.registerEventHandler(RegistryEvents.DIALOG.compose()
            .newHandler(event -> event.registry().register(
                    DialogKeys.create(Key.key(Deceiv.PLUGIN_ID, "info")), builder -> builder
                        .base(DialogBase.builder(Component.text("Important Information"))
                            .body(List.of(
                                    DialogBody.plainMessage(Component.empty()),
                                    DialogBody.plainMessage(Component.empty()),
                                    DialogBody.plainMessage(Component.empty()),
                                    DialogBody.plainMessage(Component.text("Deceiv: Murder Mystery")),
                                    DialogBody.plainMessage(Component.empty())
                                )
                            )
                            .build()
                        )
                        .type(DialogType.notice(ActionButton.builder(Component.text("Vrátit se")).build()))
                )
            )
        );
        lifecycleManager.registerEventHandler(RegistryEvents.DIALOG.compose()
            .newHandler(event -> event.registry().register(
                    DialogKeys.create(Key.key(Deceiv.PLUGIN_ID, "shop")), builder -> builder
                        .base(DialogBase.builder(Component.text("Murderer's Item Shop"))
                            .body(List.of(
                                    DialogBody.plainMessage(Component.empty()),
                                    DialogBody.plainMessage(Component.empty()),
                                    DialogBody.plainMessage(Component.empty()),
                                    DialogBody.plainMessage(Component.text("Number of Murder Points")),
                                    DialogBody.plainMessage(Component.empty())
                                )
                            )
                            .build()
                        )
                        .type(DialogType.notice(ActionButton.builder(Component.text("Close")).build()))
                )
            )
        );
    }
}
