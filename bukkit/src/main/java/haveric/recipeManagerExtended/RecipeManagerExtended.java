package haveric.recipeManagerExtended;

import haveric.recipeManager.RecipeManager;
import haveric.recipeManager.flag.FlagBit;
import haveric.recipeManager.flag.FlagLoader;

import haveric.recipeManagerExtended.events.RMEPlaceBreakOnlyListener;
import haveric.recipeManagerExtended.flags.FlagBreakOnly;
import haveric.recipeManagerExtended.flags.FlagPlaceOnly;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeManagerExtended extends JavaPlugin {

    private static RecipeManagerExtended plugin;

    @Override
    public void onEnable() {
        plugin = this;

        FlagLoader flagLoader = RecipeManager.getFlagLoader();
        flagLoader.loadFlag("placeonly", new FlagPlaceOnly(), FlagBit.NONE);
        flagLoader.loadFlag("breakonly", new FlagBreakOnly(), FlagBit.NONE);

        Bukkit.getPluginManager().registerEvents(new RMEPlaceBreakOnlyListener(), plugin);
    }

    /**
     * @return plugin's main class
     */
    public static RecipeManagerExtended getPlugin() {
        return plugin;
    }
}
