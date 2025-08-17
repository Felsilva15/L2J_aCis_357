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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.CharacterKillingManager;
import net.sf.l2j.gameserver.model.actor.L2PcPolymorph;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

import net.sf.l2j.Config;

/**
 * @author paytaly
 */
public class TopPvPMonument extends L2PcPolymorph
{
	public TopPvPMonument(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if (Config.CKM_ENABLED)
			CharacterKillingManager.getInstance().addPvPMorphListener(this);
	}
	
	@Override
	public void deleteMe()
	{
		super.deleteMe();
		if (Config.CKM_ENABLED)
			CharacterKillingManager.getInstance().removePvPMorphListener(this);
	}
}
