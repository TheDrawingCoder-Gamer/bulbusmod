package gay.menkissing.bulbus.content.block.entity

import net.minecraft.network.chat.{Component, ComponentSerialization}
import net.minecraft.world.Nameable
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.storage.{ValueInput, ValueOutput}

trait NameableBlockEntity extends BlockEntity, Nameable:
  var name: Option[Component] = None

  def defaultName: Component

  override def getName: Component =
    name.getOrElse(defaultName)

  def setCustomName(name: Component): Unit =
    this.name = Option(name)

  override def getCustomName: Component | Null =
    name.orNull

  override def loadAdditional(input: ValueInput): Unit =
    super.loadAdditional(input)
    setCustomName(BlockEntity.parseCustomNameSafe(input, "CustomName"))

  override def saveAdditional(output: ValueOutput): Unit =
    super.saveAdditional(output)
    output.storeNullable("CustomName", ComponentSerialization.CODEC, name.orNull[Component | Null])