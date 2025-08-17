package Dev.Event.BossEvent;

import net.sf.l2j.gameserver.model.actor.instance.Player;

public class KTBPlayer
{
	private Player _player;

	/**
	 * @param player
	 */
	public KTBPlayer(Player player)
	{
		_player = player;
	}

	/**
	 * @return the _player
	 */
	public Player getPlayer()
	{
		return _player;
	}

	/**
	 * @param player the _player to set
	 */
	public void setPlayer(Player player)
	{
		_player = player;
	}

}