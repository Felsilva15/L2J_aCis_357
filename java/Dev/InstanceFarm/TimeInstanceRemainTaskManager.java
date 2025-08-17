package Dev.InstanceFarm;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.concurrent.ThreadPool;

public class TimeInstanceRemainTaskManager implements Runnable {
    private Player _playerInstance = null;
    private ScheduledFuture<?> _scheduledTask = null; // Armazenando a referência do agendamento
    
    /**
     * Inicializa o teletransportador e inicia a tarefa atrasada.
     * @param playerInstance
     */
    public TimeInstanceRemainTaskManager(Player playerInstance) {
        _playerInstance = playerInstance;
        startTask();
    }
    
    /**
     * Inicia a tarefa que será executada periodicamente.
     */
    private void startTask() {
        // Só inicia a tarefa se ela ainda não estiver agendada ou se foi cancelada
        if (_scheduledTask == null || _scheduledTask.isCancelled()) {
            _scheduledTask = ThreadPool.scheduleAtFixedRate(this, 5 * 1000, 1000); // Executa a cada 1 segundo
        }
    }

    /**
     * Pausa a execução da tarefa.
     */
    public void pauseTask() {
        // Verifica se a tarefa foi agendada e cancela se necessário
        if (_scheduledTask != null && !_scheduledTask.isCancelled()) {
            _scheduledTask.cancel(true);  // Cancela a execução periódica
        }
    }

    @Override
    public void run() {
        if (_playerInstance == null) {
            return;
        }

        // Verifica se o jogador está dentro da zona TIME_FARM
        if (!_playerInstance.isInsideZone(ZoneId.TIME_FARM)) {
            pauseTask();  // Pausa a contagem de tempo se o jogador sair da zona
            return;
        }

        // Caso o jogador esteja dentro da zona, executa a lógica normalmente
        TimeInstanceManager.broadcastTimer(_playerInstance);  // Exibe o tempo restante
    }
    
    /**
     * Método que pode ser chamado quando o jogador retornar à zona de tempo.
     */
    public void resumeTask() {
        // Só retoma a contagem se o jogador estiver dentro da zona
        if (_playerInstance != null && _playerInstance.isInsideZone(ZoneId.TIME_FARM)) {
            startTask();  // Inicia novamente a tarefa de contagem de tempo
        }
    }
}
