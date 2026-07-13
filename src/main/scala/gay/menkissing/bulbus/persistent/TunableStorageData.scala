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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import gay.menkissing.bulbus.components.StorageItemContents


// i WOULD copy this but the MIT one is so obscene that I'd rather just make my own
final class TunableStorageData extends SavedData:
  val storedStorageLists: ju.Map[TuningChannel, NonNullList[ItemStack]] = ju.HashMap()
  val storedTanks: mutable.Map[TuningChannel, StorageItemContents.Builder[FluidVariant]] = mutable.HashMap.empty

  def getOrCreateSlots(key: TuningChannel): NonNullList[ItemStack] =
    storedStorageLists.computeIfAbsent(key, _ => NonNullList.withSize(27, ItemStack.EMPTY))

  def getOrCreateTank(key: TuningChannel): StorageItemContents.Builder[FluidVariant] =
    storedTanks.getOrElseUpdate(key, StorageItemContents.Builder(FluidVariant.blank(), 0L, TunableStorageData.Tank.capacity, FluidVariant.blank()))

object TunableStorageData:

  object Tank:
    val capacity: Long = FluidConstants.BUCKET * 512

  def extractSavedStorages(storage: TunableStorageData): ju.List[TuningKV[ju.List[ItemStackWithSlot]]] =
    val resultMap = ju.ArrayList[TuningKV[ju.List[ItemStackWithSlot]]]()
    storage.storedStorageLists.forEach: (k, v) =>
      val resultList = ju.ArrayList[ItemStackWithSlot]()
      v.asScala.iterator.zipWithIndex.foreach: (stack, idx) =>
        if !stack.isEmpty then
          resultList.add(ItemStackWithSlot(idx, stack))
      
      resultMap.add((k, resultList))
    
    resultMap

  def extractSavedTanks(storage: TunableStorageData): ju.List[TuningKV[StorageItemContents[FluidVariant]]] =
    val resultMap = ju.ArrayList[TuningKV[StorageItemContents[FluidVariant]]]()
    storage.storedTanks.foreach: (k, v) =>
      resultMap.add((k, v.result))
    resultMap
    

  def apply(map: ju.List[TuningKV[ju.List[ItemStackWithSlot]]], storedTanks: ju.List[TuningKV[StorageItemContents[FluidVariant]]]): TunableStorageData =
    val storage = new TunableStorageData
    map.forEach:
      case (a, b) =>
        val newList = NonNullList.withSize(27, ItemStack.EMPTY)
        b.forEach: slottedStack =>
          newList.set(slottedStack.slot(), slottedStack.stack())
        storage.storedStorageLists.put(a, newList)
    storedTanks.forEach:
      case (a, b) =>
        storage.storedTanks(a) = StorageItemContents.Fluid.builder(b, Tank.capacity)
    storage

  type TuningKV[V] = (channel: TuningChannel, value: V)

  def tuningKeyedCodec[V](fieldName: String, valueCodec: Codec[V]): Codec[TuningKV[V]] =
    RecordCodecBuilder.create: inst =>
      inst.group(
        TuningChannel.CODEC.fieldOf("channel").forGetter(_.channel),
        valueCodec.fieldOf(fieldName).forGetter(_.value)
      ).apply(inst, (channel = _, value = _))

  val KVCODEC: Codec[TuningKV[ju.List[ItemStackWithSlot]]] =
    tuningKeyedCodec("slots", Codec.list(ItemStackWithSlot.CODEC))
  val CODEC: Codec[TunableStorageData] = RecordCodecBuilder.create: inst =>
    inst.group(
      Codec.list(KVCODEC).fieldOf("stored_storages").forGetter(extractSavedStorages),
      Codec.list(tuningKeyedCodec("tank", StorageItemContents.Fluid.CODEC)).fieldOf("stored_tanks").forGetter(extractSavedTanks)
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
