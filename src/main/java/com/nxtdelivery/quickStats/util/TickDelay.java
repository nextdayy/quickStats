package com.nxtdelivery.quickStats.util;

import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

public class TickDelay {
	Integer delay;
	Runnable funct;

	public TickDelay(Runnable functionName, int ticks) {
		regist();
		delay = ticks;
		funct = functionName;
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			// Delay expired
			if (delay < 1) {
				run();
				destroy();
			}
			delay--;
		}
	}

	@EventHandler()
	private void destroy() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@EventHandler()
	private void regist() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void run() {
		funct.run();
	}
}
