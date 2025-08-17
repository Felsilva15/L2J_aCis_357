package net.sf.l2j.loginserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.commons.mmocore.ReceivablePacket;

import net.sf.l2j.loginserver.L2LoginClient;

public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
	private static Logger _log;

	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			L2LoginClientPacket._log.severe("ERROR READING: " + this.getClass().getSimpleName());
			e.printStackTrace();
			return false;
		}
	}

	protected abstract boolean readImpl();

	static
	{
		L2LoginClientPacket._log = Logger.getLogger(L2LoginClientPacket.class.getName());
	}
}
