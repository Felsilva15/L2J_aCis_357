package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import net.sf.l2j.ConnectionPool;
import net.sf.l2j.commons.concurrent.ThreadPool;

import hwid.Hwid;
import hwid.HwidConfig;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.FloodProtectors.Action;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.CharSelected;
import net.sf.l2j.gameserver.network.serverpackets.SSQInfo;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowHtml;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1; // new in C4
	@SuppressWarnings("unused")
	private int _unk2; // new in C4
	@SuppressWarnings("unused")
	private int _unk3; // new in C4
	@SuppressWarnings("unused")
	private int _unk4; // new in C4
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		if (!FloodProtectors.performAction(client, Action.CHARACTER_SELECT))
			return;
		
		// we should always be able to acquire the lock but if we cant lock then nothing should be done (ie repeated packet)
		if (client.getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null but if not then this is repeated packet and nothing should be done here
				if (client.getActiveChar() == null)
				{
					final CharSelectInfoPackage info = client.getCharSelection(_charSlot);
					if (info == null || info.getAccessLevel() < 0)
						return;
					
					// Load up character from disk
					final Player cha = client.loadCharFromDisk(_charSlot);
					if (cha == null)
						return;
					
					cha.setClient(client);
					client.setActiveChar(cha);
					cha.setOnlineStatus(true, true);
					
					sendPacket(SSQInfo.sendSky());
					
				//	if (!Hwid.checkPlayerWithHWID(client, cha.getObjectId(), cha.getName()))
				//		return;
					
					if (client.BanedHwid(client.getHWID()) && HwidConfig.ALLOW_GUARD_SYSTEM)
					{
				       	_log.info("Player Name: [" + cha.getName() + "] - HWID: [" + client.getHWID() + "] Trying to logon with hwid ban!");
			    		ThreadPool.schedule(new Disconect(cha), 100);			        	
						return;
					}	
					
					cha.ReloadBlockChat(false);
					
					if (cha.ChatProtection(cha.getHWID()) && ((cha.getChatBanTimer() - 1500) > System.currentTimeMillis()))
						cha.setChatBlock(true);
					
					client.setState(GameClientState.ENTERING);
					
					sendPacket(new CharSelected(cha, client.getSessionId().playOkID1));
				}
			}
			finally
			{
				client.getActiveCharLock().unlock();
			}
		}
	}
	private class Disconect implements Runnable
	{
		@SuppressWarnings("unused")
		private Player _activeChar;
		
		public Disconect(Player activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			final L2GameClient client = getClient();				
			client.close(ServerClose.STATIC_PACKET);
		}
	}
	
}