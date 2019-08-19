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
	MathService mathService;

	@Autowired
	NodesService nodeGeneFactory;

	public double[] feedForward(Genome genome, double[] input) throws InvalidInputException {
		// Validate
		if (genome.getSortedNodeGenes(NodeGeneType.INPUT).size() != input.length)
			throw new InvalidInputException();

		// Directly set for inputnodes
		List<NodeGene> inputNodeGenes = genome.getSortedNodeGenes(NodeGeneType.INPUT);
		for (int i = 0; i < input.length; i++) {
			inputNodeGenes.get(i).setOutput(input[i]);
		}

		// Process for hiddennodes in squence
		List<NodeGene> hiddenNodeGenes = genome.getSortedNodeGenes(NodeGeneType.HIDDEN);
		for (NodeGene hiddenNodeGene : hiddenNodeGenes) {
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getSortedConnectionGenes().stream()
					.filter(thisConn -> thisConn.getToNodeGeneId() == hiddenNodeGene.getId()).iterator();
			double sumOfInput = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) Over all connections
				// do only if its enabled... else you should skip it..
				if (tempConnGene.isEnabled()) {
					sumOfInput += tempConnGene.getWeight()
							* nodeGeneFactory.getNodeGene(tempConnGene.getFromNodeGeneId()).getOutput();
				}
			}
			double finalOutput = mathService.applySigmiodActivationFunction(sumOfInput);
			hiddenNodeGene.setOutput(finalOutput);
		}

		// Process for outputnodes
		List<NodeGene> outputNodeGenes = genome.getSortedNodeGenes(NodeGeneType.OUTPUT);
		for (NodeGene outputNodeGene : outputNodeGenes) {
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getSortedConnectionGenes().stream()
					.filter(thisConn -> thisConn.getToNodeGeneId() == outputNodeGene.getId()).iterator();
			double sumOfInput = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) + Over all connections
				sumOfInput += tempConnGene.getWeight()
						* nodeGeneFactory.getNodeGene(tempConnGene.getFromNodeGeneId()).getOutput();
			}
			double finalOutput = mathService.applySigmiodActivationFunction(sumOfInput);
			outputNodeGene.setOutput(finalOutput);
		}

		// return the output values
		List<NodeGene> toReturnList = genome.getSortedNodeGenes(NodeGeneType.OUTPUT);
		double[] toReturnArray = new double[toReturnList.size()];
		for (int i = 0; i < toReturnList.size(); i++) {
			toReturnArray[i] = toReturnList.get(i).getOutput();
		}
		return toReturnArray;
	}
}
