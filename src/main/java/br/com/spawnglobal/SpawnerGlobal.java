package br.com.spawnglobal;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class SpawnerGlobal extends JavaPlugin implements Listener {
    private int spawnCount;
    private int delayTicks;
    private int maxNearby;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        spawnCount = Math.max(1, getConfig().getInt("spawn-count", 10));
        delayTicks = Math.max(1, getConfig().getInt("delay-ticks", 400));
        maxNearby = Math.max(1, getConfig().getInt("max-nearby-entities", 20));
        getServer().getPluginManager().registerEvents(this, this);

        // Update only chunks already loaded; unloaded chunks are updated safely as they load.
        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                applyToChunk(chunk);
            }
        }
        getLogger().info("Ativo: " + spawnCount + " mobs por ciclo; intervalo de " + delayTicks + " ticks.");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        applyToChunk(event.getChunk());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Run one tick later so the placed block has become its final block state.
        new BukkitRunnable() {
            @Override public void run() {
                BlockState state = event.getBlockPlaced().getState();
                if (state instanceof CreatureSpawner spawner) {
                    configure(spawner);
                }
            }
        }.runTask(this);
    }

    private void applyToChunk(Chunk chunk) {
        for (BlockState state : chunk.getTileEntities()) {
            if (state instanceof CreatureSpawner spawner) {
                configure(spawner);
            }
        }
    }

    private void configure(CreatureSpawner spawner) {
        spawner.setSpawnCount(spawnCount);
        spawner.setMinSpawnDelay(delayTicks);
        spawner.setMaxSpawnDelay(delayTicks);
        spawner.setMaxNearbyEntities(maxNearby);
        spawner.setDelay(delayTicks);
        spawner.update(true, false);
    }
}
