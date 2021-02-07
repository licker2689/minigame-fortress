package com.github.monun.fortress

import com.github.monun.fortress.plugin.FortressPlugin
import com.github.monun.tap.fake.FakeEntityServer
import com.github.monun.tap.fake.FakeProjectileManager
import com.github.monun.tap.math.normalizeAndLength
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.HashMap

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
            val iterator = missiles.iterator()

            //fast skip
            while (true) {
                if (iterator.next() === missile) break
            }

            val lastTrail = missile.lastTrail!!
            val from = lastTrail.from.toVector()
            val vector = lastTrail.velocity!!
            val length = vector.normalizeAndLength()


            while (iterator.hasNext()) {
                val other = iterator.next()
                if (missile in destroyed && other in destroyed) continue

                val box = BoundingBox.of(other.lastTrail!!.from, 3.0, 3.0, 3.0)
                box.rayTrace(from, vector, length)?.run {
                    destroyed.add(missile)
                    destroyed.add(other)
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