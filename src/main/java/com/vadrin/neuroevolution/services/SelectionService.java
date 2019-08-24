package com.vadrin.neuroevolution.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectionService {

	@Autowired
	SpeciationService speciationService;

	@Autowired
	PoolService poolService;

	private static final double PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES = 0.20;// 20%
	private static final int GENERATIONTHRESHOLDTOKILLEVERYONEINSPECIES = 15;

	public void select() {

		// Remove all species which are almost extinct. I.e Has One genome in them
//		Set<Integer> speciesToKillOff = new HashSet<Integer>();
//		speciationService.getSpeciesIds().forEach(thisSpeciesId -> {
//			if (speciationService.getNumberOfGenomesInSpecies(thisSpeciesId) <= 1) {
//				speciesToKillOff.add(thisSpeciesId);
//			}
//		});
//		speciesToKillOff.forEach(s -> speciationService.extinctThisSpeciesAlsoKillOfAnyRemainingGenomes(s));
		
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?

		speciationService.getSpeciesIds().forEach(thisSpeciesId -> {
			poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)
					.sorted((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()))
					.limit((long) (PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES * (poolService.getGenomes().stream()
							.filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)).count()))
					.forEach(toDel -> poolService.killGenome(toDel.getId()));
		});
	}

}
