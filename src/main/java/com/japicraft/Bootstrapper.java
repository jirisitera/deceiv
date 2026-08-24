package com.japicraft;

import com.japicraft.ui.InformationInterface;
import com.japicraft.ui.ShopInterface;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.DialogKeys;
import io.papermc.paper.registry.keys.tags.DialogTagKeys;
import io.papermc.paper.tag.TagEntry;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

// suppressed unused class warning, due to bootstrap not interacting with the plugin (as it only exists during bootup phase)
@SuppressWarnings("unused")
public class Bootstrapper implements PluginBootstrap {
    private static final String DATAPACK_PATH = "/datapack";
    private static final String DATAPACK_NAME = "data";

    @Override
    public void bootstrap(BootstrapContext context) {
        LifecycleEventManager<BootstrapContext> lifecycleManager = context.getLifecycleManager();
        registerDatapacks(lifecycleManager);
        registerTags(lifecycleManager);
        registerDialogs(lifecycleManager);
    }

    public void registerDatapacks(LifecycleEventManager<BootstrapContext> lifecycleManager) {
        lifecycleManager.registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(event -> {
            try {
                URL datapack = getClass().getResource(DATAPACK_PATH);
                if (datapack == null) {
                    return;
                }
                event.registrar().discoverPack(datapack.toURI(), DATAPACK_NAME);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public void registerTags(LifecycleEventManager<BootstrapContext> lifecycleManager) {
        lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.DIALOG), event -> {
            // register dialog tags
            event.registrar().setTag(DialogTagKeys.PAUSE_SCREEN_ADDITIONS, Set.of(TagEntry.valueEntry(DialogKeys.create(InformationInterface.DIALOG_KEY))));
            event.registrar().setTag(DialogTagKeys.QUICK_ACTIONS, Set.of(TagEntry.valueEntry(DialogKeys.create(ShopInterface.DIALOG_KEY))));
        });
    }

    public void registerDialogs(LifecycleEventManager<BootstrapContext> lifecycleManager) {
        List<PrioritizedLifecycleEventHandlerConfiguration<BootstrapContext>> dialogs = List.of(
            new InformationInterface().get(),
            new ShopInterface().get()
        );
        for (PrioritizedLifecycleEventHandlerConfiguration<BootstrapContext> dialog : dialogs) {
            lifecycleManager.registerEventHandler(dialog);
        }
    }
}
