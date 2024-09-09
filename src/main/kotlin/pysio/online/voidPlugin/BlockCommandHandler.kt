import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field

class BlockCommandHandler(private val plugin: JavaPlugin) {

    init {
        registerCommand("deleteblock", DeleteBlockCommand(plugin))
        registerCommand("copyblock", CopyBlockCommand(plugin))
    }

    private fun registerCommand(name: String, command: Command) {
        try {
            val commandMapField: Field = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
            commandMapField.isAccessible = true
            val commandMap = commandMapField.get(Bukkit.getServer()) as CommandMap
            commandMap.register(plugin.description.name, command)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class DeleteBlockCommand(private val plugin: JavaPlugin) : BukkitCommand("deleteblock") {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("该命令只能由玩家执行。")
            return true
        }

        val player = sender

        if (!player.isOp) {
            player.sendMessage("你没有权限执行此命令。")
            return true
        }

        val playerLocation = player.location
        val playerChunk = playerLocation.chunk

        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0 until player.world.maxHeight) {
                    val block = playerChunk.getBlock(x, y, z)
                    block.type = Material.AIR
                }
            }
        }
        player.sendMessage("已删除你脚下的区块。")
        return true
    }
}

class CopyBlockCommand(private val plugin: JavaPlugin) : BukkitCommand("copyblock") {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("该命令只能由玩家执行。")
            return true
        }

        val player = sender

        if (!player.isOp) {
            player.sendMessage("你没有权限执行此命令。")
            return true
        }

        val playerLocation = player.location
        val playerChunk = playerLocation.chunk

        val mainWorld = plugin.server.getWorld("world")
        if (mainWorld == null) {
            player.sendMessage("主世界未正确加载。")
            return true
        }

        val mainWorldChunk = mainWorld.getChunkAt(playerLocation)
        if (!mainWorldChunk.isLoaded) {
            mainWorldChunk.load()
        }

        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0 until mainWorld.maxHeight) {
                    val block = mainWorldChunk.getBlock(x, y, z)
                    val targetBlock = playerChunk.getBlock(x, y, z)
                    targetBlock.type = block.type
                }
            }
        }
        player.sendMessage("已从主世界拷贝区块到你脚下。")
        return true
    }
}