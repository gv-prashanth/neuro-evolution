package com.vadrin.neuroevolution.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectionService {

	private static final double PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES = 0.20;// 20%
	private static final int GENERATIONTHRESHOLDTOKILLEVERYONEINSPECIES = 15;
	private static final int RANKTOBESOASTOBEBESTINTHEPOOL = 5;

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
					.limit((long) (PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES * (poolService.getGenomes().stream()
							.filter(g -> g.getReferenceSpeciesNumber() == thisSpeciesId)).count()))
					.forEach(toDel -> poolService.killGenome(toDel.getId()));
		});
	}

	public List<String> bestAndMostImportantAndSpeciesWinnersAndNeverKill() {
		// TODO: Need to think if i should limit by 1 or by
		// NUMBEROFCHAMPIONSTOGETWILDCARDENTRYTONEXTGENERATION
		List<String> toReturn = new ArrayList<String>();
		poolService.getGenomes().stream().sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
				.limit(RANKTOBESOASTOBEBESTINTHEPOOL).forEach(g -> toReturn.add(g.getId()));
		speciationService.getSpeciesIds().stream().forEach(s -> {
			poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber() == s)
					.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
					.limit(RANKTOBESOASTOBEBESTINTHEPOOL).forEach(g -> toReturn.add(g.getId()));
		});
		return toReturn;
	}

}
