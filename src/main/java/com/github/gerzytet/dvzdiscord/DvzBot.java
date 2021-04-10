package com.github.gerzytet.dvzdiscord;

import java.util.logging.Logger;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;

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
	private Role pingRole;
	
	@SuppressWarnings("unused")
	private DvzBot() {}
	
	/**
	 * Create the bot and log in.
     * Blocks until the bot is connected
     * 
     * @param token the bot token
	 * @param guildID guild id of the bot
	 * @param channelID channel id to print messages to
	 * @param pingRoleID The id of the role to ping.
	 */
	@SuppressWarnings("deprecation")
	public DvzBot(String token, long guildID, long channelID, long pingRoleID) {
		try {
			bot = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES)
					.setActivity(Activity.playing("DvZ at pvp.lihp.us"))
					.setAutoReconnect(true)
					.addEventListeners(new ReadyListener())
					.build();
			bot.awaitReady();
			
			Guild guild = bot.getGuildById(guildID);
			channel = guild.getTextChannelById(channelID);
			pingRole = guild.getRoleById(pingRoleID);
			
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
	
	//private static char[] clocks;
	
	/*
	static {
		clocks = new char[12];
		//one o'clock
		char clock = (char) 128336;
		for (int i = 0; i < 12; i++) {
			clocks[i] = clock;
			clock++;
		}
	}*/
	
	private static String clockFromDouble(double d) {
		return ":clock" + Math.max(Math.round(d * 12), 1) + ":";
	}
	
	private static String TROPHY = ":trophy:";
	private static String CHECK = ":white_check_mark:";
	private static String SWORD = ":crossed_swords:";
	private static String MOON = ":crescent_moon:";
	private static String STOP = ":octagonal_sign:";
	
	public void send(String message) {
		channel.sendMessage(message).queue();
	}
	
	public void sendWithPing(String message) {
		if (pingRole == null) {
			send(message);
		} else {
			send(message + " " + pingRole.getAsMention());
		}
	}
	
	/**
	 * Announce game start
	 * 
	 * @param players players online
	 */
	public void start(int players) {
		send(CHECK + " DvZ started with " + players + " players");
	}
	
	private String formatPlayers(int dwarves, int monsters, int assassins) {
		return dwarves + " dwarves, " + monsters + " monsters, and " + assassins + " assassins";
	}
	
	private String formatMinutesSeconds(int minutes, int seconds) {
		return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
	}
	
	/**
	 * Sends a status report during the daytime phase
	 * 
	 * @param dwarves number of dwarves online
	 * @param secondsUntilNight seconds until monsters are released
	 * @param totalDaytimeSeconds the total seconds that daytime lasts
	 * @param ping true if the bot should ping the ping role for this message
	 */
	public void daytimeStatus(int dwarves, int secondsUntilNight, int totalDaytimeSeconds, boolean ping) {
		int minutes = secondsUntilNight / 60;
		int seconds = secondsUntilNight % 60;
		String message = clockFromDouble(1 - ((double) secondsUntilNight / totalDaytimeSeconds)) + " " + 
			     dwarves + " dwarves online.  " + formatMinutesSeconds(minutes, seconds) + " until the monsters are released.";
		if (ping) {
			sendWithPing(message);
		} else {
			send(message);
		}
	}
	
	/**
	 * Announce monster release
	 */
	public void nightfall() {
		send(MOON + " The monsters were released!");
	}
	
	/**
	 * Sends a status report during the nighttime phase
	 * 
	 * @param dwarves number of dwarves
	 * @param monsters number of monsters
	 * @param assassins number of assassins
	 * @param secondsUntilDwarvesWin seconds until dwarves win
	 * @param totalNighttimeSeconds total number of seconds
	 * @param ping true if the bot should ping the ping role for this message
	 */
	public void nighttimeStatus(int dwarves, int monsters, int assassins, int secondsUntilDwarvesWin, int totalNighttimeSeconds, boolean ping) {
		int minutes = secondsUntilDwarvesWin / 60;
		int seconds = secondsUntilDwarvesWin % 60;
		String message = clockFromDouble(1 - ((double) secondsUntilDwarvesWin / totalNighttimeSeconds)) + " " +
		         formatPlayers(dwarves, monsters, assassins) + " online.  " + formatMinutesSeconds(minutes, seconds) + " until dwarves win.";
		if (ping) {
			sendWithPing(message);
		} else {
			send(message);
		}
	}
	
	/**
	 * Announce shrine change.
	 * 
	 * @param shrine The new shrine.  either 2 or 3.
	 */
	public void changeShrine(int shrine) {
		String x = "";
		if (shrine == 2) {
			x = "first";
		} else if (shrine == 3) {
			x = "second";
		}
		send(SWORD + " The monsters conquered the " + x + " shrine");
	}
	
	/**
	 * Announce game end
	 * 
	 * @param reason reason for game end
	 */
	public void endGame(EndReason reason) {
		String msg = "";
		switch (reason) {
		case MONSTERS_WON:
			msg = TROPHY + " The monsters won!";
			break;
		case DWARVES_WON:
			msg = TROPHY + " The dwarves won!";
			break;
		case SERVER_RESET:
			msg = STOP + " The server stopped.";
			break;
		default:
			break;
			
		}
		send(msg);
	}
	
	public static enum EndReason {
		MONSTERS_WON, DWARVES_WON, SERVER_RESET
	}
}
