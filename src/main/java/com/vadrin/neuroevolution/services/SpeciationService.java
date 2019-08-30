package com.vadrin.neuroevolution.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;

@Service
public class SpeciationService {

	private static final double C1 = 1.0d;
	private static final double C2 = 1.0d;
	private static final double C3 = 0.4d;
	private static final double DELTAT = 3.0d;

	@Autowired
	private PoolService poolService;

	@Autowired
	private MathService mathService;

	public void speciate() {
		Iterator<Genome> iterator = poolService.getGenomes().iterator();
		if (iterator.hasNext()) {
			Genome firstGenome = iterator.next();
			markThisGenomeAsNewSpecies(firstGenome);
		}
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			boolean notDone = true;
			Iterator<String> speciesIterator = getSpeciesIds().iterator();
			while (speciesIterator.hasNext()) {
				String thisSpeciesId = speciesIterator.next();
				if (isSameSpecies(genome.getId(), thisSpeciesId)) {
					genome.setReferenceSpeciesNumber(thisSpeciesId);
					notDone = false;
					break;
				}
			}
			if (notDone)
				markThisGenomeAsNewSpecies(genome);
		}
		System.out.print("Species Pop breakdown: ");
		getSpeciesIds().forEach(sId -> System.out.print(getAllGenomesOfThisSpecies(sId).size()+","));
		System.out.println();
	}

	private void markThisGenomeAsNewSpecies(Genome genome) {
		genome.setReferenceSpeciesNumber(UUID.randomUUID().toString());
	}

	private boolean isSameSpecies(String genomeId, String speciesId) {
		List<ConnectionGene> connectionList1 = poolService.getGenome(genomeId).getConnectionGenesSorted();
		List<ConnectionGene> connectionList2 = poolService.getGenome(getReferenceGenomeOfSpeciesId(speciesId))
				.getConnectionGenesSorted();
		ConnectionGene[] connectionGenes1 = new ConnectionGene[connectionList1.size()];
		connectionGenes1 = connectionList1.toArray(connectionGenes1);
		ConnectionGene[] connectionGenes2 = new ConnectionGene[connectionList2.size()];
		connectionGenes2 = connectionList2.toArray(connectionGenes2);

		// When i initially started testing this, i was getting arrayindex going -1 due
		// to empty connection genes array at the start of program. I beleive this is
		// the reason why the NEAT documentation says that you start with a BIAS node
		// with output always 1. So that you wont run into such issues. You can avoid
		// issues in crossOver method also if you simply start with a bias node
		// additionally.

		if (connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber() > connectionGenes2[connectionGenes2.length - 1]
						.getReferenceInnovationNumber()) {
			ConnectionGene[] temp = connectionGenes1;
			connectionGenes1 = connectionGenes2;
			connectionGenes2 = temp;
		}

		// post this line, connectiongene1 is having smaller max innovation when
		// compared to that of connectiongene2
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

		double E = 0;
		double D = 0;
		double W = 0;
		double w = 0;
		double N = connectionGenes2.length < 20 ? 1 : connectionGenes2.length;

		for (int i = 0; i < ConnectionGeneMostlyEmpty1.length; i++) {
			if (ConnectionGeneMostlyEmpty1[i] != null && ConnectionGeneMostlyEmpty2[i] != null) {
				w++;
				W = W + (ConnectionGeneMostlyEmpty1[i].getWeight() - ConnectionGeneMostlyEmpty2[i].getWeight());
			}
			if ((ConnectionGeneMostlyEmpty1[i] == null && ConnectionGeneMostlyEmpty2[i] != null)
					|| (ConnectionGeneMostlyEmpty2[i] == null && ConnectionGeneMostlyEmpty1[i] != null)) {
				if (i < connectionGene1MaxInnovationNumber) {
					D++;
				} else {
					E++;
				}
			}
		}

		// Average weight difference
		W = W / w;

		double deltaScore = ((C1 * E) / N) + ((C2 * D) / N) + C3 * W;
		return deltaScore < DELTAT;
	}

	private String getReferenceGenomeOfSpeciesId(String speciesId) {
		return poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(speciesId))
				.findFirst().get().getId();
	}

	public Set<String> getSpeciesIds() {
		Set<String> toReturn = new HashSet<String>();
		poolService.getGenomes().forEach(g -> {
			if (g.getReferenceSpeciesNumber() != null)
				toReturn.add(g.getReferenceSpeciesNumber());
		});
		return toReturn;
	}

	public Genome getRandomGenomeOfThisSpecies(String thisSpeciesId) {
		int randomPos = (int) mathService.randomNumber(0, getNumberOfGenomesInSpecies(thisSpeciesId) - 1);
		return poolService.getGenomes().stream()
				.filter(genome -> genome.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)).skip(randomPos)
				.findFirst().get();
	}

	public int getNumberOfGenomesInSpecies(String thisSpeciesId) {
		return (int) poolService.getGenomes().stream()
				.filter(genome -> genome.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)).count();
	}

	public void extinctThisSpeciesAlsoKillOfAnyRemainingGenomes(String thisSpeciesId) {
		Set<Genome> genomesToKill = new HashSet<Genome>();
		poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
				.forEach(g -> genomesToKill.add(g));
		genomesToKill.forEach(g -> poolService.killGenome(g.getId()));
	}

	public Genome getMaxFitGenomeOfThisSpecies(String thisSpeciesId) {
		return poolService.getGenomes().stream()
				.filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).limit(1).findFirst().get();
	}

	public Set<Genome> getAllGenomesOfThisSpecies(String speciesId) {
		return poolService.getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(speciesId))
				.collect(Collectors.toSet());
	}

}
