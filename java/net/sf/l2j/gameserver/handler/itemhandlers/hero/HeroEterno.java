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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.itemhandlers.hero;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHero;
import net.sf.l2j.gameserver.instancemanager.HeroManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

/**
 * @author MeGaPacK
 */
public class HeroEterno implements IItemHandler
{
	
	protected static final Logger LOGGER = Logger.getLogger(HeroEterno.class.getName());
	
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		Player activeChar = (Player) playable;
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("SYS: This Item Cannot Be Used On Olympiad Games.");
			return;
		}
		
		if (activeChar.isHero())
		{
			activeChar.sendMessage("SYS: Voce ja esta com status Hero.");
			return;
		}
		
		if (HeroManager.getInstance().hasHeroPrivileges(activeChar.getObjectId()))		
			AdminHero.removeHero(activeChar, activeChar);
				
		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		activeChar.setHero(true);
		activeChar.setNoble(true, true);
		AdminHero.updateDatabase(activeChar, true);
		
		activeChar.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "[Hero System]", "Voce se tornou um HERO ETERNO."));
		if (activeChar.isSubClassActive())
		{
			for (L2Skill s : SkillTable.getHeroSkills())
				activeChar.addSkill(s, false); // Dont Save Hero skills to database
		}
		activeChar.sendSkillList();
		activeChar.broadcastUserInfo();
		
		for (Player allgms : World.getAllGMs())
			allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "(Donate Manager)", activeChar.getName() + " ativou Hero Eterno."));
				
	}
	
}
