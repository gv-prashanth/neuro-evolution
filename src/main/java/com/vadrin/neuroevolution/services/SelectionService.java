package com.vadrin.neuroevolution.services;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectionService {

	private static final double X_PERCENTAGE_OF_CHAMPIONS_TO_SELECT_IN_EACH_SPECIES = 0.20;// 20%
	private static final int GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES = 15;
	// TODO: Need to think if i should limit by 1 or 5
	private static final int NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES = 1;
	private static final int MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED = 5;

	@Autowired
	private SpeciationService speciationService;

	@Autowired
	private PoolService poolService;
	
	public void select() {

		// Remove all species whose average fitness didnt increase since 15 generations
		//TODO: Needs development
		
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?
		speciationService.getSpeciesIds().forEach(thisSpeciesId -> {
			poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)
					.sorted((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()))
					.limit((long) (X_PERCENTAGE_OF_CHAMPIONS_TO_SELECT_IN_EACH_SPECIES * (poolService.getGenomes().stream()
							.filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)).count()))
					.forEach(toDel -> poolService.killGenome(toDel.getId()));
		});
	}
	
	public Set<String> championsWhoShouldntBeHarmed() {
		Set<String> toReturn = new HashSet<String>();
		
		//Pick top overall in the pool
		poolService.getGenomes().stream().sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
		.limit(1).forEach(g -> toReturn.add(g.getId()));			
		
		//Pick top in each species
		speciationService.getSpeciesIds().stream().forEach(s -> {
			if(speciationService.getNumberOfGenomesInSpecies(s)>MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED) {
				poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber() == s)
						.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
						.limit(NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES).forEach(g -> toReturn.add(g.getId()));	
			}
		});
		
		return toReturn;
	}

}
