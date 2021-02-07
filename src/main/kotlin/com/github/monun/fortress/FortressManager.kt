package com.github.monun.fortress

import com.github.monun.fortress.plugin.FortressPlugin
import com.github.monun.tap.fake.FakeEntityServer
import com.github.monun.tap.fake.FakeProjectileManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.scheduler.BukkitTask
import java.util.*

class FortressManager(
    plugin: FortressPlugin
) {
    val players = HashMap<UUID, FortressPlayer>()
    val onlinePlayers = IdentityHashMap<Player, FortressPlayer>()

    private val fakeEntityServer: FakeEntityServer
    private val fakeProjectileManager: FakeProjectileManager

    val listener: FortressListener
    val task: BukkitTask

    private val missiles = arrayListOf<Missile>()

    init {
        val server = plugin.server

        // 모듈 초기화
        fakeEntityServer = FakeEntityServer.create(plugin)
        fakeProjectileManager = FakeProjectileManager()

        // 어댑터 설정
        listener = FortressListener(this).also {
            server.pluginManager.registerEvents(it, plugin)
        }
        task = server.scheduler.runTaskTimer(plugin, this::update, 0L, 1L)

        Bukkit.getOnlinePlayers().forEach(this::join)
    }

    private fun update() {
        onlinePlayers.values.forEach { it.update() }
        fakeProjectileManager.update()








        fakeEntityServer.update()


    }

    //fun main() {
//    val r1 = Vector(2, 6, -9)
//    val r2 = Vector(-1, -2, 3)
//    val e1 = Vector(3, 4, -4)
//    val e2 = Vector(2, -6, 1)
//
//    val n = e1.getCrossProduct(e2)
//
//    val distance = n.clone().dot(r1.clone().subtract(r2)) / n.length()
//
//    println(distance)
//}

    fun join(player: Player) {
        players.computeIfAbsent(player.uniqueId) { FortressPlayer(this, player) }.also {
            onlinePlayers[player] = it
        }

        fakeEntityServer.addPlayer(player)
    }

    fun quit(player: Player) {
        onlinePlayers.remove(player)
        fakeEntityServer.removePlayer(player)
    }

    internal fun shutdown() {
        missiles.clear()
        fakeProjectileManager.clear()
        fakeEntityServer.shutdown()

        HandlerList.unregisterAll(listener)
        task.cancel()
    }

    fun prepLaunch(missile: Missile, loc: Location) {
        missile.manager = this
        missile.launchLocation = loc
        missile.fakeEntity = fakeEntityServer.spawnEntity(loc, ArmorStand::class.java)
        missile.initFakeEntity(missile.fakeEntity)
        missile.updateFakeEntityLocation(loc)
    }

    fun launch(missile: Missile, power: Double, shooter: FortressPlayer) {
        val projectile = MissileProjectile(missile, 400, 256.0)
        missile.missileProjectile = projectile
        missile.shooter = shooter

        fakeProjectileManager.launch(missile.launchLocation, projectile)
        missiles.add(missile)
        projectile.velocity = missile.launchLocation.direction.multiply(power)
    }

    fun removeMissile(missile: MissileDragonHead) {
        missiles.remove(missile)
    }
}