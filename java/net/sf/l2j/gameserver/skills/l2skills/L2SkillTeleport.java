package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.events.CTF;
import net.sf.l2j.events.TvT;
import net.sf.l2j.gameserver.data.MapRegionTable;
import net.sf.l2j.gameserver.data.MapRegionTable.TeleportType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.TvTFortress.FOSEvent;

public class L2SkillTeleport extends L2Skill
{
	private final String _recallType;
	private final Location _loc;
	
	public L2SkillTeleport(StatsSet set)
	{
		super(set);
		
		_recallType = set.getString("recallType", "");
		String coords = set.getString("teleCoords", null);
		if (coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_loc = new Location(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]), Integer.parseInt(valuesSplit[2]));
		}
		else
			_loc = null;
	}
	
	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets)
	{
		if (activeChar instanceof Player)
		{
			// Check invalid states.
			if (activeChar.isAfraid() || ((Player) activeChar).isInOlympiadMode())
				return;
		}
		
		boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			
			if (target instanceof Player)
			{
				Player targetChar = (Player) target;
				
				// Check invalid states.
				if (targetChar.isFestivalParticipant() || targetChar.isInJail() || targetChar.isInDuel())
					continue;
				
				if ((TvT.is_started() && targetChar._inEventTvT) || (CTF.is_started() && targetChar._inEventCTF) || targetChar.isArenaProtection() || KTBEvent.isPlayerParticipant(targetChar.getObjectId()) && KTBEvent.isStarted() || DMEvent.isStarted() && DMEvent.isPlayerParticipant(targetChar.getObjectId()) || FOSEvent.isStarted() && FOSEvent.isPlayerParticipant(targetChar.getObjectId()))
				{
					targetChar.sendMessage("You can't use escape skill in Event.");
					continue;
				}
				
				if (targetChar != activeChar)
				{
					if (targetChar.isInOlympiadMode())
						continue;
				}
			}
			
			Location loc = null;
			if (getSkillType() == L2SkillType.TELEPORT)
			{
				if (_loc != null)
				{
					if (!(target instanceof Player) || !target.isFlying())
						loc = _loc;
				}
			}
			else
			{
				if (_recallType.equalsIgnoreCase("Castle"))
					loc = MapRegionTable.getInstance().getLocationToTeleport(target, TeleportType.CASTLE);
				else if (_recallType.equalsIgnoreCase("ClanHall"))
					loc = MapRegionTable.getInstance().getLocationToTeleport(target, TeleportType.CLAN_HALL);
				else
					loc = MapRegionTable.getInstance().getLocationToTeleport(target, TeleportType.TOWN);
			}
			
			if (loc != null)
			{
				if (target instanceof Player)
					((Player) target).setIsIn7sDungeon(false);
				
				target.teleToLocation(loc, 20);
			}
		}
		
		activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, isStaticReuse());
	}
}