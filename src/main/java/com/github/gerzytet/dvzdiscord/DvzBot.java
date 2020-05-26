package com.github.gerzytet.dvzdiscord;

import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class DvzBot {
	static Logger logger;
	
	static void setLogger(Logger logger) {
		DvzBot.logger = logger;
	}
	
	private static class ReadyListener implements EventListener
	{

	    @Override
	    public void onEvent(GenericEvent event)
	    {
	        if (event instanceof ReadyEvent)
	        	logger.info("Successfully logged into discord");
	    }
	}
	
	private JDA bot;
	private MessageChannel channel;
	
	@SuppressWarnings("unused")
	private DvzBot() {}
	
    /**
     * Create the bot and log in.
     * Blocks until the bot is connected
     * 
     * @param token the bot token
     */
	@SuppressWarnings("deprecation")
	public DvzBot(String token, long guild_id, long channel_id) {
		try {
			//the docs say this constructor is deprecated
			//but the examples all use this
			//I don't know what else to do
			bot = new JDABuilder(token)
					.setActivity(Activity.playing("DvZ at pvp.lihp.us"))
					.setAutoReconnect(true)
					.addEventListeners(new ReadyListener())
					.build();
			bot.awaitReady();
			channel = bot.getGuildById(guild_id).getTextChannelById(channel_id);
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * log out of discord
	 */
	public void logoff() {
		bot.shutdown();
	}
	
	private void send(String message) {
		channel.sendMessage(message).queue();
	}
	
	/**
	 * 
	 * @param players players online
	 */
	public void start(int players) {
		send("DvZ is starting with " + players + " players");
	}
}
