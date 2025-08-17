package Dev.InstanceFarm;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.holder.RewardHolder;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.ConnectionPool;
import net.sf.l2j.commons.concurrent.ThreadPool;

public class TimeInstanceManager
{
	protected static final Logger LOGGER = Logger.getLogger(TimeInstanceManager.class.getName());
	
	private TimeInstanceTask _task;
	
	protected TimeInstanceManager()
	{
		if (Config.TIME_INSTANCE_ENABLED)
		{
			SheduleTimeInstanceTask(true);
			
			LOGGER.info("Time Instance Manager: Loaded");
		}
		else
			LOGGER.info("Time Instance Manager: Disabled");
	}
	
	public static TimeInstanceManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public static int getPlayerTime(Player player)
	{
		long playerTime = Cd(player.getObjectId());
		
		if (playerTime > System.currentTimeMillis())
		{
			int time = (int) Math.round((playerTime - System.currentTimeMillis()) / 1000.0);
			return time / 60;
		}
		
		return 0;
	}
	
	public static void onLogin(Player player)
	{
		if (checkIfIPorPlayerExistInDB(player.getObjectId()))
		{
			if (Cd(player.getObjectId()) > System.currentTimeMillis())
			{
				player.setTimeInstanceAvaiable(true);
				return;
			}
			
			player.setTimeInstanceAvaiable(false);
			return;
		}
		
		insertNewParentOfPlayer(player.getObjectId(), 0);
		player.setTimeInstanceAvaiable(false);
	}
	
	public static boolean checkPlayerTime(Player player)
	{
	//	if (player.isGM())
	//		return true;
		
		if (Cd(player.getObjectId()) > System.currentTimeMillis())
		{
			player.setTimeInstanceAvaiable(true);
			return true;
		}
		
		player.setTimeInstanceAvaiable(false);
		return false;
	}
	
//	public static void broadcastTimer(Player player)
//	{
//		if (player.isInsideZone(ZoneId.TIME_FARM))
//		{
//			long playerTime = Cd(player.getObjectId());
//			int secondsLeft = (int) Math.round((playerTime - System.currentTimeMillis()) / 1000.0);
//			int minutes = secondsLeft/60;
//			int seconds = secondsLeft%60;
//			ExShowScreenMessage packet = new ExShowScreenMessage("Time Left: " + String.format("%02d:%02d", minutes, seconds), 1010, SMPOS.MIDDLE_RIGHT, false);
//			player.sendPacket(packet);
//		}
//	}
	public static void broadcastTimer(Player player)
	{
		if (player.isInsideZone(ZoneId.TIME_FARM))
		{
			long playerTime = Cd(player.getObjectId()); // Certifique-se de que este método existe e retorna um long
			int secondsLeft = Math.max(0, (int) Math.round((playerTime - System.currentTimeMillis()) / 1000.0));
			int minutes = secondsLeft / 60;
			int seconds = secondsLeft % 60;
			
			ExShowScreenMessage packet = new ExShowScreenMessage("Time Left: " + String.format("%02d:%02d", minutes, seconds), 1010, SMPOS.MIDDLE_RIGHT, false);
			player.sendPacket(packet);
		}
	}

	
	public static void updatePlayerTime(Player player)
	{
		Calendar time = Calendar.getInstance();
		time.add(Calendar.MINUTE, Config.TIME_INSTANCE_PLAYER_TIME > 0 ? Config.TIME_INSTANCE_PLAYER_TIME : 30);
		
		updateTime(player.getObjectId(), time.getTimeInMillis());
		
		player.setTimeInstanceAvaiable(true);
	}
	
	public static void checkAndRewardPlayer(Player player)
	{
		if (player.getTimeInstanceMobs() >= Config.TIME_INSTANCE_MOBS_TO_REWARD)
		{
			player.setTimeInstanceMobs(0);
			
			RewardHolder reward = Config.TIME_INSTANCE_DROP_ITEMS_IDS.get(Rnd.get(Config.TIME_INSTANCE_DROP_ITEMS_IDS.size()));
			
			if (Rnd.get(100) <= reward.getRewardChance())
				player.addItem("TI", reward.getRewardId(), Rnd.get(reward.getRewardMin(), reward.getRewardMax()), player, true);
		}
	}
	
	private static void updateTime(int objectId, long time)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE time_instance_manager SET expire_time=? WHERE objectId=?");
			statement.setLong(1, time);
			statement.setLong(2, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static Long Cd(int objectId)
	{
		long CdMs = 0;
		
		PreparedStatement statement = null;
		try (Connection con = ConnectionPool.getConnection())
		{
			statement = con.prepareStatement("SELECT expire_time FROM time_instance_manager WHERE objectId=?");
			statement.setLong(1, objectId);
			
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
				CdMs = rset.getLong("expire_time");
			
			rset.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return CdMs;
	}
	
	private static boolean checkIfIPorPlayerExistInDB(int objectId)
	{
		boolean flag = false;
		PreparedStatement statement = null;
		
		try (Connection con = ConnectionPool.getConnection())
		{
			statement = con.prepareStatement("SELECT * FROM time_instance_manager WHERE objectId=?");
			statement.setLong(1, objectId);
			
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
				flag = true;
			
			rset.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return flag;
	}
	
	private static void insertNewParentOfPlayer(int objectId, long time)
	{
		PreparedStatement statement = null;
		try (Connection con = ConnectionPool.getConnection())
		{
			statement = con.prepareStatement("INSERT INTO time_instance_manager (objectId,expire_time) VALUES (?,?)");
			statement.setLong(1, objectId);
			statement.setLong(2, time);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	public static void waitSecs(int i)
//	{
//		try
//		{
//			Thread.sleep(i * 1000);
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//	}
	public static void waitSecs(int i)
	{
	    try
	    {
	        Thread.sleep(i * 1000);
	    }
	    catch (InterruptedException e)
	    {
	        // Restaurando o estado de interrupção
	        Thread.currentThread().interrupt();
	        // Log para entender o que ocorreu
	   //     System.err.println("Thread was interrupted during sleep.");
	    }
	}

	public void SheduleTimeInstanceTask(boolean init)
	{
		if (!init)
			waitSecs(60);
		
		_task = new TimeInstanceTask();
		ThreadPool.execute(_task);
	}
	
	class TimeInstanceTask implements Runnable
	{
		@Override
		public void run()
		{
			for (Player player : World.getInstance().getPlayers())
			{
				if (player.isInsideZone(ZoneId.TIME_FARM))
				{
					if (!checkPlayerTime(player))
					{
						player.setTimeInstanceAvaiable(false);
						player.setIsInTimeInstance(false);
						player.sendMessage("Your time in the Time Instance Zone has expired.");
						
						if (Config.TIME_INSTANCE_SCREEN_MESSAGE)
							player.sendPacket(new ExShowScreenMessage("Your time in the Time Instance Zone has expired.", 6000));
						
						player.startAbnormalEffect(0x0800);
						player.setIsParalyzed(true);
						player.startParalyze();				
						player.broadcastPacket(new StopMove(player));
						
						new TimeInstanceTeleportTaskManager(player);
					}
				}
			}
			
			SheduleTimeInstanceTask(false);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final TimeInstanceManager _instance = new TimeInstanceManager();
	}
}