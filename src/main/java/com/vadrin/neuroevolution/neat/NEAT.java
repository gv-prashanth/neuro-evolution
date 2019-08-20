package com.vadrin.neuroevolution.neat;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.vadrin.neuroevolution.commons.exceptions.InvalidInputException;
import com.vadrin.neuroevolution.genome.Genome;
import com.vadrin.neuroevolution.genome.GenomesService;

@Controller
public class NEAT {

	@Autowired
	GenomesService genomesService;

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
		genomesService.instantiateRandomGenomePool(poolSize, inputNodesSize, outputNodesSize);
	}

	public Collection<Genome> getGenomes() {
		return genomesService.getAllGenomes();
	}

	public double[] process(String genomeId, double[] input) throws InvalidInputException {
		return feedForwardService.feedForward(genomesService.getGenome(genomeId), input);
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
		System.out.println(new Timestamp(System.currentTimeMillis()));
		System.out.println("Current population is "+sortedBestGenomeInPool().size());
	}

	public List<Genome> sortedBestGenomeInPool() {
		return genomesService.getAllGenomes().stream()
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
				.collect(Collectors.toList());
	}

	public void setFitnessScore(String genomeId, double fitnessScore) {
		genomesService.setFitnessScore(genomeId, fitnessScore);
	}

}
