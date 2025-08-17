/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <[url="http://www.gnu.org/licenses/>."]http://www.gnu.org/licenses/>.[/url]
 */
package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.instancemanager.RaidZoneManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

import net.sf.l2j.Config;

public class L2FlagZone extends L2SpawnZone
{
	
	public L2FlagZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.FLAGZONE, true);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			//activeChar.sendPacket(new ExShowScreenMessage("You have entered a Boss Zone!", 4000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
			activeChar.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			
		//	if (Config.ENABLE_FLAGZONE)
		//	{
				
				if (!activeChar.isInObserverMode())
				{
					if (activeChar.getPvpFlag() > 0)
						PvpFlagTaskManager.getInstance().remove(activeChar);
					
					activeChar.updatePvPFlag(1);
					
					if (!activeChar.isGM())
						activeChar.getAppearance().setVisible();
					
		//		}
			}
			
			if (activeChar.isMounted())
			{
				activeChar.dismount();	
			}
			
		}
	}

	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.FLAGZONE, false);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			activeChar.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		//	if(Config.ENABLE_FLAGZONE)
		//	{
				PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
		//	}
		}
	}

	@Override
	public void onDieInside(Creature character)
	{
	}

	@Override
	public void onReviveInside(Creature character)
	{
	}
}