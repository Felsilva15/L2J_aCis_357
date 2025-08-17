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
package Dev.Event.DeathMatch;

import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.commons.concurrent.ThreadPool;

public class DMEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private Player _activeChar = null;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];
	/** Admin removed this player from event */
	private boolean _adminRemove = false;

	/**
	 * Initialize the teleporter and start the delayed task<br><br>
	 *
	 * @param activeChar as Player<br>
	 * @param coordinates as int[]<br>
	 * @param fastSchedule 
	 * @param adminRemove as boolean<br>
	 */
	public DMEventTeleporter(Player activeChar, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_activeChar = activeChar;
		_coordinates = coordinates;
		_adminRemove = adminRemove;

		loadTeleport(fastSchedule);
	}

	/**
	 * Initialize the teleporter and start the delayed task<br><br>
	 *
	 * @param activeChar as Player<br>
	 * @param fastSchedule 
	 * @param adminRemove as boolean<br>
	 */
	public DMEventTeleporter(Player activeChar, boolean fastSchedule, boolean adminRemove)
	{
		_activeChar = activeChar;
		_coordinates = DMConfig.DM_EVENT_PLAYER_COORDINATES.get(Rnd.get(DMConfig.DM_EVENT_PLAYER_COORDINATES.size()));
		_adminRemove = adminRemove;

		loadTeleport(fastSchedule);
	}

	private void loadTeleport(boolean fastSchedule)
	{
		long delay = (DMEvent.isStarted() ? DMConfig.DM_EVENT_RESPAWN_TELEPORT_DELAY : DMConfig.DM_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;
		ThreadPool.schedule(this, fastSchedule ? 0 : delay);
	}	
	
	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info<br><br>
	 */
	@Override
	public void run()
	{
		if (_activeChar == null) 
			return;

		Summon summon = _activeChar.getPet();

		if (summon != null)
			summon.unSummon(_activeChar);

		if (_activeChar.isInDuel())
			_activeChar.setDuelState(DuelState.INTERRUPTED);

		_activeChar.doRevive();

	//	if (_activeChar instanceof FakePlayer && !DMEvent.isStarted())
	//		_activeChar.teleToLocation(-114584,-251256,-2992, 0);
	//	else
			_activeChar.teleToLocation(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], 0);

		if (DMEvent.isStarted() && !_adminRemove)
		{
			_activeChar.setTeam(2);
			PvpFlagTaskManager.getInstance().remove(_activeChar);
			_activeChar.updatePvPFlag(0);
		}
		else
			_activeChar.setTeam(0);

		_activeChar.setCurrentCp(_activeChar.getMaxCp());
		_activeChar.setCurrentHp(_activeChar.getMaxHp());
		_activeChar.setCurrentMp(_activeChar.getMaxMp());

		_activeChar.broadcastStatusUpdate();
		_activeChar.broadcastUserInfo();		
	}
}