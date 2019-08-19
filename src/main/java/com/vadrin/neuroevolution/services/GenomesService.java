package com.vadrin.neuroevolution.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;

@Service
public class GenomesService {

	private Map<String, Genome> genomesPool = new HashMap<String, Genome>();

	@Autowired
	ConnectionsService connectionsService;

	@Autowired
	NodesService nodesService;

	public String constructRandomGenome(int inputNodesSize, int outputNodesSize) {
		// TODO: Somehow i need to add the bias node here... it wont be coming as part
		// of inputs array but still ill need to acomodate. Read the comments on the
		// mutate method regarding the bias nodes.
		Set<NodeGene> inputNodeGenes = new HashSet<NodeGene>();
		Set<NodeGene> outputNodeGenes = new HashSet<NodeGene>();
		for (int j = 0; j < inputNodesSize; j++) {
			inputNodeGenes.add(nodesService.constructRandomNodeGene(NodeGeneType.INPUT));
		}
		for (int j = 0; j < outputNodesSize; j++) {
			outputNodeGenes.add(nodesService.constructRandomNodeGene(NodeGeneType.OUTPUT));
		}
		// Instatiate Empty Connection Genes
		Set<ConnectionGene> connectionGenes = new HashSet<ConnectionGene>();
		inputNodeGenes.forEach(in -> {
			outputNodeGenes.forEach(out -> {
				connectionGenes.add(connectionsService.constructRandomConnectionGene(in.getId(), out.getId()));
			});
		});
		inputNodeGenes.addAll(outputNodeGenes);
		Genome genome = new Genome(inputNodeGenes, connectionGenes);
		genomesPool.put(genome.getId(), genome);
		return genome.getId();
	}

	public Collection<Genome> getAllGenomes() {
		return genomesPool.values();
	}

	public Genome getGenome(String id) {
		return genomesPool.get(id);
	}

	public void kill(String toDel) {
		genomesPool.remove(toDel);
	}

}
