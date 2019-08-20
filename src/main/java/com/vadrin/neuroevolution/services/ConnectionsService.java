package com.vadrin.neuroevolution.services;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;

@Service
public class ConnectionsService {

	private int referenceInnovationCounter = 0;
	private Set<ConnectionGene> connectionGenesPool = new HashSet<ConnectionGene>();
	private static final double RANDOMWEIGHTLOWERBOUND = -2d;
	private static final double RANDOMWEIGHTUPPERBOUND = 2d;

	@Autowired
	MathService mathService;

	@Autowired
	NodesService nodesService;

	public ConnectionGene constructRandomConnectionGene(String fromNodeGeneId, String toNodeGeneId) {
		return constructRandomConnectionGene(fromNodeGeneId, toNodeGeneId,
				mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
	}

	public ConnectionGene constructRandomConnectionGene(String fromNodeGeneId, String toNodeGeneId, double weight) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGeneId, toNodeGeneId,
				referenceInnovationCounter);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	public ConnectionGene constructConnectionGeneWithInnovationNumber(int referenceInnovationNumber, double weight,
			String fromNodeGeneId, String toNodeGeneId) {
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGeneId, toNodeGeneId,
				referenceInnovationCounter);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	public ConnectionGene constructConnectionGeneWithInnovationNumber(int referenceInnovationNumber,
			String fromNodeGeneId, String toNodeGeneId) {
		return constructConnectionGeneWithInnovationNumber(referenceInnovationNumber,
				mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND), fromNodeGeneId, toNodeGeneId);
	}

	public int getInnovationNumber(int fromReferenceNodeNumber, int toReferenceNodeNumber) {
		return this.connectionGenesPool.stream().filter(c -> (nodesService.getNodeGene(c.getFromNodeGeneId())
				.getReferenceNodeNumber() == fromReferenceNodeNumber)
				&& (nodesService.getNodeGene(c.getToNodeGeneId()).getReferenceNodeNumber() == toReferenceNodeNumber))
				.findAny().get().getReferenceInnovationNumber();
	}

	public ConnectionGene getConnection(String id) {
		return connectionGenesPool.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().get();
	}

	public void setRandomWeight(String id) {
		getConnection(id).setWeight(mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
	}

	public boolean isConnectionPresentBetweenNodes(String fromNodeId, String toNodeId) {
		return this.connectionGenesPool.stream().anyMatch(c -> c.getFromNodeGeneId().equalsIgnoreCase(fromNodeId)
				&& c.getToNodeGeneId().equalsIgnoreCase(toNodeId));
	}
}
