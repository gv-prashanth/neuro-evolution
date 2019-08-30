package com.vadrin.neuroevolution.controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.exceptions.InvalidInputException;
import com.vadrin.neuroevolution.services.CrossOverService;
import com.vadrin.neuroevolution.services.FeedForwardService;
import com.vadrin.neuroevolution.services.MutationService;
import com.vadrin.neuroevolution.services.PoolService;
import com.vadrin.neuroevolution.services.SelectionService;
import com.vadrin.neuroevolution.services.SpeciationService;

@Controller
public class NEAT {

	@Autowired
	PoolService poolService;

	@Autowired
	FeedForwardService feedForwardService;

	@Autowired
	SpeciationService speciationService;

	@Autowired
	SelectionService selectionService;

	@Autowired
	CrossOverService crossOverService;

	@Autowired
	MutationService mutationService;

	public void instantiateNEAT(int poolSize, int inputNodesSize, int outputNodesSize) {
		poolService.startNewGeneration();
		poolService.constructRandomGenomePool(poolSize, inputNodesSize, outputNodesSize);
	}

	public Collection<Genome> getGenomes() {
		return poolService.getGenomes();
	}

	public double[] process(String genomeId, double[] input) throws InvalidInputException {
		return feedForwardService.feedForward(poolService.getGenome(genomeId), input);
	}

	public void stepOneGeneration() {
		// increase generation counter
		poolService.startNewGeneration();
		// Load the speciesId for each species
		speciationService.speciate();
		// Top x% of genomes in each species are selected.
		selectionService.select();
		// within the species select two random parents are re populate the pool
		crossOverService.crossOver();
		// mutate all except some best
		mutationService.mutate();
	}

	public int getGeneration() {
		return poolService.getGENERATION();
	}

}
