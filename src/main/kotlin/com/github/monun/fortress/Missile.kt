package com.github.monun.fortress

import com.github.monun.tap.fake.FakeEntity
import com.github.monun.tap.fake.Movement
import com.github.monun.tap.fake.Trail
import com.github.monun.tap.math.toRadians
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.bukkit.util.RayTraceResult

abstract class Missile {
    companion object {
        val dragonHeadItemStack = ItemStack(Material.DRAGON_HEAD).apply {
            itemMeta = itemMeta.apply { setDisplayName("${ChatColor.RESET}드래곤 머리 미사일") }
        }
    }

    lateinit var manager: FortressManager
    lateinit var shooter: FortressPlayer
    lateinit var launchLocation: Location
    lateinit var fakeEntity: FakeEntity
    lateinit var missileProjectile: MissileProjectile
    var lastTrail: Trail? = null

    abstract fun initFakeEntity(fakeEntity: FakeEntity)

    abstract fun onHit(result: RayTraceResult)

    abstract fun playPrepareEffect(launch: Boolean)

    abstract fun playPrepareSound(launch: Boolean)

    abstract fun playExhaustEffect(trail: Trail)

    abstract fun playSound(loc: Location)

    private var soundTicks = 0

    fun onMove(movement: Movement) {
        updateFakeEntityLocation(movement.to)
        if (soundTicks++ % 2 == 0)
            playSound(movement.from)
    }

    fun onTrail(trail: Trail) {
        this.lastTrail = trail
    }

    fun updateFakeEntityLocation(loc: Location) {
        fakeEntity.moveTo(loc.clone().also { it.y -= 1.62 })
        fakeEntity.updateMetadata<ArmorStand> {
            headPose = EulerAngle(
                loc.pitch.toDouble().toRadians(), 0.0, 0.0
            )
        }
    }
}