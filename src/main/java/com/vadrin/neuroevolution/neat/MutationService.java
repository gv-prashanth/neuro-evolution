package com.vadrin.neuroevolution.neat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.MathService;
import com.vadrin.neuroevolution.commons.MutationType;
import com.vadrin.neuroevolution.commons.NodeGeneType;
import com.vadrin.neuroevolution.genome.ConnectionGene;
import com.vadrin.neuroevolution.genome.Genome;
import com.vadrin.neuroevolution.genome.GenomesService;
import com.vadrin.neuroevolution.genome.NodeGene;

@Service
public class MutationService {

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
	SpeciationService speciationService;

	@Autowired
	MathService mathService;

	private Map<ConnectionGene, NodeGene> luckyConnectionGenesInThisGeneration;

	private void prepare() {
		this.luckyConnectionGenesInThisGeneration = new HashMap<ConnectionGene, NodeGene>();
	}

	protected void mutate() {
		// everyone should not get mutated.. the best ones should be left as is..else
		// your best fitness will go down if you keep mutating
		// your best guy
		prepare();
		Iterator<Genome> genomeI = genomesService.getAllGenomes().iterator();
		while (genomeI.hasNext()) {
			Genome genome = genomeI.next();
			if (!bestInThisSpecies(genome)) {
				Iterator<MutationType> mTypeI = Arrays.asList(MutationType.class.getEnumConstants()).stream()
						.iterator();
				while (mTypeI.hasNext()) {
					MutationType mutationType = mTypeI.next();
					mutate(genome, mutationType);
				}
			}
		}
	}

