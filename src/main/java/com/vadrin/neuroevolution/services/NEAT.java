package com.vadrin.neuroevolution.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.Pool;
import com.vadrin.neuroevolution.models.exceptions.InvalidInputException;

@Service
public class NEAT {

	@Autowired
	private FeedForwardService feedForwardService;

	@Autowired
	private SpeciationService speciationService;

	@Autowired
	private SelectionService selectionService;

	@Autowired
	private CrossOverService crossOverService;

	@Autowired
	private MutationService mutationService;

	public double[] process(Genome genome, double[] input) throws InvalidInputException {
		return feedForwardService.feedForward(genome, input);
	}

	public void stepOneGeneration(Pool pool) {
		// increase generation counter
		pool.startNewGeneration();
		// Load the speciesId for each species
		speciationService.speciate(pool);
		// Top x% of genomes in each species are selected.
		selectionService.select(pool);
		// within the species select two random parents are re populate the pool
		crossOverService.crossOver(pool);
		// mutate all except some best
		mutationService.mutate(pool);
	}

}
