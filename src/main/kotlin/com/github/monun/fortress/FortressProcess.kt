package com.github.monun.fortress

import com.github.monun.fortress.plugin.FortressPlugin
import com.github.monun.tap.fake.FakeEntityServer
import com.github.monun.tap.fake.FakeProjectileManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.scheduler.BukkitTask
import java.util.*

class FortressProcess(
    plugin: FortressPlugin
) {
    val players = HashMap<UUID, FortressPlayer>()
    val onlinePlayers = IdentityHashMap<Player, FortressPlayer>()

    private val fakeEntityServer: FakeEntityServer
    private val fakeProjectileManager: FakeProjectileManager

    val listener: FortressListener
    val task: BukkitTask

    init {
        val server = plugin.server

        // 게임모드에 의한 참가 플레이어 결정
        Bukkit.getOnlinePlayers().filter {
            val mode = it.gameMode
            mode == GameMode.ADVENTURE && mode == GameMode.SURVIVAL
        }.associateWithTo(onlinePlayers) { player -> FortressPlayer(this, player) }

        // 오프라인을 대비한 모든 플레이어 기억
        onlinePlayers.values.associateByTo(players) { it.uniqueId }

        // 모듈 초기화
        fakeEntityServer = FakeEntityServer.create(plugin)
        fakeProjectileManager = FakeProjectileManager()

        // 어댑터 설정
        listener = FortressListener(this)
        task = server.scheduler.runTaskTimer(plugin, this::update, 0L, 1L)
    }

    private fun update() {
        onlinePlayers.values.forEach { it.update() }
        fakeProjectileManager.update()
        fakeEntityServer.update()
    }

    fun shutdown() {
        fakeProjectileManager.clear()
        fakeEntityServer.shutdown()

        HandlerList.unregisterAll(listener)
        task.cancel()
    }
}