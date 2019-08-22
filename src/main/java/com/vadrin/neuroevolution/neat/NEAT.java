package com.vadrin.neuroevolution.neat;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.vadrin.neuroevolution.commons.exceptions.InvalidInputException;
import com.vadrin.neuroevolution.genome.Genome;

@Controller
public class NEAT {

	@Autowired
	GenomesPool genomesPool;

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
		genomesPool.constructRandomGenomePool(poolSize, inputNodesSize, outputNodesSize);
	}

	public Collection<Genome> getGenomes() {
		return genomesPool.getAllGenomes();
	}

	public double[] process(String genomeId, double[] input) throws InvalidInputException {
		return feedForwardService.feedForward(genomesPool.getGenome(genomeId), input);
	}

	public void stepOneGeneration() {
		printPool();
		// Find the speciesId for each species
		speciationService.speciate();
		printPool();
		// Top 50% of genomes in each species are selected.
		selectionService.select();
		printPool();
		// within the species select two random parents are re populate the pool
		crossOverService.crossOver();
		printPool();
		// mutate the ONLY NEW ONES OR ALL???
		mutationService.mutate();
		printPool();
	}

	private void printPool() {
//		System.out.println(new Timestamp(System.currentTimeMillis()));
//		System.out.println("Current population is "+sortedBestGenomeInPool().size());
	}

	public List<Genome> sortedBestGenomeInPool() {
		return genomesPool.getAllGenomes().stream()
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
				.collect(Collectors.toList());
	}

	public void setFitnessScore(String genomeId, double fitnessScore) {
		genomesPool.getGenome(genomeId).setFitnessScore(fitnessScore);
	}

}
