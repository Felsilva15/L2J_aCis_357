package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.handler.itemhandlers.BeastSoulShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSpice;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BlessedSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.Book;
import net.sf.l2j.gameserver.handler.itemhandlers.Calculator;
import net.sf.l2j.gameserver.handler.itemhandlers.Elixir;
import net.sf.l2j.gameserver.handler.itemhandlers.EnchantScrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.FishShots;
import net.sf.l2j.gameserver.handler.itemhandlers.Harvester;
import net.sf.l2j.gameserver.handler.itemhandlers.ItemSkills;
import net.sf.l2j.gameserver.handler.itemhandlers.Keys;
import net.sf.l2j.gameserver.handler.itemhandlers.Maps;
import net.sf.l2j.gameserver.handler.itemhandlers.MercTicket;
import net.sf.l2j.gameserver.handler.itemhandlers.PaganKeys;
import net.sf.l2j.gameserver.handler.itemhandlers.PetFood;
import net.sf.l2j.gameserver.handler.itemhandlers.Recipes;
import net.sf.l2j.gameserver.handler.itemhandlers.RollingDice;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfResurrection;
import net.sf.l2j.gameserver.handler.itemhandlers.SeedHandler;
import net.sf.l2j.gameserver.handler.itemhandlers.SevenSignsRecord;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulCrystals;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulShots;
import net.sf.l2j.gameserver.handler.itemhandlers.SpecialXMas;
import net.sf.l2j.gameserver.handler.itemhandlers.SpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.SummonItems;
import net.sf.l2j.gameserver.handler.itemhandlers.aio.Aio15days;
import net.sf.l2j.gameserver.handler.itemhandlers.aio.Aio24h;
import net.sf.l2j.gameserver.handler.itemhandlers.aio.Aio30days;
import net.sf.l2j.gameserver.handler.itemhandlers.aio.Aio7days;
import net.sf.l2j.gameserver.handler.itemhandlers.aio.AioEterno;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.ClanLevel6;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.ClanLevel7;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.ClanLevel8;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.ClanReputation;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillAegis;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillAgility;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillClarity;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillCyclonicResistance;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillEmpowerment;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillEssence;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillFortitude;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillFreedom;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillGuidance;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillImperium;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillLifeblood;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillLuck;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillMagicProtection;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillMagmaticResistance;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillMarch;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillMight;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillMorale;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillShieldBoost;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillSpirituality;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillVigilance;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillVitality;
import net.sf.l2j.gameserver.handler.itemhandlers.clan.SkillWithstandAttack;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.AllyNameChange;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.ClanFull;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.ClanNameChange;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.ClassItem;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.DeletePk;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.InfinityStone;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.LevelCoin;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.LuckBox;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.LuckBox2;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.NameChange;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.NoblesItem;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.RoletaItem;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.SexCoin;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.Skins;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.TeleportBook;
import net.sf.l2j.gameserver.handler.itemhandlers.hero.Hero15days;
import net.sf.l2j.gameserver.handler.itemhandlers.hero.Hero24h;
import net.sf.l2j.gameserver.handler.itemhandlers.hero.Hero30days;
import net.sf.l2j.gameserver.handler.itemhandlers.hero.Hero7days;
import net.sf.l2j.gameserver.handler.itemhandlers.hero.HeroEterno;
import net.sf.l2j.gameserver.handler.itemhandlers.vip.Vip15days;
import net.sf.l2j.gameserver.handler.itemhandlers.vip.Vip24h;
import net.sf.l2j.gameserver.handler.itemhandlers.vip.Vip30days;
import net.sf.l2j.gameserver.handler.itemhandlers.vip.Vip7days;
import net.sf.l2j.gameserver.handler.itemhandlers.vip.VipEterno;
import net.sf.l2j.gameserver.model.item.kind.EtcItem;

import java.util.HashMap;
import java.util.Map;

public class ItemHandler
{
	private final Map<Integer, IItemHandler> _datatable = new HashMap<>();
	
