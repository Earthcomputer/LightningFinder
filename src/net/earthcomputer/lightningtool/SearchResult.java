package net.earthcomputer.lightningtool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SearchResult {

	private Map<Property<Object>, Object> properties = new IdentityHashMap<>();

	@SuppressWarnings("unchecked")
	public SearchResult(Property<?>... properties) {
		for (Property<?> property : properties) {
			this.properties.put((Property<Object>) property, property.getWorstValue());
		}
	}

	@SuppressWarnings("unchecked")
	public SearchResult(Collection<? extends Property<?>> properties) {
		for (Property<?> property : properties) {
			this.properties.put((Property<Object>) property, property.getWorstValue());
		}
	}

	public Set<Property<?>> getProperties() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	@SuppressWarnings("unchecked")
	public <T> SearchResult withProperty(Property<T> property, T value) {
		properties.replace((Property<Object>) property, value);
		return this;
	}

	public boolean isIdeal() {
		return properties.keySet().stream().allMatch(prop -> prop.isIdeal(properties.get(prop)));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Property<T> property) {
		return (T) properties.get(property);
	}

	public boolean mergeBetter(SearchResult other) {
		if (properties.size() != other.properties.size())
			throw new IllegalArgumentException("Incompatible search results");

		boolean merged = false;
		boolean allEqual = true;

		for (Property<Object> prop : new HashSet<>(properties.keySet())) {

			Object otherVal = other.properties.get(prop);
			if (otherVal == null) {
				System.out.println(prop.getName());
				throw new IllegalArgumentException("Incompatible search results");
			}
			Object thisVal = properties.get(prop);

			if (prop.isFirstBetter(otherVal, thisVal)) {
				merged = true;
				properties.put(prop, otherVal);
			}
			if (!prop.isEqual(thisVal, otherVal))
				allEqual = false;
		}

		return merged || allEqual;
	}

	@Override
	public String toString() {
		List<Property<Object>> properties = new ArrayList<>(this.properties.keySet());
		properties.sort(Comparator.comparing(Property::getName));

		StringBuilder str = new StringBuilder();
		for (Property<Object> prop : properties) {
			if (str.length() != 0)
				str.append(", ");
			str.append(prop.getName()).append(" = ").append(prop.valueToString(this.properties.get(prop)));
		}

		return str.toString();
	}

	public static class Property<T> {
		private String name;
		private T idealValue;
		private T worstValue;
		private Comparator<? super T> comparator;
		private Function<T, String> valueSerializer = Object::toString;

		private Property(String name, T idealValue, T worstValue, Comparator<? super T> comparator) {
			this.name = name;
			this.idealValue = idealValue;
			this.worstValue = worstValue;
			this.comparator = comparator;
		}

		public String getName() {
			return name;
		}

		public boolean isFirstBetter(T first, T second) {
			if (comparator instanceof IndifferentComparator)
				return false;
			else
				return comparator.compare(first, second) < 0;
		}

		public boolean isEqual(T first, T second) {
			if (comparator instanceof IndifferentComparator)
				return true;
			else
				return comparator.compare(first, second) == 0;
		}

		public boolean isIdeal(T value) {
			if (comparator instanceof IndifferentComparator)
				return true;
			else
				return comparator.compare(value, idealValue) <= 0;
		}

		public T getWorstValue() {
			return worstValue;
		}

		public String valueToString(T value) {
			return valueSerializer.apply(value);
		}

		public Property<T> setValueSerializer(Function<T, String> serializer) {
			this.valueSerializer = serializer;
			return this;
		}

		public static <T> Property<T> create(String name, T idealValue, T worstValue,
				Comparator<? super T> comparator) {
			return new Property<T>(name, idealValue, worstValue, comparator);
		}

		public static <T extends Comparable<T>> Comparator<T> minimize() {
			return Comparator.naturalOrder();
		}

		public static <T extends Comparable<T>> Comparator<T> maximize() {
			return Comparator.reverseOrder();
		}

		public static Comparator<Number> distanceTo(long value) {
			return Comparator.comparing(n -> Math.abs(n.longValue() - value));
		}

		public static Comparator<Float> distanceTo(float value) {
			return Comparator.comparing(n -> Math.abs(n - value));
		}

		public static Comparator<Double> distanceTo(double value) {
			return Comparator.comparing(n -> Math.abs(n - value));
		}

		public static <T> Comparator<T> indifferent() {
			return new IndifferentComparator<T>();
		}

		private static class IndifferentComparator<T> implements Comparator<T> {
			@Override
			public int compare(T a, T b) {
				return 0;
			}
		}
	}

}
