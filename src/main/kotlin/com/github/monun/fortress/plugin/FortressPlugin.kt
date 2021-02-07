package com.github.monun.fortress.plugin

import com.github.monun.fortress.Fortress
import com.github.monun.fortress.FortressManager
import com.github.monun.fortress.Missile
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Monun
 */
class FortressPlugin : JavaPlugin() {
    override fun onEnable() {
        Bukkit.getWorlds().forEach { world ->
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        }

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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        ((sender as Player).inventory).addItem(Missile.dragonHeadItemStack.clone())

        return true
    }
}

