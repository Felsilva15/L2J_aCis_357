package net.sf.l2j.loginserver.network.loginserverpackets;

import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.network.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
	public AuthResponse(final int serverId)
	{
		writeC(2);
		writeC(serverId);
		writeS(GameServerTable.getInstance().getServerNames().get(serverId));
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
