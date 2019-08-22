package com.vadrin.neuroevolution.neat;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.MathService;
import com.vadrin.neuroevolution.genome.ConnectionGene;
import com.vadrin.neuroevolution.genome.NodeGene;

@Service
public class ConnectionsPool {

	private int referenceInnovationCounter = 0;
	private Set<ConnectionGene> connectionGenesPool = new HashSet<ConnectionGene>();

	private static final double RANDOMWEIGHTLOWERBOUND = -20d;
	private static final double RANDOMWEIGHTUPPERBOUND = 20d;

	protected ConnectionGene constructConnectionGeneWithNewInnovationNumber(NodeGene fromNodeGene,
			NodeGene toNodeGene) {
		return constructConnectionGeneWithNewInnovationNumber(fromNodeGene, toNodeGene,
				MathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
	}

	protected ConnectionGene constructConnectionGeneWithNewInnovationNumber(NodeGene fromNodeGene, NodeGene toNodeGene,
			double weight) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene,
				referenceInnovationCounter);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	protected ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			double weight, NodeGene fromNodeGene, NodeGene toNodeGene) {
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene, referenceInnovationNumber);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	protected ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			NodeGene fromNodeGene, NodeGene toNodeGene) {
		return constructConnectionGeneWithExistingInnovationNumber(referenceInnovationNumber,
				MathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND), fromNodeGene, toNodeGene);
	}

}
