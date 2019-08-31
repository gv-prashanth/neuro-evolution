package com.vadrin.neuroevolution.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectionService {

	private static final int GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES = 15;
	private static final int NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES = 1;
	private static final int MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED = 5;
	protected static final double FRACTION_OF_TOTAL_POPULATION_RESULTING_FROM_MUTATION_ALONE = 0.25d; // 0.25 MEANS 25%

	@Autowired
	private SpeciationService speciationService;

	@Autowired
	private PoolService poolService;

	public void select() {
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?
		speciationService.getSpeciesIds().forEach(thisSpeciesId -> {
			if (isSpeciesStagnated(thisSpeciesId) && !championsWhoShouldntBeHarmed().stream().anyMatch(
					gid -> poolService.getGenome(gid).getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))) {
				// Remove all species whose average fitness didnt increase since 15 generations
				System.out.println("species stagnanted " + thisSpeciesId);
				speciationService.extinctThisSpeciesAlsoKillOfAnyRemainingGenomes(thisSpeciesId);
			} else {
				poolService.getGenomes().stream()
						.filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
						.sorted((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()))
						.limit((long) ((1 - fractionOfChampionsToSelectInThisSpecies(thisSpeciesId))
								* (poolService.getGenomes().stream()
										.filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)))
												.count()))
						.forEach(toDel -> poolService.killGenome(toDel.getId()));
			}
		});
	}

	private boolean isSpeciesStagnated(String thisSpeciesId) {
		Iterator<Integer> genomesI = speciationService.getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog()
				.keySet().stream().sorted((a, b) -> Integer.compare(b, a))
				.limit(GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES).iterator();
		double prevVal = genomesI.hasNext()
				? speciationService.getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog().get(genomesI.next())
				: 0d;
		if (prevVal == 0d)
			return false;
		int counter = 1;
		while (genomesI.hasNext()) {
			double thisNum = speciationService.getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog()
					.get(genomesI.next());
			if (prevVal <= thisNum) {
				counter++;
			}
		}
		if (counter >= GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES)
			return true;
		return false;
	}

	private double fractionOfChampionsToSelectInThisSpecies(String thisSpeciesId) {
		return FRACTION_OF_TOTAL_POPULATION_RESULTING_FROM_MUTATION_ALONE;
	}

	public Set<String> championsWhoShouldntBeHarmed() {
		Set<String> toReturn = new HashSet<String>();

		// Pick top one in the overall pool
		poolService.getGenomes().stream().sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
				.limit(1).forEachOrdered(g -> toReturn.add(g.getId()));

		// Pick top in each species
		speciationService.getSpeciesIds().stream().forEach(s -> {
			if (speciationService.getNumberOfGenomesInSpecies(
					s) > MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED) {
				poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(s))
						.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
						.limit(NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES)
						.forEachOrdered(g -> toReturn.add(g.getId()));
			}
		});

		return toReturn;
	}

}
