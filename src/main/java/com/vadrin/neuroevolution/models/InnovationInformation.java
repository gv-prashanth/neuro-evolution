package com.vadrin.neuroevolution.models;

public class InnovationInformation {

	private int referenceInnovationNumber;
	private int createdReferenceNodeNumber;
	private int createdFromReferenceInnovationNumber;
	private int createdToReferenceInnovationNumber;

	public int getReferenceInnovationNumber() {
		return referenceInnovationNumber;
	}

	public int getCreatedReferenceNodeNumber() {
		return createdReferenceNodeNumber;
	}

	public int getCreatedFromReferenceInnovationNumber() {
		return createdFromReferenceInnovationNumber;
	}

	public int getCreatedToReferenceInnovationNumber() {
		return createdToReferenceInnovationNumber;
	}

	protected InnovationInformation(int referenceInnovationNumber, int createdReferenceNodeNumber,
			int createdFromReferenceInnovationNumber, int createdToReferenceInnovationNumber) {
		super();
		this.referenceInnovationNumber = referenceInnovationNumber;
		this.createdReferenceNodeNumber = createdReferenceNodeNumber;
		this.createdFromReferenceInnovationNumber = createdFromReferenceInnovationNumber;
		this.createdToReferenceInnovationNumber = createdToReferenceInnovationNumber;
	}

}
