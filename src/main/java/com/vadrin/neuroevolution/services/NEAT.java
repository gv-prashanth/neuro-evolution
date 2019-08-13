package com.vadrin.neuroevolution.services;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.MutationType;

@Service
public class NEAT {

	private void mutate(Genome genome, MutationType mutationType) {
		switch (mutationType) {
		case ADDCONNECTIONGENE:
			break;
		case ADDNODEGENE:
			break;
		case ALTERWEIGHTOFCONNECTIONGENE:
			break;
		case ENABLEDISABLECONNECTIONGENE:
			break;
		}
	}

	public Set<Set<Genome>> speciate(Set<Genome> allGenomes) {
		return null;
	}

	private void calculateAndSetFitness(Genome genome) {

	}

	public Set<Genome> createRandomPool(int poolSize) {
		return null;
	}

	public void calculateAndSetFitness(Set<Genome> allGenomes) {
		allGenomes.stream().forEach((genome) -> calculateAndSetFitness(genome));
	}

	public void mutate(Set<Genome> allGenomes) {
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ADDCONNECTIONGENE));
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ADDNODEGENE));
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ALTERWEIGHTOFCONNECTIONGENE));
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ENABLEDISABLECONNECTIONGENE));
	}

}
