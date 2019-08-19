package com.vadrin.neuroevolution.controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.exceptions.InvalidInputException;
import com.vadrin.neuroevolution.services.CrossOverService;
import com.vadrin.neuroevolution.services.FeedForwardService;
import com.vadrin.neuroevolution.services.GenomesService;
import com.vadrin.neuroevolution.services.SelectionService;
import com.vadrin.neuroevolution.services.SpeciationService;

@Controller
public class NEAT {

	private static final int GENERATIONTHRESHOLDTOKILLEVERYONEINSPECIES = 15;
	private static final int NUMBEROFCHAMPIONSTOGETWILDCARDENTRYTONEXTGENERATION = 1; // ASSUSMING GENOMES IN SPECIES IS
																						// // > 5
	private static final double CHANCEFORWEIGHTMUTATION = 0.8d; // 0.8 MEANS 80%
	private static final double CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT = 0.1d; // 0.1 MEANS 10%
	private static final double PERTUBEDVARIANCEDIFFERENCE = 0.05d;
	private static final double CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS = 0.75d; // 0.75 MEANS 75%
	private static final double CHANCEFOROFFSPRINGFROMMUTATIONALONEWITHOUTCROSSOVER = 0.25d; // 0.25 MEANS 25%
	private static final double CHANCEFORINTERSPECIESMATING = 0.001d;
	private static final double CHANCEFORADDINGNEWNODE = 0.03d;
	private static final double CHANCEFORTOGGLEENABLEDISABLE = 0.03d;
	private static final double CHANCEFORADDINGNEWCONNECTION = 0.05d;

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

	public void instantiateNEAT(int poolSize, int inputNodesSize, int outputNodesSize) {
		for (int i = 0; i < poolSize; i++) {
			genomesService.constructRandomGenome(inputNodesSize, outputNodesSize);
		}
	}

	public Collection<Genome> getGenomes() {
		return genomesService.getAllGenomes();
	}

	public double[] process(String genomeId, double[] input) throws InvalidInputException {
		return feedForwardService.feedForward(genomesService.getGenome(genomeId), input);
	}

	public void stepOneGeneration() {
		// Find the speciesId for each species
		speciationService.speciate();
		// Top 50% of genomes in each species are selected.
		selectionService.select();
		// within the species select two random parents are re populate the pool
		crossOverService.crossOver();
		// mutate the ONLY NEW ONES OR ALL???
		mutate();
	}

}
