/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.CollectPreconditions.checkNonnegative;
import static com.google.common.collect.CollectPreconditions.checkRemove;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import javax.annotation.Nullable;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.math.IntMath;
import com.google.common.primitives.Ints;

/**
 * Static utility methods pertaining to {@link List} instances. Also see this
 * class's counterparts {@link Sets}, {@link Maps} and {@link Queues}.
 *
 * <p>
 * See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/CollectionUtilitiesExplained#Lists">
 * {@code Lists}</a>.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 * @author Louis Wasserman
 * @since 2.0 (imported from Google Collections Library)
 */
@GwtCompatible(emulated = true)
public final class Lists {
	private Lists() {
	}

	// ArrayList

	/**
	 * Creates a <i>mutable</i>, empty {@code ArrayList} instance.
	 *
	 * <p>
	 * <b>Note:</b> if mutability is not required, use {@link ImmutableList#of()}
	 * instead.
	 *
	 * @return a new, empty {@code ArrayList}
	 */
	@GwtCompatible(serializable = true)
	public static <E> ArrayList<E> newArrayList() {
		return new ArrayList<E>();
	}

	/**
	 * Creates a <i>mutable</i> {@code ArrayList} instance containing the given
	 * elements.
	 *
	 * <p>
	 * <b>Note:</b> if mutability is not required and the elements are non-null, use
	 * an overload of {@link ImmutableList#of()} (for varargs) or
	 * {@link ImmutableList#copyOf(Object[])} (for an array) instead.
	 *
	 * @param elements the elements that the list should contain, in order
	 * @return a new {@code ArrayList} containing those elements
	 */
	@GwtCompatible(serializable = true)
	public static <E> ArrayList<E> newArrayList(E... elements) {
		checkNotNull(elements); // for GWT
		// Avoid integer overflow when a large array is passed in
		int capacity = computeArrayListCapacity(elements.length);
		ArrayList<E> list = new ArrayList<E>(capacity);
		Collections.addAll(list, elements);
		return list;
	}

	@VisibleForTesting
	static int computeArrayListCapacity(int arraySize) {
		checkNonnegative(arraySize, "arraySize");

		// TODO(kevinb): Figure out the right behavior, and document it
		return Ints.saturatedCast(5L + arraySize + (arraySize / 10));
	}

