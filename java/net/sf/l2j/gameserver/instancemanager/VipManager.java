package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.ConnectionPool;
import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author rapfersan92
 */
public class VipManager
{
    private static final Logger _log = Logger.getLogger(VipManager.class.getName());

    private final Map<Integer, Long> _vips;
    protected final Map<Integer, Long> _vipsTask;
    private ScheduledFuture<?> _scheduler;

    public static VipManager getInstance()
    {
        return SingletonHolder._instance;
    }

    protected VipManager()
    {
        _vips = new ConcurrentHashMap<>();
        _vipsTask = new ConcurrentHashMap<>();
        _scheduler = ThreadPool.scheduleAtFixedRate(new VipTask(), 1000, 1000);
        load();
    }

    public void reload()
    {
        _vips.clear();
        _vipsTask.clear();
        if (_scheduler != null)
            _scheduler.cancel(true);
        _scheduler = ThreadPool.scheduleAtFixedRate(new VipTask(), 1000, 1000);
        load();
    }

    public void load()
    {
        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT objectId, duration FROM character_vip ORDER BY objectId");
            ResultSet rs = statement.executeQuery();
            while (rs.next())
                _vips.put(rs.getInt("objectId"), rs.getLong("duration"));
            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Exception: VipManager load: " + e.getMessage());
        }

        _log.info("VipManager: Loaded " + _vips.size() + " characters with vip privileges.");
    }

//    public void addVip(int objectId, long duration)
//    {
//        _vips.put(objectId, duration);
//        _vipsTask.put(objectId, duration);
//        addVipPrivileges(objectId, true);
//
//        try (Connection con = ConnectionPool.getConnection())
//        {
//            PreparedStatement statement = con.prepareStatement("INSERT INTO character_vip (objectId, duration) VALUES (?, ?)");
//            statement.setInt(1, objectId);
//            statement.setLong(2, duration);
//            statement.execute();
//            statement.close();
//        }
//        catch (Exception e)
//        {
//            _log.warning("Exception: VipManager addVip: " + e.getMessage());
//        }
//    }
    public void addVip(int objectId, long duration)
    {
        if (_vips.containsKey(objectId)) {
            // Se o VIP já estiver registrado, apenas atualize a duração
            updateVip(objectId, duration);
            return;
        }

        _vips.put(objectId, duration);
        _vipsTask.put(objectId, duration);
        addVipPrivileges(objectId, true);

        try (Connection con = ConnectionPool.getConnection()) {
            // Verificar se o VIP já existe no banco
            try (PreparedStatement checkStmt = con.prepareStatement("SELECT objectId FROM character_vip WHERE objectId = ?")) {
                checkStmt.setInt(1, objectId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Se existir, atualize a duração
                        try (PreparedStatement updateStmt = con.prepareStatement("UPDATE character_vip SET duration = ? WHERE objectId = ?")) {
                            updateStmt.setLong(1, duration);
                            updateStmt.setInt(2, objectId);
                            updateStmt.execute();
                        }
                    } else {
                        // Caso contrário, insira como novo
                        try (PreparedStatement insertStmt = con.prepareStatement("INSERT INTO character_vip (objectId, duration) VALUES (?, ?)")) {
                            insertStmt.setInt(1, objectId);
                            insertStmt.setLong(2, duration);
                            insertStmt.execute();
                        }
                    }
                }
            }
        } catch (Exception e) {
            _log.warning("Exception: VipManager addVip: " + e.getMessage());
        }
    }


    public void updateVip(int objectId, long duration)
    {
        _vips.put(objectId, duration);
        _vipsTask.put(objectId, duration);

        // Envia uma mensagem ao jogador informando sobre a duração do VIP
        final Player player = World.getInstance().getPlayer(objectId);
        if (player != null)
        {
            player.sendMessage("Your VIP privileges are updated. You now have " + (duration - System.currentTimeMillis()) / 86400000 + " days left.");
            player.broadcastUserInfo();
        }

        // Atualiza no banco
        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("UPDATE character_vip SET duration = ? WHERE objectId = ?");
            statement.setLong(1, duration);
            statement.setInt(2, objectId);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Exception: VipManager updateVip: " + e.getMessage());
        }
    }

    public void removeVip(int objectId)
    {
        _vips.remove(objectId);
        _vipsTask.remove(objectId);
        removeVipPrivileges(objectId, false);

        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_vip WHERE objectId = ?");
            statement.setInt(1, objectId);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Exception: VipManager removeVip: " + e.getMessage());
        }
    }

    public boolean hasVipPrivileges(int objectId)
    {
        if (_vips.containsKey(objectId))
        {
            long vipEndTime = _vips.get(objectId);
            return System.currentTimeMillis() < vipEndTime; // Verifica se o VIP ainda é válido
        }
        return false;
    }

    public long getVipDuration(int objectId)
    {
        return _vips.getOrDefault(objectId, 0L);
    }

    public void addVipTask(int objectId, long duration)
    {
        _vipsTask.put(objectId, duration);
    }

    public void removeVipTask(int objectId)
    {
        _vipsTask.remove(objectId);
    }

    public void addVipPrivileges(int objectId, boolean apply)
    {
        final Player player = World.getInstance().getPlayer(objectId);
        if (player != null)
        {
            player.setVip(true);
            player.sendMessage("You are now a VIP member! Enjoy your privileges.");
            player.broadcastUserInfo();
        }
    }

    public void removeVipPrivileges(int objectId, boolean apply)
    {
        final Player player = World.getInstance().getPlayer(objectId);
        if (player != null)
        {
            player.setVip(false);
            player.sendMessage("Your VIP privileges have been removed.");
            player.broadcastUserInfo();
        }
    }

    public class VipTask implements Runnable
    {
        @Override
        public final void run()
        {
            if (_vipsTask.isEmpty())
                return;

            for (Map.Entry<Integer, Long> entry : _vipsTask.entrySet())
            {
                final long duration = entry.getValue();
                if (System.currentTimeMillis() > duration)
                {
                    final int objectId = entry.getKey();
                    removeVip(objectId);

                    final Player player = World.getInstance().getPlayer(objectId);
                    if (player != null)
                    {
                        player.sendPacket(new ExShowScreenMessage("Your VIP privileges were removed.", 10000));
                        player.sendMessage("Your VIP privileges have expired.");
                    }
                }
            }
        }
    }

    private static class SingletonHolder
    {
        protected static final VipManager _instance = new VipManager();
    }
}
