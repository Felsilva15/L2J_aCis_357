package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.instancemanager.AioManager;
import net.sf.l2j.gameserver.instancemanager.RaidZoneManager;
import net.sf.l2j.gameserver.instancemanager.custom.SiegeZoneManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.instance.SiegeSummon;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

/**
 * A siege zone handles following spawns type :
 * <ul>
 * <li>Generic spawn locs : other_restart_village_list (spawns used on siege, to respawn on second closest town.</li>
 * <li>Chaotic spawn locs : chao_restart_point_list (spawns used on siege, to respawn PKs on second closest town.</li>
 * </ul>
 */
public class L2SiegeZone extends L2SpawnZone
{
	private static final int DISMOUNT_DELAY = 5;
	
	private int _siegableId = -1;
	private boolean _isActiveSiege = false;
	private int _maxClanMembers;
	private int _maxAllyMembers;
	private int _minPartyMembers;
	private boolean _checkParty;
	private boolean _checkClan;
	private boolean _checkAlly;
	public L2SiegeZone(int id)
	{
		super(id);
		_maxClanMembers = 0;
		_maxAllyMembers = 0;
		_minPartyMembers = 0;
		_checkParty = false;
		_checkClan = false;
		_checkAlly = false;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId") || name.equals("clanHallId"))
			_siegableId = Integer.parseInt(value);
		else if (name.equals("MaxClanMembers"))
			_maxClanMembers = Integer.parseInt(value);
		else if (name.equals("MaxAllyMembers"))
			_maxAllyMembers = Integer.parseInt(value);
		else if (name.equals("MinPartyMembers"))
			_minPartyMembers = Integer.parseInt(value);
		else if (name.equals("checkParty"))
			_checkParty = Boolean.parseBoolean(value);
		else if (name.equals("checkClan"))
			_checkClan = Boolean.parseBoolean(value);
		else if (name.equals("checkAlly"))
			_checkAlly = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (_isActiveSiege)
		{
			character.setInsideZone(ZoneId.PVP, true);
			character.setInsideZone(ZoneId.SIEGE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			
			if (character instanceof Player)
			{
				Player activeChar = (Player) character;
				
				activeChar.setIsInSiege(true); // in siege
				
				activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				
				if (_checkParty)
				{
					if (!activeChar.isInParty() || activeChar.getParty().getMemberCount() < _minPartyMembers)
					{
						activeChar.sendPacket(new ExShowScreenMessage("Your party does not have " + _minPartyMembers + " members to enter on Raid Zone!", 6 * 1000));
						ThreadPool.schedule(() -> RaidZoneManager.getInstance().RandomTeleport(activeChar), 2000);

					}
				}
				if (_checkClan)
					MaxClanMembersOnArea(activeChar);

				if (_checkAlly)
					MaxAllyMembersOnArea(activeChar);
				
					if (activeChar.isAio() || AioManager.getInstance().hasAioPrivileges(activeChar.getObjectId())) 
					{
			            // Envia a mensagem na tela por 6 segundos, posição 2 (top center), com destaque (true)
			            activeChar.sendPacket(new ExShowScreenMessage("Class not allowed in this area.", 6000, 2, true));

			            // Agenda o teleporte para 4 segundos depois
			            ThreadPool.schedule(() -> {
			                // Verifica se o personagem ainda está online e não está dentro da zona de paz
			                if (activeChar.isOnline() && !activeChar.isInsideZone(ZoneId.PEACE)) {
			                    // Teleporta para coordenadas específicas com heading 50
			                    activeChar.teleToLocation(83464, 148616, -3400, 50);
			                }
			            }, 4000); // 4000 milissegundos = 4 segundos
			        }
		        
		        if (Config.BLOCK_DUALBOX_SIEGEZONE)
				{
		        	ThreadPool.schedule(() -> MaxPlayersOnArea(activeChar), 2000);
				}
				if (activeChar.getMountType() == 2)
				{
					activeChar.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
					activeChar.enteredNoLanding(DISMOUNT_DELAY);
				}
			}
		}
	}
	public boolean MaxPlayersOnArea(Player activeChar)
	{
		return SiegeZoneManager.getInstance().checkPlayersArea(activeChar, 1, true);
	}
	public boolean MaxClanMembersOnArea(Player activeChar)
	{
		return SiegeZoneManager.getInstance().checkClanArea(activeChar, _maxClanMembers, true);
	}
	
	public boolean MaxAllyMembersOnArea(Player activeChar)
	{
		return SiegeZoneManager.getInstance().checkAllyArea(activeChar, _maxAllyMembers, World.getInstance().getPlayers(), true);
	}
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.PVP, false);
		character.setInsideZone(ZoneId.SIEGE, false);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			if (_isActiveSiege)
			{
				activeChar.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
				
				if (activeChar.getMountType() == 2)
					activeChar.exitedNoLanding();
				
				PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
				
				// Set pvp flag
				if (activeChar.getPvpFlag() == 0)
					activeChar.updatePvPFlag(1);
			}
			
			activeChar.setIsInSiege(false);
		}
		else if (character instanceof SiegeSummon)
			((SiegeSummon) character).unSummon(((SiegeSummon) character).getOwner());
	}
	
	@Override
	public void onDieInside(Creature character)
	{
	}
	
	@Override
	public void onReviveInside(Creature character)
	{
	}
	
	public void updateZoneStatusForCharactersInside()
	{
		if (_isActiveSiege)
		{
			for (Creature character : _characterList.values())
				onEnter(character);
		}
		else
		{
			for (Creature character : _characterList.values())
			{
				character.setInsideZone(ZoneId.PVP, false);
				character.setInsideZone(ZoneId.SIEGE, false);
				character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
				
				if (character instanceof Player)
				{
					final Player player = ((Player) character);
					
					player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					
					if (player.getMountType() == 2)
						player.exitedNoLanding();
				}
				else if (character instanceof SiegeSummon)
					((SiegeSummon) character).unSummon(((SiegeSummon) character).getOwner());
			}
		}
	}
	
	/**
	 * Sends a message to all players in this zone
	 * @param message
	 */
	public void announceToPlayers(String message)
	{
		for (Player player : getKnownTypeInside(Player.class))
			player.sendMessage(message);
	}
	
	public int getSiegeObjectId()
	{
		return _siegableId;
	}
	
	public boolean isActive()
	{
		return _isActiveSiege;
	}
	
	public void setIsActive(boolean val)
	{
		_isActiveSiege = val;
	}
	
	/**
	 * Removes all foreigners from the zone
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		if (_characterList.isEmpty())
			return;
		
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.getClanId() == owningClanId)
				continue;
			
			player.teleToLocation((player.getKarma() > 0) ? getChaoticSpawnLoc() : getSpawnLoc(), 20);
		}
	}
}