package com.vadrin.neuroevolution.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectionService {

	@Autowired
	SpeciationService speciationService;

	@Autowired
	PoolService poolService;

	private static final double PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES = 0.5;// 50%
	private static final int GENERATIONTHRESHOLDTOKILLEVERYONEINSPECIES = 15;

	public void select() {
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?

		speciationService.getSpeciesPool().keySet().forEach(thisSpeciesId -> {
			poolService.getAllGenomes().stream().filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)
					.sorted((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()))
					.limit((long) (PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES * (poolService.getAllGenomes().stream()
							.filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)).count()))
					.forEach(toDel -> poolService.killGenome(toDel.getId()));
		});
	}

}
