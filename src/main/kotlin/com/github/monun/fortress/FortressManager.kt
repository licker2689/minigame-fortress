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
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

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

        val destroyed = hashSetOf<Missile>()
        val missiles = this.missiles.filter { it.lastTrail != null }

        // 미사일간 경로 거리재기
        for (missile in missiles) {
            for (other in missiles) {
                if (missile === other) continue
                if (missile in destroyed && other in destroyed) continue

                val trail = missile.lastTrail!!
                val otherTrail = other.lastTrail!!


                val a1 = trail.from.toVector()
                val a2 = otherTrail.from.toVector()
                val b1 = trail.to.toVector()
                val b2 = otherTrail.to.toVector()
                val midpoint = Vector().add(a1).add(a2).add(b1).add(b2).multiply(1.0 / 4.0)

                val distance = midpoint.distanceFromLine(a1, b1) + midpoint.distanceFromLine(a2, b2)

                if (distance < 2.0) {
                    destroyed.add(missile)
                }
            }
        }


        this.missiles.removeAll(destroyed)
        for (missile in destroyed) {
            missile.detonate(missile.lastTrail!!.to.toVector())
            missile.destroy()
        }

        fakeEntityServer.update()
    }

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

    fun removeMissile(missile: Missile) {
        missiles.remove(missile)
    }
}

fun Vector.distanceFromLine(from: Vector, to: Vector): Double {
    return  clone().subtract(from).getCrossProduct(clone().subtract(to)).length() / to.clone().subtract(from).length()
}