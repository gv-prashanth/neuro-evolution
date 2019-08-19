package com.vadrin.neuroevolution.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;

@Service
public class NodesService {

	private int referenceNodeCounter = 0;
	private Map<String, NodeGene> nodeGenesPool = new HashMap<String, NodeGene>();

	public NodeGene constructRandomNodeGene(NodeGeneType type) {
		referenceNodeCounter++;
		NodeGene toReturn = new NodeGene(referenceNodeCounter, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}
	
	public NodeGene constructNodeGeneWithReferenceNodeNumber(int referenceNodeNumber, NodeGeneType type) {
		NodeGene toReturn = new NodeGene(referenceNodeCounter, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	public NodeGene getNodeGene(String id) {
		return nodeGenesPool.get(id);
	}
}