	/**
	 * Creates a <i>mutable</i> {@code ArrayList} instance containing the given
	 * elements.
	 *
	 * <p>
	 * <b>Note:</b> if mutability is not required and the elements are non-null, use
	 * {@link ImmutableList#copyOf(Iterator)} instead.
	 *
	 * @param elements the elements that the list should contain, in order
	 * @return a new {@code ArrayList} containing those elements
	 */
	@GwtCompatible(serializable = true)
	public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
		checkNotNull(elements); // for GWT
		// Let ArrayList's sizing logic work, if possible
		return (elements instanceof Collection) ? new ArrayList<E>(Collections2.cast(elements))
				: newArrayList(elements.iterator());
	}

	/**
	 * Creates a <i>mutable</i> {@code ArrayList} instance containing the given
	 * elements.
	 *
	 * <p>
	 * <b>Note:</b> if mutability is not required and the elements are non-null, use
	 * {@link ImmutableList#copyOf(Iterator)} instead.
	 *
	 * @param elements the elements that the list should contain, in order
	 * @return a new {@code ArrayList} containing those elements
	 */
	@GwtCompatible(serializable = true)
	public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
		ArrayList<E> list = newArrayList();
		Iterators.addAll(list, elements);
		return list;
	}

	/**
	 * Creates an {@code ArrayList} instance backed by an array of the <i>exact</i>
	 * size specified; equivalent to {@link ArrayList#ArrayList(int)}.
	 *
	 * <p>
	 * <b>Note:</b> if you know the exact size your list will be, consider using a
	 * fixed-size list ({@link Arrays#asList(Object[])}) or an {@link ImmutableList}
	 * instead of a growable {@link ArrayList}.
	 *
	 * <p>
	 * <b>Note:</b> If you have only an <i>estimate</i> of the eventual size of the
	 * list, consider padding this estimate by a suitable amount, or simply use
	 * {@link #newArrayListWithExpectedSize(int)} instead.
	 *
	 * @param initialArraySize the exact size of the initial backing array for the
	 *                         returned array list ({@code ArrayList} documentation
	 *                         calls this value the "capacity")
	 * @return a new, empty {@code ArrayList} which is guaranteed not to resize
	 *         itself unless its size reaches {@code initialArraySize + 1}
	 * @throws IllegalArgumentException if {@code initialArraySize} is negative
	 */
	@GwtCompatible(serializable = true)
	public static <E> ArrayList<E> newArrayListWithCapacity(int initialArraySize) {
		checkNonnegative(initialArraySize, "initialArraySize"); // for GWT.
		return new ArrayList<E>(initialArraySize);
	}

	/**
	 * Creates an {@code ArrayList} instance sized appropriately to hold an
	 * <i>estimated</i> number of elements without resizing. A small amount of
	 * padding is added in case the estimate is low.
	 *
	 * <p>
	 * <b>Note:</b> If you know the <i>exact</i> number of elements the list will
	 * hold, or prefer to calculate your own amount of padding, refer to
	 * {@link #newArrayListWithCapacity(int)}.
	 *
	 * @param estimatedSize an estimate of the eventual {@link List#size()} of the
	 *                      new list
	 * @return a new, empty {@code ArrayList}, sized appropriately to hold the
	 *         estimated number of elements
	 * @throws IllegalArgumentException if {@code estimatedSize} is negative
	 */
	@GwtCompatible(serializable = true)
	public static <E> ArrayList<E> newArrayListWithExpectedSize(int estimatedSize) {
		return new ArrayList<E>(computeArrayListCapacity(estimatedSize));
	}

	// LinkedList

	/**
	 * Creates an empty {@code LinkedList} instance.
	 *
	 * <p>
	 * <b>Note:</b> if you need an immutable empty {@link List}, use
	 * {@link ImmutableList#of()} instead.
	 *
	 * @return a new, empty {@code LinkedList}
	 */
	@GwtCompatible(serializable = true)
	public static <E> LinkedList<E> newLinkedList() {
		return new LinkedList<E>();
	}

	/**
	 * Creates a {@code LinkedList} instance containing the given elements.
	 *
	 * @param elements the elements that the list should contain, in order
	 * @return a new {@code LinkedList} containing those elements
	 */
	@GwtCompatible(serializable = true)
	public static <E> LinkedList<E> newLinkedList(Iterable<? extends E> elements) {
		LinkedList<E> list = newLinkedList();
		Iterables.addAll(list, elements);
		return list;
	}

	/**
	 * Returns an unmodifiable list containing the specified first element and
	 * backed by the specified array of additional elements. Changes to the {@code
	 * rest} array will be reflected in the returned list. Unlike
	 * {@link Arrays#asList}, the returned list is unmodifiable.
	 *
	 * <p>
	 * This is useful when a varargs method needs to use a signature such as
	 * {@code (Foo firstFoo, Foo... moreFoos)}, in order to avoid overload ambiguity
	 * or to enforce a minimum argument count.
	 *
	 * <p>
	 * The returned list is serializable and implements {@link RandomAccess}.
	 *
	 * @param first the first element
	 * @param rest  an array of additional elements, possibly empty
	 * @return an unmodifiable list containing the specified elements
	 */
	public static <E> List<E> asList(@Nullable E first, E[] rest) {
		return new OnePlusArrayList<E>(first, rest);
	}

	/** @see Lists#asList(Object, Object[]) */
	private static class OnePlusArrayList<E> extends AbstractList<E> implements Serializable, RandomAccess {
		final E first;
		final E[] rest;

		OnePlusArrayList(@Nullable E first, E[] rest) {
			this.first = first;
			this.rest = checkNotNull(rest);
		}

		@Override
		public int size() {
			return rest.length + 1;
		}

		@Override
		public E get(int index) {
			// check explicitly so the IOOBE will have the right message
			checkElementIndex(index, size());
			return (index == 0) ? first : rest[index - 1];
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Returns an unmodifiable list containing the specified first and second
	 * element, and backed by the specified array of additional elements. Changes to
	 * the {@code rest} array will be reflected in the returned list. Unlike
	 * {@link Arrays#asList}, the returned list is unmodifiable.
	 *
	 * <p>
	 * This is useful when a varargs method needs to use a signature such as
	 * {@code (Foo firstFoo, Foo secondFoo, Foo... moreFoos)}, in order to avoid
	 * overload ambiguity or to enforce a minimum argument count.
	 *
	 * <p>
	 * The returned list is serializable and implements {@link RandomAccess}.
	 *
	 * @param first  the first element
	 * @param second the second element
	 * @param rest   an array of additional elements, possibly empty
	 * @return an unmodifiable list containing the specified elements
	 */
	public static <E> List<E> asList(@Nullable E first, @Nullable E second, E[] rest) {
		return new TwoPlusArrayList<E>(first, second, rest);
	}

	/** @see Lists#asList(Object, Object, Object[]) */
	private static class TwoPlusArrayList<E> extends AbstractList<E> implements Serializable, RandomAccess {
		final E first;
		final E second;
		final E[] rest;

		TwoPlusArrayList(@Nullable E first, @Nullable E second, E[] rest) {
			this.first = first;
			this.second = second;
			this.rest = checkNotNull(rest);
		}

		@Override
		public int size() {
			return rest.length + 2;
		}

		@Override
		public E get(int index) {
			switch (index) {
			case 0:
				return first;
			case 1:
				return second;
			default:
				// check explicitly so the IOOBE will have the right message
				checkElementIndex(index, size());
				return rest[index - 2];
			}
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Returns every possible list that can be formed by choosing one element from
	 * each of the given lists in order; the "n-ary
	 * <a href="http://en.wikipedia.org/wiki/Cartesian_product">Cartesian
	 * product</a>" of the lists. For example:
	 * 
	 * <pre>
	 *    {@code
	 *
	 * Lists.cartesianProduct(ImmutableList.of(ImmutableList.of(1, 2), ImmutableList.of("A", "B", "C")))
	 * }
	 * </pre>
	 *
	 * <p>
	 * returns a list containing six lists in the following order:
	 *
	 * <ul>
	 * <li>{@code ImmutableList.of(1, "A")}
	 * <li>{@code ImmutableList.of(1, "B")}
	 * <li>{@code ImmutableList.of(1, "C")}
	 * <li>{@code ImmutableList.of(2, "A")}
	 * <li>{@code ImmutableList.of(2, "B")}
	 * <li>{@code ImmutableList.of(2, "C")}
	 * </ul>
	 *
	 * <p>
	 * The result is guaranteed to be in the "traditional", lexicographical order
	 * for Cartesian products that you would get from nesting for loops:
	 * 
	 * <pre>
	 *    {@code
	 *
	 *   for (B b0 : lists.get(0)) {
	 *     for (B b1 : lists.get(1)) {
	 *       ...
	 *       ImmutableList<B> tuple = ImmutableList.of(b0, b1, ...);
	 *       // operate on tuple
	 *     }
	 *   }}
	 * </pre>
	 *
	 * <p>
	 * Note that if any input list is empty, the Cartesian product will also be
	 * empty. If no lists at all are provided (an empty list), the resulting
	 * Cartesian product has one element, an empty list (counter-intuitive, but
	 * mathematically consistent).
	 *
	 * <p>
	 * <i>Performance notes:</i> while the cartesian product of lists of size
	 * {@code m, n, p} is a list of size {@code m x n x p}, its actual memory
	 * consumption is much smaller. When the cartesian product is constructed, the
	 * input lists are merely copied. Only as the resulting list is iterated are the
	 * individual lists created, and these are not retained after iteration.
	 *
	 * @param lists the lists to choose elements from, in the order that the
	 *              elements chosen from those lists should appear in the resulting
	 *              lists
	 * @param <B>   any common base class shared by all axes (often just
	 *              {@link Object})
	 * @return the Cartesian product, as an immutable list containing immutable
	 *         lists
	 * @throws IllegalArgumentException if the size of the cartesian product would
	 *                                  be greater than {@link Integer#MAX_VALUE}
	 * @throws NullPointerException     if {@code lists}, any one of the
	 *                                  {@code lists}, or any element of a provided
	 *                                  list is null
	 */
	static <B> List<List<B>> cartesianProduct(List<? extends List<? extends B>> lists) {
		return CartesianList.create(lists);
	}

	/**
	 * Returns every possible list that can be formed by choosing one element from
	 * each of the given lists in order; the "n-ary
	 * <a href="http://en.wikipedia.org/wiki/Cartesian_product">Cartesian
	 * product</a>" of the lists. For example:
	 * 
	 * <pre>
	 *    {@code
	 *
	 * Lists.cartesianProduct(ImmutableList.of(ImmutableList.of(1, 2), ImmutableList.of("A", "B", "C")))
	 * }
	 * </pre>
	 *
	 * <p>
	 * returns a list containing six lists in the following order:
	 *
	 * <ul>
	 * <li>{@code ImmutableList.of(1, "A")}
	 * <li>{@code ImmutableList.of(1, "B")}
	 * <li>{@code ImmutableList.of(1, "C")}
	 * <li>{@code ImmutableList.of(2, "A")}
	 * <li>{@code ImmutableList.of(2, "B")}
	 * <li>{@code ImmutableList.of(2, "C")}
	 * </ul>
	 *
	 * <p>
	 * The result is guaranteed to be in the "traditional", lexicographical order
	 * for Cartesian products that you would get from nesting for loops:
	 * 
	 * <pre>
	 *    {@code
	 *
	 *   for (B b0 : lists.get(0)) {
	 *     for (B b1 : lists.get(1)) {
	 *       ...
	 *       ImmutableList<B> tuple = ImmutableList.of(b0, b1, ...);
	 *       // operate on tuple
	 *     }
	 *   }}
	 * </pre>
	 *
	 * <p>
	 * Note that if any input list is empty, the Cartesian product will also be
	 * empty. If no lists at all are provided (an empty list), the resulting
	 * Cartesian product has one element, an empty list (counter-intuitive, but
	 * mathematically consistent).
	 *
	 * <p>
	 * <i>Performance notes:</i> while the cartesian product of lists of size
	 * {@code m, n, p} is a list of size {@code m x n x p}, its actual memory
	 * consumption is much smaller. When the cartesian product is constructed, the
	 * input lists are merely copied. Only as the resulting list is iterated are the
	 * individual lists created, and these are not retained after iteration.
	 *
	 * @param lists the lists to choose elements from, in the order that the
	 *              elements chosen from those lists should appear in the resulting
	 *              lists
	 * @param <B>   any common base class shared by all axes (often just
	 *              {@link Object})
	 * @return the Cartesian product, as an immutable list containing immutable
	 *         lists
	 * @throws IllegalArgumentException if the size of the cartesian product would
	 *                                  be greater than {@link Integer#MAX_VALUE}
	 * @throws NullPointerException     if {@code lists}, any one of the
	 *                                  {@code lists}, or any element of a provided
	 *                                  list is null
	 */
	static <B> List<List<B>> cartesianProduct(List<? extends B>... lists) {
		return cartesianProduct(Arrays.asList(lists));
	}

	/**
	 * Returns a list that applies {@code function} to each element of {@code
	 * fromList}. The returned list is a transformed view of {@code fromList};
	 * changes to {@code fromList} will be reflected in the returned list and vice
	 * versa.
	 *
	 * <p>
	 * Since functions are not reversible, the transform is one-way and new items
	 * cannot be stored in the returned list. The {@code add}, {@code addAll} and
	 * {@code set} methods are unsupported in the returned list.
	 *
	 * <p>
	 * The function is applied lazily, invoked when needed. This is necessary for
	 * the returned list to be a view, but it means that the function will be
	 * applied many times for bulk operations like {@link List#contains} and
	 * {@link List#hashCode}. For this to perform well, {@code function} should be
	 * fast. To avoid lazy evaluation when the returned list doesn't need to be a
	 * view, copy the returned list into a new list of your choosing.
	 *
	 * <p>
	 * If {@code fromList} implements {@link RandomAccess}, so will the returned
	 * list. The returned list is threadsafe if the supplied list and function are.
	 *
	 * <p>
	 * If only a {@code Collection} or {@code Iterable} input is available, use
	 * {@link Collections2#transform} or {@link Iterables#transform}.
	 *
	 * <p>
	 * <b>Note:</b> serializing the returned list is implemented by serializing
	 * {@code fromList}, its contents, and {@code function} -- <i>not</i> by
	 * serializing the transformed values. This can lead to surprising behavior, so
	 * serializing the returned list is <b>not recommended</b>. Instead, copy the
	 * list using {@link ImmutableList#copyOf(Collection)} (for example), then
	 * serialize the copy. Other methods similar to this do not implement
	 * serialization at all for this reason.
	 */
	public static <F, T> List<T> transform(List<F> fromList, Function<? super F, ? extends T> function) {
		return (fromList instanceof RandomAccess) ? new TransformingRandomAccessList<F, T>(fromList, function)
				: new TransformingSequentialList<F, T>(fromList, function);
	}

	/**
	 * Implementation of a sequential transforming list.
	 *
	 * @see Lists#transform
	 */
	private static class TransformingSequentialList<F, T> extends AbstractSequentialList<T> implements Serializable {
		final List<F> fromList;
		final Function<? super F, ? extends T> function;

		TransformingSequentialList(List<F> fromList, Function<? super F, ? extends T> function) {
			this.fromList = checkNotNull(fromList);
			this.function = checkNotNull(function);
		}

		/**
		 * The default implementation inherited is based on iteration and removal of
		 * each element which can be overkill. That's why we forward this call directly
		 * to the backing list.
		 */
		@Override
		public void clear() {
			fromList.clear();
		}

		@Override
		public int size() {
			return fromList.size();
		}

		@Override
		public ListIterator<T> listIterator(final int index) {
			return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
				@Override
				T transform(F from) {
					return function.apply(from);
				}
			};
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Implementation of a transforming random access list. We try to make as many
	 * of these methods pass-through to the source list as possible so that the
	 * performance characteristics of the source list and transformed list are
	 * similar.
	 *
	 * @see Lists#transform
	 */
	private static class TransformingRandomAccessList<F, T> extends AbstractList<T>
			implements RandomAccess, Serializable {
		final List<F> fromList;
		final Function<? super F, ? extends T> function;

		TransformingRandomAccessList(List<F> fromList, Function<? super F, ? extends T> function) {
			this.fromList = checkNotNull(fromList);
			this.function = checkNotNull(function);
		}

		@Override
		public void clear() {
			fromList.clear();
		}

		@Override
		public T get(int index) {
			return function.apply(fromList.get(index));
		}

		@Override
		public Iterator<T> iterator() {
			return listIterator();
		}

		@Override
		public ListIterator<T> listIterator(int index) {
			return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
				@Override
				T transform(F from) {
					return function.apply(from);
				}
			};
		}

		@Override
		public boolean isEmpty() {
			return fromList.isEmpty();
		}

		@Override
		public T remove(int index) {
			return function.apply(fromList.remove(index));
		}

		@Override
		public int size() {
			return fromList.size();
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Returns consecutive {@linkplain List#subList(int, int) sublists} of a list,
	 * each of the same size (the final list may be smaller). For example,
	 * partitioning a list containing {@code [a, b, c, d, e]} with a partition size
	 * of 3 yields {@code [[a, b, c], [d, e]]} -- an outer list containing two inner
	 * lists of three and two elements, all in the original order.
	 *
	 * <p>
	 * The outer list is unmodifiable, but reflects the latest state of the source
	 * list. The inner lists are sublist views of the original list, produced on
	 * demand using {@link List#subList(int, int)}, and are subject to all the usual
	 * caveats about modification as explained in that API.
	 *
	 * @param list the list to return consecutive sublists of
	 * @param size the desired size of each sublist (the last may be smaller)
	 * @return a list of consecutive sublists
	 * @throws IllegalArgumentException if {@code partitionSize} is nonpositive
	 */
	public static <T> List<List<T>> partition(List<T> list, int size) {
		checkNotNull(list);
		checkArgument(size > 0);
		return (list instanceof RandomAccess) ? new RandomAccessPartition<T>(list, size) : new Partition<T>(list, size);
	}

	private static class Partition<T> extends AbstractList<List<T>> {
		final List<T> list;
		final int size;

		Partition(List<T> list, int size) {
			this.list = list;
			this.size = size;
		}

		@Override
		public List<T> get(int index) {
			checkElementIndex(index, size());
			int start = index * size;
			int end = Math.min(start + size, list.size());
			return list.subList(start, end);
		}

		@Override
		public int size() {
			return IntMath.divide(list.size(), size, RoundingMode.CEILING);
		}

		@Override
		public boolean isEmpty() {
			return list.isEmpty();
		}
	}

	private static class RandomAccessPartition<T> extends Partition<T> implements RandomAccess {
		RandomAccessPartition(List<T> list, int size) {
			super(list, size);
		}
	}

	/**
	 * Returns a view of the specified string as an immutable list of {@code
	 * Character} values.
	 *
	 * @since 7.0
	 */
	@Beta
	public static ImmutableList<Character> charactersOf(String string) {
		return new StringAsImmutableList(checkNotNull(string));
	}

	@SuppressWarnings("serial") // serialized using ImmutableList serialization
	private static final class StringAsImmutableList extends ImmutableList<Character> {

		private final String string;

		StringAsImmutableList(String string) {
			this.string = string;
		}

		@Override
		public int indexOf(@Nullable Object object) {
			return (object instanceof Character) ? string.indexOf((Character) object) : -1;
		}

		@Override
		public int lastIndexOf(@Nullable Object object) {
			return (object instanceof Character) ? string.lastIndexOf((Character) object) : -1;
		}

		@Override
		public ImmutableList<Character> subList(int fromIndex, int toIndex) {
			checkPositionIndexes(fromIndex, toIndex, size()); // for GWT
			return charactersOf(string.substring(fromIndex, toIndex));
		}

		@Override
		boolean isPartialView() {
			return false;
		}

		@Override
		public Character get(int index) {
			checkElementIndex(index, size()); // for GWT
			return string.charAt(index);
		}

		@Override
		public int size() {
			return string.length();
		}
	}

	/**
	 * Returns a view of the specified {@code CharSequence} as a {@code
	 * List<Character>}, viewing {@code sequence} as a sequence of Unicode code
	 * units. The view does not support any modification operations, but reflects
	 * any changes to the underlying character sequence.
	 *
	 * @param sequence the character sequence to view as a {@code List} of
	 *                 characters
	 * @return an {@code List<Character>} view of the character sequence
	 * @since 7.0
	 */
	@Beta
	public static List<Character> charactersOf(CharSequence sequence) {
		return new CharSequenceAsList(checkNotNull(sequence));
	}

	private static final class CharSequenceAsList extends AbstractList<Character> {
		private final CharSequence sequence;

		CharSequenceAsList(CharSequence sequence) {
			this.sequence = sequence;
		}

		@Override
		public Character get(int index) {
			checkElementIndex(index, size()); // for GWT
			return sequence.charAt(index);
		}

		@Override
		public int size() {
			return sequence.length();
		}
	}

	/**
	 * Returns a reversed view of the specified list. For example, {@code
	 * Lists.reverse(Arrays.asList(1, 2, 3))} returns a list containing {@code 3,
	 * 2, 1}. The returned list is backed by this list, so changes in the returned
	 * list are reflected in this list, and vice-versa. The returned list supports
	 * all of the optional list operations supported by this list.
	 *
	 * <p>
	 * The returned list is random-access if the specified list is random access.
	 *
	 * @since 7.0
	 */
	public static <T> List<T> reverse(List<T> list) {
		if (list instanceof ImmutableList) {
			return ((ImmutableList<T>) list).reverse();
		} else if (list instanceof ReverseList) {
			return ((ReverseList<T>) list).getForwardList();
		} else if (list instanceof RandomAccess) {
			return new RandomAccessReverseList<T>(list);
		} else {
			return new ReverseList<T>(list);
		}
	}

	private static class ReverseList<T> extends AbstractList<T> {
		private final List<T> forwardList;

		ReverseList(List<T> forwardList) {
			this.forwardList = checkNotNull(forwardList);
		}

		List<T> getForwardList() {
			return forwardList;
		}

		private int reverseIndex(int index) {
			int size = size();
			checkElementIndex(index, size);
			return (size - 1) - index;
		}

		private int reversePosition(int index) {
			int size = size();
			checkPositionIndex(index, size);
			return size - index;
		}

		@Override
		public void add(int index, @Nullable T element) {
			forwardList.add(reversePosition(index), element);
		}

		@Override
		public void clear() {
			forwardList.clear();
		}

		@Override
		public T remove(int index) {
			return forwardList.remove(reverseIndex(index));
		}

		@Override
		protected void removeRange(int fromIndex, int toIndex) {
			subList(fromIndex, toIndex).clear();
		}

		@Override
		public T set(int index, @Nullable T element) {
			return forwardList.set(reverseIndex(index), element);
		}

		@Override
		public T get(int index) {
			return forwardList.get(reverseIndex(index));
		}

		@Override
		public int size() {
			return forwardList.size();
		}

		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			checkPositionIndexes(fromIndex, toIndex, size());
			return reverse(forwardList.subList(reversePosition(toIndex), reversePosition(fromIndex)));
		}

		@Override
		public Iterator<T> iterator() {
			return listIterator();
		}

		@Override
		public ListIterator<T> listIterator(int index) {
			int start = reversePosition(index);
			final ListIterator<T> forwardIterator = forwardList.listIterator(start);
			return new ListIterator<T>() {

				boolean canRemoveOrSet;

				@Override
				public void add(T e) {
					forwardIterator.add(e);
					forwardIterator.previous();
					canRemoveOrSet = false;
				}

				@Override
				public boolean hasNext() {
					return forwardIterator.hasPrevious();
				}

				@Override
				public boolean hasPrevious() {
					return forwardIterator.hasNext();
				}

				@Override
				public T next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					canRemoveOrSet = true;
					return forwardIterator.previous();
				}

				@Override
				public int nextIndex() {
					return reversePosition(forwardIterator.nextIndex());
				}

				@Override
				public T previous() {
					if (!hasPrevious()) {
						throw new NoSuchElementException();
					}
					canRemoveOrSet = true;
					return forwardIterator.next();
				}

				@Override
				public int previousIndex() {
					return nextIndex() - 1;
				}

				@Override
				public void remove() {
					checkRemove(canRemoveOrSet);
					forwardIterator.remove();
					canRemoveOrSet = false;
				}

				@Override
				public void set(T e) {
					checkState(canRemoveOrSet);
					forwardIterator.set(e);
				}
			};
		}
	}

	private static class RandomAccessReverseList<T> extends ReverseList<T> implements RandomAccess {
		RandomAccessReverseList(List<T> forwardList) {
			super(forwardList);
		}
	}

	/**
	 * An implementation of {@link List#hashCode()}.
	 */
	static int hashCodeImpl(List<?> list) {
		// TODO(user): worth optimizing for RandomAccess?
		int hashCode = 1;
		for (Object o : list) {
			hashCode = 31 * hashCode + (o == null ? 0 : o.hashCode());

			hashCode = ~~hashCode;
			// needed to deal with GWT integer overflow
		}
		return hashCode;
	}

	/**
	 * An implementation of {@link List#equals(Object)}.
	 */
	static boolean equalsImpl(List<?> list, @Nullable Object object) {
		if (object == checkNotNull(list)) {
			return true;
		}
		if (!(object instanceof List)) {
			return false;
		}

		List<?> o = (List<?>) object;

		return list.size() == o.size() && Iterators.elementsEqual(list.iterator(), o.iterator());
	}

	/**
	 * An implementation of {@link List#addAll(int, Collection)}.
	 */
	static <E> boolean addAllImpl(List<E> list, int index, Iterable<? extends E> elements) {
		boolean changed = false;
		ListIterator<E> listIterator = list.listIterator(index);
		for (E e : elements) {
			listIterator.add(e);
			changed = true;
		}
		return changed;
	}

	/**
	 * An implementation of {@link List#indexOf(Object)}.
	 */
	static int indexOfImpl(List<?> list, @Nullable Object element) {
		ListIterator<?> listIterator = list.listIterator();
		while (listIterator.hasNext()) {
			if (Objects.equal(element, listIterator.next())) {
				return listIterator.previousIndex();
			}
		}
		return -1;
	}

	/**
	 * An implementation of {@link List#lastIndexOf(Object)}.
	 */
	static int lastIndexOfImpl(List<?> list, @Nullable Object element) {
		ListIterator<?> listIterator = list.listIterator(list.size());
		while (listIterator.hasPrevious()) {
			if (Objects.equal(element, listIterator.previous())) {
				return listIterator.nextIndex();
			}
		}
		return -1;
	}

	/**
	 * Returns an implementation of {@link List#listIterator(int)}.
	 */
	static <E> ListIterator<E> listIteratorImpl(List<E> list, int index) {
		return new AbstractListWrapper<E>(list).listIterator(index);
	}

	/**
	 * An implementation of {@link List#subList(int, int)}.
	 */
	static <E> List<E> subListImpl(final List<E> list, int fromIndex, int toIndex) {
		List<E> wrapper;
		if (list instanceof RandomAccess) {
			wrapper = new RandomAccessListWrapper<E>(list) {
				@Override
				public ListIterator<E> listIterator(int index) {
					return backingList.listIterator(index);
				}

				private static final long serialVersionUID = 0;
			};
		} else {
			wrapper = new AbstractListWrapper<E>(list) {
				@Override
				public ListIterator<E> listIterator(int index) {
					return backingList.listIterator(index);
				}

				private static final long serialVersionUID = 0;
			};
		}
		return wrapper.subList(fromIndex, toIndex);
	}

	private static class AbstractListWrapper<E> extends AbstractList<E> {
		final List<E> backingList;

		AbstractListWrapper(List<E> backingList) {
			this.backingList = checkNotNull(backingList);
		}

		@Override
		public void add(int index, E element) {
			backingList.add(index, element);
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			return backingList.addAll(index, c);
		}

		@Override
		public E get(int index) {
			return backingList.get(index);
		}

		@Override
		public E remove(int index) {
			return backingList.remove(index);
		}

		@Override
		public E set(int index, E element) {
			return backingList.set(index, element);
		}

		@Override
		public boolean contains(Object o) {
			return backingList.contains(o);
		}

		@Override
		public int size() {
			return backingList.size();
		}
	}

	private static class RandomAccessListWrapper<E> extends AbstractListWrapper<E> implements RandomAccess {
		RandomAccessListWrapper(List<E> backingList) {
			super(backingList);
		}
	}

	/**
	 * Used to avoid http://bugs.sun.com/view_bug.do?bug_id=6558557
	 */
	static <T> List<T> cast(Iterable<T> iterable) {
		return (List<T>) iterable;
	}
}
