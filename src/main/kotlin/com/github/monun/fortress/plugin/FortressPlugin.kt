package com.github.monun.fortress.plugin

import com.github.monun.fortress.Fortress
import com.github.monun.fortress.FortressManager
import com.github.monun.fortress.Missile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Monun
 */
class FortressPlugin : JavaPlugin() {
    override fun onEnable() {
        val fortressManager = FortressManager(this)
        Fortress.fortressManager = fortressManager

        registerRecipe()
    }

    private fun registerRecipe() {
        val key = NamespacedKey(this,  "dragon_missile")
        val recipe = ShapedRecipe(key, Missile.dragonHeadItemStack).apply {
            shape(
                " S ",
                "CIC",
                "CLC"
            )
            setIngredient('S', Material.STONE)
            setIngredient('C', Material.COBBLESTONE)
            setIngredient('I', Material.STICK)
            setIngredient('L', Material.COAL)
        }

        Bukkit.addRecipe(recipe)
    }

    override fun onDisable() {
        Fortress.fortressManager.shutdown()
    }
}