	public static ItemHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ItemHandler()
	{
		registerItemHandler(new RoletaItem());
		registerItemHandler(new TeleportBook());
		registerItemHandler(new LuckBox2());
		registerItemHandler(new BeastSoulShot());
		registerItemHandler(new BeastSpice());
		registerItemHandler(new BeastSpiritShot());
		registerItemHandler(new BlessedSpiritShot());
		registerItemHandler(new Book());
		registerItemHandler(new Calculator());
		registerItemHandler(new Elixir());
		registerItemHandler(new EnchantScrolls());
		registerItemHandler(new FishShots());
		registerItemHandler(new Harvester());
		registerItemHandler(new ItemSkills());
		registerItemHandler(new Keys());
		registerItemHandler(new Maps());
		registerItemHandler(new MercTicket());
		registerItemHandler(new PaganKeys());
		registerItemHandler(new PetFood());
		registerItemHandler(new Recipes());
		registerItemHandler(new RollingDice());
		registerItemHandler(new ScrollOfResurrection());
		registerItemHandler(new SeedHandler());
		registerItemHandler(new SevenSignsRecord());
		registerItemHandler(new SoulShots());
		registerItemHandler(new SpecialXMas());
		registerItemHandler(new SoulCrystals());
		registerItemHandler(new SpiritShot());
		registerItemHandler(new SummonItems());
		// custom
		registerItemHandler(new AllyNameChange());
		registerItemHandler(new ClanNameChange());
	    registerItemHandler(new Aio24h());
	    registerItemHandler(new Aio7days());
	    registerItemHandler(new Aio15days());
	    registerItemHandler(new Aio30days());
	    registerItemHandler(new AioEterno());
		registerItemHandler(new LuckBox());
		registerItemHandler(new ClanFull());
		registerItemHandler(new NoblesItem());
		registerItemHandler(new Vip24h());
		registerItemHandler(new Vip7days());
		registerItemHandler(new Vip15days());
		registerItemHandler(new Vip30days());
		registerItemHandler(new VipEterno());
		registerItemHandler(new Hero24h());
		registerItemHandler(new Hero7days());
		registerItemHandler(new Hero15days());
		registerItemHandler(new Hero30days());
		registerItemHandler(new HeroEterno());
		registerItemHandler(new ClassItem());
		registerItemHandler(new NameChange());
		registerItemHandler(new SexCoin());
		registerItemHandler(new LevelCoin());
		registerItemHandler(new Skins());
		registerItemHandler(new DeletePk());
		registerItemHandler(new InfinityStone());
        //Clan Items
		registerItemHandler(new ClanLevel6());
		registerItemHandler(new ClanLevel7());
		registerItemHandler(new ClanLevel8());
		registerItemHandler(new ClanReputation());
		registerItemHandler(new SkillAegis());
		registerItemHandler(new SkillAgility());
		registerItemHandler(new SkillClarity());
		registerItemHandler(new SkillCyclonicResistance());
		registerItemHandler(new SkillEmpowerment());
		registerItemHandler(new SkillEssence());
		registerItemHandler(new SkillFortitude());
		registerItemHandler(new SkillFreedom());
		registerItemHandler(new SkillGuidance());
		registerItemHandler(new SkillImperium());
		registerItemHandler(new SkillLifeblood());
		registerItemHandler(new SkillLuck());
		registerItemHandler(new SkillMagicProtection());
		registerItemHandler(new SkillMagmaticResistance());
		registerItemHandler(new SkillMarch());
		registerItemHandler(new SkillMight());
		registerItemHandler(new SkillMorale());
		registerItemHandler(new SkillShieldBoost());
		registerItemHandler(new SkillSpirituality());
		registerItemHandler(new SkillVigilance());
		registerItemHandler(new SkillVitality());
		registerItemHandler(new SkillWithstandAttack());
	
	}
	
	public void registerItemHandler(IItemHandler handler)
	{
		_datatable.put(handler.getClass().getSimpleName().intern().hashCode(), handler);
	}
	
	public IItemHandler getItemHandler(EtcItem item)
	{
		if (item == null || item.getHandlerName() == null)
			return null;
		
		return _datatable.get(item.getHandlerName().hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler _instance = new ItemHandler();
	}
}