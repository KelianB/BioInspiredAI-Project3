package utils;

import java.util.function.Supplier;

/**
 * Handles storing values that should only be re-calculated when necessary.
 * It is up to the user to call <code>needsUpdating()</code> whenever re-calculation is necessary.
 * @author Kelian Baert & Caroline de Pourtales
 * @param <T>
 */
public class CachedValue<T> {
	// The current value
	private T value;
	
	// Whether or not the value needs to be updated
	private boolean needsUpdating;
	
	// The function responsible for re-calculating the value upon request
	private Supplier<T> updateFunction;
	
	/**
	 * Create a new CachedValue with the given initial value and update function
	 * @param value - The initial value
	 * @param updateFunction - The function responsible for re-calculating the value when necessary
	 */
	public CachedValue(T value, Supplier<T> updateFunction) {
		this.value = value;
		this.updateFunction = updateFunction;
		this.needsUpdating = (value == null);
	}
	
	/**
	 * Create a new CachedValue with the given initial value and update function.
	 * Since no initial value is given, the value will be calculated upon the first access.
	 * @param updateFunction - The function responsible for re-calculating the value when necessary
	 */
	public CachedValue(Supplier<T> updateFunction) {
		this(null, updateFunction);
	}
	
	/**
	 * Get the cached value. Updates it first if it needs to be updated.
	 * @return the cached value
	 */
	public T getValue() {
		if(needsUpdating)
			updateValue();
		return value;
	}
	
	/**
	 * Notify that this CachedValue is out of date. It will be re-calculated upon the next access.
	 */
	public void needsUpdating() {
		this.needsUpdating = true;
	}
	
	/**
	 * Creates a fully independent copy of this CachedValue.
	 * @return a new CachedValue instance with the same value and update function.
	 */
	public CachedValue<T> copy() {
		CachedValue<T> copy = new CachedValue<>(updateFunction);
		copy.needsUpdating = needsUpdating;
		copy.value = value;
		return copy;
	}
	
	/**
	 * Update the cached value using the supplier function.
	 */
	private void updateValue() {
		value = updateFunction.get();
		needsUpdating = false;
	}
}
