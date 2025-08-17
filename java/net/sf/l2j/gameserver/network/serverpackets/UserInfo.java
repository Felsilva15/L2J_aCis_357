package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.events.CTF;
import net.sf.l2j.events.TvT;
import net.sf.l2j.gameserver.instancemanager.AioManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.DressMe;
import net.sf.l2j.gameserver.model.WorldObject.PolyType;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.skills.AbnormalEffect;

import net.sf.l2j.Config;

import Dev.Event.TvTFortress.FOSConfig;
import Dev.Event.TvTFortress.FOSEvent;

public class UserInfo extends L2GameServerPacket
{
	private final Player _activeChar;
	private int _relation;
	private final int _fakeWeaponObjectId;
	private final int _fakeWeaponItemId;
	public UserInfo(Player character)
	{
		_activeChar = character;
		
		_relation = _activeChar.isClanLeader() ? 0x40 : 0;
		_fakeWeaponObjectId = _activeChar.getFakeWeaponObjectId();
		_fakeWeaponItemId = _activeChar.getFakeWeaponItemId();
		if (_activeChar.getSiegeState() == 1)
			_relation |= 0x180;
		if (_activeChar.getSiegeState() == 2)
			_relation |= 0x80;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x04);
		
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getHeading());
		writeD(_activeChar.getObjectId());
		
		if (Config.ALLOW_ZONE_NAME && _activeChar.isInsideZone(ZoneId.NEW_NAME))
		{
			writeS(Config.ZONE_NAME);
		}
		else if (_activeChar.isInDMEvent())
		{
			writeS("Enemy");
		}
		else if (_activeChar.isInFOSEvent())
		{
			byte playerTeamId = FOSEvent.getParticipantTeamId(_activeChar.getObjectId());
			
			if (playerTeamId == 0)
				writeS("Team " + FOSConfig.FOS_EVENT_TEAM_1_NAME);
			
			if (playerTeamId == 1)
				writeS("Team " + FOSConfig.FOS_EVENT_TEAM_2_NAME);
		}
		else
		{
			writeS((_activeChar.getPolyTemplate() != null) ? _activeChar.getPolyTemplate().getName() : _activeChar.getName());
		}
		
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getAppearance().getSex().ordinal());
		
		if (_activeChar.getClassIndex() == 0)
			writeD(_activeChar.getClassId().getId());
		else
			writeD(_activeChar.getBaseClass());
		
		writeD(_activeChar.getLevel());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getSTR());
		writeD(_activeChar.getDEX());
		writeD(_activeChar.getCON());
		writeD(_activeChar.getINT());
		writeD(_activeChar.getWIT());
		writeD(_activeChar.getMEN());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		
		writeD(_activeChar.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		//COM SKIN WEAPON
		DressMe dress = _activeChar.getDress();
	//	writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL) : ((dress.getHairId() == 0) ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL) : dress.getHairId()));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
	//	writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_fakeWeaponItemId == 0 ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND) : _fakeWeaponItemId);
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));	
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES) : ((dress.getGlovesId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES) : dress.getGlovesId()));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) : ((dress.getChestId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) : dress.getChestId()));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS) : ((dress.getLegsId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS) : dress.getLegsId()));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET) : ((dress.getFeetId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET) : dress.getFeetId()));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
	//	writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));	
		writeD(_fakeWeaponItemId == 0 ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND) : _fakeWeaponItemId);
	//	writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : ((dress.getHairId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : dress.getHairId()));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : ((dress.getHairId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : dress.getHairId()));
		
		
	//	writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE) : ((dress.getHairId() == 0) ? _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE) : dress.getHairId()));
	//	writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL) : ((dress.getHairId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL) : dress.getHairId()));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
	//	writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_fakeWeaponItemId == 0 ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND) : _fakeWeaponItemId);
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));			
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES) : ((dress.getGlovesId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES) : dress.getGlovesId()));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) : ((dress.getChestId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) : dress.getChestId()));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS) : ((dress.getLegsId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS) : dress.getLegsId()));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET) : ((dress.getFeetId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET) : dress.getFeetId()));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
	//	writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_fakeWeaponItemId == 0 ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND) : _fakeWeaponItemId);
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : ((dress.getHairId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : dress.getHairId()));
	//	writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		writeD((dress == null) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE) : ((dress.getHairId() == 0) ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE) : dress.getHairId()));
		
		
		// c6 new h's
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		// end of c6 new h's
		
		writeD(_activeChar.getPAtk(null));
		writeD(_activeChar.getPAtkSpd());
		writeD(_activeChar.getPDef(null));
		writeD(_activeChar.getEvasionRate(null));
		writeD(_activeChar.getAccuracy());
		writeD(_activeChar.getCriticalHit(null, null));
		writeD(_activeChar.getMAtk(null, null));
		
		writeD(_activeChar.getMAtkSpd());
		writeD(_activeChar.getPAtkSpd());
		
		writeD(_activeChar.getMDef(null, null));
		
		writeD(_activeChar.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		writeD(_activeChar.getKarma());
		
		int _runSpd = _activeChar.getStat().getBaseRunSpeed();
		int _walkSpd = _activeChar.getStat().getBaseWalkSpeed();
		int _swimSpd = _activeChar.getStat().getBaseSwimSpeed();
		writeD(_runSpd); // base run speed
		writeD(_walkSpd); // base walk speed
		writeD(_swimSpd); // swim run speed
		writeD(_swimSpd); // swim walk speed
		writeD(0);
		writeD(0);
		writeD(_activeChar.isFlying() ? _runSpd : 0); // fly run speed
		writeD(_activeChar.isFlying() ? _walkSpd : 0); // fly walk speed
		writeF(_activeChar.getStat().getMovementSpeedMultiplier()); // run speed multiplier
		writeF(_activeChar.getStat().getAttackSpeedMultiplier()); // attack speed multiplier
		
		Summon pet = _activeChar.getPet();
		if (_activeChar.getMountType() != 0 && pet != null)
		{
			writeF(pet.getTemplate().getCollisionRadius());
			writeF(pet.getTemplate().getCollisionHeight());
		}
		else
		{
			writeF(_activeChar.getBaseTemplate().getCollisionRadius());
			writeF(_activeChar.getBaseTemplate().getCollisionHeight());
		}
		
		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());
		writeD(_activeChar.isGM() ? 1 : 0); // builder level
		
		if (_activeChar.getAppearance().getInvisible() && _activeChar.isGM())
			writeS("Invisible");
		else if(AioManager.getInstance().hasAioPrivileges(this._activeChar.getObjectId()) || _activeChar.isAio())
			writeS(Config.AIO_TITLE);
		else if(Config.ENABLE_NEW_TITLE_AUTOFARM && _activeChar.isAutoFarm())
		{
			writeS(Config.NEW_TITLE_AUTOFARM);
		}
		else if (_activeChar.isInDMEvent())
		{
			writeS("Kills: " + _activeChar.getDMPointScore());
		}
		else if (_activeChar.isInFOSEvent())
		{
			byte playerTeamId = FOSEvent.getParticipantTeamId(_activeChar.getObjectId());
			
			if (playerTeamId == 0)
				writeS("Score: " + _activeChar.getFOSPointScore());
			
			if (playerTeamId == 1)
				writeS("Score: " + _activeChar.getFOSPointScore());
		}
		else
			writeS((_activeChar.getPolyType() != PolyType.DEFAULT) ? "Morphed" : _activeChar.getTitle());
		
		
		if (((TvT.is_started() || TvT.is_teleport()) && _activeChar._inEventTvT) || ((CTF.is_started() || CTF.is_teleport()) && _activeChar._inEventCTF)  || _activeChar.isInsideZone(ZoneId.PVP_CUSTOM) || _activeChar.isInFOSEvent())
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(_activeChar.getClanId());
			writeD(_activeChar.getClanCrestId());
			writeD(_activeChar.getAllyId());
			writeD(_activeChar.getAllyCrestId()); // ally crest id	
		}
	
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(_activeChar.getMountType()); // mount type
		writeC(_activeChar.getStoreType().getId());
		writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getPvpKills());
		
		writeH(_activeChar.getCubics().size());
		for (int id : _activeChar.getCubics().keySet())
			writeH(id);
		
		writeC(_activeChar.isInPartyMatchRoom() ? 1 : 0);
		
		if (_activeChar.getAppearance().getInvisible() && _activeChar.isGM()) {
			writeD(_activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask());
		} else {
			writeD(_activeChar.getAbnormalEffect());
		}
		
		writeC(0x00);
		
		writeD(_activeChar.getClanPrivileges());
		
		writeH(_activeChar.getRecomLeft()); // c2 recommendations remaining
		writeH(_activeChar.getRecomHave()); // c2 recommendations received
		writeD(_activeChar.getMountNpcId() > 0 ? _activeChar.getMountNpcId() + 1000000 : 0);
		writeH(_activeChar.getInventoryLimit());
		
		writeD(_activeChar.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(_activeChar.getMaxCp());
		writeD((int) _activeChar.getCurrentCp());
		//visao do player
		if (_activeChar.isDisableGlowWeapon()){
			writeC(0);
		}else{
			writeC(_activeChar.isMounted() ? 0 : _activeChar.getEnchantEffect());
		}
		
		if (_activeChar.getTeam() == 1 || _activeChar.getTeamTour() == 1)
			writeC(0x01); // team circle around feet 1= Blue, 2 = red
		else if(Config.ENABLE_AURA_AUTOFARM && _activeChar.isAutoFarm())
			writeC(0x01);
		else if (_activeChar.getTeam() == 2 || _activeChar.getTeamTour() == 2)
			writeC(0x02); // team circle around feet 1= Blue, 2 = red
		else
			writeC(0x00); // team circle around feet 1= Blue, 2 = red
		
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); // 0x01: symbol on char menu ctrl+I
		writeC(_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA) ? 1 : 0); // 0x01: Hero Aura
		
		writeC(_activeChar.isFishing() ? 1 : 0); // Fishing Mode
		
		Location loc = _activeChar.getFishingLoc();
		if (loc != null)
		{
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0);
		}
		if (_activeChar.isInDMEvent())
		{
			writeD(0x0000F8); // Red
		}
		else if (_activeChar.isInFOSEvent())
		{
			byte playerTeamId = FOSEvent.getParticipantTeamId(_activeChar.getObjectId());
			
			if (playerTeamId == 0)
				writeD(0xFF3500); // Blue
			
			if (playerTeamId == 1)
				writeD(0x0000F8); // Red
		}
		else
		{
			writeD(_activeChar.getAppearance().getNameColor());
		}
		//writeD(_activeChar.getAppearance().getNameColor());
		
		// new c5
		writeC(_activeChar.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeD(_activeChar.getPledgeClass()); // changes the text above CP on Status Window
		writeD(_activeChar.getPledgeType());
		
		writeD(AioManager.getInstance().hasAioPrivileges(_activeChar.getObjectId()) ? Config.AIO_TCOLOR : _activeChar.getAppearance().getTitleColor());
		
		if (_activeChar.isCursedWeaponEquipped())
			writeD(CursedWeaponsManager.getInstance().getCurrentStage(_activeChar.getCursedWeaponEquippedId()) - 1);
		else
			writeD(0x00);
	}
}