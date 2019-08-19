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

	public ConnectionGene constructRandomConnectionGene(String fromNodeGeneId, String toNodeGeneId) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(
				mathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND), true, fromNodeGeneId,
				toNodeGeneId, referenceInnovationCounter);
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

	public ConnectionGene getConnection(String id) {
		return connectionGenesPool.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().get();
	}
}
