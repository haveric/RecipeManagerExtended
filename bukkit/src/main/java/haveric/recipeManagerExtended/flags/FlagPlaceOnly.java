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

public class FlagPlaceOnly extends Flag {
    @Override
    public String getFlagType() {
        return "@placeonly";
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
            "Restricts where a block can be placed.",
            "Works similarly to the the canPlaceOn tag, but for all gamemodes.",
            "",
            "The <material> argument accepts a comma separated list of materials.",
            "",
            "Using a '!' before the materials will reverse the functionality.",
            "  This means all materials will be allowed except for those defined", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} cobblestone, stone // Can only place on cobblestone and stone",
            "{flag} !cobblestone, stone // Can place on anything except cobblestone and stone"};
    }

    private List<Material> placeAllowed = new ArrayList<>();
    private List<Material> placeDisallowed = new ArrayList<>();

    public FlagPlaceOnly() { }

    public FlagPlaceOnly(FlagPlaceOnly flag) {
        placeAllowed.addAll(flag.placeAllowed);
        placeDisallowed.addAll(flag.placeDisallowed);
    }

    @Override
    public FlagPlaceOnly clone() {
        return new FlagPlaceOnly((FlagPlaceOnly) super.clone());
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
                    placeAllowed.add(material);
                } else {
                    placeDisallowed.add(material);
                }
            }
        }

        return true;
    }

    @Override
    public void onPrepare(Args a) {
        if (canAddMeta(a)) {
            ItemMeta meta = a.result().getItemMeta();

            if (meta != null && (!placeAllowed.isEmpty() || !placeDisallowed.isEmpty())) {
                setLore(a, meta);
            }
        }
    }

    @Override
    public void onCrafted(Args a) {
        if (canAddMeta(a)) {
            ItemMeta meta = a.result().getItemMeta();

            if (meta != null) {
                if (!placeAllowed.isEmpty()) {
                    String[] materialsArray = new String[placeAllowed.size()];
                    for (int i = 0; i < placeAllowed.size(); i++) {
                        Material material = placeAllowed.get(i);
                        materialsArray[i] = material.toString();
                    }

                    NamespacedKey key = new NamespacedKey(RecipeManagerExtended.getPlugin(), "placeallowed");
                    meta.getPersistentDataContainer().set(key, new StringArrayItemTagType(Charset.defaultCharset()), materialsArray);
                }

                if (!placeDisallowed.isEmpty()) {
                    String[] materialsArray = new String[placeDisallowed.size()];
                    for (int i = 0; i < placeDisallowed.size(); i++) {
                        Material material = placeDisallowed.get(i);
                        materialsArray[i] = material.toString();
                    }

                    NamespacedKey key = new NamespacedKey(RecipeManagerExtended.getPlugin(), "placedisallowed");
                    meta.getPersistentDataContainer().set(key, new StringArrayItemTagType(Charset.defaultCharset()), materialsArray);
                }

                if (!placeAllowed.isEmpty() || !placeDisallowed.isEmpty()) {
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

        if (!placeAllowed.isEmpty()) {
            lores.add(ChatColor.GRAY + "Can be placed on:");
            for (Material material : placeAllowed) {
                lores.add(ChatColor.DARK_GRAY + WordUtils.capitalizeFully(material.toString().replace("_", " ")));
            }
        }

        if (!placeDisallowed.isEmpty()) {
            lores.add(ChatColor.GRAY + "Cannot be placed on:");
            for (Material material : placeDisallowed) {
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
        for (int i = 0; i < placeAllowed.size(); i++) {
            Material material = placeAllowed.get(i);
            toHash.append(material.toString());

            if (i < placeAllowed.size() - 1) {
                toHash.append(",");
            }
        }

        toHash.append("materialsDisallowed: ");
        for (int i = 0; i < placeDisallowed.size(); i++) {
            Material material = placeDisallowed.get(i);
            toHash.append(material.toString());

            if (i < placeDisallowed.size() - 1) {
                toHash.append(",");
            }
        }

        return toHash.toString().hashCode();
    }
}
