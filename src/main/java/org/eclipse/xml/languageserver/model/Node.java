package org.eclipse.xml.languageserver.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Node {

	public String tag;
	public boolean closed = false;
	public Integer endTagStart;

	public Map<String, String> attributes;
	public List<Node> children;
	public final int start;
	public int end;
	public Node parent;

	public Set<String> attributeNames() {
		return this.attributes != null ? attributes.keySet() : Collections.emptySet();
	}

	public Node(int start, int end, List<Node> children) {
		this(start, end, children, null);
	}

	public Node(int start, int end, List<Node> children, Node parent) {
		this.start = start;
		this.end = end;
		this.children = children;
		this.parent = parent;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	private String toString(int indent) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < indent; i++) {
			result.append("\t");
		}
		result.append("{start: ");
		result.append(start);
		result.append(", end: ");
		result.append(end);
		result.append(", tag: ");
		result.append(tag);
		if (children != null && children.size() > 0) {
			result.append(", \n");
			for (int i = 0; i < indent + 1; i++) {
				result.append("\t");
			}
			result.append("children:[");
			for (int i = 0; i < children.size(); i++) {
				Node node = children.get(i);
				result.append("\n");
				result.append(node.toString(indent + 2));
				if (i < children.size() - 1) {
					result.append(",");
				}
			}
			result.append("\n");
			for (int i = 0; i < indent + 1; i++) {
				result.append("\t");
			}
			result.append("]");
			result.append("\n");
			for (int i = 0; i < indent; i++) {
				result.append("\t");
			}
			result.append("}");
		} else {
			result.append("}");
		}
		return result.toString();
	}

	public boolean isSameTag(String tagInLowerCase) {
		return this.tag != null && tagInLowerCase != null && this.tag.length() == tagInLowerCase.length()
				&& this.tag.toLowerCase().equals(tagInLowerCase);
	}

	public Node firstChild() {
		return this.children.get(0);
	}

	public Node lastChild() {
		return this.children != null ? this.children.get(this.children.size() - 1) : null;
	}

	public Node findNodeBefore(int offset) {
		int idx = findFirst(this.children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = this.children.get(idx);
			if (offset > child.start) {
				if (offset < child.end) {
					return child.findNodeBefore(offset);
				}
				Node lastChild = child.lastChild();
				if (lastChild != null && lastChild.end == child.end) {
					return child.findNodeBefore(offset);
				}
				return child;
			}
		}
		return this;
	}

	public Node findNodeAt(int offset) {
		int idx = findFirst(this.children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = this.children.get(idx);
			if (offset > child.start && offset <= child.end) {
				return child.findNodeAt(offset);
			}
		}
		return this;
	}

	/**
	 * Takes a sorted array and a function p. The array is sorted in such a way that
	 * all elements where p(x) is false are located before all elements where p(x)
	 * is true.
	 * 
	 * @returns the least x for which p(x) is true or array.length if no element
	 *          fullfills the given function.
	 */
	private static <T> int findFirst(List<T> array, Function<T, Boolean> p) {
		int low = 0, high = array.size();
		if (high == 0) {
			return 0; // no children
		}
		while (low < high) {
			int mid = (int) Math.floor((low + high) / 2);
			if (p.apply(array.get(mid))) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return low;
	}
}