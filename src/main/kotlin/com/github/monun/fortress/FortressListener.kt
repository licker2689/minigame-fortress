package com.github.monun.fortress

import org.bukkit.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BlockIterator
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextInt


class FortressListener(
    private val manager: FortressManager
) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        manager.join(event.player)

        val player = event.player
        val team = Bukkit.getScoreboardManager().mainScoreboard.getEntryTeam(player.name)

        if (team == null || team.color == ChatColor.RESET) {
            player.gameMode = GameMode.SPECTATOR
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        manager.quit(event.player)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val player = event.player

        if (player.isSneaking || player.gameMode == GameMode.SPECTATOR) return

        if (event.action == Action.RIGHT_CLICK_AIR) {
            if (item.isSimilar(Missile.dragonHeadItemStack)) {
                //미사일 발사

                event.isCancelled = true

                val missile = MissileDragonHead()
                player.fortress().addMissile(missile, player.eyeLocation.apply {
                    add(direction.multiply(2.0))
                })
                item.amount--
            } else if (item.type == Material.COBBLESTONE) {
                // 브릿지 설치
                event.isCancelled = true

                val loc = player.location.apply { y -= 0.001; pitch = 0F }
                if (loc.block.type.isAir) return
                val iterator = BlockIterator(loc, 0.0, 8)

                while (iterator.hasNext()) {
                    val block = iterator.next()

                    if (block.type.isAir) {
                        block.type = Material.COBBLESTONE
                        loc.world.playSound(
                            block.location.add(0.5, 0.5, 0.5),
                            Sound.BLOCK_STONE_PLACE,
                            1.0F, 1.0F
                        )
                        item.amount--
                        break
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        if (event.player.gameMode == GameMode.SPECTATOR) return

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
        // 돌 블록을 부술때 보너스

        val block = event.block
        val player = event.player

        if (block.type == Material.STONE
            && player.gameMode.let { it == GameMode.SURVIVAL || it == GameMode.ADVENTURE }
            && block.getDrops(player.inventory.itemInMainHand, player).isNotEmpty()
        ) {
            event.isDropItems = false

            val loc = block.location.add(0.5, 0.8, 0.5)
            val count = max(1, sqrt(nextInt(64).toDouble()).toInt())

            for (i in 0 until count) {
                val item = if (nextInt(16) == 0) {
                    ItemStack(Material.COAL)
                } else {
                    ItemStack(Material.COBBLESTONE)
                }

                loc.world.dropItem(loc, item).apply {
                    pickupDelay -= i * 2
                }
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