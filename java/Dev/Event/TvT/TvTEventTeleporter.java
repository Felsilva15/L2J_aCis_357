package Dev.Event.TvT;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class TvTEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private Player _playerInstance = null;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];
	/** Admin removed this player from event */
	private boolean _adminRemove = false;
	
	/**
	 * Initialize the teleporter and start the delayed task.
	 * @param playerInstance
	 * @param coordinates
	 * @param fastSchedule
	 * @param adminRemove
	 */
	public TvTEventTeleporter(Player playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_adminRemove = adminRemove;
		
		long delay = (TvTEvent.isStarted() ? TvTConfig.TVT_EVENT_RESPAWN_TELEPORT_DELAY : TvTConfig.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;
		
		ThreadPool.schedule(this, fastSchedule ? 0 : delay);
	}
	
	/**
	 * The task method to teleport the player
	 * 1. Unsummon pet if there is one
	 * 2. Remove all effects
	 * 3. Revive and full heal the player
	 * 4. Teleport the player
	 * 5. Broadcast status and user info
	 */
	@Override
	public void run()
	{
	    if (_playerInstance == null)
	        return;

	    Summon summon = _playerInstance.getPet();

	    if (summon != null)
	        summon.unSummon(_playerInstance);

	    if (_playerInstance.isInDuel())
	        _playerInstance.setDuelState(DuelState.INTERRUPTED);

	    _playerInstance.doRevive();

	    int randomOffsetX = Rnd.get(100) - 50;  // -50 a +49
	    int randomOffsetY = Rnd.get(100) - 50;  // -50 a +49

	    _playerInstance.teleToLocation(_coordinates[0] + randomOffsetX, _coordinates[1] + randomOffsetY, _coordinates[2], 0);


	    if (TvTEvent.isStarted() && !_adminRemove)
	    {
	        _playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
	        PvpFlagTaskManager.getInstance().remove(_playerInstance);
	        _playerInstance.updatePvPFlag(0);
	    }
	    else
	        _playerInstance.setTeam(0);

	    _playerInstance.setCurrentCp(_playerInstance.getMaxCp());
	    _playerInstance.setCurrentHp(_playerInstance.getMaxHp());
	    _playerInstance.setCurrentMp(_playerInstance.getMaxMp());
	//    TvTEvent.unparalyzeAll();
	    _playerInstance.broadcastStatusUpdate();
	    _playerInstance.broadcastUserInfo();
	}

}