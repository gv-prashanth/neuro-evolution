package com.vadrin.neuroevolution.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.MutationType;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;

@Service
public class MutationService {

	private static final double CHANCEFORWEIGHTMUTATION = 0.8d; // 0.8 MEANS 80%
	private static final double CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT = 0.1d; // 0.1 MEANS 10%
	private static final double PERTUBEDVARIANCEDIFFERENCE = 0.05d;
	private static final double CHANCEFORADDINGNEWNODE = 0.003d;
	private static final double CHANCEFORTOGGLEENABLEDISABLE = 0.03d;
	private static final double CHANCEFORADDINGNEWCONNECTION = 0.05d;
	protected static final double RANDOMWEIGHTLOWERBOUND = -40d;
	protected static final double RANDOMWEIGHTUPPERBOUND = 40d;

	@Autowired
	private PoolService poolService;

	@Autowired
	private MathService mathService;

	@Autowired
	private SelectionService selectionService;

	//TODO: Need to get rid of this below variable.
	private Map<ConnectionGene, NodeGene> luckyConnectionGenesInThisGeneration;

	private void prepare() {
		this.luckyConnectionGenesInThisGeneration = new HashMap<ConnectionGene, NodeGene>();
	}

	public void mutate() {
		// everyone should not get mutated.. the best ones should be left as is..else
		// your best fitness will go down if you keep mutating
		// your best guy
		prepare();
		Iterator<Genome> genomeI = poolService.getGenomes().iterator();
		while (genomeI.hasNext()) {
			Genome genome = genomeI.next();
			if (!selectionService.bestAndMostImportantAndSpeciesWinnersAndNeverKill().stream()
					.anyMatch(m -> genome.getId().equalsIgnoreCase(m))) {
				Iterator<MutationType> mTypeI = Arrays.asList(MutationType.class.getEnumConstants()).stream()
						.iterator();
				while (mTypeI.hasNext()) {
					MutationType mutationType = mTypeI.next();
					mutate(genome, mutationType);
				}
			}
		}
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
		genome.getConnectionGenesSorted().forEach(connectionGene -> {
			if (connectionGene.isLucky(CHANCEFORTOGGLEENABLEDISABLE)) {
				connectionGene.setEnabled(!connectionGene.isEnabled());
			}
		});
	}

	private void mutationAlterWeightOfConnectionGene(Genome genome) {
		genome.getConnectionGenesSorted().forEach(connectionGene -> {
			if (connectionGene.isLucky(CHANCEFORWEIGHTMUTATION)) {
				if (connectionGene.isLucky(CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT)) {
					connectionGene.setWeight(mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
				} else {
					connectionGene.setWeight(connectionGene.getWeight()
							* mathService.randomNumber(1 - PERTUBEDVARIANCEDIFFERENCE, 1 + PERTUBEDVARIANCEDIFFERENCE));
				}
			}
		});
	}

	private void mutationAddNodeGene(Genome genome) {
		Iterator<ConnectionGene> connIterator = genome.getConnectionGenesSorted().iterator();
		while (connIterator.hasNext()) {
			ConnectionGene connectionGene = connIterator.next();
			if (connectionGene.isLucky(CHANCEFORADDINGNEWNODE)) {
				NodeGene newNodeGene;
				try {
					ConnectionGene refCon = luckyConnectionGenesInThisGeneration.keySet().stream()
							.filter(oneOfLuckyConnectionGene -> genome
									.getNodeGene(oneOfLuckyConnectionGene.getFromNode().getId())
									.getReferenceNodeNumber() == genome
											.getNodeGene(connectionGene.getFromNode().getId()).getReferenceNodeNumber()
									&& genome.getNodeGene(oneOfLuckyConnectionGene.getToNode().getId())
											.getReferenceNodeNumber() == genome
													.getNodeGene(connectionGene.getToNode().getId())
													.getReferenceNodeNumber())
							.findAny().get();
					int referenceNodeNumber = luckyConnectionGenesInThisGeneration.get(refCon).getReferenceNodeNumber();
					newNodeGene = poolService.constructNodeGeneWithReferenceNodeNumber(genome, referenceNodeNumber,
							NodeGeneType.HIDDEN);
				} catch (NoSuchElementException e) {
					newNodeGene = poolService.constructRandomNodeGene(genome, NodeGeneType.HIDDEN);
					luckyConnectionGenesInThisGeneration.put(connectionGene, newNodeGene);
				}
				genome.addNode(newNodeGene);

				// Now that the node is added. Lets make connections and also lets not forget to
				// disable the prev connection
				connectionGene.setEnabled(false);
				ConnectionGene firstHalf = poolService.constructConnectionGeneWithNewInnovationNumber(
						connectionGene.getFromNode(), newNodeGene, 1.0d);
				ConnectionGene secondHalf = poolService.constructConnectionGeneWithNewInnovationNumber(newNodeGene,
						connectionGene.getToNode(), connectionGene.getWeight());

				genome.addConnection(firstHalf);
				genome.addConnection(secondHalf);
			}
		}
	}

	private void mutationAddConnectionGene(Genome genome) {
		Set<NodeGene> luckyPairs = new HashSet<NodeGene>();
		genome.getNodeGenesSorted().forEach(nodeGene -> {
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
					if (!genome.isConnectionPresentBetweenNodes(from.getId(), to.getId())) {
						ConnectionGene toAdd = null;
						try {
							toAdd = poolService.constructConnectionGeneWithExistingInnovationNumber(poolService
									.getInnovationNumber(from.getReferenceNodeNumber(), to.getReferenceNodeNumber()),
									from, to);
						} catch (NoSuchElementException e) {
							toAdd = poolService.constructConnectionGeneWithNewInnovationNumber(from, to);
						}
						genome.addConnection(toAdd);
					}
				}
			}
		}
	}

}
