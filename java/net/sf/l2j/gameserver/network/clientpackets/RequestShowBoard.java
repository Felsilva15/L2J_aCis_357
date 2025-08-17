package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.communitybbs.CommunityBoard;
import net.sf.l2j.gameserver.model.actor.instance.Player;

import net.sf.l2j.Config;

import Dev.Community.MarketPlace.MarketplaceCBBypasses;

public final class RequestShowBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("Impossible used in Jail...");
			return;
		}
		MarketplaceCBBypasses.showMarketBoard(activeChar, 1, "*null*");      
	//	CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
	}
}