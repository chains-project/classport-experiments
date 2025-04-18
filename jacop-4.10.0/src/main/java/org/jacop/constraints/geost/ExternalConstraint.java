/*
 * ExternalConstraint.java
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
package org.jacop.constraints.geost;

import org.jacop.util.SimpleHashSet;

import java.util.Collection;

/**
 * @author Marc-Olivier Fleury and Radoslaw Szymanek
 * @version 4.10
 *          <p>
 *          This interface defines the minimal functionality that is required by
 *          a constraint in order to be used by Geost as an external constraint.
 *          <p>
 *          External constraints are loosely coupled with Geost internals, in the sense
 *          that they are only required to be able to provide a representation usable by
 *          Geost.
 *          <p>
 *          The generation of internal constraints is done only once per search,
 *          implying that it is possible to do costly operations in order to
 *          generate these constraints.
 */
public interface ExternalConstraint {

    /**
     * It generates internal constraints which will be used by Geost's sweeping
     * algorithm.
     * <p>
     * The generation of internal constraints is done only once per search,
     * implying that it is possible to do costly operations in order to
     * generate internal constraints. However, another implication of
     * this one-time use is that the internal constraints generated have
     * to remain valid during the whole search.
     *
     * @param geost the geost kernel that will use the generated constraint
     * @return the collection of internal constraints which correspond
     * to this external constraint
     */
    Collection<? extends InternalConstraint> genInternalConstraints(Geost geost);


    /**
     * It provides the collection of internal constraints that the given object has to satisfy.
     * For instance, for the non overlapping constraint, the result would contain all the constraints
     * corresponding to the objects in the non overlapping group.
     * <p>
     * This method is used by the kernel only once per object, at the beginning of the search,
     * to collect the set of constraints to use for each object.
     *
     * @param o the geost object that needs to be constrained
     * @return the collection of internal constraints acting on the given object
     */
    Collection<? extends InternalConstraint> getObjectConstraints(GeostObject o);

    /**
     * It adds to the accumulator collection the objects that are likely to be pruned if the given object
     * changes. For instance, in the case of the non-overlapping constraint, these would be the objects
     * that are close to the given object.
     * <p>
     * TODO, optimize all the code around this functionality, avoid situation when accumulator has all objects
     * anyway and external constraints are continuously queried to add objects and keep adding objects which
     * are already in the set.
     *
     * @param o           the object that was pruned
     * @param accumulator the set of objects to add the object to
     * @return true if a value was added, false otherwise
     */
    boolean addPrunableObjects(GeostObject o, SimpleHashSet<GeostObject> accumulator);

    /**
     * Handler method called by the Geost kernel when the domain of the object changes.
     * Use this method to make changes to the state of the constraint (and of its relative internal
     * constraints) if needed.
     *
     * @param o the object
     */
    void onObjectUpdate(GeostObject o);

    /**
     * Returns true if the external constraint generated the supplied internal constraint ic, and that ic
     * applies to object o.
     *
     * @param ic internal constraint being checked
     * @param o  object to which internal constrain should apply to
     * @return true if the constraint was generated by this external constraint for the object o.
     */
    boolean isInternalConstraintApplicableTo(InternalConstraint ic, GeostObject o);

    /**
     * Provides the collection of objects that this constraint applies to
     *
     * @return the collection of objects, or null if the constraint applies to all objects
     */
    GeostObject[] getObjectScope();
}
