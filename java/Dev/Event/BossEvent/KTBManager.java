package Dev.Event.BossEvent;

import net.sf.l2j.gameserver.util.Broadcast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

public class KTBManager
{
	protected static final Logger _log = Logger.getLogger(KTBManager.class.getName());

	private KTBStartTask _task;

	private KTBManager()
	{
		if (KTBConfig.KTB_EVENT_ENABLED)
		{
			KTBEvent.init();

			scheduleEventStart();
			_log.info("Kill The Boss Engine: is Started.");
		}
		else
			_log.info("Kill The Boss Engine: Engine is disabled.");
	}

	public static KTBManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for (String timeOfDay : KTBConfig.KTB_EVENT_INTERVAL)
			{
				// Creating a Calendar object from the specified interval value
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				// Check for the test date to be the minimum (smallest in the specified list)
				if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
					nextStartTime = testStartTime;
			}
			_task = new KTBStartTask(nextStartTime.getTimeInMillis());
			ThreadPool.execute(_task);
		}
		catch (Exception e)
		{
			_log.warning("KTBEventEngine: Error figuring out a start time. Check KTBEventInterval in config file.");
		}
	}
	
	private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

	public String getNextTime()
	{
		if (getNextEventTime().getTime() != null)
			return format.format(getNextEventTime().getTime());
		return "Erro";
	}
	
	public Calendar getNextEventTime()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for (String timeOfDay : KTBConfig.KTB_EVENT_INTERVAL)
			{
				// Creating a Calendar object from the specified interval value
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				// Check for the test date to be the minimum (smallest in the specified list)
				if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
					nextStartTime = testStartTime;
			}
			return nextStartTime;
		}
		catch (Exception e)
		{
			_log.warning("KTBEventEngine: Error figuring out a start time. Check KTBEventInterval in config file.");
			return null;
		}
	}

	public void startReg()
	{
		if (!KTBEvent.startParticipation())
		{
			Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: Event was cancelled.");
			_log.warning("KTBEventEngine: Error spawning event npc for participation.");

			scheduleEventStart();
		}
		else
		{
			Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: Joinable in " + KTBConfig.KTB_NPC_LOC_NAME + "!");

			if (KTBConfig.ALLOW_EVENT_KTB_COMMANDS)
				Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: Command: .ktbjoin / .ktbleave / .ktbinfo");

			// schedule registration end
			_task.setStartTime(System.currentTimeMillis() + KTBConfig.KTB_EVENT_PARTICIPATION_TIME);
			ThreadPool.execute(_task);
		}
	}

	public void startEvent()
	{
		if (!KTBEvent.startFight())
		{
			Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: Event cancelled due to lack of Participation.");
			_log.info("KTBEventEngine: Lack of registration, abort event.");

			scheduleEventStart();
		}
		else
		{
			KTBEvent.sysMsgToAllParticipants("Teleporting in " + KTBConfig.KTB_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");
			_task.setStartTime(System.currentTimeMillis() + 60000 * KTBConfig.KTB_EVENT_RUNNING_TIME);
			ThreadPool.execute(_task);
		}
	}
	
	public void raidKilled()
	{
		Broadcast.gameAnnounceToOnlinePlayers(KTBEvent.calculateRewards());
		KTBEvent.sysMsgToAllParticipants("Teleporting back town in " + KTBConfig.KTB_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");
		KTBEvent.stopFight();
		
		if (_task.nextRun != null)
			_task.nextRun.cancel(true);
		
		scheduleEventStart();
	}

	public void endEvent()
	{
		Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: You all failed against the raid boss.");
		KTBEvent.sysMsgToAllParticipants("Teleporting back town in " + KTBConfig.KTB_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");
		KTBEvent.stopFight();

		scheduleEventStart();
	}

	public void skipDelay()
	{
		if (_task.nextRun.cancel(false))
		{
			_task.setStartTime(System.currentTimeMillis());
			ThreadPool.execute(_task);
		}
	}	

	class KTBStartTask implements Runnable
	{
		private long _startTime;
		public ScheduledFuture<?> nextRun;

		public KTBStartTask(long startTime)
		{
			_startTime = startTime;
		}

		public void setStartTime(long startTime)
		{
			_startTime = startTime;
		}

		@Override
		public void run()
		{
			int delay = (int) Math.round((_startTime - System.currentTimeMillis()) / 1000.0);

			if (delay > 0)
			{
				this.announce(delay);
			}

			int nextMsg = 0;
			if (delay > 3600)
			{
				nextMsg = delay - 3600;
			}
			else if (delay > 1800)
			{
				nextMsg = delay - 1800;
			}
			else if (delay > 900)
			{
				nextMsg = delay - 900;
			}
			else if (delay > 600)
			{
				nextMsg = delay - 600;
			}
			else if (delay > 300)
			{
				nextMsg = delay - 300;
			}
			else if (delay > 60)
			{
				nextMsg = delay - 60;
			}
			else if (delay > 5)
			{
				nextMsg = delay - 5;
			}
			else if (delay > 0)
			{
				nextMsg = delay;
			}
			else
			{
				// start
				if (KTBEvent.isInactive())
				{
					startReg();
				}
				else if (KTBEvent.isParticipating())
				{
					startEvent();
				}
				else
				{
					endEvent();
				}
			}

			if (delay > 0)
			{
				nextRun = ThreadPool.schedule(this, nextMsg * 1000);
			}
		}

		private void announce(long time)
		{
			if (time >= 3600 && time % 3600 == 0)
			{
				if (KTBEvent.isParticipating())
				{
					Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: " + (time / 60 / 60) + " hour(s) until registration is closed!");
				}
				else if (KTBEvent.isStarted())
				{
					KTBEvent.sysMsgToAllParticipants("" + (time / 60 / 60) + " hour(s) until event is finished!");
				}
			}
			else if (time >= 60)
			{
				if (KTBEvent.isParticipating())
				{
					Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: " + (time / 60) + " minute(s) until registration is closed!");
				}
				else if (KTBEvent.isStarted())
				{
					KTBEvent.sysMsgToAllParticipants("" + (time / 60) + " minute(s) until the event is finished!");
				}
			}
			else
			{
				if (KTBEvent.isParticipating())
				{
					Broadcast.gameAnnounceToOnlinePlayers("Kill The Boss: " + time + " second(s) until registration is closed!");
				}
				else if (KTBEvent.isStarted())
				{
					KTBEvent.sysMsgToAllParticipants("" + time + " second(s) until the event is finished!");
				}
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final KTBManager _instance = new KTBManager();
	}
}