/*
 * Min.java
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

package org.jacop.floats.constraints;

import org.jacop.api.SatisfiedPresent;
import org.jacop.constraints.Constraint;
import org.jacop.core.IntDomain;
import org.jacop.core.Store;
import org.jacop.floats.core.FloatDomain;
import org.jacop.floats.core.FloatVar;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Min constraint implements the minimum/2 constraint. It provides the minimum
 * varable from all FD varaibles on the list.
 *
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.10
 */

public class Min extends Constraint implements SatisfiedPresent {

    static AtomicInteger idNumber = new AtomicInteger(0);

    /**
     * It specifies a list of variables among which the minimum value is being searched for.
     */
    public FloatVar list[];

    /**
     * It specifies variable min, which stores the minimum value within the whole list.
     */
    public FloatVar min;

    /**
     * It constructs min constraint.
     *
     * @param min  variable denoting the minimal value
     * @param list the array of variables for which the minimal value is imposed.
     */
    public Min(FloatVar[] list, FloatVar min) {

        checkInputForNullness(new String[] {"list", "max"}, new Object[][] {list, {min}});

        this.queueIndex = 1;
        this.numberId = idNumber.incrementAndGet();
        this.min = min;
        this.list = Arrays.copyOf(list, list.length);

        setScope(Stream.concat(Arrays.stream(list), Stream.of(min)));
    }

    /**
     * It constructs min constraint.
     *
     * @param min  variable denoting the minimal value
     * @param list the array of variables for which the minimal value is imposed.
     */
    public Min(List<? extends FloatVar> list, FloatVar min) {

        this(list.toArray(new FloatVar[list.size()]), min);

    }

    @Override public void consistency(Store store) {

        FloatVar var;
        FloatDomain vDom;

        //@todo keep one variable with the smallest value as watched variable
        // only check for other support if that smallest value is no longer part
        // of the variable domain.

        do {

            store.propagationHasOccurred = false;

            // @todo, optimize, if there is no change on min.min() then
            // the below inMin does not have to be executed.

            double minValue = FloatDomain.MaxFloat;
            double maxValue = FloatDomain.MaxFloat;

            double minMin = min.min();
            for (int i = 0; i < list.length; i++) {
                var = list[i];

                var.domain.inMin(store.level, var, minMin);

                vDom = var.dom();
                double VdomMin = vDom.min(), VdomMax = vDom.max();
                minValue = (minValue < VdomMin) ? minValue : VdomMin;

                maxValue = (maxValue < VdomMax) ? maxValue : VdomMax;
            }

            min.domain.in(store.level, min, minValue, maxValue);

            int n = 0, pos = -1;
            for (int i = 0; i < list.length; i++) {
                var = list[i];

                if (maxValue < var.min())
                    n++;
                else
                    pos = i;
            }
            if (n == list.length - 1) // one variable on the list is minimal; its is max < min of all other variables
                list[pos].domain.in(store.level, list[pos], min.dom());

        } while (store.propagationHasOccurred);

    }

    @Override public int getDefaultConsistencyPruningEvent() {
        return IntDomain.BOUND;
    }

    @Override public boolean satisfied() {

        if (!min.singleton())
            return false;

        double minValue = min.max();
        int i = 0;
        boolean eq = false;

        while (i < list.length) {
            if (list[i].min() < minValue)
                return false;
            if (!eq && (list[i].singleton() && list[i].value() == minValue))
                eq = true;
            i++;
        }

        return eq;
    }

    @Override public String toString() {
        StringBuffer result = new StringBuffer(id());

        result.append(" : min( [ ");
        for (int i = 0; i < list.length; i++) {
            result.append(list[i]);
            if (i < list.length - 1)
                result.append(", ");
        }

        result.append("], ").append(this.min);
        result.append(")");

        return result.toString();

    }

}
