package gay.menkissing.bulbus.content.block.entity.stasis_storage

/**
 * Every tick, this transfers between storages. This lets you implement push and pull based systems.
 * @tparam World The storage type of the stasis storage
 * @tparam GenericWorld The generic storage type
 */
trait ForwardingTransferer[-World, -GenericWorld]:
  def transfer(self: World, that: GenericWorld): Unit

object ForwardingTransferer:
  val passive: ForwardingTransferer[Any, Any] = (_, _) => ()