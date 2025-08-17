package Dev.ItemsTime;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.ConnectionPool;

public class TimedItemManager {

    public final Map<Integer, Info> _timedItems = new HashMap<>();
    private static final Logger _log = Logger.getLogger(TimedItemManager.class.getName());
    
    public class Info {
        int _charId;
        int _itemId;
        public long _activationTime;
    }

    public static final TimedItemManager getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final TimedItemManager _instance = new TimedItemManager();
    }

    public TimedItemManager() {
        restore();
        _startControlTask.schedule(60000);  // Run every 60 seconds
    }

    public boolean getActiveTimed(Player pl, boolean trade) {
        for (Info i : _timedItems.values()) {
            if (i != null && i._charId == pl.getObjectId()) {
                ItemInstance item = pl.getInventory().getItemByObjectId(i._itemId);
                if (item != null && System.currentTimeMillis() < i._activationTime) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void destroy(ItemInstance item) {
        Info inf = _timedItems.remove(item.getObjectId());
        if (inf != null) {
            try (Connection con = ConnectionPool.getConnection()) {
                try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_timed_items WHERE charId = ? AND itemId = ?")) {
                    statement.setInt(1, inf._charId);
                    statement.setInt(2, inf._itemId);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                _log.warning("Failed to delete timed item from DB: " + e.getMessage());
            }
        }
    }

//    public synchronized void setTimed(ItemInstance item) {
//        // Obtém o item do mapa com base no ID do objeto
//        Info inf = _timedItems.get(item.getObjectId());
//
//        // Se o item ainda não foi registrado
//        if (inf == null) {
//            inf = new Info();
//            inf._itemId = item.getObjectId();
//            inf._charId = item.getOwnerId();
//            
//            // Define o tempo de expiração (agora)
//            inf._activationTime = System.currentTimeMillis() / 1000 + (Config.TIMED_ITEM_TIME * 60); // tempo em segundos
//            _timedItems.put(inf._itemId, inf);  // Adiciona o item ao mapa
//
//            // Log para depuração
//            System.out.println("Item " + inf._itemId + " registrado com tempo de expiração: " + inf._activationTime);
//        } else {
//            // Atualiza o dono do item, se necessário
//            inf._charId = item.getOwnerId();
//        }
//
//        // Verifica imediatamente se o item já expirou
//        long currentTime = System.currentTimeMillis() / 1000;
//        if (inf._activationTime < currentTime) {
//            // Se já expirou, elimina o item imediatamente
//            delete(inf);  // Exclui o item imediatamente
//        } else {
//            // Caso contrário, salva o item no banco
//            saveToDb(inf);
//        }
//    }
//    public synchronized void setTimed(ItemInstance item) {
//        // Obtém o item do mapa com base no ID do objeto
//        Info inf = _timedItems.get(item.getObjectId());
//
//        // Se o item ainda não foi registrado
//        if (inf == null) {
//            inf = new Info();
//            inf._itemId = item.getObjectId();
//            inf._charId = item.getOwnerId();
//
//            // Define o tempo de expiração com base em dias (1 dia = 86400 segundos)
//            inf._activationTime = System.currentTimeMillis() / 1000 + (Config.TIMED_ITEM_TIME * 86400);
//            _timedItems.put(inf._itemId, inf);  // Adiciona o item ao mapa
//
//            // Log para depuração com data formatada
//            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//            String formattedDate = sdf.format(new java.util.Date(inf._activationTime * 1000));
//   //         System.out.println("Item " + inf._itemId + " registrado com tempo de expiração: " + formattedDate);
//        } else {
//            // Atualiza o dono do item, se necessário
//            inf._charId = item.getOwnerId();
//        }
//
//        // Verifica imediatamente se o item já expirou
//        long currentTime = System.currentTimeMillis() / 1000;
//        if (inf._activationTime < currentTime) {
//            // Se já expirou, elimina o item imediatamente
//            delete(inf);
//        } else {
//            // Caso contrário, salva o item no banco
//            saveToDb(inf);
//        }
//    }
//    public synchronized void setTimed(ItemInstance item) {
//        // Obtém o item do mapa com base no ID do objeto
//        Info inf = _timedItems.get(item.getObjectId());
//
//        // Se o item ainda não foi registrado
//        if (inf == null) {
//            inf = new Info();
//            inf._itemId = item.getObjectId();
//            inf._charId = item.getOwnerId();
//
//            // Define o tempo de expiração com base em dias (1 dia = 86400 segundos)
//            inf._activationTime = System.currentTimeMillis() / 1000 + (Config.TIMED_ITEM_TIME * 86400);
//            _timedItems.put(inf._itemId, inf);  // Adiciona o item ao mapa
//
//            // Log para depuração com data formatada
//            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//            String formattedDate = sdf.format(new java.util.Date(inf._activationTime * 1000));
//            System.out.println("Item " + inf._itemId + " registrado com tempo de expiração: " + formattedDate);
//        } else {
//            // Atualiza o dono do item, se necessário
//            inf._charId = item.getOwnerId();
//        }
//
//        // Verifica imediatamente se o item já expirou
//        long currentTime = System.currentTimeMillis() / 1000;
//        if (inf._activationTime < currentTime) {
//            System.out.println("Item " + inf._itemId + " já expirou. Eliminando...");
//            delete(inf);
//        } else {
//            long segundosRestantes = inf._activationTime - currentTime;
//            long horas = segundosRestantes / 3600;
//            long minutos = (segundosRestantes % 3600) / 60;
//            long segundos = segundosRestantes % 60;
//
//            System.out.println("Item " + inf._itemId + " salvo no banco. Expira em: " + horas + "h " + minutos + "m " + segundos + "s.");
//            saveToDb(inf);
//        }
//    }

    public synchronized void setTimed(ItemInstance item) {
        // Obtém o item do mapa com base no ID do objeto
        Info inf = _timedItems.get(item.getObjectId());

        // Se o item ainda não foi registrado
        if (inf == null) {
            inf = new Info();
            inf._itemId = item.getObjectId();
            inf._charId = item.getOwnerId();

            // Define o tempo de expiração com base em dias (1 dia = 86400 segundos)
            inf._activationTime = System.currentTimeMillis() / 1000 + (Config.TIMED_ITEM_TIME * 86400);
            _timedItems.put(inf._itemId, inf);  // Adiciona o item ao mapa

            // Log para depuração com data formatada
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String formattedDate = sdf.format(new java.util.Date(inf._activationTime * 1000));
         //   System.out.println("Item " + inf._itemId + " registrado com tempo de expiração: " + formattedDate);
        } else {
            // Atualiza o dono do item, se necessário
            inf._charId = item.getOwnerId();
        }

        // Verifica imediatamente se o item já expirou
        long currentTime = System.currentTimeMillis() / 1000;
        if (inf._activationTime < currentTime) {
      //      System.out.println("Item " + inf._itemId + " já expirou. Eliminando...");
            delete(inf);
        } else {
            long segundosRestantes = inf._activationTime - currentTime;
            long horas = segundosRestantes / 3600;
            long minutos = (segundosRestantes % 3600) / 60;
            long segundos = segundosRestantes % 60;

    //        System.out.println("Item " + inf._itemId + " salvo no banco. Expira em: " + horas + "h " + minutos + "m " + segundos + "s.");
            saveToDb(inf);
        }
    }


    
    public boolean isActive(ItemInstance item) {
        return _timedItems.containsKey(item.getObjectId());
    }

    private void restore() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT charId, itemId, time FROM character_timed_items");
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Info inf = new Info();
                inf._charId = rs.getInt("charId");
                inf._itemId = rs.getInt("itemId");
                inf._activationTime = rs.getLong("time");
                _timedItems.put(inf._itemId, inf);
            }
            _log.info("TimedItems: loaded " + _timedItems.size() + " items.");
        } catch (SQLException e) {
            _log.warning("Failed to restore timed items from DB: " + e.getMessage());
        }
    }

    private static void saveToDb(Info temp) {
        try (Connection con = ConnectionPool.getConnection()) {
            try (PreparedStatement statement = con.prepareStatement("UPDATE character_timed_items SET charId = ? WHERE itemId = ?")) {
                statement.setInt(1, temp._charId);
                statement.setInt(2, temp._itemId);
                if (statement.executeUpdate() == 0) {
                    try (PreparedStatement insertStmt = con.prepareStatement("INSERT INTO character_timed_items (charId, itemId, time) VALUES (?, ?, ?)")) {
                        insertStmt.setInt(1, temp._charId);
                        insertStmt.setInt(2, temp._itemId);
                        insertStmt.setLong(3, temp._activationTime);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            _log.warning("Failed to save timed item to DB: " + e.getMessage());
        }
    }

    public void delete(Info temp) {
        _timedItems.remove(temp._itemId);

        Player pl = World.getInstance().getPlayer(temp._charId);
        if (pl != null) {
            ItemInstance item = pl.getInventory().getItemByObjectId(temp._itemId);
            if (item != null) {
                // Remover o item do inventário antes de fazer qualquer atualização no banco de dados
                if (item.isEquipped()) {
                    pl.getInventory().unEquipItemInSlot(item.getLocationSlot());
                }
                pl.getInventory().destroyItem("timeLost", item, pl, pl);
                pl.sendPacket(new ItemList(pl, false));

                SystemMessage msg = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
                msg.addItemName(item.getItemId());
                pl.sendPacket(msg);

                // Agora, podemos remover do banco de dados
                try (Connection con = ConnectionPool.getConnection()) {
                    try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_timed_items WHERE charId = ? AND itemId = ?")) {
                        statement.setInt(1, temp._charId);
                        statement.setInt(2, temp._itemId);
                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    _log.warning("Failed to delete timed item from DB: " + e.getMessage());
                }
            }
        } else {
            // Caso o player esteja offline, remover o item diretamente da tabela de itens
            try (Connection con = ConnectionPool.getConnection()) {
                if (temp._charId != 0) {
                    try (PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? AND object_id = ?")) {
                        statement.setInt(1, temp._charId);
                        statement.setInt(2, temp._itemId);
                        statement.executeUpdate();
                    }
                } else {
                    for (WorldObject o : World.getInstance().getObjects()) {
                        if (o.getObjectId() == temp._itemId) {
                        //    World.getInstance().removeVisibleObject(o, o.getRegion());
                        	World.getInstance().removeObject(o);
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                _log.warning("Failed to remove item from world DB: " + e.getMessage());
            }
        }
    }


    private void removeItemFromWorld(Info temp) {
        try (Connection con = ConnectionPool.getConnection()) {
            // Se o jogador não estiver online, remove o item do banco de dados do mundo
            if (temp._charId != 0) {
                try (PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? AND object_id = ?")) {
                    statement.setInt(1, temp._charId);
                    statement.setInt(2, temp._itemId);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        _log.info("Item " + temp._itemId + " removido da tabela de items para o jogador " + temp._charId);
                    } else {
                        _log.warning("Nenhuma linha afetada na remoção do item " + temp._itemId + " do banco de dados.");
                    }
                }
            } else {
                // Se o charId for 0, significa que o item está no mundo e não atribuído a um jogador.
                for (WorldObject o : World.getInstance().getObjects()) {
                    if (o.getObjectId() == temp._itemId) {
               //         World.getInstance().removeVisibleObject(o, o.getRegion());
                    	World.getInstance().removeObject(o);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            _log.warning("Falha ao remover item do banco de dados do mundo: " + e.getMessage());
        }
    }

    private final ExclusiveTask _startControlTask = new ExclusiveTask() {
        @Override
        protected void onElapsed() {
            long currentTime = System.currentTimeMillis() / 1000;
            for (Info temp : _timedItems.values()) {
                if (temp._activationTime < currentTime) {
                    delete(temp);
                }
            }
            schedule(60000); // Run every 60 seconds
        }
    };
 // In TimedItemManager.java
    public long getRemainingTime(int itemObjectId) {
        Info itemInfo = _timedItems.get(itemObjectId);

        if (itemInfo != null) {
            long currentTime = System.currentTimeMillis() / 1000;
            long remainingTime = itemInfo._activationTime - currentTime;

            // Return remaining time if it's greater than 0
            if (remainingTime > 0) {
                return remainingTime;
            }
        }
        return 0; // Item expired or not found
    }

}