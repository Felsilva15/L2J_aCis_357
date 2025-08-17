package net.sf.l2j.gameserver;

import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author ProGramMoS
 */

public class PcPoint implements Runnable
{
	Logger _log = Logger.getLogger(PcPoint.class.getName());
	private static PcPoint _instance;
	
	public static PcPoint getInstance()
	{
		if (_instance == null)
		{
			_instance = new PcPoint();
		}
		
		return _instance;
	}
	
	private PcPoint()
	{
		_log.info("PcBang point event started.");
	}
	
	@Override
	public void run()
	{
		
		int score = 0;
		for (Player activeChar : World.getInstance().getPlayers())
		{			
			if (activeChar.isOnline() && activeChar.getLevel() > Config.PCB_MIN_LEVEL && !activeChar.getClient().isDetached() && !activeChar.isOff() && !activeChar.isOffShop())
			{
				score = Rnd.get(Config.PCB_POINT_MIN, Config.PCB_POINT_MAX);
				
				if (Rnd.get(100) <= Config.PCB_CHANCE_DUAL_POINT)
				{
					score *= 2;
					
					activeChar.addPcBangScore(score);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
					sm.addNumber(score);
					activeChar.sendPacket(sm);
					sm = null;
					
					activeChar.updatePcBangWnd(score, true, true);
				}
				else
				{
					activeChar.addPcBangScore(score);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT);
					sm.addNumber(score);
					activeChar.sendPacket(sm);
					sm = null;
					
					activeChar.updatePcBangWnd(score, true, false);
				}
			}
			
			activeChar = null;
		}
	}
}
