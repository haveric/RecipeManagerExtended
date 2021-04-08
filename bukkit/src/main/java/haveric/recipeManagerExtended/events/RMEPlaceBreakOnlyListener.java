package haveric.recipeManagerExtended.events;

import haveric.recipeManagerExtended.RecipeManagerExtended;
import haveric.recipeManagerExtended.StringArrayItemTagType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RMEPlaceBreakOnlyListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void playerPlace(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        ItemMeta meta = hand.getItemMeta();
        if (meta != null) {
            PersistentDataType<byte[], String[]> tagType = new StringArrayItemTagType(Charset.defaultCharset());
            NamespacedKey keyAllowed = new NamespacedKey(RecipeManagerExtended.getPlugin(), "placeallowed");

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(keyAllowed, tagType)) {
                String[] materialsArray = container.get(keyAllowed, tagType);

                if (materialsArray != null) {
                    List<Material> materials = new ArrayList<>();
                    for (String materialString : materialsArray) {
                        Material material = Material.matchMaterial(materialString);
                        if (material != null) {
                            materials.add(material);
                        }
                    }

                    Material blockType = event.getBlockAgainst().getType();
                    if (!materials.contains(blockType)) {
                        event.setCancelled(true);
                    }
                }
            }

            NamespacedKey keyDisallowed = new NamespacedKey(RecipeManagerExtended.getPlugin(), "placedisallowed");
            if (container.has(keyDisallowed, tagType)) {
                String[] materialsArray = container.get(keyDisallowed, tagType);

                if (materialsArray != null) {
                    List<Material> materials = new ArrayList<>();
                    for (String materialString : materialsArray) {
                        Material material = Material.matchMaterial(materialString);
                        if (material != null) {
                            materials.add(material);
                        }
                    }

                    Material blockType = event.getBlockAgainst().getType();
                    if (materials.contains(blockType)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                Player player = event.getPlayer();
                checkBreak(event, player, block.getType());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void playerDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        checkBreak(event, player, blockType);
    }

    @EventHandler(ignoreCancelled = true)
    public void playerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        checkBreak(event, player, blockType);
    }

    private void checkBreak(Cancellable event, Player player, Material blockType) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        if (meta != null) {
            PersistentDataType<byte[], String[]> tagType = new StringArrayItemTagType(Charset.defaultCharset());
            NamespacedKey keyAllowed = new NamespacedKey(RecipeManagerExtended.getPlugin(), "breakallowed");

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(keyAllowed, tagType)) {
                String[] materialsArray = container.get(keyAllowed, tagType);

                if (materialsArray != null) {
                    List<Material> materials = new ArrayList<>();
                    for (String materialString : materialsArray) {
                        Material material = Material.matchMaterial(materialString);
                        if (material != null) {
                            materials.add(material);
                        }
                    }

                    if (!materials.contains(blockType)) {
                        event.setCancelled(true);
                    }
                }
            }

            NamespacedKey keyDisallowed = new NamespacedKey(RecipeManagerExtended.getPlugin(), "breakdisallowed");
            if (container.has(keyDisallowed, tagType)) {
                String[] materialsArray = container.get(keyDisallowed, tagType);

                if (materialsArray != null) {
                    List<Material> materials = new ArrayList<>();
                    for (String materialString : materialsArray) {
                        Material material = Material.matchMaterial(materialString);
                        if (material != null) {
                            materials.add(material);
                        }
                    }

                    if (materials.contains(blockType)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
