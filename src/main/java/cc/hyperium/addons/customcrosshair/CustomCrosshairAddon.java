package cc.hyperium.addons.customcrosshair;

import cc.hyperium.Hyperium;
import cc.hyperium.addons.AbstractAddon;
import cc.hyperium.addons.customcrosshair.command.CommandCustomCrosshair;
import cc.hyperium.addons.customcrosshair.crosshair.CustomCrosshair;
import cc.hyperium.addons.customcrosshair.utils.CustomCrosshairConfig;
import cc.hyperium.event.EventBus;
import cc.hyperium.utils.ChatColor;
import java.awt.Color;

public class CustomCrosshairAddon extends AbstractAddon {

    public static final Color PRIMARY = new Color(23, 107, 192, 255);
    public static final Color PRIMARY_T = new Color(23, 107, 192, 128);
    public static final Color SECONDARY = new Color(255, 255, 255, 255);

    private static CustomCrosshairAddon instance;

    private CustomCrosshair crosshair;
    private CustomCrosshairConfig config;

    public static String VERSION = "0.5.3-hyperium";

    @Override
    public AbstractAddon init() {
        EventBus.INSTANCE.register(this);
        instance = this;
        this.crosshair = new CustomCrosshair();
        this.config = new CustomCrosshairConfig(this);

        if (!this.config.readSaveFile()) {
            this.config.writeSaveFileDefault();
        }

        Hyperium.INSTANCE.getHandlers().getHyperiumCommandHandler()
            .registerCommand(new CommandCustomCrosshair(this));

        EventBus.INSTANCE.register(this.crosshair);

        return this;
    }

    @Override
    public Metadata getAddonMetadata() {
        AbstractAddon.Metadata metadata = new AbstractAddon.Metadata(this, "Custom Crosshair Addon", "0.5.3", "Amplifiable");
        metadata.setDisplayName(ChatColor.GREEN + "Custom Crosshair Addon");
        metadata.setDescription("Customise the Rich Presence of Hyperium");

        return metadata;
    }

    public void resetCrosshair() {
        this.crosshair = new CustomCrosshair();
    }

    public CustomCrosshair getCrosshair() {
        if (this.crosshair == null) {
            resetCrosshair();
        }

        return this.crosshair;
    }

    public CustomCrosshairConfig getConfig() {
        return this.config;
    }

    public static CustomCrosshairAddon getCrosshairMod() {
        return instance;
    }
}
