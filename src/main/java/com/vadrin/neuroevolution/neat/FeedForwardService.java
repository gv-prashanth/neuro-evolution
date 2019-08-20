package com.vadrin.neuroevolution.neat;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.commons.MathService;
import com.vadrin.neuroevolution.commons.NodeGeneType;
import com.vadrin.neuroevolution.commons.exceptions.InvalidInputException;
import com.vadrin.neuroevolution.genome.ConnectionGene;
import com.vadrin.neuroevolution.genome.Genome;
import com.vadrin.neuroevolution.genome.GenomesService;
import com.vadrin.neuroevolution.genome.NodeGene;

@Service
public class FeedForwardService {

	@Autowired
	MathService mathService;

	@Autowired
	GenomesService genomesService;

	protected double[] feedForward(Genome genome, double[] input) throws InvalidInputException {
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
							* genomesService.getFromNodeOfThisConnectionGene(tempConnGene.getId()).getOutput();
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
						* genomesService.getFromNodeOfThisConnectionGene(tempConnGene.getId()).getOutput();
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
