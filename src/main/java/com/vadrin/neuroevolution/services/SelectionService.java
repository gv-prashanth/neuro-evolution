package com.vadrin.neuroevolution.services;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.Pool;

@Service
public class SelectionService {

	protected static final double FRACTION_OF_TOTAL_POPULATION_RESULTING_FROM_MUTATION_ALONE = 0.25d; // 0.25 MEANS 25%

	public void select(Pool pool) {
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?
		pool.getSpeciesIds().forEach(thisSpeciesId -> {
			if (pool.isSpeciesStagnated(thisSpeciesId) && !pool.championsWhoShouldntBeHarmed().stream().anyMatch(
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

}
