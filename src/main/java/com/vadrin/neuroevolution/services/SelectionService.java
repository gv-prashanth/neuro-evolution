package com.vadrin.neuroevolution.services;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.Pool;

@Service
public class SelectionService {

	private static final int NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES = 1;
	private static final int MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED = 5;
	protected static final double FRACTION_OF_TOTAL_POPULATION_RESULTING_FROM_MUTATION_ALONE = 0.25d; // 0.25 MEANS 25%

	public void select(Pool pool) {
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?
		pool.getSpeciesIds().forEach(thisSpeciesId -> {
			if (pool.isSpeciesStagnated(thisSpeciesId) && !championsWhoShouldntBeHarmed(pool).stream().anyMatch(
					gid -> pool.getGenome(gid).getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))) {
				// Remove all species whose average fitness didnt increase since 15 generations
				System.out.println("species stagnanted " + thisSpeciesId);
				pool.extinctThisSpeciesAlsoKillOfAnyRemainingGenomes(thisSpeciesId);
			} else {
				pool.getGenomes().stream()
						.filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
						.sorted((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()))
						.limit((long) ((1 - fractionOfChampionsToSelectInThisSpecies(thisSpeciesId))
								* (pool.getGenomes().stream()
										.filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)))
												.count()))
						.forEach(toDel -> pool.killGenome(toDel.getId()));
			}
		});
	}

	private double fractionOfChampionsToSelectInThisSpecies(String thisSpeciesId) {
		return FRACTION_OF_TOTAL_POPULATION_RESULTING_FROM_MUTATION_ALONE;
	}

	public Set<String> championsWhoShouldntBeHarmed(Pool pool) {
		Set<String> toReturn = new HashSet<String>();

		// Pick top one in the overall pool
		pool.getGenomes().stream().sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
				.limit(1).forEachOrdered(g -> toReturn.add(g.getId()));

		// Pick top in each species
		pool.getSpeciesIds().stream().forEach(s -> {
			if (pool.getNumberOfGenomesInSpecies(
					s) > MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED) {
				pool.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(s))
						.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
						.limit(NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES)
						.forEachOrdered(g -> toReturn.add(g.getId()));
			}
		});

		return toReturn;
	}

}
