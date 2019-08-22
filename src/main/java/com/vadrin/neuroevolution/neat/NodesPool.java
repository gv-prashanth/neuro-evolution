package com.vadrin.neuroevolution.neat;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.NodeGeneType;
import com.vadrin.neuroevolution.genome.NodeGene;

@Service
public class NodesPool {

	private int referenceNodeCounter = 0;
	private Map<String, NodeGene> nodeGenesPool = new HashMap<String, NodeGene>();

	protected NodeGene constructRandomNodeGene(NodeGeneType type) {
		referenceNodeCounter++;
		NodeGene toReturn = new NodeGene(referenceNodeCounter, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}
	
	protected NodeGene constructNodeGeneWithReferenceNodeNumber(int referenceNodeNumber, NodeGeneType type) {
		NodeGene toReturn = new NodeGene(referenceNodeNumber, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

}
