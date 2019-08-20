package com.vadrin.neuroevolution.neat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.genome.ConnectionGene;
import com.vadrin.neuroevolution.genome.Genome;
import com.vadrin.neuroevolution.genome.GenomesService;

@Service
public class SpeciationService {

	@Autowired
	GenomesService genomesService;

	private static final double C1 = 1.0d;
	private static final double C2 = 1.0d;
	private static final double C3 = 0.4d;
	private static final double DELTAT = 3.0d;

	private int referenceSpeciesCounter = 0;
	private Map<Integer, String> speciesPool = new HashMap<Integer, String>();

	protected void speciate() {
		resetSpeciesPool();
		Iterator<Genome> iterator = genomesService.getAllGenomes().iterator();
		if (iterator.hasNext()) {
			Genome firstGenome = iterator.next();
			constructNewSpeciesWithGenome(firstGenome);
		}
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			boolean notDone = true;
			for (int i = 1; i <= referenceSpeciesCounter; i++) {
				if (isSameSpecies(genome.getId(), i)) {
					genome.setReferenceSpeciesNumber(i);
					notDone = false;
					break;
				}
			}
			if (notDone)
				constructNewSpeciesWithGenome(genome);
		}
	}

	private void resetSpeciesPool() {
		speciesPool = new HashMap<Integer, String>();
	}

	private void constructNewSpeciesWithGenome(Genome genome) {
		referenceSpeciesCounter++;
		genome.setReferenceSpeciesNumber(referenceSpeciesCounter);
		String singleGenomeId = genome.getId();
		speciesPool.put(genome.getReferenceSpeciesNumber(), singleGenomeId);
	}

	private boolean isSameSpecies(String genomeId, int speciesId) {
		List<ConnectionGene> connectionList1 = genomesService.getGenome(genomeId).getSortedConnectionGenes();
		List<ConnectionGene> connectionList2 = genomesService.getGenome(getReferenceGenomeOfSpeciesId(speciesId))
				.getSortedConnectionGenes();
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
				.forEach(thisConnectionGene1Entry -> ConnectionGeneMostlyEmpty1[thisConnectionGene1Entry
						.getReferenceInnovationNumber()] = thisConnectionGene1Entry);
		Arrays.asList(connectionGenes2).stream()
				.forEach(thisConnectionGene2Entry -> ConnectionGeneMostlyEmpty2[thisConnectionGene2Entry
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

	protected String getReferenceGenomeOfSpeciesId(int speciesId) {
		return this.speciesPool.get(speciesId);
	}

	protected Map<Integer, String> getSpeciesPool() {
		return speciesPool;
	}

}
