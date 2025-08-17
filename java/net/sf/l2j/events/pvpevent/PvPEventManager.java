package net.sf.l2j.events.pvpevent;

import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.events.manager.PvPEventNext;
import net.sf.l2j.gameserver.model.Announcement;
import net.sf.l2j.gameserver.util.Broadcast;

public class PvPEventManager implements Runnable
{
    protected static final Logger _log;
    private int _tick;
    public EngineState _state;
    private PvPEvent _event;
    
    protected PvPEventManager() 
    {
        this._event = PvPEvent.getInstance();
        if (Config.PVP_EVENT_ENABLED) 
        {
            this._state = EngineState.AWAITING;
            ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
            PvPEventManager._log.info("PvPEvent: Event is active.");
            PvPEventNext.getInstance().StartCalculationOfNextEventTime();
        }
        else
        {
            this._state = EngineState.INACTIVE;
            PvPEventManager._log.info("PvPEvent: Event is disabled.");
        }
    }
    
    @Override
    public void run() 
    {
        if (this._state == EngineState.AWAITING)
        {
            final Calendar calendar = Calendar.getInstance();
            final int hour = calendar.get(11);
            final int minute = calendar.get(12);
            for (final String time : Config.PVP_EVENT_INTERVAL) 
            {
                final String[] splitTime = time.split(":");
                if (Integer.parseInt(splitTime[0]) == hour && Integer.parseInt(splitTime[1]) == minute)
                {
                    this.startEvent();
                }
            }
        }
        else if (this._state == EngineState.ACTIVE)
        {
            switch (this._tick)
            {
                case 7200:
                {
                	Announcement.PvPAnnounce(""+Config.NAME_PVP+" " + this._tick / 60 + " minute(s) until the event is finished!");
                    Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 5400:
                {
                	Announcement.PvPAnnounce(""+Config.NAME_PVP+" " + this._tick / 60 + " minute(s) until the event is finished!");
                    Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 3600:
                {
                	Announcement.PvPAnnounce(""+Config.NAME_PVP+" " + this._tick / 60 + " minute(s) until the event is finished!");
                    Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 2700:
                case 1800:
                {
                	Announcement.PvPAnnounce(""+Config.NAME_PVP+" " + this._tick / 60 + " minute(s) until the event is finished!");
                	Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 1200:
                case 900: 
                {
                	Announcement.PvPAnnounce(""+Config.NAME_PVP+" " + this._tick / 60 + " minute(s) until the event is finished!");
                    Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 600:
                case 300: 
                {
                	Announcement.PvPAnnounce(""+Config.NAME_PVP+" " + this._tick / 60 + " minute(s) until the event is finished!");
                	Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 60:
                case 180: 
                {
                	Announcement.PvPAnnounce(""+Config.NAME_PVP+" " + this._tick / 60 + " minute(s) until the event is finished!");
                    Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 30: 
                {
                	Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" " + this._tick + " second(s) until the event is finished!");
                    Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" check the current ranking by .pvpEvent!");
                    break;
                }
                case 3:
                case 4:
                case 5:
                case 10: 
                {
                	Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" " + this._tick + " second(s) until the event is finished!");
                    break;
                }
                case 2: 
                {
                	Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" " + this._tick + " second(s) until the event is finished!");
                    break;
                }
                case 1: 
                {
                	Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" " + this._tick + " second(s) until the event is finished!");
                    break;
                }
            }
            if (this._tick == 0)
            {
                this.endEvent();
            }
            --this._tick;
        }
    }
    
    public void startEvent()
    {
        if (this._event.startPartyEvent())
        {
        	Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" is enabled, go to PvP Zone.");
            this._state = EngineState.ACTIVE;
            this._tick = Config.PVP_EVENT_RUNNING_TIME * 60;
        }
    }
    
    public void endEvent()
    {
        if (this._event.endPartyEvent()) 
        {
            this._event.rewardFinish();
            Broadcast.gameAnnounceToOnlinePlayers(""+Config.NAME_PVP+" is finished, thank you for participating.");
            PvPEventNext.getInstance().StartCalculationOfNextEventTime();
            this._state = EngineState.AWAITING;
        }
    }
    
    public static PvPEventManager getInstance()
    {
        return SingletonHolder.INSTANCE;
    }
    
    static 
    {
        _log = Logger.getLogger(PvPEventManager.class.getName());
    }
    
    protected enum EngineState
    {
        AWAITING, 
        ACTIVE, 
        INACTIVE;
    }
    
    private static class SingletonHolder
    {
        protected static final PvPEventManager INSTANCE;
        
        static
        {
            INSTANCE = new PvPEventManager();
        }
    }
}
