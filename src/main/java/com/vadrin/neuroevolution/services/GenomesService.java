package com.vadrin.neuroevolution.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	public Genome constructRandomGenome(int inputNodesSize, int outputNodesSize) {
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
		return genome;
	}

	public Genome constructGenomeFromSampleConnectionGeneIds(Set<String> sampleConnectionGeneIds) {
		Set<NodeGene> actualNodeGenes = new HashSet<NodeGene>();
		Set<ConnectionGene> actualConnectionGenes = new HashSet<ConnectionGene>();

		Iterator<String> cgng = sampleConnectionGeneIds.stream().iterator();
		while (cgng.hasNext()) {
			ConnectionGene thisSampleConnectionGene = connectionsService.getConnection(cgng.next());
			int sampleRefNumberOfFrom = nodesService.getNodeGene(thisSampleConnectionGene.getFromNodeGeneId())
					.getReferenceNodeNumber();
			NodeGeneType sampleTypeOfFrom = nodesService.getNodeGene(thisSampleConnectionGene.getFromNodeGeneId())
					.getType();
			int sampleRefNumberOfTo = nodesService.getNodeGene(thisSampleConnectionGene.getToNodeGeneId())
					.getReferenceNodeNumber();
			NodeGeneType sampleTypeOfTo = nodesService.getNodeGene(thisSampleConnectionGene.getToNodeGeneId())
					.getType();

			NodeGene constructedNodeGeneOfFrom = null;
			if (actualNodeGenes.stream().anyMatch(n -> n.getReferenceNodeNumber() == sampleRefNumberOfFrom)) {
				constructedNodeGeneOfFrom = actualNodeGenes.stream()
						.filter(n -> n.getReferenceNodeNumber() == sampleRefNumberOfFrom).findFirst().get();
			} else {
				constructedNodeGeneOfFrom = nodesService.constructNodeGeneWithReferenceNodeNumber(sampleRefNumberOfFrom,
						sampleTypeOfFrom);
				actualNodeGenes.add(constructedNodeGeneOfFrom);
			}

			NodeGene constructedNodeGeneOfTo = null;
			if (actualNodeGenes.stream().anyMatch(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo)) {
				constructedNodeGeneOfTo = actualNodeGenes.stream()
						.filter(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo).findFirst().get();
			} else {
				constructedNodeGeneOfTo = nodesService.constructNodeGeneWithReferenceNodeNumber(sampleRefNumberOfTo,
						sampleTypeOfTo);
				actualNodeGenes.add(constructedNodeGeneOfTo);
			}
		}
		sampleConnectionGeneIds.forEach(sampleId -> {
			ConnectionGene sampleConn = connectionsService.getConnection(sampleId);
			String fromNodeGeneId = actualNodeGenes
					.stream().filter(n -> n.getReferenceNodeNumber() == nodesService
							.getNodeGene(sampleConn.getFromNodeGeneId()).getReferenceNodeNumber())
					.findAny().get().getId();
			String toNodeGeneId = actualNodeGenes
					.stream().filter(n -> n.getReferenceNodeNumber() == nodesService
							.getNodeGene(sampleConn.getToNodeGeneId()).getReferenceNodeNumber())
					.findAny().get().getId();
			connectionsService.constructConnectionGeneWithInnovationNumber(sampleConn.getReferenceInnovationNumber(),
					sampleConn.getWeight(), fromNodeGeneId, toNodeGeneId);
		});
		return new Genome(actualNodeGenes, actualConnectionGenes);
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