	private boolean bestInThisSpecies(Genome genome) {
		return genomesService.getAllGenomes().stream()
				.filter(g -> g.getReferenceSpeciesNumber() == genome.getReferenceSpeciesNumber())
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).limit(1).findFirst().get()
				.getId() == genome.getId();
	}

	private void mutate(Genome genome, MutationType mutationType) {
		switch (mutationType) {
		case ADDCONNECTIONGENE:
			mutationAddConnectionGene(genome);
			break;
		case ADDNODEGENE:
			mutationAddNodeGene(genome);
			break;
		case ALTERWEIGHTOFCONNECTIONGENE:
			mutationAlterWeightOfConnectionGene(genome);
			break;
		case ENABLEDISABLECONNECTIONGENE:
			mutationEnableDisableConnectionGene(genome);
			break;
		}
	}

	private void mutationEnableDisableConnectionGene(Genome genome) {
		genome.getSortedConnectionGenes().forEach(connectionGene -> {
			if (connectionGene.isLucky(CHANCEFORTOGGLEENABLEDISABLE)) {
				connectionGene.setEnabled(!connectionGene.isEnabled());
			}
		});
	}

	private void mutationAlterWeightOfConnectionGene(Genome genome) {
		genome.getSortedConnectionGenes().forEach(connectionGene -> {
			if (connectionGene.isLucky(CHANCEFORWEIGHTMUTATION)) {
				if (connectionGene.isLucky(CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT)) {
					genomesService.setRandomWeight(genome, connectionGene.getId());
				} else {
					connectionGene.setWeight(connectionGene.getWeight()
							* mathService.randomNumber(1 - PERTUBEDVARIANCEDIFFERENCE, 1 + PERTUBEDVARIANCEDIFFERENCE));
				}
			}
		});
	}

	private void mutationAddNodeGene(Genome genome) {
		Iterator<ConnectionGene> connIterator = genome.getSortedConnectionGenes().iterator();
		while (connIterator.hasNext()) {
			ConnectionGene connectionGene = connIterator.next();
			if (connectionGene.isLucky(CHANCEFORADDINGNEWNODE)) {
				NodeGene newNodeGene;
				try {
					ConnectionGene refCon = luckyConnectionGenesInThisGeneration.keySet().stream()
							.filter(oneOfLuckyConnectionGene -> genomesService
									.getNodeGene(genome, oneOfLuckyConnectionGene.getFromNodeGeneId())
									.getReferenceNodeNumber() == genomesService
											.getNodeGene(genome, connectionGene.getFromNodeGeneId())
											.getReferenceNodeNumber()
									&& genomesService.getNodeGene(genome, oneOfLuckyConnectionGene.getToNodeGeneId())
											.getReferenceNodeNumber() == genomesService
													.getNodeGene(genome, connectionGene.getToNodeGeneId())
													.getReferenceNodeNumber())
							.findAny().get();
					int referenceNodeNumber = luckyConnectionGenesInThisGeneration.get(refCon).getReferenceNodeNumber();
					newNodeGene = genomesService.constructNodeGeneWithReferenceNodeNumber(genome, referenceNodeNumber,
							NodeGeneType.HIDDEN);
				} catch (NoSuchElementException e) {

					newNodeGene = genomesService.constructRandomNodeGene(genome, NodeGeneType.HIDDEN);
					luckyConnectionGenesInThisGeneration.put(connectionGene, newNodeGene);
				}
				genomesService.addNode(genome, newNodeGene);

				// Now that the node is added. Lets make connections and also lets not forget to
				// disable the prev connection
				connectionGene.setEnabled(false);
				ConnectionGene firstHalf = genomesService.constructConnectionGeneWithNewInnovationNumber(genome,
						connectionGene.getFromNodeGeneId(), newNodeGene.getId(), 1.0d);
				ConnectionGene secondHalf = genomesService.constructConnectionGeneWithNewInnovationNumber(genome,
						newNodeGene.getId(), connectionGene.getToNodeGeneId(), connectionGene.getWeight());

				genomesService.addConnection(genome, firstHalf);
				genomesService.addConnection(genome, secondHalf);
			}
		}
	}

	private void mutationAddConnectionGene(Genome genome) {
		Set<NodeGene> luckyPairs = new HashSet<NodeGene>();
		genome.getSortedNodeGenes(NodeGeneType.INPUT).forEach(nodeGene -> {
			if (nodeGene.isLucky(CHANCEFORADDINGNEWCONNECTION)) {
				luckyPairs.add(nodeGene);
			}
		});
		genome.getSortedNodeGenes(NodeGeneType.HIDDEN).forEach(nodeGene -> {
			if (nodeGene.isLucky(CHANCEFORADDINGNEWCONNECTION)) {
				luckyPairs.add(nodeGene);
			}
		});
		genome.getSortedNodeGenes(NodeGeneType.OUTPUT).forEach(nodeGene -> {
			if (nodeGene.isLucky(CHANCEFORADDINGNEWCONNECTION)) {
				luckyPairs.add(nodeGene);
			}
		});
		Iterator<NodeGene> luckyPairsIterator = luckyPairs.iterator();
		while (luckyPairsIterator.hasNext()) {
			NodeGene n1 = luckyPairsIterator.next();
			if (luckyPairsIterator.hasNext()) {
				NodeGene n2 = luckyPairsIterator.next();
				// To make sure we dont join input to input or output to output
				if ((n1.getType() != n2.getType())
						|| (n1.getType() == n2.getType() && n1.getType() == NodeGeneType.HIDDEN)) {

					NodeGene from = n1.getReferenceNodeNumber() < n2.getReferenceNodeNumber() ? n1 : n2;
					NodeGene to = n1.getReferenceNodeNumber() < n2.getReferenceNodeNumber() ? n2 : n1;

					// If no connection is already present between the two nodes
					if (!genomesService.isConnectionPresentBetweenNodes(from.getId(), to.getId())) {
						ConnectionGene toAdd = null;
						try {
							toAdd = genomesService
									.constructConnectionGeneWithExistingInnovationNumber(
											genome, genomesService.getInnovationNumber(genome,
													from.getReferenceNodeNumber(), to.getReferenceNodeNumber()),
											from.getId(), to.getId());
						} catch (NoSuchElementException e) {
							toAdd = genomesService.constructConnectionGeneWithNewInnovationNumber(genome, from.getId(),
									to.getId());
						}
						genomesService.addConnection(genome, toAdd);
					}
				}
			}
		}
	}

}
