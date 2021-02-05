package com.github.monun.fortress

import com.google.common.base.Predicate
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class FortressPlayer(
    private val process: FortressProcess,
    player: Player
) {
    var player: Player? = player
    val uniqueId = player.uniqueId
    val name: String = player.name

    val enemySelector: Predicate<Entity>
        get() {
            val player = player ?: error("Null player")
            val team = Bukkit.getScoreboardManager().mainScoreboard.getEntryTeam(name) ?: return Predicate { it !== player }

            return Predicate {
                if (it == null || it === player) false
                else team.hasEntry(it.name) || team.hasEntry(it.uniqueId.toString())
            }
        }

    private val launchers = arrayListOf<Missile>()

    fun removePlayer() {
        player = null
    }

    fun update() {
        TODO()
    }
}