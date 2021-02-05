package com.github.monun.fortress

import com.github.monun.tap.fake.FakeEntity
import com.github.monun.tap.fake.FakeProjectile
import com.github.monun.tap.fake.Trail
import com.github.monun.tap.math.normalizeAndLength
import org.bukkit.FluidCollisionMode
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

abstract class Missile : FakeProjectile(40, 512.0) {
    lateinit var process: FortressProcess
    lateinit var player: FortressPlayer
    lateinit var fakeEntity: FakeEntity
    lateinit var lastTrail: Trail

    override fun onPreUpdate() {
        // 중력 적용
        val gravity = Vector(0.0, 0.01, 0.0)
        velocity.add(gravity)
    }

    override fun onTrail(trail: Trail) {
        // 근처를 지나가는 미사일을 계산하기 위한 라인
        lastTrail = trail

        // 블록과 엔티티 충돌 계산
        val velocity = trail.velocity ?: return
        val from = trail.from
        val world = from.world
        val length = velocity.normalizeAndLength()

        world.rayTrace(
            from,
            velocity,
            length,
            FluidCollisionMode.NEVER,
            true,
            0.75,
            player.enemySelector
        )?.let {
            // 충돌시
            onHit(it)
            remove()
            return
        }

        // 배기 시각 효과 재생
        playExhaustEffect(trail)
    }

    abstract fun initFakeEntity(fakeEntity: FakeEntity)

    abstract fun onHit(hitResult: RayTraceResult)

    abstract fun playExhaustEffect(trail: Trail)

}