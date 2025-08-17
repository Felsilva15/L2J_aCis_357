package Dev.AutoFarm;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class AutofarmConstants 
{
    public final static List<Integer> attackSlots = Arrays.asList(0, 1, 2, 3);
    public final static List<Integer> chanceSlots = Arrays.asList(4, 5, 6, 7);
    public final static List<Integer> lowLifeSlots = Arrays.asList(8, 9, 10, 11);
    
    public static Integer useMpPotsPercentageThreshold = 40;
    public static Integer useHpPotsPercentageThreshold = 70;
    public static Integer mpPotItemId = 728;
    public static Integer hpPotItemId = 1539;
    public static Integer hpPotSkillId = 2037;
    
    public static void useSkills(List<Integer> skillSlots)
    {
        List<Integer> availableSkills = new ArrayList<>(skillSlots); // Copia da lista original para manipular as disponíveis

        // Continuar enquanto houver habilidades disponíveis
        while (!availableSkills.isEmpty())
        {
            // Escolhe uma habilidade aleatória de entre as disponíveis
            int randomIndex = (int) (Math.random() * availableSkills.size());
            Integer selectedSkill = availableSkills.get(randomIndex);
            
            // Usa a habilidade selecionada
            System.out.println("Usando habilidade no slot " + selectedSkill);
            
            // Remove a habilidade da lista de disponíveis após usá-la
            availableSkills.remove(randomIndex);
        }
    }

    public static void main(String[] args)
    {
        // Testa a função de uso das habilidades para cada conjunto de slots
        System.out.println("Usando habilidades de ataque:");
        useSkills(attackSlots);
        
        System.out.println("\nUsando habilidades de chance:");
        useSkills(chanceSlots);
        
        System.out.println("\nUsando habilidades de lowLife:");
        useSkills(lowLifeSlots);
    }
}
