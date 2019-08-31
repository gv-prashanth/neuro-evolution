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

	private static final double CHANCE_FOR_GENE_TO_BE_PICKEDUP_FROM_EITHER_OF_PARENT = 0.5d; // half
	private static final double CHANCE_FOR_GENE_DISABLED_IF_DISABLED_IN_BOTH_PARENTS = 0.75d; // 0.75 MEANS 75%
	private static final double CHANCE_FOR_INTER_SPECIES_MATING = 0.001d;

	@Autowired
	private SpeciationService speciationService;

	@Autowired
	private PoolService poolService;

	@Autowired
	private MathService mathService;

	public void crossOver() {
		//System.out.println("pop before crossover " + poolService.getGenomes().size());

		// Inter species mating
		Map<Genome, Genome> fatherMotherPairs = new HashMap<Genome, Genome>();
		Iterator<Genome> allGenomesInPool = poolService.getGenomes().iterator();
		while (allGenomesInPool.hasNext()) {
			Genome parent1 = allGenomesInPool.next();
			if (mathService.randomNumber(0d, 1d) < CHANCE_FOR_INTER_SPECIES_MATING) {
				// This parent is lucky. Lets interspecies cross over it
				Genome parent2;
				try {
					parent2 = poolService.getGenomes().stream().filter(
							p2 -> !p2.getReferenceSpeciesNumber().equalsIgnoreCase(parent1.getReferenceSpeciesNumber()))
							.findAny().get();
				} catch (NoSuchElementException e) {
					// Means the whole pool is of same species. So we cant interspecies crossover.
					parent2 = poolService.getGenomes().stream().findAny().get();
				}
				fatherMotherPairs.put(parent1, parent2);
			}
		}
		fatherMotherPairs.forEach((f, m) -> {
			Genome newGenome = constructGenomeByCrossingOver(f, m);
			newGenome.setReferenceSpeciesNumber(f.getReferenceSpeciesNumber());
		});
		//System.out.println("pop after inter crossover " + poolService.getGenomes().size());

		// Intra species mating
		speciationService.getSpeciesIds().forEach(thisSpeciesId -> {
			int speciesPopToReach = calculateSpeciesPopToReachForThisSpecies(thisSpeciesId);
			int i = speciationService.getNumberOfGenomesInSpecies(thisSpeciesId);
			while (i < speciesPopToReach) {
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
		//System.out.println("pop after intra crossover " + poolService.getGenomes().size());
		int times=0;
		while (poolService.getGenomes().size() < poolService.getPOOLCAPACITY()) {
			int randomPos = (int) mathService.randomNumber(0, speciationService.getSpeciesIds().size() - 1);
			String randomSpeciesId = speciationService.getSpeciesIds().stream().skip(randomPos).findAny().get();
			// pick any two random genomes in this species
			// and then cross over between them
			// and then put them back in the pool with same speciesid
			Genome parent1 = speciationService.getRandomGenomeOfThisSpecies(randomSpeciesId);
			Genome parent2 = speciationService.getRandomGenomeOfThisSpecies(randomSpeciesId);
			Genome newGenome = constructGenomeByCrossingOver(parent1, parent2);
			newGenome.setReferenceSpeciesNumber(randomSpeciesId);
			times++;
		}
		//System.out.println("BIG PROBLEM AVERTED "+times+" times");
	}

	//TODO: Need to visit this
	private int calculateSpeciesPopToReachForThisSpecies(String thisSpeciesId) {
		return (int) ((((double) poolService.getPOOLCAPACITY()) / poolService.getGenomes().size())
				* speciationService.getNumberOfGenomesInSpecies(thisSpeciesId));
	}

	private Genome constructGenomeByCrossingOver(final Genome genome1, final Genome genome2) {
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
			// Just call this method again with reverse order
			return constructGenomeByCrossingOver(genome2, genome1);
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
				.forEachOrdered(thisConnectionGene1Entry -> ConnectionGeneMostlyEmpty1[thisConnectionGene1Entry
						.getReferenceInnovationNumber()] = thisConnectionGene1Entry);
		Arrays.asList(connectionGenes2).stream()
				.forEachOrdered(thisConnectionGene2Entry -> ConnectionGeneMostlyEmpty2[thisConnectionGene2Entry
						.getReferenceInnovationNumber()] = thisConnectionGene2Entry);

		// We shall start by comparing column by column for each connectiongene as in
		// NEAT crossover picture
		Set<ConnectionGene> sampleConnectionGenes = new HashSet<ConnectionGene>();
		for (int i = 0; i < ConnectionGeneMostlyEmpty1.length; i++) {
			if (ConnectionGeneMostlyEmpty1[i] != null && ConnectionGeneMostlyEmpty2[i] != null) {
				// Both present so Pick one of connectionGene
				if (ConnectionGeneMostlyEmpty1[i].isLucky(CHANCE_FOR_GENE_TO_BE_PICKEDUP_FROM_EITHER_OF_PARENT)) {
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
		Genome toReturn = poolService.constructGenomeFromSampleConnectionGenes(sampleConnectionGenes);

		// Lets try to implement CHANCE_FOR_GENE_DISABLED_IF_DISABLED_IN_BOTH_PARENTS
		// logic
		toReturn.getConnectionGenesSorted().forEach(c -> {
			try {
				if (!genome1.getConnectionGenesSorted().stream()
						.filter(g1c -> g1c.getReferenceInnovationNumber() == c.getReferenceInnovationNumber())
						.findFirst().get().isEnabled()
						&& !genome2.getConnectionGenesSorted().stream()
								.filter(g2c -> g2c.getReferenceInnovationNumber() == c.getReferenceInnovationNumber())
								.findFirst().get().isEnabled()) {
					if (c.isLucky(CHANCE_FOR_GENE_DISABLED_IF_DISABLED_IN_BOTH_PARENTS))
						c.setEnabled(false);
					else
						c.setEnabled(true);
				}
			} catch (NoSuchElementException e) {
				// do nothing since the connection is not there in both the parents
			}
		});
		return toReturn;
	}

}
