package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent

object BulbusSounds:
  private def registerSound(name: String): SoundEvent =
    val id = BulbusMod.locate(name)
    Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id))

  val stasisAccessorAddItem: SoundEvent = registerSound("block.stasis_accessor.add_item")
  val stasisAccessorRemoveItem: SoundEvent = registerSound("block.stasis_accessor.remove_item")
  val stasisAccessorAddItemFail: SoundEvent = registerSound("block.stasis_accessor.add_item_fail")
  
  val stasisWormOpen: SoundEvent = registerSound("block.stasis_worm.open")
  val stasisWormClose: SoundEvent = registerSound("block.stasis_worm.close")
  
  val stasisShelfOpen: SoundEvent = registerSound("block.stasis_shelf.open")
  val stasisShelfClose: SoundEvent = registerSound("block.stasis_shelf.close")

  def init(): Unit = ()
