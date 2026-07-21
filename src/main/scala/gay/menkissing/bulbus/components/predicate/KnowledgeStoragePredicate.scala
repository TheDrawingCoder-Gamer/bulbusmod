package gay.menkissing.bulbus.components.predicate

import net.minecraft.advancements.criterion.MinMaxBounds
import net.minecraft.core.component.predicates.DataComponentPredicate
import net.minecraft.core.component.DataComponentGetter
import gay.menkissing.bulbus.registries.BulbusDataComponentTypes
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gay.menkissing.bulbus.content.item.KnowledgeStorageItem

final case class KnowledgeStoragePredicate
  (fill: MinMaxBounds.Doubles)
    extends DataComponentPredicate:
  override def matches(components: DataComponentGetter): Boolean =
    val comp: Long | Null =
      components.get(BulbusDataComponentTypes.KNOWLEDGE_STORAGE_CONTENTS)
    if comp == null then false
    else if comp > 1_000_000 then true
    else
      val max = KnowledgeStorageItem.getMaxEvil(components)
      val percent = comp.toDouble / max.toDouble
      fill.matches(percent)

object KnowledgeStoragePredicate:
  val CODEC: Codec[KnowledgeStoragePredicate] =
    RecordCodecBuilder.create: inst =>
      inst.group(
        MinMaxBounds.Doubles.CODEC
          .optionalFieldOf("fill", MinMaxBounds.Doubles.ANY).forGetter(_.fill)
      ).apply(inst, KnowledgeStoragePredicate.apply)
