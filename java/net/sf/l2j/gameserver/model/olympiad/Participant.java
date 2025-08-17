package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.gameserver.data.CharTemplateTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * @author DS
 */
public final class Participant
{
	public final int objectId;
	public Player player;
	public String name;
	public String class_name;
	
	public final int side;
	public final int baseClass;
	public boolean disconnected = false;
	public boolean defaulted = false;
	public final StatsSet stats;
	
	public Participant(Player plr, int olympiadSide)
	{
		objectId = plr.getObjectId();
		player = plr;
		name = plr.getName();
		if (name.length() > 11)
			name = name.substring(0, 11) + "..";
	
		class_name = CharTemplateTable.getInstance().getClassNameById(plr.getBaseClass());
		if (class_name.length() > 12)
			class_name = class_name.substring(0, 12) + "..";
		
		side = olympiadSide;
		baseClass = plr.getBaseClass();
		stats = Olympiad.getNobleStats(objectId);
	}
	
	public Participant(int objId, int olympiadSide)
	{
		objectId = objId;
		player = null;
		name = "-";
		class_name = "-";
		side = olympiadSide;
		baseClass = 0;
		stats = null;
	}
	
	public final void updatePlayer()
	{
		if (player == null || !player.isOnline())
			player = World.getInstance().getPlayer(objectId);
	}
	
	public final void updateStat(String statName, int increment)
	{
		stats.set(statName, Math.max(stats.getInteger(statName) + increment, 0));
	}
}