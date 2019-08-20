package com.vadrin.neuroevolution.genome;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.NodeGeneType;
import com.vadrin.neuroevolution.commons.exceptions.ConnectionAlreadyBelongsToAnotherGeneException;
import com.vadrin.neuroevolution.commons.exceptions.ConnectionAlreadyConnectedException;
import com.vadrin.neuroevolution.commons.exceptions.InvalidConnectionRequestException;
import com.vadrin.neuroevolution.commons.exceptions.ThisReferencedConnectionAlreadyPresentInThisGenomeException;

@Service
public class GenomesService {

	private Map<String, Genome> genomesPool = new HashMap<String, Genome>();

	@Autowired
	ConnectionsService connectionsService;

	@Autowired
	NodesService nodesService;

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
			actualConnectionGenes.add(connectionsService.constructConnectionGeneWithExistingInnovationNumber(
					sampleConn.getReferenceInnovationNumber(), sampleConn.getWeight(), fromNodeGeneId, toNodeGeneId));
		});
		Genome toReturn = new Genome(actualNodeGenes, actualConnectionGenes);
		genomesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	public Collection<Genome> getAllGenomes() {
		return genomesPool.values();
	}

	public Genome getGenome(String id) {
		return genomesPool.get(id);
	}

	public void kill(String toDel) {
//		if(!genomesPool.containsKey(toDel))
//			throw new GenomeDoesNotExistException();
		if(genomesPool.remove(toDel)==null) {
			System.out.println("THIS GENOME DOES ONT EVEN EXIST IN THE FIRST PLACE");
		}
	}

	public boolean isConnectionPresentBetweenNodes(String fromNodeId, String toNodeId) throws InvalidConnectionRequestException {
		//TODO: Need to check and exception incase both the nodes dont belong to same genome 
		if(fromNodeId.equalsIgnoreCase(toNodeId))
			throw new InvalidConnectionRequestException();
		return connectionsService.getConnectionGenesPool().stream()
				.anyMatch(c -> c.getFromNodeGeneId().equalsIgnoreCase(fromNodeId)
						&& c.getToNodeGeneId().equalsIgnoreCase(toNodeId));
	}

	public void addConnection(Genome genome, ConnectionGene toAdd)
			throws ConnectionAlreadyBelongsToAnotherGeneException, ConnectionAlreadyConnectedException,
			ThisReferencedConnectionAlreadyPresentInThisGenomeException {
		if (genomesPool.values().stream().anyMatch(someG -> someG.getSortedConnectionGenes().stream()
				.anyMatch(c -> c.getId().equalsIgnoreCase(toAdd.getId()))))
			throw new ConnectionAlreadyBelongsToAnotherGeneException();
		if (genome.getSortedConnectionGenes().stream().anyMatch(c -> c.getId().equalsIgnoreCase(toAdd.getId())))
			throw new ConnectionAlreadyConnectedException();
		if (genome.getSortedConnectionGenes().stream()
				.anyMatch(c -> c.getReferenceInnovationNumber() == toAdd.getReferenceInnovationNumber()))
			throw new ThisReferencedConnectionAlreadyPresentInThisGenomeException();
		genome.addConnectionGene(toAdd);
	}
	
	private void validate() {
		
	}

	public void addNode(Genome genome, NodeGene newNodeGene) {
		validate();
		genome.addNodeGene(newNodeGene);
	}

	public NodeGene getFromNodeOfThisConnectionGene(String connId) {
		return nodesService.getNodeGene(connectionsService.getConnection(connId).getFromNodeGeneId());
	}

	public void setRandomWeight(Genome genome, String id) {
		validate();
		connectionsService.setRandomWeight(id);
	}

	public NodeGene getNodeGene(Genome genome, String id) {
		validate();
		return nodesService.getNodeGene(id);
	}

	public NodeGene constructNodeGeneWithReferenceNodeNumber(Genome genome, int referenceNodeNumber,
			NodeGeneType type) {
		validate();
		return nodesService.constructNodeGeneWithReferenceNodeNumber(referenceNodeNumber, type);
	}

	public NodeGene constructRandomNodeGene(Genome genome, NodeGeneType type) {
		validate();
		return nodesService.constructRandomNodeGene(type);
	}

	public ConnectionGene constructConnectionGeneWithNewInnovationNumber(Genome genome, String fromNodeGeneId,
			String toNodeGeneId, double weight) {
		validate();
		return connectionsService.constructConnectionGeneWithNewInnovationNumber(fromNodeGeneId, toNodeGeneId, weight);
	}

	public ConnectionGene constructConnectionGeneWithExistingInnovationNumber(Genome genome, int innovationNumber,
			String id, String id2) {
		validate();
		return connectionsService.constructConnectionGeneWithExistingInnovationNumber(innovationNumber, id, id2);
	}

	public ConnectionGene constructConnectionGeneWithNewInnovationNumber(Genome genome, String id, String id2)
			throws InvalidConnectionRequestException {
		validate();
		return connectionsService.constructConnectionGeneWithNewInnovationNumber(id, id2);
	}

	public int getInnovationNumber(Genome genome, int referenceNodeNumber, int referenceNodeNumber2) {
		validate();
		return connectionsService.getInnovationNumber(referenceNodeNumber, referenceNodeNumber2);
	}

	public void setFitnessScore(String genomeId, double fitnessScore) {
		genomesPool.get(genomeId).setFitnessScore(fitnessScore);
	}

	public void instantiateRandomGenomePool(int poolSize, int inputNodesSize, int outputNodesSize)
			throws InvalidConnectionRequestException {
		// TODO: Somehow i need to add the bias node here... it wont be coming as part
		// of inputs array but still ill need to acomodate. Read the comments on the
		// mutate method regarding the bias nodes.
		Genome firstRandomGenome = constructRandomGenome(inputNodesSize, outputNodesSize);
		for (int i = 1; i < poolSize; i++) {
			constructCopyGenome(firstRandomGenome);
		}
		Iterator<Genome> ifindissue = genomesPool.values().stream().iterator();
		while(ifindissue.hasNext()) {
			Genome issue = ifindissue.next();
			if(issue.getSortedConnectionGenes().get(0).getReferenceInnovationNumber()==issue.getSortedConnectionGenes().get(1).getReferenceInnovationNumber()) {
				System.out.println("Im creating pool badly");
			}
		}
	}

	private Genome constructRandomGenome(int inputNodesSize, int outputNodesSize)
			throws InvalidConnectionRequestException {
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
		Iterator<NodeGene> itr = inputNodeGenes.stream().iterator();
		while (itr.hasNext()) {
			NodeGene in = itr.next();
			Iterator<NodeGene> oitr = outputNodeGenes.stream().iterator();
			while (oitr.hasNext()) {
				NodeGene out = oitr.next();
				connectionGenes.add(
						connectionsService.constructConnectionGeneWithNewInnovationNumber(in.getId(), out.getId()));
			}
			;
		}
		inputNodeGenes.addAll(outputNodeGenes);
		Genome genome = new Genome(inputNodeGenes, connectionGenes);
		genomesPool.put(genome.getId(), genome);
		return genome;
	}

	private Genome constructCopyGenome(Genome oriGenome) {
		Set<String> sampleConnectionGeneIds = new HashSet<String>();
		oriGenome.getSortedConnectionGenes().forEach(c -> sampleConnectionGeneIds.add(c.getId()));
		Genome copyGenome = constructGenomeFromSampleConnectionGeneIds(sampleConnectionGeneIds);
		genomesPool.put(copyGenome.getId(), copyGenome);
		return copyGenome;
	}
}
