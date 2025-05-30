package org.epos.api.facets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.epos.api.beans.DiscoveryItem;

public class FacetsGeneration {

	public static Node generateOnlyFacetsTree(Facets.Type type) {
		FacetsNodeTree fnt = new FacetsNodeTree(true, type);
		return fnt.getFacets();
	}

	public static FacetsNodeTree generateResponseUsingCategories(Collection<DiscoveryItem> discoveryList,
			Facets.Type type) {
		FacetsNodeTree fnt = new FacetsNodeTree(true, type);
		for (Node node : fnt.getNodes()) {
			List<DiscoveryItem> distributionsItem = new ArrayList<>();
			for (DiscoveryItem dp : discoveryList) {
				if (dp.getCategories() != null) {
					if (node.getDdss() != null && dp.getCategories().contains(node.getDdss())) {
						if (distributionsItem.stream().noneMatch(p -> p.getId().equals(dp.getId()))) {
							distributionsItem.add(dp);
						}
					}
				}
			}
			if (!distributionsItem.isEmpty()) {
				node.setDistributions(new ArrayList<>());
				node.getDistributions().addAll(distributionsItem);
			}
		}
		fnt.removeEmptyLeaves(fnt.getFacets());
		return fnt;
	}

	public static FacetsNodeTree generateResponseUsingDataproviders(Collection<DiscoveryItem> discoveryList) {
		FacetsNodeTree facets = new FacetsNodeTree();
		discoveryList.forEach(discoveryItem -> {
			if (discoveryItem.getDataprovider().isEmpty())
				addToFacets(facets, "Undefined", discoveryItem);
			else {
				for (String org : discoveryItem.getDataprovider()) {
					addToFacets(facets, org, discoveryItem);
				}
			}
		});
		return facets;
	}

	public static FacetsNodeTree generateResponseUsingServiceproviders(Collection<DiscoveryItem> discoveryList) {

		FacetsNodeTree facets = new FacetsNodeTree();

		discoveryList.forEach(discoveryItem -> {
			if (discoveryItem.getServiceprovider().isEmpty())
				addToFacets(facets, "Undefined", discoveryItem);
			else {
				for (String org : discoveryItem.getServiceprovider()) {
					addToFacets(facets, org, discoveryItem);
				}
			}
		});
		return facets;
	}

	public static void addToFacets(FacetsNodeTree facets, String name, DiscoveryItem item) {

		OptionalInt indexOpt = IntStream.range(0, facets.getNodes().size())
				.filter(i -> name.equals(facets.getNodes().get(i).getName()))
				.findFirst();

		if (indexOpt.isPresent()) {
			facets.getNodes().get(indexOpt.getAsInt()).addDistribution(item);
		} else {
			Node node = new Node(name);
			node.addDistribution(item);
			facets.getNodes().add(node);
		}
	}
}
