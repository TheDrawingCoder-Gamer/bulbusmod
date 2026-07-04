package gay.menkissing.bulbus.screen

import gay.menkissing.bulbus.registries.BulbusScreens
import net.minecraft.world.{Container, SimpleContainer}
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.{AbstractContainerMenu, MenuType, Slot}

class StasisStorageMenu(menuType: MenuType[StasisStorageMenu], windowId: Int, override val playerInv: Inventory, override val container: Container)
  extends AbstractContainerMenu(menuType, windowId), CommonStasisStorageMenu:
  locally:
    AbstractContainerMenu.checkContainerSize(container, 9)
    container.startOpen(playerInv.player)
    
    for
      i <- 0 until 3
      j <- 0 until 3
    do
      addSlot(new CommonStasisStorageMenu.BoxSlot(container, j + i * 3, 62 + j * 18, 17 + i * 18))
    
    for
      i <- 0 until ScreenCommon.playerRows
      j <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, j + i * ScreenCommon.playerColumns + 9, 8 + j * 18, 84 + i * 18))
    
    for
      i <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, i, 8 + i * 18, 142))
    
  override def boxStart: Int = 0
  override def boxEnd: Int = 9

object StasisStorageMenu:
  def client(windowId: Int, playerInv: Inventory): StasisStorageMenu =
    new StasisStorageMenu(BulbusScreens.stasisStorageMenu, windowId, playerInv, new SimpleContainer(9))
  
  def server(windowId: Int, playerInv: Inventory, container: Container): StasisStorageMenu =
    new StasisStorageMenu(BulbusScreens.stasisStorageMenu, windowId, playerInv, container)