package gay.menkissing.bulbus.content.item

import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.registries.BulbusDataComponentTypes
import gay.menkissing.bulbus.util.FluidUtil
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidConstants, FluidVariant, FluidVariantAttributes}
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.{BlockPos, Holder}
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.enchantment.{Enchantment, Enchantments}
import net.minecraft.world.item.{Item, ItemStack, Items, TooltipFlag}
import net.minecraft.world.level.block.{BucketPickup, LiquidBlockContainer}
import net.minecraft.world.level.{ClipContext, Level}
import net.minecraft.world.phys.HitResult

import java.util.function.Consumer

class StasisBottleItem(props: Item.Properties) extends Item(props):
  override def canBeEnchantedWith(stack: ItemStack, enchantment: Holder[Enchantment], context: EnchantingContext): Boolean =
    super.canBeEnchantedWith(stack, enchantment, context) || enchantment.is(Enchantments.POWER)

  override def use(level: Level, player: Player, hand: InteractionHand): InteractionResult =
    val stack = player.getItemInHand(hand)
    val contents = stack.get(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS)
    if contents == null then
      return InteractionResult.PASS
    val blockHitResult = Item.getPlayerPOVHitResult(level, player, if player.isShiftKeyDown then ClipContext.Fluid.NONE else ClipContext.Fluid.SOURCE_ONLY)
    blockHitResult.getType match
      case HitResult.Type.MISS | HitResult.Type.ENTITY =>
        InteractionResult.PASS
      case HitResult.Type.BLOCK =>
        val hitPos = blockHitResult.getBlockPos
        val direction = blockHitResult.getDirection
        val placePos = hitPos.relative(direction)
        if !level.mayInteract(player, hitPos) || !player.mayUseItemAt(placePos, direction, stack) then
          InteractionResult.FAIL
        else
          val hitState = level.getBlockState(hitPos)
          // todo, max
          val builder = StorageItemContents.Fluid.builder(contents, StasisBottleItem.baseMax)
          if player.isShiftKeyDown then
            // placing
            val targetPos = if hitState.getBlock.isInstanceOf[LiquidBlockContainer] then hitPos else placePos
            if builder.extract(builder.template, FluidConstants.BUCKET) != FluidConstants.BUCKET then
              InteractionResult.FAIL
            else if FluidUtil.placeFluid(player, level, targetPos, blockHitResult, contents.variant) then
              if !player.getAbilities.instabuild then
                stack.set(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS, builder.result)
              InteractionResult.SUCCESS
            else
              InteractionResult.FAIL
          else
            // pickup
            if builder.max - builder.amount >= FluidConstants.BUCKET then
              val fluid = level.getFluidState(hitPos)

              if fluid != null && (builder.isEmpty || builder.template.getFluid == fluid.getType) then
                if builder.insert(FluidVariant.of(fluid.getType), FluidConstants.BUCKET) == FluidConstants.BUCKET then
                  hitState.getBlock match
                    case bucketPickup: BucketPickup if !bucketPickup.pickupBlock(player, level, hitPos, hitState).isEmpty =>
                      val sound = FluidVariantAttributes.getFillSound(FluidVariant.of(fluid.getType))
                      level.playSound(player, hitPos, sound, SoundSource.BLOCKS, 1f, 1f)
                      stack.set(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS, builder.result)
                      InteractionResult.SUCCESS
                    case _ => InteractionResult.FAIL
                else
                  InteractionResult.FAIL
              else
                InteractionResult.FAIL
            else
              InteractionResult.FAIL



object StasisBottleItem:

  val baseMax: Long = FluidConstants.BUCKET * 256

  final class StasisBottleStorage(context: ContainerItemContext, val capacity: Long) extends SingleVariantItemStorage[FluidVariant](context):
    override def getCapacity(variant: FluidVariant): Long = capacity

    override def getBlankResource: FluidVariant = FluidVariant.blank()
    

    override def getUpdatedVariant(currentVariant: ItemVariant, newResource: FluidVariant, newAmount: Long): ItemVariant =
      val newContents = StorageItemContents[FluidVariant](newResource, newAmount)

      currentVariant.withComponents(DataComponentPatch.builder().set(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS, newContents).build())

    override def getResource(currentVariant: ItemVariant): FluidVariant =
      val contents = currentVariant.get(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS)
      if contents != null then
        contents.variant
      else
        FluidVariant.blank()

    override def getAmount(currentVariant: ItemVariant): Long =
      val contents = currentVariant.get(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS)
      if contents != null then
        contents.amount
      else
        0






