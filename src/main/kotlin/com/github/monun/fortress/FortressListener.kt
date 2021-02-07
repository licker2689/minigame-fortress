package com.github.monun.fortress

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextInt

private val Action.isRightClick: Boolean
    get() {
        return this == Action.RIGHT_CLICK_AIR || this == Action.RIGHT_CLICK_BLOCK
    }

class FortressListener(
    private val manager: FortressManager
) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        manager.join(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        manager.quit(event.player)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val player = event.player

        if (player.isSneaking) return

        if (event.action.isRightClick && item.isSimilar(Missile.dragonHeadItemStack)) {
            event.isCancelled = true

            val missile = MissileDragonHead()
            player.fortress().addMissile(missile, player.eyeLocation.apply {
                add(direction.multiply(2.0))
            })
            item.amount--
        }
    }

    @EventHandler
    fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        if (event.isSneaking) {
            event.player.fortress().prepareLaunch()
        } else {
            event.player.fortress().launch()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onMergeItem(event: ItemMergeEvent) {
        if (event.entity.itemStack.type == Material.COBBLESTONE) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block

        if (block.type == Material.STONE && event.isDropItems) {
            event.isDropItems = false

            val loc = block.location
            val count = max(1, sqrt(nextInt(64).toDouble()).toInt())

            for (i in 0 until count) {
                loc.world.dropItemNaturally(loc, ItemStack(Material.COBBLESTONE, 1))
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockForm(event: BlockFormEvent) {
        val state = event.newState

        if (state.type == Material.COBBLESTONE) {
            state.type = Material.STONE
        }
    }
}