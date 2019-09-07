package com.vadrin.neuroevolution.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.Pool;
import com.vadrin.neuroevolution.models.exceptions.InvalidInputException;

@Service
public class NEAT {

	// TODO: Somehow i need to add the bias logic... it wont be coming as part
	// of inputs array but still ill need to accommodate. Read the comments on the
	// mutate method regarding the bias nodes.

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
		pool.increnemtReferenceGenerationCounter();
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
