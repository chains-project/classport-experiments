/*
 * MinCardDiff.java
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

package org.jacop.set.search;

import org.jacop.search.ComparatorVariable;
import org.jacop.set.core.SetDomain;
import org.jacop.set.core.SetVar;

/**
 * Defines a minimum cardinality difference variable comparator. The variable with the minimum
 * difference in cardinality between the greatest lower bound  and the least upper bound has the priority.
 *
 * @param <T> type of variable being used in the search.
 * @author Krzysztof Kuchcinski and Robert Åkemalm
 * @version 4.10
 */

public class MinCardDiff<T extends SetVar> implements ComparatorVariable<T> {

    /**
     * It constructs a minimum cardinality difference variable comparator.
     */
    public MinCardDiff() {
    }

    /**
     * Compares the cardinality difference of the variable to the float value.
     */
    public int compare(double left, T var) {

        SetDomain SD = var.dom();

        int right = SD.lub().getSize() - SD.glb().getSize();

        if (left < right)
            return 1;
        if (left > right)
            return -1;
        return 0;
    }

    /**
     * Compares the cardinality difference of the variables.
     */
    public int compare(T leftVar, T rightVar) {

        SetDomain leftSD = leftVar.dom();
        SetDomain rightSD = rightVar.dom();

        int left = leftSD.lub().getSize() - leftSD.glb().getSize();
        int right = rightSD.lub().getSize() - rightSD.glb().getSize();

        if (left < right)
            return 1;
        if (left > right)
            return -1;
        return 0;
    }

    /**
     * Returns the metric(Cardinality difference) of the variable.
     */
    public double metric(T var) {

        SetDomain SD = var.dom();
        return SD.lub().getSize() - SD.glb().getSize();
    }

}
