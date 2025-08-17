package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.instance.Player;

import java.util.logging.Logger;

public interface ITutorialHandler
{
	public static Logger _log = Logger.getLogger(ITutorialHandler.class.getName());

	public boolean useLink(String command, Player activeChar, String params);

	public String[] getLinkList();
}