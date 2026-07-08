package gay.menkissing.bulbus.util

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockEntityApiProvider
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}

object LookupUtil:
  extension[A, C] (self: BlockApiLookup[A, C])
    def registerScalaEntities[T <: BlockEntity](entities: BlockEntityType[T]*)(register: (T, C) => A | Null): Unit =
      self.registerForBlockEntities(
        (a, b) => register(a.asInstanceOf[T], b),
        entities*
      )
