package com.vadrin.neuroevolution.genome;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.MathService;
import com.vadrin.neuroevolution.commons.exceptions.InvalidConnectionRequestException;

@Service
public class ConnectionsService {

	private int referenceInnovationCounter = 0;
	private Set<ConnectionGene> connectionGenesPool = new HashSet<ConnectionGene>();
	private static final double RANDOMWEIGHTLOWERBOUND = -10d;
	private static final double RANDOMWEIGHTUPPERBOUND = 10d;

	@Autowired
	MathService mathService;

	@Autowired
	NodesService nodesService;

	protected ConnectionGene constructConnectionGeneWithNewInnovationNumber(String fromNodeGeneId, String toNodeGeneId)
			throws InvalidConnectionRequestException {
		if (fromNodeGeneId.equalsIgnoreCase(toNodeGeneId)) {
			throw new InvalidConnectionRequestException();
		}
		if (nodesService.getNodeGene(fromNodeGeneId).getReferenceNodeNumber() == nodesService.getNodeGene(toNodeGeneId)
				.getReferenceNodeNumber()) {
			throw new InvalidConnectionRequestException();
		}
		return constructConnectionGeneWithNewInnovationNumber(fromNodeGeneId, toNodeGeneId,
				mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
	}

	protected ConnectionGene constructConnectionGeneWithNewInnovationNumber(String fromNodeGeneId, String toNodeGeneId,
			double weight) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGeneId, toNodeGeneId,
				referenceInnovationCounter);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	protected ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			double weight, String fromNodeGeneId, String toNodeGeneId) {
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGeneId, toNodeGeneId,
				referenceInnovationNumber);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	protected ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			String fromNodeGeneId, String toNodeGeneId) {
		return constructConnectionGeneWithExistingInnovationNumber(referenceInnovationNumber,
				mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND), fromNodeGeneId, toNodeGeneId);
	}

	protected int getInnovationNumber(int fromReferenceNodeNumber, int toReferenceNodeNumber) {
		return this.connectionGenesPool.stream().filter(c -> (nodesService.getNodeGene(c.getFromNodeGeneId())
				.getReferenceNodeNumber() == fromReferenceNodeNumber)
				&& (nodesService.getNodeGene(c.getToNodeGeneId()).getReferenceNodeNumber() == toReferenceNodeNumber))
				.findAny().get().getReferenceInnovationNumber();
	}

	protected ConnectionGene getConnection(String id) {
		return connectionGenesPool.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().get();
	}

	protected void setRandomWeight(String id) {
		getConnection(id).setWeight(mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
	}

	protected Set<ConnectionGene> getConnectionGenesPool() {
		return connectionGenesPool;
	}

}
