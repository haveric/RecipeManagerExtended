package haveric.recipeManagerExtended.flags;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.Files;
import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.tools.Tools;
import haveric.recipeManagerExtended.RecipeManagerExtended;
import haveric.recipeManagerExtended.StringArrayItemTagType;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FlagBreakOnly extends Flag {
    @Override
    public String getFlagType() {
        return "@breakonly";
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <material>, [...]",
            "{flag} !<material>, [...]"};
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Restricts what blocks can be broken with this item.",
            "Works similarly to the the CanDestroy tag, but for all gamemodes.",
            "",
            "The <material> argument accepts a comma separated list of materials.",
            "",
            "Using a '!' before the materials will reverse the functionality.",
            "  This means all materials can be broken except for those defined", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} cobblestone, stone // Can only break cobblestone and stone",
            "{flag} !cobblestone, stone // Can break anything except cobblestone and stone"};
    }

    private List<Material> breakAllowed = new ArrayList<>();
    private List<Material> breakDisallowed = new ArrayList<>();

    public FlagBreakOnly() { }

    public FlagBreakOnly(FlagBreakOnly flag) {
        breakAllowed.addAll(flag.breakAllowed);
        breakDisallowed.addAll(flag.breakDisallowed);
    }

    @Override
    public FlagBreakOnly clone() {
        return new FlagBreakOnly((FlagBreakOnly) super.clone());
    }

    @Override
    public boolean onParse(String value, String fileName, int lineNum, int restrictedBit) {
        super.onParse(value, fileName, lineNum, restrictedBit);

        boolean allowed = true;
        if (value.startsWith("!")) {
            allowed = false;
            value = value.substring(1);
        }

        String[] split = value.split(",");

        for (String materialString : split) {
            materialString = materialString.trim();

            Material material = Tools.parseMaterial(materialString);
            if (material == null) {
                ErrorReporter.getInstance().warning("Material '" + materialString + "' does not exist!", "Name could be different, look in '" + Files.FILE_INFO_NAMES + "' or '" + Files.FILE_ITEM_ALIASES + "' for material names.");
            } else {
                if (allowed) {
                    breakAllowed.add(material);
                } else {
                    breakDisallowed.add(material);
                }
            }
        }

        return true;
    }

    @Override
    public void onPrepare(Args a) {
        if (canAddMeta(a)) {
            ItemMeta meta = a.result().getItemMeta();

            if (meta != null && (!breakAllowed.isEmpty() || !breakDisallowed.isEmpty())) {
                setLore(a, meta);
            }
        }
    }

    @Override
    public void onCrafted(Args a) {
        if (canAddMeta(a)) {
            ItemMeta meta = a.result().getItemMeta();

            if (meta != null) {
                if (!breakAllowed.isEmpty()) {
                    String[] materialsArray = new String[breakAllowed.size()];
                    for (int i = 0; i < breakAllowed.size(); i++) {
                        Material material = breakAllowed.get(i);
                        materialsArray[i] = material.toString();
                    }

                    NamespacedKey key = new NamespacedKey(RecipeManagerExtended.getPlugin(), "breakallowed");
                    meta.getPersistentDataContainer().set(key, new StringArrayItemTagType(Charset.defaultCharset()), materialsArray);
                }

                if (!breakDisallowed.isEmpty()) {
                    String[] materialsArray = new String[breakDisallowed.size()];
                    for (int i = 0; i < breakDisallowed.size(); i++) {
                        Material material = breakDisallowed.get(i);
                        materialsArray[i] = material.toString();
                    }

                    NamespacedKey key = new NamespacedKey(RecipeManagerExtended.getPlugin(), "breakdisallowed");
                    meta.getPersistentDataContainer().set(key, new StringArrayItemTagType(Charset.defaultCharset()), materialsArray);
                }

                if (!breakAllowed.isEmpty() || !breakDisallowed.isEmpty()) {
                    setLore(a, meta);
                }
            }
        }
    }

    private void setLore(Args a, ItemMeta meta) {
        List<String> lores = meta.getLore();
        if (lores == null) {
            lores = new ArrayList<>();
        }

        if (!breakAllowed.isEmpty()) {
            lores.add(ChatColor.GRAY + "Can break:");
            for (Material material : breakAllowed) {
                lores.add(ChatColor.DARK_GRAY + WordUtils.capitalizeFully(material.toString().replace("_", " ")));
            }
        }

        if (!breakDisallowed.isEmpty()) {
            lores.add(ChatColor.GRAY + "Cannot break:");
            for (Material material : breakDisallowed) {
                lores.add(ChatColor.DARK_GRAY + WordUtils.capitalizeFully(material.toString().replace("_", " ")));
            }
        }

        meta.setLore(lores);
        a.result().setItemMeta(meta);
    }

    @Override
    public int hashCode() {
        StringBuilder toHash = new StringBuilder("" + super.hashCode());

        toHash.append("materialsAllowed: ");
        for (int i = 0; i < breakAllowed.size(); i++) {
            Material material = breakAllowed.get(i);
            toHash.append(material.toString());

            if (i < breakAllowed.size() - 1) {
                toHash.append(",");
            }
        }

        toHash.append("materialsDisallowed: ");
        for (int i = 0; i < breakDisallowed.size(); i++) {
            Material material = breakDisallowed.get(i);
            toHash.append(material.toString());

            if (i < breakDisallowed.size() - 1) {
                toHash.append(",");
            }
        }

        return toHash.toString().hashCode();
    }
}
