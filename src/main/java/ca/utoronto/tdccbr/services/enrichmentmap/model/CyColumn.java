package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.Objects;

public class CyColumn {

	private final CyTable table;
	private final String name;
	private final Class<?> type;
	private final Class<?> listElementType;
	
	public CyColumn(CyTable table, String name, Class<?> type, Class<?> listElementType) {
		this.table = table;
		this.name = name;
		this.type = type;
		this.listElementType = listElementType;
	}

	public CyTable getTable() {
		return table;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public Class<?> getListElementType() {
		return listElementType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, table);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		var other = (CyColumn) obj;
		
		return Objects.equals(name, other.name) && Objects.equals(table, other.table);
	}

	@Override
	public String toString() {
		return "CyColumn [table=" + table + ", name=" + name + ", type=" + type + ", listElementType=" + listElementType
				+ "]";
	}
}
