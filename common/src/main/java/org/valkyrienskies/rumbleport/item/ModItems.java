package org.valkyrienskies.rumbleport.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.valkyrienskies.rumbleport.RumblePortMod;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(RumblePortMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<ArmorItem> STONE_HELMET = ITEMS.register(
        "stone_helmet",
        () -> new ArmorItem(ModArmorMaterials.STONE, ArmorItem.Type.HELMET, new Item.Properties())
    );
    public static final RegistrySupplier<ArmorItem> STONE_CHESTPLATE = ITEMS.register(
        "stone_chestplate",
        () -> new ArmorItem(ModArmorMaterials.STONE, ArmorItem.Type.CHESTPLATE, new Item.Properties())
    );
    public static final RegistrySupplier<ArmorItem> STONE_LEGGINGS = ITEMS.register(
        "stone_leggings",
        () -> new ArmorItem(ModArmorMaterials.STONE, ArmorItem.Type.LEGGINGS, new Item.Properties())
    );
    public static final RegistrySupplier<ArmorItem> STONE_BOOTS = ITEMS.register(
        "stone_boots",
        () -> new ArmorItem(ModArmorMaterials.STONE, ArmorItem.Type.BOOTS, new Item.Properties())
    );

    private ModItems() {
    }

    public static void register() {
        ITEMS.register();
        CreativeTabRegistry.modify(CreativeTabRegistry.defer(CreativeModeTabs.COMBAT), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptAfter(Items.CHAINMAIL_BOOTS, STONE_BOOTS.get());
            output.accept(STONE_LEGGINGS.get());
            output.accept(STONE_CHESTPLATE.get());
            output.accept(STONE_HELMET.get());
        });
    }
}
