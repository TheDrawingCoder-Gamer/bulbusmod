package gay.menkissing.bulbus.registries

import net.minecraft.world.entity.EntityType
import gay.menkissing.bulbus.content.entity.ThrownRepairBottle
import gay.menkissing.bulbus.content.entity.RepairOrb
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.Entity
import gay.menkissing.bulbus.BulbusMod
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries

object BulbusEntities:
  def register[T <: Entity](name: String)(builder: EntityType.Builder[T]): EntityType[T] =
    val id = BulbusMod.locate(name)
    val key = ResourceKey.create(Registries.ENTITY_TYPE, id)
    Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key))


  val thrownRepairBottle: EntityType[ThrownRepairBottle] =
    register("repair_bottle"):
      EntityType.Builder.of[ThrownRepairBottle](ThrownRepairBottle.create, MobCategory.MISC)
        .noLootTable()
        .sized(0.25f, 0.25f)
        .clientTrackingRange(4)
        .updateInterval(10)
  val repairOrb: EntityType[RepairOrb] =
    register("repair_orb"):
      EntityType.Builder.of[RepairOrb](RepairOrb.apply, MobCategory.MISC)
        .noLootTable()
        .sized(0.5f, 0.5f)
        .clientTrackingRange(6)
        .updateInterval(20)

  def init(): Unit = ()