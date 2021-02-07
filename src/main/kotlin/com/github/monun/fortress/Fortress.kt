package com.github.monun.fortress

import org.bukkit.entity.Player

object Fortress {
    lateinit var fortressManager: FortressManager
}

fun Player.fortress() : FortressPlayer {
    return requireNotNull(Fortress.fortressManager.onlinePlayers[this]) {
        "Unregistered fortress player $name@${uniqueId}"
    }
}