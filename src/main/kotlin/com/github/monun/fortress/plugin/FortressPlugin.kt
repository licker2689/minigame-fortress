package com.github.monun.fortress.plugin

import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Monun
 */
class FortressPlugin : JavaPlugin() {
    override fun onEnable() {

    }
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