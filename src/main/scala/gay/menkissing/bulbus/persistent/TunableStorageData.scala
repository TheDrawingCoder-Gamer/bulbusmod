package gay.menkissing.bulbus.persistent

import com.mojang.datafixers.util.Pair
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import com.mojang.serialization.Codec
import net.minecraft.world.level.saveddata.SavedDataType
import gay.menkissing.bulbus.BulbusMod

import java.util.UUID
import net.minecraft.world.level.block.entity.ListBackedContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.core.NonNullList
import net.minecraft.world.entity.player.Player

import java.util as ju
import collection.mutable
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs
import net.minecraft.core.UUIDUtil
import net.minecraft.world.ItemStackWithSlot

import scala.jdk.CollectionConverters.*


// i WOULD copy this but the MIT one is so obscene that I'd rather just make my own
final class TunableStorageData extends SavedData:
  val storedStorageLists: ju.Map[TuningChannel, NonNullList[ItemStack]] = ju.HashMap()

  def getOrCreateSlots(key: TuningChannel): NonNullList[ItemStack] =
    storedStorageLists.computeIfAbsent(key, _ => NonNullList.withSize(27, ItemStack.EMPTY))

  

object TunableStorageData:
  def extractSavedStorages(storage: TunableStorageData): ju.List[(TuningChannel, ju.List[ItemStackWithSlot])] =
    val resultMap = ju.ArrayList[(TuningChannel, ju.List[ItemStackWithSlot])]()
    storage.storedStorageLists.forEach: (k, v) =>
      val resultList = ju.ArrayList[ItemStackWithSlot]()
      v.asScala.iterator.zipWithIndex.foreach: (stack, idx) =>
        if !stack.isEmpty then
          resultList.add(ItemStackWithSlot(idx, stack))
      
      resultMap.add((k, resultList))
    
    resultMap


  def apply(map: ju.List[(TuningChannel, ju.List[ItemStackWithSlot])]): TunableStorageData =
    val storage = new TunableStorageData
    map.forEach:
      case (a, b) =>
        val newList = NonNullList.withSize(27, ItemStack.EMPTY)
        b.forEach: slottedStack =>
          newList.set(slottedStack.slot(), slottedStack.stack())
        storage.storedStorageLists.put(a, newList)
    storage

  val KVCODEC: Codec[(TuningChannel, ju.List[ItemStackWithSlot])] =
    RecordCodecBuilder.create: inst =>
      inst.group(
        TuningChannel.CODEC.fieldOf("channel").forGetter(_._1),
        Codec.list(ItemStackWithSlot.CODEC).fieldOf("slots").forGetter(_._2)
      ).apply(inst, (_, _))
  val CODEC: Codec[TunableStorageData] = RecordCodecBuilder.create: inst =>
    inst.group(
      Codec.list(KVCODEC).fieldOf("stored_storages").forGetter(extractSavedStorages)
    ).apply(inst, apply)

  val TYPE: SavedDataType[TunableStorageData] =
    new SavedDataType(
      BulbusMod.locate("tunable_storage_data"),
      () => new TunableStorageData(),
      CODEC,
      null
    )

  def get(server: MinecraftServer): TunableStorageData =
    val level: ServerLevel | Null = server.getLevel(Level.OVERWORLD)

    if level != null then level.getDataStorage.computeIfAbsent(TYPE)
    else new TunableStorageData
