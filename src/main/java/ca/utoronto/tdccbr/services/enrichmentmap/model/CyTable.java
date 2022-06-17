package ca.utoronto.tdccbr.services.enrichmentmap.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CyTable {

	private final static int DEF_INIT_SIZE = 100;
	
	private final String id = UUID.randomUUID().toString();
	private final String title;
	private final String primaryKey;
	private Class<?> primaryKeyType;
	
	/** Maps the column name to CyColumn. */
	private Map<String, CyColumn> columns = new ConcurrentHashMap<>(16, 0.75f, 2);
	
	/** Maps the primary key to CyRow. */
	private final Map<Object/*PK*/, CyRow> rows = new ConcurrentHashMap<>(DEF_INIT_SIZE, 0.5f, 2);
	
	/** 
	 * Caches the normalized names, in order to prevent creating new strings (e.g. name.toLowerCase())
	 * every time a column or value is retrieved.
	 */
	private final Map<String/*name*/, String/*normalized-name*/> normalizedColumnNames = new HashMap<>(); 
	
	/**
	 * Maps column names to (key, value) pairs, where "key" is the primary key.
	 */
	private Map<String, ColumnData> attributes = new HashMap<>();
	
	private final static CanonicalStringPool stringPool = new CanonicalStringPool();
	
	private final Object lock = new Object();
	
	public CyTable(String title, String primaryKey, Class<?> primaryKeyType) {
		this.title = title;
		this.primaryKey = primaryKey;
		this.primaryKeyType = primaryKeyType;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}

	public CyColumn getColumn(String name) {
		return columns.get(normalizeColumnName(name));
	}
	
	public Collection<CyColumn> getColumns() {
		synchronized (lock) {
			return new ArrayList<>(columns.values());
		}
	}

	public <T> void createColumn(String name, Class<? extends T> type) {
		synchronized (lock) {
			if (name == null)
				throw new NullPointerException("Column name is null");

			var normalizedName = normalizeColumnName(name);

			if (columns.containsKey(normalizedName))
				throw new IllegalArgumentException("column already exists with name: '" + name + "' with type: "
						+ columns.get(normalizedName).getType());

			if (type == null)
				throw new IllegalArgumentException("'type' is null");

			if (type == List.class)
				throw new IllegalArgumentException(
						"use createListColumn() to create List columns instead of createColumn for column '" + name);

			checkClass(type);

			var col = new CyColumn(this, name, type, null);
			columns.put(normalizedName, col);
			attributes.put(normalizedName, new ColumnData());
		}
	}

	public <T> void createListColumn(String name, Class<?extends T> elementType) {
		synchronized(lock) {
			if (name == null)
				throw new NullPointerException("column name is null");

			var normalizedName = normalizeColumnName(name);
			
			if (columns.containsKey(normalizedName))
				throw new IllegalArgumentException("column already exists with name: '" + name + "' with type: "
						+ columns.get(normalizedName).getType());

			if (elementType == null)
				throw new NullPointerException("elementType is null");

			checkClass(elementType);

			var column = new CyColumn(this, name, List.class, elementType);
			columns.put(normalizedName, column);
			attributes.put(normalizedName, new ColumnData());
		}
	}
	
	public CyRow getRow(Object key) {
		var row = rows.get(key);
		
		if (row != null)
			return row;

		row = new CyRow(key);
		rows.put(key, row);

		return row;
	}
	
	/**
	 * Normalizes the column names to be case insensitive.
	 */
	private String normalizeColumnName(String initialName) {
		synchronized(lock) {
			var name = normalizedColumnNames.get(initialName);
			
			if (name == null) {
				name = initialName.toLowerCase();
				// cache the normalized name, to avoid creating new strings in future accesses.
				normalizedColumnNames.put(initialName, name);
			}
			
			return name;
		}
	}
	
	private void checkClass(Class<?> c) {
		if (c == Integer.class || c == Long.class || c == Double.class || c == String.class || c == Boolean.class)
			return;
		
		throw new IllegalArgumentException("invalid class: " + c.getName());
	}
	
	private final void checkType(Object o) {
		if (o instanceof String)
			return;
		else if (o instanceof Integer)
			return;
		else if (o instanceof Boolean)
			return;
		else if (o instanceof Double)
			return;
		else if (o instanceof Long)
			return;
		else
			throw new IllegalArgumentException("invalid type: " + o.getClass().toString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		var other = (CyTable) obj;
		
		return Objects.equals(id, other.id) && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "CyTable [title=" + title + "]";
	}
	
	public class CyRow {

		private final Object key;
		
		public CyRow(Object key) {
			this.key = key;
		}
		
		public <T> T get(String columnName, Class<? extends T> type) {
			Object value = getValue(columnName);
			
			return value != null ? type.cast(value) : null;
		}

		public <T> List<T> getList(String columnName, Class<? extends T> elementType) {
			Object value = null;
			
			synchronized (lock) {
				var normalizedName = normalizeColumnName(columnName);
				var column = columns.get(normalizedName);

				if (column == null)
					return null;

				var expectedListElementType = column.getListElementType();

				if (expectedListElementType == null)
					throw new IllegalArgumentException("'" + columnName + "' is not a List.");

				if (expectedListElementType != elementType)
					throw new IllegalArgumentException(
							"invalid list element type for column '" + columnName + ", found: " + elementType.getName()
									+ ", expected: " + expectedListElementType.getName());

				value = getValue(columnName);
			}
			
			return value != null ? (List) value : null;
		}

		public <T> void set(String columnName, T value) {
			if (columnName == null)
				throw new IllegalArgumentException("columnName must not be null.");
			
			var normalizedColName = normalizeColumnName(columnName);
			ColumnData colData = null;
			Class<?> columnType = null;
			
			synchronized (lock) {
				if (columns.get(normalizedColName) == null)
					throw new IllegalArgumentException("column: '" + columnName + "' does not yet exist.");
				
				colData = attributes.get(normalizedColName);
				
				if (colData == null)
					throw new IllegalArgumentException("column: '" + columnName + "' does not yet exist.");
				
				columnType = columns.get(normalizedColName).getType();
			}
			
			if (value == null) {
				synchronized (lock) {
					colData.remove(key);
				}
			} else {
				// First check if the column type is List.
				synchronized(lock) {
					Object newValue = null;
					
					if (columnType == List.class) {
						if (value instanceof List == false)
							throw new IllegalArgumentException("'value' must be an instance of java.util.List.");
						
						var column = columns.get(normalizedColName);
						var type = column.getListElementType();
						
						if (value instanceof List) {
							var list = (List<?>) value;
							
							if (!list.isEmpty())
								checkType(list.get(0));
							
							newValue = createList(type, list);
						}
					} else {
						if (!columnType.isAssignableFrom(value.getClass()))
							throw new IllegalArgumentException(
									"Value of '" + columnName + "' is not of type " + columnType);

						newValue = value;
					}
					
					colData.put(key, newValue);
				}
			}
		}
		
		private Object getValue(String columnName) {
			synchronized (lock) {
				var normalizedName = normalizeColumnName(columnName);

				if (primaryKey.equalsIgnoreCase(normalizedName))
					return key;

				var colData = attributes.get(normalizedName);
				
				return colData == null ? null : colData.get(key);
			}
		}
		
		@SuppressWarnings("unchecked")
		public List<?> createList(Class<?> elementType, List<?> data) {
			if (Integer.class.equals(elementType)) {
				return new IntArrayList((List<Integer>) data);
			} else if (Long.class.equals(elementType)) {
				return new LongArrayList((List<Long>) data);
			} else if (Double.class.equals(elementType)) {
				return new DoubleArrayList((List<Double>) data);
			} else if (Boolean.class.equals(elementType)) {
				return new BooleanArrayList((List<Boolean>) data);
			} else if (String.class.equals(elementType)) {
				var canonData = new ArrayList<Object>(data.size());

				for (var value : data) {
					if (value instanceof String)
						value = stringPool.canonicalize((String) value);

					canonData.add(value);
				}
				
				return canonData;
			}

			return new ArrayList<>(data);
		}
	}
	
	private class ColumnData {

		private final Map<Object, Object> attributes;

		public ColumnData() {
			this(new HashMap<>(DEF_INIT_SIZE));
		}
		
		public ColumnData(Map<Object,Object> attributes) {
			this.attributes = attributes;
		}

		public int countMatchingRows(Object value) {
			return Collections.frequency(attributes.values(), value);
		}

		public Collection<CyRow> getMatchingRows(Map<Object, CyRow> rows, Object value) {
			var matchingRows = new ArrayList<CyRow>();
			
			for (var entry : attributes.entrySet()) {
				if (entry.getValue().equals(value))
					matchingRows.add(rows.get(entry.getKey()));
			}
			
			return matchingRows;
		}

		@SuppressWarnings("unchecked")
		public <T> Collection<T> getMatchingKeys(Object value, Class<T> type) {
			var matchingKeys = new ArrayList<T>();
			
			for (var entry : attributes.entrySet()) {
				if (entry.getValue().equals(value))
					matchingKeys.add((T) entry.getKey());
			}
			
			return matchingKeys;
		}

		public boolean put(Object key, Object value) {
			if (value == null)
				return remove(key);
			
			var prev = attributes.put(key, value);
			
			if (prev == null)
				return true;
			
			return !prev.equals(value);
		}

		public Object get(Object key) {
			return attributes.get(key);
		}

		public Set<Object> keySet() {
			return attributes.keySet();
		}

		public boolean remove(Object key) {
			return attributes.remove(key) != null;
		}
	}
	
	private static class CanonicalStringPool {

		private static final int MAX_SIZE = 2000;

		private final Map<String, String> pool = new Object2ObjectOpenHashMap<>();

		public String canonicalize(String s) {
			if (pool.size() > MAX_SIZE)
				pool.clear();
			
			var canon = pool.get(s);
			
			if (canon == null) {
				pool.put(s, s);
				canon = s;
			}
			
			return canon;
		}

		public void clear() {
			pool.clear();
		}
	}
}
