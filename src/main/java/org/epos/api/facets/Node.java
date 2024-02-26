package org.epos.api.facets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.epos.api.beans.DiscoveryItem;


public class Node
{
    private List<Node> children = null;
	private Collection<DiscoveryItem> distributions = null;
    private String ddss = null;
    private String id = null;
    private String code = null;
    private String linkUrl = null;
    private String imgUrl = null;
    private String color = null;
    private String name;
    
    public Node()
    {
        this.name = null;
    }


    public Node(String value)
    {
        this.children = new ArrayList<>();
        this.name = value;
    }

    public void addChild(Node child)
    {
    	if(children == null) children = new ArrayList<>();
        children.add(child);
    }
    
    public void addDistribution(DiscoveryItem distribution)
    {
    	if(distributions == null) distributions = new ArrayList<>();
    	distributions.add(distribution);
    }

	public Collection<DiscoveryItem> getDistributions() {
		return distributions;
	}

	public void setDistributions(Collection<DiscoveryItem> distributions) {
		this.distributions = distributions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDdss() {
		return ddss;
	}

	public void setDdss(String ddss) {
		this.ddss = ddss;
	}
	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}
	
	public String getLinkUrl() {
		return linkUrl;
	}


	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}


	public String getImgUrl() {
		return imgUrl;
	}


	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}


	public String getColor() {
		return color;
	}


	public void setColor(String color) {
		this.color = color;
	}


	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}


	@Override
	public int hashCode() {
		return Objects.hash(children, code, ddss, distributions, id, name);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		return Objects.equals(children, other.children) && Objects.equals(code, other.code)
				&& Objects.equals(ddss, other.ddss) && Objects.equals(distributions, other.distributions)
				&& Objects.equals(id, other.id) && Objects.equals(name, other.name);
	}


	@Override
	public String toString() {
		return "Node [children=" + children + ", distributions=" + distributions + ", ddss=" + ddss + ", id=" + id
				+ ", code=" + code + ", name=" + name + "]";
	}

	
}