package com.vadrin.neuroevolution.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.MutationType;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;
import com.vadrin.neuroevolution.models.Pool;

@Service
public class MutationService {

	private static final double CHANCE_FOR_WEIGHT_MUTATION = 0.8d; // 0.8 MEANS 80%
	private static final double IF_WEIGHT_MUTATION_THEN_CHANCE_FOR_RANDOM_WEIGHT = 0.1d; // 0.1 MEANS 10%
	private static final double CHANCE_FOR_ADDING_NEW_NODE = 0.03d;
	private static final double CHANCE_FOR_ADDING_NEW_CONNECTION = 0.05d;
	// TODO: looks like below is necessary although not mentioned in paper. just
	// have to figure right value
	private static final double X_IF_WEIGHT_MUTATION_THEN_PERTUBED_VARIANCE_PERCENTAGE = 0.1d; // Plus or minus 10%
	private static final double X_CHANCE_FOR_TOGGLING_GENOME_ENABLE_FLAG = 0.03d;
	public static final double X_RANDOM_WEIGHT_LOWER_BOUND = -40d;
	public static final double X_RANDOM_WEIGHT_UPPER_BOUND = 40d;

	private Map<ConnectionGene, NodeGene> luckyConnectionGenesInThisGeneration;

	private void prepare() {
		this.luckyConnectionGenesInThisGeneration = new HashMap<ConnectionGene, NodeGene>();
	}

	public void mutate(Pool pool) {
		prepare();
		Iterator<Genome> genomeI = pool.getGenomes().iterator();
		while (genomeI.hasNext()) {
			Genome genome = genomeI.next();
			// if this genome was born in this generation via crossover, then dont mutate it already
			if(genome.getBirthGeneration()!=pool.getGENERATION()) {
				// everyone should not get mutated.. the best ones should be left as is..else
				// your best fitness will go down if you keep mutating
				// your best guy
				if (!pool.championsWhoShouldntBeHarmed().stream()
						.anyMatch(m -> genome.getId().equalsIgnoreCase(m))) {
					Iterator<MutationType> mTypeI = Arrays.asList(MutationType.class.getEnumConstants()).stream()
							.iterator();
					while (mTypeI.hasNext()) {
						MutationType mutationType = mTypeI.next();
						mutate(pool, genome, mutationType);
					}
				}	
			}
		}
	}

