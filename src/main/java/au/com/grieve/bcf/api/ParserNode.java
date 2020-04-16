/*
 * PortalNetwork - Portals for Players
 * Copyright (C) 2020 PortalNetwork Developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.com.grieve.bcf.api;


import lombok.Getter;
import lombok.Setter;

import java.io.StringReader;
import java.util.*;

public class ParserNode implements Iterable<ParserNode> {

    @Getter
    ParserNode parent;
    @Getter
    final List<ParserNode> children;

    @Getter
    ParserNodeData data;

    private final List<ParserNode> elementsIndex;
    // Method to call for this node
    @Getter
    @Setter
    ParserMethod execute;
    // Method to call if no child node found
    ParserMethod default_;
    // Method to call if an error on children
    @Getter
    @Setter
    ParserMethod error;

    public ParserNode() {
        this.children = new ArrayList<>();
        this.elementsIndex = new ArrayList<>();
        this.elementsIndex.add(this);
    }

    public ParserNode(ParserNodeData data) {
        this();
        this.data = data;
    }

    public boolean isRoot() {
        return parent == null;
    }

    @SuppressWarnings("unused")
    public boolean isLeaf() {
        return children.size() == 0;
    }

    public ParserNode addChild(ParserNode child) {
        assert (child.isRoot());

        child.parent = this;
        this.children.add(child);
        this.registerChildForSearch(child);
        return child;
    }

    @SuppressWarnings("unused")
    public int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }

    private void registerChildForSearch(ParserNode node) {
        elementsIndex.add(node);
        if (parent != null)
            parent.registerChildForSearch(node);
    }

    @SuppressWarnings("unused")
    public ParserNode find(Comparable<ParserNode> cmp) {
        for (ParserNode element : this.elementsIndex) {
            if (cmp.compareTo(element) == 0)
                return element;
        }

        return null;
    }

    @SuppressWarnings("unused")
    public boolean contains(ParserNode node) {
        return children.stream()
                .filter(c -> c.equals(node))
                .findFirst()
                .orElse(null) != null;
    }

    /**
     * Create a new node at path.
     * <p>
     * As this may result in multiple nodes created the new leaf nodes are returned
     */
    public List<ParserNode> create(String path) {
        StringReader reader = new StringReader(path);

        return create(reader);
    }

    public List<ParserNode> create(StringReader reader) {

        List<ParserNode> current = Collections.singletonList(this);

        for (List<ParserNodeData> newData = ParserNodeData.parse(reader); newData.size() > 0; newData = ParserNodeData.parse(reader)) {
            List<ParserNode> newCurrent = new ArrayList<>();

            for (ParserNode node : current) {
                for (ParserNodeData nodeData : newData) {
                    boolean found = false;
                    for (ParserNode child : node.children) {
                        if (child.data.equals(nodeData)) {
                            newCurrent.add(child);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        newCurrent.add(node.addChild(new ParserNode(nodeData)));
                    }
                }
            }

            current = newCurrent;
        }

        return current;
    }

    /**
     * Return a walk of the tree under ourself
     */
    @SuppressWarnings("unused")
    public String walkTree() {
        return walkTree(0);
    }

    private String walkTree(int depth) {
        StringBuilder result = new StringBuilder();

        char[] repeat = new char[depth];
        Arrays.fill(repeat, ' ');
        String pad = new String(repeat);

        result.append(pad).append(toString()).append("\n");

        for (ParserNode n : children) {
            result.append(n.walkTree(depth + 1));
        }
        return result.toString();
    }

    public ParserMethod getDefault() {
        return default_;
    }

    public void setDefault(ParserMethod value) {
        default_ = value;
    }

    @Override
    public Iterator<ParserNode> iterator() {
        return new ParserNodeIter<ParserNode>(this);
    }

    @Override
    public String toString() {
        return "node(data=" + (data == null ? "null" : data.toString()) + ", " +
                "execute=" + execute + ", " +
                "default=" + default_ + ", " +
                "error=" + error + ")";
    }

}