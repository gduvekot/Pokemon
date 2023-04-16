package nl.delphinity.pokemon.model.general;

import nl.delphinity.pokemon.model.area.Area;
import nl.delphinity.pokemon.model.area.Pokecenter;
import nl.delphinity.pokemon.model.battle.Battle;
import nl.delphinity.pokemon.model.item.ItemType;
import nl.delphinity.pokemon.model.trainer.Badge;
import nl.delphinity.pokemon.model.trainer.GymLeader;
import nl.delphinity.pokemon.model.trainer.Trainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {

	private static final ArrayList<Area> areas = new ArrayList<>();
	private static final Scanner sc = new Scanner(System.in);
	private static Trainer trainer = null;

	// set up the game in this static block

	static {

		// PEWTER City
		Pokecenter pewterCenter = new Pokecenter("Pewter City's Pokecenter");
		Area pewterCity = new Area("Pewter city", null, true, null, pewterCenter);
		pewterCity.setContainsPokemon(Arrays.asList(PokemonType.GRASS, PokemonType.FLYING, PokemonType.BUG,
				PokemonType.GROUND, PokemonType.FIRE));

		// VIRIDIAN City
		Pokecenter viridianCenter = new Pokecenter("Viridian City's Pokecenter");
		Area viridianCity = new Area("Viridian city", null, true, pewterCity, viridianCenter);
		viridianCity.setContainsPokemon(
				Arrays.asList(PokemonType.GRASS, PokemonType.FLYING, PokemonType.BUG, PokemonType.GROUND));

		// PALLET Town
		Pokecenter palletCenter = new Pokecenter("Pallet Town's Pokecenter");
		Area palletTown = new Area("Pallet town", null, true, viridianCity, palletCenter);
		palletTown.setContainsPokemon(Arrays.asList(PokemonType.GRASS));

		areas.add(palletTown);
		areas.add(viridianCity);
		areas.add(pewterCity);

		// SETUP gym leaders
		GymLeader pewterLeader = new GymLeader("Bram", new Badge("Boulder Badge"), pewterCity);
		Pokemon p = new Pokemon(PokemonData.ONIX);
		p.setLevel(5);
		p.setOwner(pewterLeader);
		pewterLeader.setActivePokemon(p);
		pewterLeader.getPokemonCollection().add(p);
		pewterCity.setGymLeader(pewterLeader);
	}

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("Wil je een eerdere game laden zeg dan 'ja' zo niet zeg dan 'nee'");
		String a = scan.nextLine();
		if (a.equals("ja")) {
			deserializeTrainer();
		} else {
			System.out.println("Welcome new trainer, what's your name?");
			String name = sc.nextLine();
			trainer = new Trainer(name, areas.get(0));
			System.out.println("Hi, " + trainer.getName());
			Pokemon firstPokemon = chooseFirstPokemon();
			firstPokemon.setOwner(trainer);
			trainer.getPokemonCollection().add(firstPokemon);
			System.out
					.println("You now have " + trainer.getPokemonCollection().size() + " pokemon in your collection!");
		}

		// game loop
		while (true) {
			showGameOptions();
		}
	}
	

	private static void showGameOptions() {
		System.out.println("What do you want to do?");
		System.out.println("1 ) Find Pokemon");
		System.out.println("2 ) My Pokemon");
		System.out.println("3 ) Inventory");
		System.out.println("4 ) Badges");
		System.out.println("5 ) Challenge " + trainer.getCurrentArea().getName() + "'s Gym Leader");
		System.out.println("6 ) Travel");
		System.out.println("7 ) Visit Pokecenter");
		System.out.println("8 ) Save and exit game");
		int action = sc.nextInt();
		switch (action) {
		case 1:
			findAndBattlePokemon();
			break;
		case 2:
			trainer.showPokemonColletion();
			break;
		case 3:
			ItemType item = showInventory();
			if (item != null) {
				trainer.useItem(item, null);
			}
			break;
		case 4:
			trainer.showBadges();
			break;
		case 5:
			if (trainer.getCurrentArea().getGymLeader() != null) {
				startGymBattle();
			} else {
				System.out.println("No Gym Leader in this town!");
			}
			break;
		case 6:
			Area area = showTravel();
			if (area != null) {
				trainer.travel(area);
			}
			break;
		case 7:
			trainer.visitPokeCenter(trainer.getCurrentArea().getPokecenter());
			break;
		case 8:
			quit();
			break;
		default:
			System.out.println("Sorry, that's not a valid option");
			break;
		}
	}

	// TODO: US-PKM-O-6
	private static void findAndBattlePokemon() {
		trainer.battle(trainer.getActivePokemon(), trainer.findPokemon()).start();
	}

	private static Area showTravel() {
		Area travelTo = null;
		int index = 1;
		List<Area> travelToAreas = new ArrayList<>();

		for (Area area : areas) {
			if (!area.equals(trainer.getCurrentArea()) && area.isUnlocked()
					&& ((area.getNextArea() != null && area.getNextArea().equals(trainer.getCurrentArea()))
							|| trainer.getCurrentArea().getNextArea() != null
									&& trainer.getCurrentArea().getNextArea().equals(area))) {
				travelToAreas.add(area);
			}
		}
		for (Area a : travelToAreas) {
			System.out.println(index + ") " + a.getName());
			index++;
		}
		System.out.println(index + ") Back");
		int choice = sc.nextInt();
		if (choice != index) {
			travelTo = travelToAreas.get(choice - 1);
		}
		return travelTo;
	}

	private static ItemType showInventory() {
		HashMap<ItemType, Integer> items = trainer.getInventory().getItems();
		Set<Map.Entry<ItemType, Integer>> entries = items.entrySet();
		int index = 1;
		for (Map.Entry<ItemType, Integer> entry : entries) {
			System.out.println(index + ") " + entry.getKey() + " " + entry.getValue());
			index++;
		}
		System.out.println(index + ") Back");
		int choice = sc.nextInt();
		if (choice != index) {
			return ItemType.values()[choice - 1];
		}
		return null;
	}

	// TODO: US-PKM-O-1
	private static Pokemon chooseFirstPokemon() {
		Pokemon chosenPokemon = null;
		System.out.println("Please choose one of these three pokemon");
		System.out.println("1 ) Charmander");
		System.out.println("2 ) Bulbasaur");
		System.out.println("3 ) Squirtle");

		int choice = sc.nextInt();
		switch (choice) {
		case 1:
			chosenPokemon = new Pokemon(PokemonData.CHARMANDER);
			chosenPokemon.setLevel(5);
			trainer.setActivePokemon(chosenPokemon);
			return chosenPokemon;
		case 2:
			chosenPokemon = new Pokemon(PokemonData.BULBASAUR);
			chosenPokemon.setLevel(5);
			trainer.setActivePokemon(chosenPokemon);
			return chosenPokemon;
		case 3:
			chosenPokemon = new Pokemon(PokemonData.SQUIRTLE);
			chosenPokemon.setLevel(5);
			trainer.setActivePokemon(chosenPokemon);
			return chosenPokemon;
		default:
			System.out.println("geen geldige optie");
			break;
		}

		return null;
	}

	// TODO: US-PKM-O-8
	private static void startGymBattle() {
		GymLeader gymLeader = trainer.getCurrentArea().getGymLeader();
		Battle trainerBattle = trainer.challengeTrainer(gymLeader);
		if (trainerBattle != null && trainerBattle.getWinner().getOwner().equals(trainer)) {
			if (trainerBattle.getEnemy().getOwner().getClass().equals(GymLeader.class)) {
				Pokemon enemyPokemon = trainerBattle.getEnemy();
				gymLeader.setDefeated(true);
				awardBadge(gymLeader.getBadge().getName());
				Area gymLeaderArea = gymLeader.getCurrentArea();
				Area nextArea = gymLeaderArea.getNextArea();
				if (nextArea != null) {
					nextArea.setUnlocked(true);
				}
			}
		}
	}

	// TODO: US-PKM-O-9
	public static void awardBadge(String badgeName) {
		Badge b = new Badge(badgeName);
		trainer.addBadge(b);

	}

	public static void gameOver(String message) {
		System.out.println(message);
		System.out.println("Game over");
		quit();
	}

	// TODO: US-PKM-O-2:
	private static void quit() {
		serializeTrainer();
		int n = 0;
		System.out.println("Game closed!");
		Runtime.getRuntime().exit(n);

	}
	public static void deserializeTrainer() {
	    String filePath = "C:\\Users\\gijsd\\OneDrive\\Documenten\\serialize\\PokemonSpel\\OpslaanTrainer.txt";
	    try (FileInputStream fileIn = new FileInputStream(filePath);
	         ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
	        Trainer trainer = (Trainer) objectIn.readObject();
	        System.out.println("Trainer is geladen. Je naam is: " + trainer.getName());
	        Game.trainer = trainer; // set the trainer object in the Game class
	    } catch (IOException e) {
	        System.err.println("Failed to deserialize trainer from file: " + filePath);
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        System.err.println("Failed to deserialize trainer: the Trainer class was not found.");
	        e.printStackTrace();
	    }
	}

	public static void serializeTrainer() {
	    File file = new File("C:\\Users\\gijsd\\OneDrive\\Documenten\\serialize\\PokemonSpel\\OpslaanTrainer.txt");
	    try (FileOutputStream fileOut = new FileOutputStream(file);
	         ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
	        objectOut.writeObject(trainer);
	        System.out.println("Trainer is opgeslagen in file: " + file.getAbsolutePath());
	    } catch (IOException e) {
	        System.err.println("Failed to serialize trainer to file: " + file.getAbsolutePath());
	        e.printStackTrace();
	    }
	}
}