	private void mutate(Pool pool, Genome genome, MutationType mutationType) {
		switch (mutationType) {
		case ADDCONNECTIONGENE:
			mutationAddConnectionGene(pool, genome);
			break;
		case ADDNODEGENE:
			mutationAddNodeGene(pool, genome);
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
			if (connectionGene.isLucky(X_CHANCE_FOR_TOGGLING_GENOME_ENABLE_FLAG)) {
				connectionGene.setEnabled(!connectionGene.isEnabled());
			}
		});
	}

	private void mutationAlterWeightOfConnectionGene(Genome genome) {
		genome.getConnectionGenesSorted().forEach(connectionGene -> {
			if (connectionGene.isLucky(CHANCE_FOR_WEIGHT_MUTATION)) {
				if (connectionGene.isLucky(IF_WEIGHT_MUTATION_THEN_CHANCE_FOR_RANDOM_WEIGHT)) {
					connectionGene.setWeight(
							MathService.randomNumber(X_RANDOM_WEIGHT_LOWER_BOUND, X_RANDOM_WEIGHT_UPPER_BOUND));
				} else {
					connectionGene.setWeight(connectionGene.getWeight()
							* MathService.randomNumber(1 - X_IF_WEIGHT_MUTATION_THEN_PERTUBED_VARIANCE_PERCENTAGE,
									1 + X_IF_WEIGHT_MUTATION_THEN_PERTUBED_VARIANCE_PERCENTAGE));
				}
			}
		});
	}

	private void mutationAddNodeGene(Pool pool, Genome genome) {
		if (MathService.randomNumber(0d, 1d) < CHANCE_FOR_ADDING_NEW_NODE) {
			// This genome will get a new node gene
			int randomConn = (int) MathService.randomNumber(0d, genome.getConnectionGenesSorted().size());
			// select a random connection gene to add the node in between
			ConnectionGene connectionGene = genome.getConnectionGenesSorted().get(randomConn);
			NodeGene newNodeGene;
			try {
				ConnectionGene refCon = luckyConnectionGenesInThisGeneration.keySet().stream()
						.filter(oneOfLuckyConnectionGene -> genome
								.getNodeGene(oneOfLuckyConnectionGene.getFromNode().getId())
								.getReferenceNodeNumber() == genome.getNodeGene(connectionGene.getFromNode().getId())
										.getReferenceNodeNumber()
								&& genome.getNodeGene(oneOfLuckyConnectionGene.getToNode().getId())
										.getReferenceNodeNumber() == genome
												.getNodeGene(connectionGene.getToNode().getId())
												.getReferenceNodeNumber())
						.findAny().get();
				int referenceNodeNumber = luckyConnectionGenesInThisGeneration.get(refCon).getReferenceNodeNumber();
				newNodeGene = pool.constructNodeGeneWithReferenceNodeNumber(genome, referenceNodeNumber,
						NodeGeneType.HIDDEN);
			} catch (NoSuchElementException e) {
				newNodeGene = pool.constructRandomNodeGene(genome, NodeGeneType.HIDDEN);
				luckyConnectionGenesInThisGeneration.put(connectionGene, newNodeGene);
			}
			genome.addNode(newNodeGene);

			// Now that the node is added. Lets make connections and also lets not forget to
			// disable the prev connection
			connectionGene.setEnabled(false);
			ConnectionGene firstHalf = pool
					.constructConnectionGeneWithNewInnovationNumber(connectionGene.getFromNode(), newNodeGene, 1.0d);
			ConnectionGene secondHalf = pool.constructConnectionGeneWithNewInnovationNumber(newNodeGene,
					connectionGene.getToNode(), connectionGene.getWeight());

			genome.addConnection(firstHalf);
			genome.addConnection(secondHalf);
		}
	}

	private void mutationAddConnectionGene(Pool pool, Genome genome) {
		if (MathService.randomNumber(0d, 1d) < CHANCE_FOR_ADDING_NEW_CONNECTION) {
			// this genome will get a new connection
			int randNodePos1 = (int) MathService.randomNumber(0d, genome.getNodeGenesSorted().size());
			NodeGene n1 = genome.getNodeGenesSorted().get(randNodePos1);
			NodeGene n2 = null;
			try {
				Set<Integer> allNumbers = new HashSet<Integer>();
				for(int i=0; i<genome.getNodeGenesSorted().size();i++) {
					allNumbers.add(i);
				}
				int randNodePos2 = allNumbers.stream()
						.filter(i -> i != randNodePos1
								&& ((n1.getType() != genome.getNodeGenesSorted().get(i).getType())
										|| (n1.getType() == genome.getNodeGenesSorted().get(i).getType()
												&& n1.getType() == NodeGeneType.HIDDEN)))
						.findAny().get();
				n2 = genome.getNodeGenesSorted().get(randNodePos2);
			} catch (NoSuchElementException e) {
				//happens very rarely only when nodes are two or three so its fine
				return;
			}
			if ((n1.getType() != n2.getType())
					|| (n1.getType() == n2.getType() && n1.getType() == NodeGeneType.HIDDEN)) {

				NodeGene from = n1.getReferenceNodeNumber() < n2.getReferenceNodeNumber() ? n1 : n2;
				NodeGene to = n1.getReferenceNodeNumber() < n2.getReferenceNodeNumber() ? n2 : n1;

				// If no connection is already present between the two nodes
				if (!genome.isConnectionPresentBetweenNodes(from.getId(), to.getId())) {
					ConnectionGene toAdd = null;
					try {
						toAdd = pool.constructConnectionGeneWithExistingInnovationNumber(pool
								.getInnovationNumber(from.getReferenceNodeNumber(), to.getReferenceNodeNumber()), from,
								to);
					} catch (NoSuchElementException e) {
						toAdd = pool.constructConnectionGeneWithNewInnovationNumber(from, to);
					}
					genome.addConnection(toAdd);
				}
			}
		}
	}

}
