package com.github.monun.fortress

import com.github.monun.tap.fake.FakeEntity
import com.github.monun.tap.fake.Trail
import com.github.monun.tap.trail.trail
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.TNTPrimed
import org.bukkit.inventory.ItemStack
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextFloat

class MissileDragonHead : Missile() {

    override fun initFakeEntity(fakeEntity: FakeEntity) {
        fakeEntity.updateMetadata<ArmorStand> {
            isInvisible = true
            isMarker = true
        }
        fakeEntity.updateEquipment {
            helmet = ItemStack(Material.DRAGON_HEAD)
        }
    }

    override fun playPrepareEffect(launch: Boolean) {
        val loc = launchLocation.clone()

        loc.run {
            var wiggle = 0.2
            var dx = 0.0
            var dy = -0.5
            var dz = 0.0

            if (launch) {
                val vector = loc.direction.multiply(-1)
                dx = vector.x
                dy = vector.y
                dz = vector.z
                wiggle = 0.4

                for (i in 0 until 3) {
                    world.spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        loc.clone().add(vector),
                        0,
                        dx + nextFloat() * wiggle - wiggle / 2.0,
                        dy + nextFloat() * wiggle - wiggle / 2.0,
                        dz + nextFloat() * wiggle - wiggle / 2.0,
                        0.5, null, true
                    )
                }
            }

            for (i in 0 until 10) {
                world.spawnParticle(
                    Particle.FLAME,
                    loc,
                    0,
                    dx + nextFloat() * wiggle - wiggle / 2.0,
                    dy + nextFloat() * wiggle - wiggle / 2.0,
                    dz + nextFloat() * wiggle - wiggle / 2.0,
                    1.0,
                    null,
                    true
                )
            }
        }
    }

    override fun playPrepareSound(launch: Boolean) {
        if (launch) {
            launchLocation.clone().run {
                world.playSound(
                    this,
                    Sound.ENTITY_GENERIC_EXPLODE,
                    0.1F,
                    2.0F
                )
            }
        }
    }

    override fun onHit(result: RayTraceResult) {
        fakeEntity.remove()
        manager.removeMissile(this)
        detonate(result.hitPosition)
    }

    override fun detonate(pos: Vector) {
        val world = missileProjectile.location.world
        pos.run {
            world.createExplosion(pos.x, pos.y, pos.z, 5.0F, false, true)
            world.spawnParticle(
                Particle.EXPLOSION_HUGE,
                pos.x, pos.y, pos.z,
                1,
                0.0, 0.0, 0.0, 1.0, null, true
            )
        }
    }

    override fun playExhaustEffect(trail: Trail) {
        trail(trail.from, trail.to, 0.3, this::playExhaustEffect)
    }

    private fun playExhaustEffect(world: World, x: Double, y: Double, z: Double) {
        world.spawnParticle(
            Particle.CLOUD,
            x, y, z,
            3,
            0.0, 0.0, 0.0,
            0.0, null, true
        )
    }

    override fun playSound(loc: Location) = loc.run {
        Bukkit.getOnlinePlayers().asSequence().filter { it.world === world }.forEach { player ->
            val playerLoc = player.eyeLocation
            val vector = loc.clone().subtract(playerLoc).toVector()
            val length = vector.length()
            if (length > 64.0) return@run

            // 거리조절
            vector.multiply(1.0 / length).multiply(length / 64.0 * 12.0)

            // 볼륨 조절
            val volume = 0.8 - (length / 64.0 * 0.8)

            playerLoc.add(vector)
            world.playSound(
                playerLoc,
                Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                volume.toFloat(),
                0.1F
            )
        }
    }
}