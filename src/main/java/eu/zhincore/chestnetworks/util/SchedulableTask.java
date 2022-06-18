package eu.zhincore.chestnetworks.util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import eu.zhincore.chestnetworks.ChestNetworksPlugin;

public class SchedulableTask {
  public ChestNetworksPlugin plugin;
  private BukkitTask task;
  public Runnable runner;
  public boolean sync = false;
  public int delay;

  public SchedulableTask(ChestNetworksPlugin plugin, Runnable runner) {
    this(plugin, runner, 1);
  }

  public SchedulableTask(ChestNetworksPlugin plugin, Runnable runner, boolean sync, int delay) {
    this(plugin, runner, delay);
    this.sync = sync;
  }

  public SchedulableTask(ChestNetworksPlugin plugin, Runnable runner, boolean sync) {
    this(plugin, runner);
    this.sync = sync;
  }

  public SchedulableTask(ChestNetworksPlugin plugin, Runnable runner, int delay) {
    this.plugin = plugin;
    this.runner = runner;
    this.delay = delay;
  }

  public void schedule() {
    cancel();
    if (sync) {
      task = Bukkit.getScheduler().runTaskLater(plugin, runner, delay);
    } else {
      task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runner, delay);
    }
  }

  public void cancel() {
    if (task != null) task.cancel();
  }
}
