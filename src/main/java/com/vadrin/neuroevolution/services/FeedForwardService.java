package com.vadrin.neuroevolution.services;

import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;
import com.vadrin.neuroevolution.models.exceptions.InvalidInputException;

@Service
public class FeedForwardService {

	protected double[] feedForward(Genome genome, double[] input) throws InvalidInputException {
		// Validate
		if (genome.getNodeGenes(NodeGeneType.INPUT).size() != input.length)
			throw new InvalidInputException();

		// cleanup any previous run stale outputs
		genome.getNodeGenes().forEach(n -> n.setOutput(0d));

		// Directly set for inputnodes
		List<NodeGene> inputNodeGenes = genome.getNodeGenes(NodeGeneType.INPUT);
		for (int i = 0; i < input.length; i++) {
			inputNodeGenes.get(i).setOutput(input[i]);
		}

		// Process for hiddennodes in squence
		List<NodeGene> hiddenNodeGenes = genome.getNodeGenes(NodeGeneType.HIDDEN);
		for (NodeGene hiddenNodeGene : hiddenNodeGenes) {
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getConnectionGenes().stream()
					.filter(thisConn -> thisConn.getToNode().getId() == hiddenNodeGene.getId()).iterator();
			double sumOfInput = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) Over all connections
				// do only if its enabled... else you should skip it..
				if (tempConnGene.isEnabled()) {
					sumOfInput += tempConnGene.getWeight()
							* genome.getNodeGene(genome.getConnectionGene(tempConnGene.getId()).getFromNode().getId())
									.getOutput();
				}
			}
			double finalOutput = MathService.applySigmiodActivationFunction(sumOfInput);
			hiddenNodeGene.setOutput(finalOutput);
		}

		// Process for outputnodes
		List<NodeGene> outputNodeGenes = genome.getNodeGenes(NodeGeneType.OUTPUT);
		for (NodeGene outputNodeGene : outputNodeGenes) {
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getConnectionGenes().stream()
					.filter(thisConn -> thisConn.getToNode().getId() == outputNodeGene.getId()).iterator();
			double sumOfInput = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) + Over all connections
				sumOfInput += tempConnGene.getWeight() * genome
						.getNodeGene(genome.getConnectionGene(tempConnGene.getId()).getFromNode().getId()).getOutput();
			}
			double finalOutput = MathService.applySigmiodActivationFunction(sumOfInput);
			outputNodeGene.setOutput(finalOutput);
		}

		// return the output values
		List<NodeGene> toReturnList = genome.getNodeGenes(NodeGeneType.OUTPUT);
		double[] toReturnArray = new double[toReturnList.size()];
		for (int i = 0; i < toReturnList.size(); i++) {
			toReturnArray[i] = toReturnList.get(i).getOutput();
		}
		return toReturnArray;
	}
}
