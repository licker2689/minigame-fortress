package com.github.monun.fortress

import com.github.monun.tap.fake.FakeProjectile
import com.github.monun.tap.fake.Movement
import com.github.monun.tap.fake.Trail
import com.github.monun.tap.math.normalizeAndLength
import org.bukkit.FluidCollisionMode
import java.lang.ref.WeakReference

class MissileProjectile(
    missile: Missile,
    maxTicks: Int, range: Double
) : FakeProjectile(maxTicks, range) {
    private val missileRef = WeakReference(missile)
    private val missile
        get() = requireNotNull(missileRef.get())

    override fun onPreUpdate() {
        // 공기저항과 중력 적용
        velocity = velocity.multiply(0.99).also { it.y -= 0.02 }
    }

    override fun onMove(movement: Movement) {
        missile.onMove(movement)
    }

    override fun onTrail(trail: Trail) {
        // 근처를 지나가는 미사일을 계산하기 위한 라인
        missile.onTrail(trail)

        // 블록과 엔티티 충돌 계산
        val velocity = trail.velocity ?: return
        val from = trail.from
        val world = from.world
        val length = velocity.normalizeAndLength()

        if (world != null) {
            world.rayTrace(
                from,
                velocity,
                length,
                FluidCollisionMode.NEVER,
                true,
                0.75,
                missile.shooter.enemySelector
            )?.let {
                // 충돌시
                missile.onHit(it)
                remove()
                return
            }
        }

        // 배기 시각 효과 재생
        missile.playExhaustEffect(trail)
    }

    override fun onRemove() {
        missile.destroy()
        missile.manager.removeMissile(missile)
    }
}