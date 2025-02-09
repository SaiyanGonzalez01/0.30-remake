package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.LongCursor;
import com.carrotsearch.hppc.predicates.LongPredicate;
import com.carrotsearch.hppc.procedures.LongProcedure;
import java.util.Iterator;

/** A generic container holding <code>long</code>s. */
@com.carrotsearch.hppc.Generated(date = "2024-06-04T15:20:17+0200", value = "KTypeContainer.java")
public interface LongContainer extends Iterable<LongCursor> {
	/**
	 * Returns an iterator to a cursor traversing the collection. The order of
	 * traversal is not defined. More than one cursor may be active at a time. The
	 * behavior of iterators is undefined if structural changes are made to the
	 * underlying collection.
	 *
	 * <p>
	 * The iterator is implemented as a cursor and it returns <b>the same cursor
	 * instance</b> on every call to {@link Iterator#next()} (to avoid boxing of
	 * primitive types). To read the current list's value (or index in the list) use
	 * the cursor's public fields. An example is shown below.
	 *
	 * <pre>
	 * for (LongCursor&lt;long&gt; c : container) {
	 *   System.out.println(&quot;index=&quot; + c.index + &quot; value=&quot; + c.value);
	 * }
	 * </pre>
	 */
	public Iterator<LongCursor> iterator();

	/**
	 * Lookup a given element in the container. This operation has no speed
	 * guarantees (may be linear with respect to the size of this container).
	 *
	 * @return Returns <code>true</code> if this container has an element equal to
	 *         <code>e</code>.
	 */
	public boolean contains(long e);

	/**
	 * Return the current number of elements in this container. The time for
	 * calculating the container's size may take <code>O(n)</code> time, although
	 * implementing classes should try to maintain the current size and return in
	 * constant time.
	 */
	public int size();

	/** Shortcut for <code>size() == 0</code>. */
	public boolean isEmpty();

	/**
	 * Copies all elements of this container to an array.
	 *
	 * <p>
	 * The returned array is always a copy, regardless of the storage used by the
	 * container.
	 */
	public long[] toArray();

	/**
	 * Applies a <code>procedure</code> to all container elements. Returns the
	 * argument (any subclass of {@link LongProcedure}. This lets the caller to call
	 * methods of the argument by chaining the call (even if the argument is an
	 * anonymous type) to retrieve computed values, for example (IntContainer):
	 *
	 * <pre>
	 * int count = container.forEach(new IntProcedure() {
	 * 	int count; // this is a field declaration in an anonymous class.
	 *
	 * 	public void apply(int value) {
	 * 		count++;
	 * 	}
	 * }).count;
	 * </pre>
	 */
	public <T extends LongProcedure> T forEach(T procedure);

	/**
	 * Applies a <code>predicate</code> to container elements as long, as the
	 * predicate returns <code>
	 * true</code>. The iteration is interrupted otherwise.
	 */
	public <T extends LongPredicate> T forEach(T predicate);
}
