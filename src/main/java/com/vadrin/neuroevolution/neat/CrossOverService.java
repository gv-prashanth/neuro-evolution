package com.vadrin.neuroevolution.neat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.MathService;
import com.vadrin.neuroevolution.genome.ConnectionGene;
import com.vadrin.neuroevolution.genome.Genome;
import com.vadrin.neuroevolution.genome.GenomesService;

@Service
public class CrossOverService {

//TODO: Need to implement the below code
//	if (!ConnectionGeneMostlyEmpty1[i].isEnabled() && !ConnectionGeneMostlyEmpty2[i].isEnabled()
//			&& toAdd.isLucky(1 - CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS)) {
//		toAdd.setEnabled(true);
//	}

	@Autowired
	MathService mathService;

	@Autowired
	SpeciationService speciationService;

	@Autowired
	GenomesService genomesService;

	private static final double CHANCEFORGENETOBEPICKEDUPFROMEITHEROFPARENT = 0.5d; // half

	protected void crossOver() {
		speciationService.getSpeciesPool().keySet().forEach(thisSpeciesId -> {
			int currentNumberOfGenomesInThisSpecies = getNumberOfGenomesInSpecies(thisSpeciesId);
			for (int i = 0; i < currentNumberOfGenomesInThisSpecies; i++) {
				// pick any two random genomes in this species
				// and then cross over between them
				// and then put them back in the pool with same speciesid
				Genome parent1 = getRandomParentOfThisSpecies(thisSpeciesId);
				Genome parent2 = getRandomParentOfThisSpecies(thisSpeciesId);
				Genome newGenome = crossOver(parent1, parent2);
				newGenome.setReferenceSpeciesNumber(thisSpeciesId);
			}
		});
	}

	private Genome getRandomParentOfThisSpecies(Integer thisSpeciesId) {
		int randomPos = (int) mathService.randomNumber(0, getNumberOfGenomesInSpecies(thisSpeciesId) - 1);
		return genomesService.getAllGenomes().stream()
				.filter(genome -> genome.getReferenceSpeciesNumber() == thisSpeciesId).skip(randomPos).findFirst()
				.get();
	}

	private int getNumberOfGenomesInSpecies(Integer thisSpeciesId) {
		return (int) genomesService.getAllGenomes().stream()
				.filter(genome -> genome.getReferenceSpeciesNumber() == thisSpeciesId).count();
	}

	private Genome crossOver(Genome genome1, Genome genome2) {
		List<ConnectionGene> connectionList1 = genome1.getSortedConnectionGenes();
		List<ConnectionGene> connectionList2 = genome2.getSortedConnectionGenes();
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
		Set<String> sampleConnectionGeneIds = new HashSet<String>();
		for (int i = 0; i < ConnectionGeneMostlyEmpty1.length; i++) {
			if (ConnectionGeneMostlyEmpty1[i] != null && ConnectionGeneMostlyEmpty2[i] != null) {
				// Both present so Pick one of connectionGene
				if (ConnectionGeneMostlyEmpty1[i].isLucky(CHANCEFORGENETOBEPICKEDUPFROMEITHEROFPARENT)) {
					sampleConnectionGeneIds.add(ConnectionGeneMostlyEmpty1[i].getId());
				} else {
					sampleConnectionGeneIds.add(ConnectionGeneMostlyEmpty2[i].getId());
				}
			}
			if ((ConnectionGeneMostlyEmpty1[i] == null && ConnectionGeneMostlyEmpty2[i] != null)
					|| (ConnectionGeneMostlyEmpty2[i] == null && ConnectionGeneMostlyEmpty1[i] != null)) {
				if (i < connectionGene1MaxInnovationNumber) {
					// disjoing genes
					if (ConnectionGeneMostlyEmpty1[i] != null) {
						sampleConnectionGeneIds.add(ConnectionGeneMostlyEmpty1[i].getId());
					} else {
						sampleConnectionGeneIds.add(ConnectionGeneMostlyEmpty2[i].getId());
					}
				} else {
					// excess genes. Pick only if excess is in max fit parent
					if (genome2.getFitnessScore() > genome1.getFitnessScore()) {
						sampleConnectionGeneIds.add(ConnectionGeneMostlyEmpty2[i].getId());
					}
				}
			}
		}
		return genomesService.constructGenomeFromSampleConnectionGeneIds(sampleConnectionGeneIds);
	}

}
