package gay.menkissing.bulbus.registries

import com.mojang.serialization.Codec
import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.components.StorageItemContents
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs

object BulbusDataComponentTypes:
  private def register[T](name: String, kind: DataComponentType[T]): kind.type =
    Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, BulbusMod.locate(name), kind)
    kind

  val STASIS_BOTTLE_CONTENTS: DataComponentType[StorageItemContents[FluidVariant]] = register(
    "stasis_bottle_contents",
    DataComponentType.builder[StorageItemContents[FluidVariant]]().persistent(StorageItemContents.Fluid.CODEC).networkSynchronized(StorageItemContents.Fluid.STREAM_CODEC).build()
  )
  
  val STASIS_TUBE_CONTENTS: DataComponentType[StorageItemContents[ItemVariant]] = register(
    "stasis_tube_contents",
    DataComponentType.builder[StorageItemContents[ItemVariant]]().persistent(StorageItemContents.Item.CODEC).networkSynchronized(StorageItemContents.Item.STREAM_CODEC).build()
  )
  
  val KNOWLEDGE_STORAGE_CONTENTS: DataComponentType[Long] = register(
    "knowledge_storage_contents",
    DataComponentType.builder[Long]().persistent(Codec.LONG.asInstanceOf).networkSynchronized(ByteBufCodecs.LONG.asInstanceOf).build()
  )

  def init(): Unit = ()

