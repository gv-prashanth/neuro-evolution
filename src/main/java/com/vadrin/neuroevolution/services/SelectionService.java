package com.vadrin.neuroevolution.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.Pool;

@Service
public class SelectionService {

	protected static final double FRACTION_OF_TOTAL_POPULATION_RESULTING_FROM_MUTATION_ALONE = 0.25d; // 0.25 MEANS 25%
	private static final int GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES = 15;
	private static final int NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES = 1;
	private static final int MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED = 5;

	protected void select(Pool pool) {
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?
		pool.getSpecies().forEach(thisSpeciesId -> {
			if (isSpeciesStagnated(pool, thisSpeciesId) && !getChampions(pool).stream()
					.anyMatch(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))) {
				// Remove all species whose average fitness didnt increase since 15 generations
				System.out
						.println("species stagnanted " + thisSpeciesId + ". Killing off all genomes in this species.");
				pool.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
						.forEach(g -> pool.killGenome(g));
			} else {
				pool.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
						.sorted((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()))
						.limit((long) ((1 - fractionOfChampionsToSelectInThisSpecies(thisSpeciesId))
								* (pool.getGenomes().stream()
										.filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)))
												.count()))
						.forEach(toDel -> pool.killGenome(toDel));
			}
		});
	}

	private Set<Genome> getChampions(Pool pool) {
		Set<Genome> toReturn = new HashSet<Genome>();

		// Pick top one in the overall pool
		pool.getGenomes().stream().sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).limit(1)
				.forEachOrdered(g -> toReturn.add(g));

		// Pick top in each species
		pool.getSpecies().stream().forEach(s -> {
			if (pool.getGenomes(s)
					.size() > MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED) {
				pool.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(s))
						.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
						.limit(NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES)
						.forEachOrdered(g -> toReturn.add(g));
			}
		});

		return toReturn;
	}

	private double fractionOfChampionsToSelectInThisSpecies(String thisSpeciesId) {
		return FRACTION_OF_TOTAL_POPULATION_RESULTING_FROM_MUTATION_ALONE;
	}

	private boolean isSpeciesStagnated(Pool pool, String thisSpeciesId) {
		Iterator<Integer> genomesI = getMaxFitGenomeOfThisSpecies(pool, thisSpeciesId).getFitnessLog().keySet().stream()
				.sorted((a, b) -> Integer.compare(b, a))
				.limit(GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES).iterator();
		double descPrevVal = genomesI.hasNext()
				? getMaxFitGenomeOfThisSpecies(pool, thisSpeciesId).getFitnessLog().get(genomesI.next())
				: 0d;
		if (descPrevVal == 0d)
			return false;
		int counter = 1;
		while (genomesI.hasNext()) {
			double thisNum = getMaxFitGenomeOfThisSpecies(pool, thisSpeciesId).getFitnessLog().get(genomesI.next());
			if (descPrevVal <= thisNum) {
				counter++;
			}
		}
		if (counter >= GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES)
			return true;
		return false;
	}

	private Genome getMaxFitGenomeOfThisSpecies(Pool pool, String thisSpeciesId) {
		return pool.getGenomes(thisSpeciesId).stream()
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).limit(1).findFirst().get();
	}
}
