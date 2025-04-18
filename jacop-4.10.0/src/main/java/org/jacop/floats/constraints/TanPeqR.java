/*
 * TanPeqR.java
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
import org.jacop.floats.core.FloatInterval;
import org.jacop.floats.core.FloatIntervalDomain;
import org.jacop.floats.core.FloatVar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Constraints sin(P) = R
 * <p>
 * Bounds consistency can be used; third parameter of constructor controls this.
 *
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.10
 */

public class TanPeqR extends Constraint implements SatisfiedPresent {

    static AtomicInteger idNumber = new AtomicInteger(0);

    /**
     * It contains variable p.
     */
    public FloatVar p;

    /**
     * It contains variable q.
     */
    public FloatVar q;

    /**
     * It constructs sin(P) = Q constraints.
     *
     * @param p variable P
     * @param q variable Q
     */
    public TanPeqR(FloatVar p, FloatVar q) {

        checkInputForNullness(new String[] {"p", "q"}, new Object[] {p, q});

        numberId = idNumber.incrementAndGet();

        this.queueIndex = 1;
        this.p = p;
        this.q = q;

        setScope(p, q);
    }

    @Override public void consistency(Store store) {

        boundConsistency(store);

    }

    void boundConsistency(Store store) {

        // System.out.println ("1. " + this);

        if (p.max() - p.min() >= FloatDomain.PI)
            return;

        do {

            store.propagationHasOccurred = false;

            if (satisfied())
                return;

            double min = p.min();
            double max = p.max();
            if (p.min() < -FloatDomain.PI || p.max() > FloatDomain.PI) {
                // normalize to -PI..PI

                FloatInterval normP = normalize(p);
                min = normP.min();
                max = normP.max();

                // System.out.println ("Not-normalized " + p);
                // System.out.println ("Normalized interval within -PI..PI interval = " + min + ".." + max);
            }

            // System.out.println ("Normalized min/max = " + min+".."+max);

            FloatInterval minMax = new FloatInterval(min, max);
            if (minMax.singleton())
                if ((FloatDomain.PI / 2 >= min && FloatDomain.PI / 2 <= max) || (-FloatDomain.PI / 2 >= min && -FloatDomain.PI / 2 <= max))
                    throw Store.failException;

            int intervalForMin = intervalNo(min);
            int intervalForMax = intervalNo(max);

            // System.out.println ("min in interval " + intervalForMin + ", max in interval " + intervalForMax);

            double qMin = -1.0, qMax = 1.0;
            switch (intervalForMin) {
                case 1:
                    switch (intervalForMax) {
                        case 1: // d >= -FloatDomain.PI && d < -FloatDomain.PI/2
                            qMin = Math.tan(min);
                            qMax = Math.tan(max);
                            qMin = FloatDomain.down(qMin);
                            qMax = FloatDomain.up(qMax);
                            if (qMax < 0)
                                qMax = FloatDomain.MaxFloat;
                            break;
                        default:
                            return;
                        // throw new InternalException("Selected impossible case in tan and atan constraint");
                    }
                    break;

                case 2:
                    switch (intervalForMax) {
                        case 2: // d >= -FloatDomain.PI/2 && d < FloatDomain.PI/2
                            qMin = Math.tan(min);
                            qMax = Math.tan(max);
                            qMin = FloatDomain.down(qMin);
                            qMax = FloatDomain.up(qMax);
                            if (qMin > qMax) {
                                if (qMax > 0)
                                    qMin = -FloatDomain.MaxFloat;
                                else if (qMin < 0)
                                    qMax = FloatDomain.MaxFloat;
                            }
                            break;
                        default:
                            return;
                        // throw new InternalException("Selected impossible case in tan and atan constraint");
                    }
                    break;

                case 3:
                    switch (intervalForMax) {
                        case 3: // d >= FloatDomain.PI/2 && d <= FloatDomain.PI
                            qMin = Math.tan(min);
                            qMax = Math.tan(max);
                            qMin = FloatDomain.down(qMin);
                            qMax = FloatDomain.up(qMax);
                            if (qMin > 0)
                                qMin = -FloatDomain.MaxFloat;
                            break;
                        default:
                            return;
                        // throw new InternalException("Selected impossible case in tan and atan constraint");

                    }
                    break;
                default:
                    return;
                // throw new InternalException("Selected impossible case in tan and atan constraint");
            }

            // System.out.println (q + " in " + qMin + ".." + qMax);

            q.domain.in(store.level, q, qMin, qMax);

            // System.out.println ("q after in " + q);

            // p update
            double pMin = Math.atan(qMin);  // range -PI/2..PI/2
            double pMax = Math.atan(qMax);  // range -PI/2..PI/2

            // System.out.println ("atan result " + p + " in " + pMin +".." + pMax + " copied to  n times -PI/2 .. PI/2");

            pMin = FloatDomain.down(pMin);
            pMax = FloatDomain.up(pMax);
            if (java.lang.Double.isNaN(pMin))
                pMin = -FloatDomain.PI / 2;
            if (java.lang.Double.isNaN(pMax))
                pMax = FloatDomain.PI / 2;

            double low, high;
            double k = Math.floor(p.min() / FloatDomain.PI);
            low = FloatDomain.down(pMin + k * FloatDomain.PI);
            k = Math.ceil(p.max() / FloatDomain.PI);
            high = FloatDomain.up(pMax + k * FloatDomain.PI);
            FloatIntervalDomain pDom = new FloatIntervalDomain(low, high);

            p.domain.in(store.level, p, pDom); //.min(), pDom.max());

            // System.out.println ("p after in " + p);

        } while (store.propagationHasOccurred);

        // System.out.println ("2. TanPeqR("+p+", "+q+")");

    }

    /*
     * Normalizes argument to interval -PI..PI
     */
    FloatInterval normalize(FloatVar v) {
        double min = v.min();
        double max = v.max();

        double normMin = min % FloatDomain.PI;
        double normMax = normMin + max - min;

        if (normMax >= FloatDomain.PI) {
            normMin -= FloatDomain.PI;
            normMax -= FloatDomain.PI;
        }

        return new FloatInterval(normMin, normMax);

    }

    int intervalNo(double d) {
        if (d >= -FloatDomain.PI && d < -FloatDomain.PI / 2)
            return 1;
        if (d >= -FloatDomain.PI / 2 && d < FloatDomain.PI / 2)
            return 2;
        if (d >= FloatDomain.PI / 2 && d <= FloatDomain.PI)
            return 3;
        else
            return 0;  // undefined
    }

    @Override public int getDefaultConsistencyPruningEvent() {
        return IntDomain.BOUND;
    }

    @Override public boolean satisfied() {

        if (grounded()) {
            double tanMin = Math.tan(p.min()), tanMax = Math.tan(p.max());

            FloatInterval minDiff = (tanMin < q.min()) ? new FloatInterval(tanMin, q.min()) : new FloatInterval(q.min(), tanMin);
            FloatInterval maxDiff = (tanMax < q.max()) ? new FloatInterval(tanMax, q.max()) : new FloatInterval(q.max(), tanMax);

            return minDiff.singleton() && maxDiff.singleton();
        } else
            return false;
    }


    @Override public String toString() {

        StringBuffer result = new StringBuffer(id());

        result.append(" : TanPeqR(").append(p).append(", ").append(q).append(" )");

        return result.toString();

    }

}
