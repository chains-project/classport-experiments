/*
 * OutputArrayAnnotation.java
 * This file is part of JaCoP.
 * <p>
 * JaCoP is a Java Constraint Programming solver.
 * <p>
 * Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * Notwithstanding any other provision of this License, the copyright
 * owners of this work supplement the terms of this License with terms
 * prohibiting misrepresentation of the origin of this work and requiring
 * that modified versions of this work be marked in reasonable ways as
 * different from the original version. This supplement of the license
 * terms is in accordance with Section 7 of GNU Affero General Public
 * License version 3.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jacop.fz;

import org.jacop.core.BooleanVar;
import org.jacop.core.IntDomain;
import org.jacop.core.ValueEnumeration;
import org.jacop.core.Var;
import org.jacop.set.core.SetVar;

import java.util.ArrayList;

/**
 * It stores information about the annotation for an output array.
 *
 * @author Krzysztof Kuchcinski
 * @version 4.10
 */
public class OutputArrayAnnotation {

    String id;

    // TODO, ArrayList of Sets? Why? Is a set needed? Maybe just IntervalDomain suffices? 
    ArrayList<IntDomain> indexes;
    Var[] array;

    /**
     * It constructs and output array annotation.
     *
     * @param name        the name of the output array annotation.
     * @param indexBounds the indexes bounds.
     */
    public OutputArrayAnnotation(String name, ArrayList<IntDomain> indexBounds) {
        id = name;
        indexes = indexBounds;
    }

    String getName() {
        return id;
    }

    void setArray(Var[] a) {
        array = a;
    }

    Var[] getArray() {
        return array;
    }

    int getNumberIndexes() {
        return indexes.size();
    }

    IntDomain getIndexes(int i) {
        return indexes.get(i);
    }

    boolean contains(Var x) {

        for (Var v : array)
            if (x.equals(v))
                return true;
        return false;
    }

    public String toString() {

        StringBuilder s = new StringBuilder(id + " = array" + indexes.size() + "d(");

        for (int i = 0; i < indexes.size(); i++)
            if (indexes.get(i).getSize() == 0)
                // s.append(indexes.get(i)).append(",");
                s.append("{}, ");
            else
                s.append(indexes.get(i).min()).append("..").append(indexes.get(i).max()).append(", ");

        s.append("[");
        for (int i = 0; i < array.length; i++) {
            Var v = array[i];

            if (v instanceof BooleanVar) {
                if (v.singleton())
                    switch (((BooleanVar) v).value()) {
                        case 0:
                            s.append("false");
                            break;
                        case 1:
                            s.append("true");
                            break;
                        default:
                            s.append(v.dom().toString());
                    }
                else
                    s.append("false..true");
            } else if (v instanceof SetVar) {
                if (v.singleton()) {
                    IntDomain glb = ((SetVar) v).dom().glb();
                    if (glb.getSize() > 0 && glb.getSize() == glb.max() - glb.min() + 1) {
                        s.append(glb.min() + ".." + glb.max());
                    } else {
                        s.append("{");
                        for (ValueEnumeration e = glb.valueEnumeration(); e.hasMoreElements(); ) {
                            int element = e.nextElement();
                            s.append(element);
                            if (e.hasMoreElements())
                                s.append(", ");
                        }
                        s.append("}");
                    }
                } else
                    s.append(v.dom().toString());
            } else {
                s.append(v.dom().toString());
            }
            if (i < array.length - 1)
                s.append(", ");
        }
        s.append("]);");
        return s.toString();
    }
}
