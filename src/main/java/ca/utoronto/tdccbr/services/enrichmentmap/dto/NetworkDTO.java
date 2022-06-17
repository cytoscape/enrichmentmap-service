package ca.utoronto.tdccbr.services.enrichmentmap.dto;

public class NetworkDTO {

	private NetworkElementsDTO elements;

	public NetworkElementsDTO getElements() {
		if (elements == null)
			elements = new NetworkElementsDTO();
		
		return elements;
	}

	public void setElements(NetworkElementsDTO elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		return "NetworkDTO [elements=" + elements + "]";
	}
}
