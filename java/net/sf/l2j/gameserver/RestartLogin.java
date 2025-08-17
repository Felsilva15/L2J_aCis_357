/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver;

import net.sf.l2j.loginserver.L2LoginServer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.sf.l2j.Config;

public class RestartLogin
{
	//Variaveis globais
	private static RestartLogin _instance = null;
	protected static final Logger _log = Logger.getLogger(RestartLogin.class.getName());
	private Calendar NextRestart;
	private SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	
	//Singleton
	public static RestartLogin getInstance()
	{
		if(_instance == null)
			_instance = new RestartLogin();
		return _instance;
	}
	
//	public String getRestartNextTime()
//	{
//		if(NextRestart.getTime() != null)
//			return format.format(NextRestart.getTime());
//		else
//			return "Erro";
//	}
	public String getRestartNextTime()
	{
	    if (NextRestart.getTime() == null)
	        return "Erro";  // Retorna "Erro" se o tempo não for válido
	    
	    return format.format(NextRestart.getTime());  // Caso contrário, formata o horário
	}

	
	//Connstrutor
	private RestartLogin()
	{
		//:D
	}
	
	public void StartCalculationOfNextRestartTime()
	{
		_log.info("#####################################");
		_log.info("#[Login Restart System]: System actived...#");
		_log.info("#####################################");
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar testStartTime = null;
			long flush2 = 0,timeL = 0;
			int count = 0;
			
			for (String timeOfDay : Config.LOGIN_RESTART_INTERVAL_BY_TIME_OF_DAY)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(Calendar.SECOND, 00);
				//Verifica a validade to tempo
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				//TimeL Recebe o quanto falta de milisegundos para o restart
				timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				
				//Verifica qual horario sera o proximo restart
				if(count == 0){
					flush2 = timeL;
					NextRestart = testStartTime;
				}
				
				if(timeL <  flush2){
					flush2 = timeL;
					NextRestart = testStartTime;
				}
				
				count ++;
			}
			_log.info("[AutoRestart]: Next Login Server Restart Time: " + NextRestart.getTime().toString());
		//	ThreadPoolManager.getInstance().scheduleGeneral(new StartLoginRestartTask(), flush2);
			  // Usar um Timer para agendar o reinício
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    _log.info("Starting automated restart of Login Server.");
                    L2LoginServer.getInstance().shutdown(true);  // reiniciar o servidor de login
                }
            }, flush2); // Agendar para o horário calculado
		}
		catch (Exception e)
		{
			System.out.println("[AutoRestartLogin]: The restart automated server presented error in load restarts period config !");
		}
	}
	 class StartLoginRestartTask implements Runnable
	    {
	        @Override
	        public void run()
	        {
	            _log.info("Starting automated restart of Login Server.");
	            L2LoginServer.getInstance().shutdown(true);  // reiniciar o servidor de login
	        }
	    }
}