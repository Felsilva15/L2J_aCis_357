package net.sf.l2j.loginserver;

import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.mmocore.IAcceptFilter;
import net.sf.l2j.commons.mmocore.IClientFactory;
import net.sf.l2j.commons.mmocore.IMMOExecutor;
import net.sf.l2j.commons.mmocore.MMOConnection;
import net.sf.l2j.commons.mmocore.ReceivablePacket;

import net.sf.l2j.Config;
import net.sf.l2j.ConnectionPool;

import net.sf.l2j.loginserver.network.serverpackets.Init;
import net.sf.l2j.util.IPv4Filter;

public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	private final ThreadPoolExecutor _generalPacketsThreadPool;
	private final IPv4Filter _ipv4filter;

	public SelectorHelper()
	{
		_generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		_ipv4filter = new IPv4Filter();
	}

	@Override
	public void execute(final ReceivablePacket<L2LoginClient> packet)
	{
		_generalPacketsThreadPool.execute(packet);
	}

	@Override
	public L2LoginClient create(final MMOConnection<L2LoginClient> con)
	{
		final L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		return client;
	}

	@Override
	public boolean accept(final SocketChannel sc)
	{
		final String ip = sc.socket().getInetAddress().getHostAddress();
		if (IPProtection(ip) || Config.IP_PROTECTION_CNFG && !ip.equals(Config.IP_PROTECTION))
		{
			System.out.println("Block connection: " + ip);
			return false;
		}
		return _ipv4filter.accept(sc) && !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
	}

	public static synchronized boolean IPProtection(final String ip)
	{
		boolean result = true;
		try (final Connection con = ConnectionPool.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT ip FROM ip_protection WHERE ip=?");
			statement.setString(1, ip);
			final ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			System.out.println("SelectorHelper -> IPProtection: " + e.getMessage());
		}
		return result;
	}
}
