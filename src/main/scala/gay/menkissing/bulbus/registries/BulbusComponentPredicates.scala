package gay.menkissing.bulbus.registries

import net.minecraft.core.component.predicates.DataComponentPredicate
import gay.menkissing.bulbus.components.predicate.KnowledgeStoragePredicate
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import gay.menkissing.bulbus.BulbusMod

object BulbusComponentPredicates {
  def register[T <: DataComponentPredicate](id: String, codec: Codec[T]): DataComponentPredicate.Type[T] =
    Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, BulbusMod.locate(id), new DataComponentPredicate.ConcreteType[T](codec))

  val knowledgeStorageContents: DataComponentPredicate.Type[KnowledgeStoragePredicate] =
    register("knowledge_storage_contents", KnowledgeStoragePredicate.CODEC)

  def init(): Unit = ()
}
