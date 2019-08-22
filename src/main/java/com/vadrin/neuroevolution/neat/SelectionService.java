package com.vadrin.neuroevolution.neat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectionService {

	@Autowired
	SpeciationService speciationService;

	@Autowired
	GenomesPool genomesPool;

	private static final double PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES = 0.5;// 50%
	private static final int GENERATIONTHRESHOLDTOKILLEVERYONEINSPECIES = 15;

	protected void select() {
		// sort within each species
		// Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?

		speciationService.getSpeciesPool().keySet().forEach(thisSpeciesId -> {
			genomesPool.getAllGenomes().stream().filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)
					.sorted((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()))
					.limit((long) (PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES * (genomesPool.getAllGenomes().stream()
							.filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)).count()))
					.forEach(toDel -> genomesPool.killGenome(toDel.getId()));
		});
	}

}
