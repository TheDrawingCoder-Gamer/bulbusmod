package gay.menkissing.bulbus.api;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface XPStorage {
    BlockApiLookup<SingleTypeStorage, @Nullable Direction> SIDED =
            BlockApiLookup.get(Identifier.fromNamespaceAndPath("bulbus", "sided_xp"), SingleTypeStorage.class, Direction.class);
    
    ItemApiLookup<SingleTypeStorage, ContainerItemContext> ITEM =
            ItemApiLookup.get(Identifier.fromNamespaceAndPath("bulbus", "xp"), SingleTypeStorage.class, ContainerItemContext.class);
}
