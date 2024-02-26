package org.epos.api.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class NodeFilters
{
    private List<NodeFilters> children = null;
    private String id = null;
    private String name;
    
    public NodeFilters()
    {
        this.name = null;
    }


    public NodeFilters(String name)
    {
        this.name = name;
    }
    
    public NodeFilters(String id, String name)
    {
    	this.id = id;
        this.name = name;
    }

    public void addChild(NodeFilters child)
    {
    	if(children == null) children = new ArrayList<>();
        children.add(child);
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<NodeFilters> getChildren() {
		return children;
	}

	public void setChildren(List<NodeFilters> children) {
		this.children = children;
	}

	@Override
	public int hashCode() {
		return Objects.hash(children, id, name);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeFilters other = (NodeFilters) obj;
		return Objects.equals(children, other.children) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name);
	}


	@Override
	public String toString() {
		return "Node [children=" + children + ", id=" + id + ", name=" + name + "]";
	}
}