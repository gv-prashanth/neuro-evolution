package com.vadrin.neuroevolution.services;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;
import com.vadrin.neuroevolution.models.exceptions.InvalidInputException;

@Service
public class FeedForwardService {
	
	@Autowired
	private PoolService poolService;
	
	@Autowired
	private MathService mathService;

	public double[] feedForward(Genome genome, double[] input) throws InvalidInputException {
		// Validate
		if (genome.getNodeGenesSorted(NodeGeneType.INPUT).size() != input.length)
			throw new InvalidInputException();

		//cleanup any previous run stale outputs
		genome.getNodeGenesSorted().forEach(n -> n.setOutput(0d));

		// Directly set for inputnodes
		List<NodeGene> inputNodeGenes = genome.getNodeGenesSorted(NodeGeneType.INPUT);
		for (int i = 0; i < input.length; i++) {
			inputNodeGenes.get(i).setOutput(input[i]);
		}

		// Process for hiddennodes in squence
		List<NodeGene> hiddenNodeGenes = genome.getNodeGenesSorted(NodeGeneType.HIDDEN);
		for (NodeGene hiddenNodeGene : hiddenNodeGenes) {
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getConnectionGenesSorted().stream()
					.filter(thisConn -> thisConn.getToNode().getId() == hiddenNodeGene.getId()).iterator();
			double sumOfInput = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) Over all connections
				// do only if its enabled... else you should skip it..
				if (tempConnGene.isEnabled()) {
					sumOfInput += tempConnGene.getWeight()
							* genome.getNodeGene(genome.getConnectionGene(tempConnGene.getId()).getFromNode().getId()).getOutput();
				}
			}
			double finalOutput = mathService.applySigmiodActivationFunction(sumOfInput);
			hiddenNodeGene.setOutput(finalOutput);
		}

		// Process for outputnodes
		List<NodeGene> outputNodeGenes = genome.getNodeGenesSorted(NodeGeneType.OUTPUT);
		for (NodeGene outputNodeGene : outputNodeGenes) {
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getConnectionGenesSorted().stream()
					.filter(thisConn -> thisConn.getToNode().getId() == outputNodeGene.getId()).iterator();
			double sumOfInput = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) + Over all connections
				sumOfInput += tempConnGene.getWeight()
						* genome.getNodeGene(genome.getConnectionGene(tempConnGene.getId()).getFromNode().getId()).getOutput();
			}
			double finalOutput = mathService.applySigmiodActivationFunction(sumOfInput);
			outputNodeGene.setOutput(finalOutput);
		}

		// return the output values
		List<NodeGene> toReturnList = genome.getNodeGenesSorted(NodeGeneType.OUTPUT);
		double[] toReturnArray = new double[toReturnList.size()];
		for (int i = 0; i < toReturnList.size(); i++) {
			toReturnArray[i] = toReturnList.get(i).getOutput();
		}
		return toReturnArray;
	}
}
