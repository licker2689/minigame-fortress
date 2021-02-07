package com.github.monun.fortress

import com.google.common.base.Predicate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import kotlin.math.min

class FortressPlayer(
    private val manager: FortressManager,
    player: Player
) {
    var player: Player? = player
        set(value) {
            field = value

            if (value != null) {
                missileBar.addPlayer(value)
                missileBar.isVisible = false
            } else {
                missileBar.removeAll()
            }
        }
    val uniqueId = player.uniqueId
    val name: String = player.name

    val enemySelector: Predicate<Entity>
        get() {
            val player = player ?: error("Null player")
            val team =
                Bukkit.getScoreboardManager().mainScoreboard.getEntryTeam(name) ?: return Predicate { it !== player }

            return Predicate {
                if (it == null || it === player) false
                else team.hasEntry(it.name) || team.hasEntry(it.uniqueId.toString())
            }
        }

    private val launchers = arrayListOf<Missile>()

    private val missileBar = Bukkit.createBossBar(null, BarColor.RED, BarStyle.SEGMENTED_10).apply {
        isVisible = false
    }

    private var launchPower = 0

    init {
        missileBar.addPlayer(player)
    }

    fun update() {
        val launch = missileBar.isVisible

        // 시프트 누르는중
        if (launch) {
            launchPower = (launchPower + 1).coerceIn(0..maxLaunchPower)
            updateMissileBar()
        }

        for (launcher in launchers) {
            launcher.playPrepareEffect(launch)
            launcher.playPrepareSound(launch)
        }
    }

    // 미사일 추가
    fun addMissile(missile: Missile, loc: Location) {
        manager.prepLaunch(missile, loc)
        launchers.add(missile)
    }

    // 웅크려서 발사 준비
    fun prepareLaunch() {
        if (launchers.isEmpty()) return

        launchPower = 0
        updateMissileBar()
        missileBar.isVisible = true
    }

    companion object {
        const val maxLaunchPower = 40
    }

    private fun updateMissileBar() {
        missileBar.progress = (launchPower.toDouble() / maxLaunchPower).coerceIn(0.0, 1.0)
    }

    fun launch() {
        missileBar.isVisible = false
        updateMissileBar()

        val power = launchPower.toDouble() / maxLaunchPower

        for (missile in launchers) {
            manager.launch(missile, power * 3.0, this)
        }
        launchers.clear()
    }
}