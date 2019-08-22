package com.vadrin.neuroevolution.neat;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.NodeGeneType;
import com.vadrin.neuroevolution.genome.ConnectionGene;
import com.vadrin.neuroevolution.genome.Genome;
import com.vadrin.neuroevolution.genome.NodeGene;

@Service
public class GenomesPool {

	private Map<String, Genome> pool = new HashMap<String, Genome>();

	@Autowired
	ConnectionsPool connectionsPool;

	@Autowired
	NodesPool nodesPool;

	public Genome constructGenomeFromSampleConnectionGeneIds(Set<ConnectionGene> sampleConnectionGenes) {
		Set<NodeGene> actualNodeGenes = new HashSet<NodeGene>();
		Set<ConnectionGene> actualConnectionGenes = new HashSet<ConnectionGene>();

		Iterator<ConnectionGene> cgng = sampleConnectionGenes.stream().iterator();
		while (cgng.hasNext()) {
			ConnectionGene thisSampleConnectionGene = cgng.next();
			int sampleRefNumberOfFrom = thisSampleConnectionGene.getFromNode().getReferenceNodeNumber();
			NodeGeneType sampleTypeOfFrom = thisSampleConnectionGene.getFromNode().getType();
			int sampleRefNumberOfTo = thisSampleConnectionGene.getToNode().getReferenceNodeNumber();
			NodeGeneType sampleTypeOfTo = thisSampleConnectionGene.getToNode().getType();

			NodeGene constructedNodeGeneOfFrom = null;
			if (actualNodeGenes.stream().anyMatch(n -> n.getReferenceNodeNumber() == sampleRefNumberOfFrom)) {
				constructedNodeGeneOfFrom = actualNodeGenes.stream()
						.filter(n -> n.getReferenceNodeNumber() == sampleRefNumberOfFrom).findFirst().get();
			} else {
				constructedNodeGeneOfFrom = nodesPool.constructNodeGeneWithReferenceNodeNumber(sampleRefNumberOfFrom,
						sampleTypeOfFrom);
				actualNodeGenes.add(constructedNodeGeneOfFrom);
			}

			NodeGene constructedNodeGeneOfTo = null;
			if (actualNodeGenes.stream().anyMatch(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo)) {
				constructedNodeGeneOfTo = actualNodeGenes.stream()
						.filter(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo).findFirst().get();
			} else {
				constructedNodeGeneOfTo = nodesPool.constructNodeGeneWithReferenceNodeNumber(sampleRefNumberOfTo,
						sampleTypeOfTo);
				actualNodeGenes.add(constructedNodeGeneOfTo);
			}
		}
		sampleConnectionGenes.forEach(sampleConn -> {
			NodeGene fromNodeGene = actualNodeGenes.stream()
					.filter(n -> n.getReferenceNodeNumber() == sampleConn.getFromNode().getReferenceNodeNumber())
					.findAny().get();
			NodeGene toNodeGene = actualNodeGenes.stream()
					.filter(n -> n.getReferenceNodeNumber() == sampleConn.getToNode().getReferenceNodeNumber())
					.findAny().get();
			actualConnectionGenes.add(connectionsPool.constructConnectionGeneWithExistingInnovationNumber(
					sampleConn.getReferenceInnovationNumber(), fromNodeGene, toNodeGene));
		});
		Genome toReturn = new Genome(actualNodeGenes, actualConnectionGenes);
		pool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	public Collection<Genome> getAllGenomes() {
		return pool.values();
	}

	public Genome getGenome(String id) {
		return pool.get(id);
	}

	public void killGenome(String toDel) {
//		if(!pool.containsKey(toDel))
//			throw new GenomeDoesNotExistException();
		if (pool.remove(toDel) == null) {
			System.out.println("THIS GENOME DOES ONT EVEN EXIST IN THE FIRST PLACE");
		}
	}

	public NodeGene constructNodeGeneWithReferenceNodeNumber(Genome genome, int referenceNodeNumber,
			NodeGeneType type) {
		return nodesPool.constructNodeGeneWithReferenceNodeNumber(referenceNodeNumber, type);
	}

	public NodeGene constructRandomNodeGene(Genome genome, NodeGeneType type) {
		return nodesPool.constructRandomNodeGene(type);
	}

	public void constructRandomGenomePool(int poolSize, int inputNodesSize, int outputNodesSize) {
		// TODO: Somehow i need to add the bias node here... it wont be coming as part
		// of inputs array but still ill need to acomodate. Read the comments on the
		// mutate method regarding the bias nodes.
		Genome firstRandomGenome = constructRandomGenome(inputNodesSize, outputNodesSize);
		for (int i = 1; i < poolSize; i++) {
			constructCopyGenome(firstRandomGenome);
		}
	}

	private Genome constructRandomGenome(int inputNodesSize, int outputNodesSize) {
		Set<NodeGene> inputNodeGenes = new HashSet<NodeGene>();
		Set<NodeGene> outputNodeGenes = new HashSet<NodeGene>();
		for (int j = 0; j < inputNodesSize; j++) {
			inputNodeGenes.add(nodesPool.constructRandomNodeGene(NodeGeneType.INPUT));
		}
		for (int j = 0; j < outputNodesSize; j++) {
			outputNodeGenes.add(nodesPool.constructRandomNodeGene(NodeGeneType.OUTPUT));
		}
		// Instatiate Empty Connection Genes
		Set<ConnectionGene> connectionGenes = new HashSet<ConnectionGene>();
		Iterator<NodeGene> itr = inputNodeGenes.stream().iterator();
		while (itr.hasNext()) {
			NodeGene in = itr.next();
			Iterator<NodeGene> oitr = outputNodeGenes.stream().iterator();
			while (oitr.hasNext()) {
				NodeGene out = oitr.next();
				connectionGenes.add(connectionsPool.constructConnectionGeneWithNewInnovationNumber(in, out));
			}
			;
		}
		inputNodeGenes.addAll(outputNodeGenes);
		Genome genome = new Genome(inputNodeGenes, connectionGenes);
		pool.put(genome.getId(), genome);
		return genome;
	}

	private Genome constructCopyGenome(Genome oriGenome) {
		Genome copyGenome = constructGenomeFromSampleConnectionGeneIds(
				oriGenome.getSortedConnectionGenes().stream().collect(Collectors.toSet()));
		pool.put(copyGenome.getId(), copyGenome);
		return copyGenome;
	}

	public Genome getGenomeWithConnection(String id) {
		return pool.values().stream()
				.filter(g -> g.getSortedConnectionGenes().stream().anyMatch(c -> c.getId().equalsIgnoreCase(id)))
				.findFirst().get();
	}

	public Genome getGenomeWithNode(String id) {
		return pool.values().stream()
				.filter(g -> g.getSortedNodeGenes().stream().anyMatch(n -> n.getId().equalsIgnoreCase(id))).findFirst()
				.get();
	}

	public int getInnovationNumber(int fromReferenceNodeNumber, int toReferenceNodeNumber) {
		Iterator<Genome> allGenomes = this.getAllGenomes().iterator();
		while (allGenomes.hasNext()) {
			Genome thisGenome = allGenomes.next();
			Iterator<ConnectionGene> allConnections = thisGenome.getSortedConnectionGenes().iterator();
			while (allConnections.hasNext()) {
				ConnectionGene thisConnection = allConnections.next();
				if (thisConnection.getFromNode().getReferenceNodeNumber() == thisConnection.getToNode()
						.getReferenceNodeNumber()) {
					return thisConnection.getReferenceInnovationNumber();
				}
			}
		}
		throw new NoSuchElementException();
	}
}
