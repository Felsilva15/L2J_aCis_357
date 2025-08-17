package Dev.DailyReward;

import net.sf.l2j.gameserver.model.actor.instance.Player;

public interface IBypassHandler
{
	public boolean handleBypass(String bypass, Player activeChar);
	
	public String[] getBypassHandlersList();
}