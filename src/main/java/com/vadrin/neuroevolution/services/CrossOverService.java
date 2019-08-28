package com.vadrin.neuroevolution.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;

@Service
public class CrossOverService {

//TODO: Need to implement the below code
//	if (!ConnectionGeneMostlyEmpty1[i].isEnabled() && !ConnectionGeneMostlyEmpty2[i].isEnabled()
//			&& toAdd.isLucky(1 - CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS)) {
//		toAdd.setEnabled(true);
//	}

//TODO: im not doing interspecies crossover

	private static final double CHANCEFORGENETOBEPICKEDUPFROMEITHEROFPARENT = 0.5d; // half
	private static final double CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS = 0.75d; // 0.75 MEANS 75%
	private static final double CHANCEFOROFFSPRINGFROMMUTATIONALONEWITHOUTCROSSOVER = 0.25d; // 0.25 MEANS 25%
	private static final double CHANCEFORINTERSPECIESMATING = 0.001d;
	private static final double POPULATIONCUTOFASPECIESFORNEXTGENERATION = 0.25d;

	@Autowired
	private SpeciationService speciationService;

	@Autowired
	private PoolService poolService;

	@Autowired
	private MathService mathService;
	
	public void crossOver() {
		// Intra species mating
		speciationService.getSpeciesIds().forEach(thisSpeciesId -> {
			int numberOfOriginalSpeciesPopToReach = speciationService.getPreSelectSpeciesPoolSize(thisSpeciesId);
			int i = speciationService.getNumberOfGenomesInSpecies(thisSpeciesId);
			while (i < numberOfOriginalSpeciesPopToReach * POPULATIONCUTOFASPECIESFORNEXTGENERATION) {
				// pick any two random genomes in this species
				// and then cross over between them
				// and then put them back in the pool with same speciesid
				Genome parent1 = speciationService.getRandomGenomeOfThisSpecies(thisSpeciesId);
				Genome parent2 = speciationService.getRandomGenomeOfThisSpecies(thisSpeciesId);
				Genome newGenome = constructGenomeByCrossingOver(parent1, parent2);
				newGenome.setReferenceSpeciesNumber(thisSpeciesId);
				i++;
			}
		});

		// Inter species mating
		Map<Genome, Genome> fatherMotherPairs = new HashMap<Genome, Genome>();

		int soFarPool = poolService.getGenomes().size();
		Iterator<Genome> fitnessSortedGenomes = poolService.getGenomes().stream()
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).iterator();
		while (fitnessSortedGenomes.hasNext() && soFarPool < poolService.getPOOLCAPACITY()) {
			Genome parent1 = fitnessSortedGenomes.next();
			Genome parent2;
			try {
				parent2 = poolService.getGenomes().stream()
						.filter(p2 -> p2.getReferenceSpeciesNumber() != parent1.getReferenceSpeciesNumber()).findAny()
						.get();
			} catch (NoSuchElementException e) {
				// Means the whole pool is of same species. So we cant interspecies crossover.
				parent2 = poolService.getGenomes().stream().findAny().get();
			}
			fatherMotherPairs.put(parent1, parent2);
			soFarPool++;
		}
		fatherMotherPairs.forEach((f, m) -> {
			Genome newGenome = constructGenomeByCrossingOver(f, m);
			newGenome.setReferenceSpeciesNumber(f.getReferenceSpeciesNumber());
		});

	}

	private Genome constructGenomeByCrossingOver(Genome genome1, Genome genome2) {
		if (poolService.getGenomes().size() >= poolService.getPOOLCAPACITY()) {
			System.out.println("BIG ISSUE HERE... NEED TO SOLVE IT BADLY");
			return null;
		}

		List<ConnectionGene> connectionList1 = genome1.getConnectionGenesSorted();
		List<ConnectionGene> connectionList2 = genome2.getConnectionGenesSorted();
		ConnectionGene[] connectionGenes1 = new ConnectionGene[connectionList1.size()];
		connectionGenes1 = connectionList1.toArray(connectionGenes1);
		ConnectionGene[] connectionGenes2 = new ConnectionGene[connectionList2.size()];
		connectionGenes2 = connectionList2.toArray(connectionGenes2);

		if (connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber() > connectionGenes2[connectionGenes2.length - 1]
						.getReferenceInnovationNumber()) {
			ConnectionGene[] temp = connectionGenes1;
			connectionGenes1 = connectionGenes2;
			connectionGenes2 = temp;

			Genome tempGenome = genome1;
			genome1 = genome2;
			genome2 = tempGenome;
		}

		// post this line, connectiongene1 is having smaller max innovation when
		// compared to that of connectiongene2. So is the case with genom1 & genom2
		// variables
		int connectionGene1MaxInnovationNumber = connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber();

		ConnectionGene[] ConnectionGeneMostlyEmpty1 = new ConnectionGene[(connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber() > connectionGenes2[connectionGenes2.length - 1]
						.getReferenceInnovationNumber()
								? connectionGenes1[connectionGenes1.length - 1].getReferenceInnovationNumber()
								: connectionGenes2[connectionGenes2.length - 1].getReferenceInnovationNumber())
				+ 1];
		ConnectionGene[] ConnectionGeneMostlyEmpty2 = new ConnectionGene[(connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber() > connectionGenes2[connectionGenes2.length - 1]
						.getReferenceInnovationNumber()
								? connectionGenes1[connectionGenes1.length - 1].getReferenceInnovationNumber()
								: connectionGenes2[connectionGenes2.length - 1].getReferenceInnovationNumber())
				+ 1];
		Arrays.asList(connectionGenes1).stream()
				.forEach(thisConnectionGene1Entry -> ConnectionGeneMostlyEmpty1[thisConnectionGene1Entry
						.getReferenceInnovationNumber()] = thisConnectionGene1Entry);
		Arrays.asList(connectionGenes2).stream()
				.forEach(thisConnectionGene2Entry -> ConnectionGeneMostlyEmpty2[thisConnectionGene2Entry
						.getReferenceInnovationNumber()] = thisConnectionGene2Entry);

		// We shall start by comparing column by column for each connectiongene as in
		// NEAT crossover picture
		Set<ConnectionGene> sampleConnectionGenes = new HashSet<ConnectionGene>();
		for (int i = 0; i < ConnectionGeneMostlyEmpty1.length; i++) {
			if (ConnectionGeneMostlyEmpty1[i] != null && ConnectionGeneMostlyEmpty2[i] != null) {
				// Both present so Pick one of connectionGene
				if (ConnectionGeneMostlyEmpty1[i].isLucky(CHANCEFORGENETOBEPICKEDUPFROMEITHEROFPARENT)) {
					sampleConnectionGenes.add(ConnectionGeneMostlyEmpty1[i]);
				} else {
					sampleConnectionGenes.add(ConnectionGeneMostlyEmpty2[i]);
				}
			}
			if ((ConnectionGeneMostlyEmpty1[i] == null && ConnectionGeneMostlyEmpty2[i] != null)
					|| (ConnectionGeneMostlyEmpty2[i] == null && ConnectionGeneMostlyEmpty1[i] != null)) {
				if (i < connectionGene1MaxInnovationNumber) {
					// disjoing genes
					if (ConnectionGeneMostlyEmpty1[i] != null) {
						sampleConnectionGenes.add(ConnectionGeneMostlyEmpty1[i]);
					} else {
						sampleConnectionGenes.add(ConnectionGeneMostlyEmpty2[i]);
					}
				} else {
					// excess genes. Pick only if excess is in max fit parent
					if ((genome2.getFitnessScore() > genome1.getFitnessScore())
							&& ConnectionGeneMostlyEmpty2[i] != null) {
						sampleConnectionGenes.add(ConnectionGeneMostlyEmpty2[i]);
					}
				}
			}
		}
		return poolService.constructGenomeFromSampleConnectionGenes(sampleConnectionGenes);
	}

}
