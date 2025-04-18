/*
 * FloatDomain.java
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

package org.jacop.floats.core;

import org.jacop.constraints.Constraint;
import org.jacop.core.*;

/*
 * Defines an integer domain and related operations on it.
 * <p>
 * FloatDomain implementations can not assume that arguments to
 * any function can not be empty domains.
 *
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.10
 */

public abstract class FloatDomain extends Domain {

    /*
     * It specifies the minimum element in the domain.
     */
    public static final double MinFloat = -Double.MAX_VALUE; // -1e150;

    /*
     * It specifies the maximum element in the domain.
     */
    public static final double MaxFloat = Double.MAX_VALUE; // 1e150;

    /*
     * Minimization step for floating-point cost function minimization.
     */
    public static double minimizationStep = 0;

    /*
     * It specifies the constant pi, as defined in java.lang.Math package.
     */
    public static final double PI = java.lang.Math.PI;

    /*
     * It specifies the constant e, as defined in java.lang.Math package.
     */
    public static final double E = java.lang.Math.E;

    /*
     * It specifies the rounding method;
     * true - extend interval outward
     * false - does not extend interval and use calculation results.
     */
    static boolean outward = true;

    /*
     * It defines rounding method
     *
     * @param out defines rounding method true = outward, false = no rounding outward
     */
    public static void setOutward(boolean out) {
        outward = out;
    }

    /*
     * It specifies the method for printing a domain.
     * If true, we print each domain as an interval regardles of
     * the current precision of calculations.
     * If true, the print-out prints singletons, defined by method
     * singleton() in FloatingInterval, as single values.
     */
    static boolean intervalPrint = false;

    public static void intervalPrint(boolean p) {
        intervalPrint = p;
    }

    /*
     * It specifies the precision format for floating point print-out
     */
    static double format = Double.MAX_VALUE;

    public static double format() {
        return format;
    }

    public static void setFormat(double f) {
        format = f;
    }

    /*
     * It specifies the precision for floating point operations, among others
     * decide when two numbers are equal and when a interval is singleton.
     */
    static double precision = 1e-11;

    public static double precision() {
        return precision;
    }

    public static void setPrecision(double p) {
        precision = p;
    }

    public static double epsilon(double f) {

        return (precision() < java.lang.Math.ulp(f)) ? java.lang.Math.ulp(f) : precision();

    }

    // Unit in the last place
    public static double ulp(double f) {

        return java.lang.Math.ulp(f);

    }

    // Unit in the last place for minimal value
    public static double minULP(FloatVar f) {

        return java.lang.Math.ulp(f.min());

    }

    // Unit in the last place for maximal value
    public static double maxULP(FloatVar f) {

        return java.lang.Math.ulp(f.max());

    }

    // returns previous (toward -inf) floating-point number before d 
    // supposed to be used by constraints
    public static double down(double d) {

        if (outward)
            return Math.nextDown(d);
        else
            return d;
    }

    // returns next (toward inf) floating-point number after d 
    // supposed to be used by constraints
    public static double up(double d) {

        if (outward)
            return Math.nextUp(d);
        else
            return d;
    }

    // returns previous (toward -inf) floating-point number before d 
    // supposed to be used by methods for domain computations
    public static double previous(double d) {
        return Math.nextDown(d);//downBit(d); //d - ulp(d);
    }

    // Sets optimization step for floating point optimization
    public static void setStep(double s) {
        minimizationStep = s;
    }
    
    // returns previous floating-point number before d 
    // for minimization with FloatVar cost function
    public static double previousForMinimization(double d) {
        if (minimizationStep == 0)
            return previous(d);
        else
            return d - minimizationStep;// + upBit(d);
    }

    // returns next (toward inf) floating-point number after d 
    // supposed to be used by methods for domain computations
    public static double next(double d) {
        return Math.nextUp(d);//upBit(d); // d + ulp(d);
    }


    /**
     * It specifies the previous domain which was used by this domain. The old
     * domain is stored here and can be easily restored if necessary.
     */

    public FloatDomain previousDomain;

    /**
     * It specifies the constant for GROUND event. It has to be smaller
     * than the constant for events BOUND and ANY.
     */
    public static final int GROUND = 0;

    /**
     * It specifies the constant for BOUND event. It has to be smaller
     * than the constant for event ANY.
     */
    public static final int BOUND = 1;

    /**
     * It specifies the constant for ANY event.
     */
    public static final int ANY = 2;


    /**
     * Unique identifier for an interval domain type.
     */

    public static final int FloatIntervalDomainID = 0;


    /**
     * It specifies for each event what other events are subsumed by this
     * event. Possibly implement this by bit flags in int.
     */
    static final int[][] eventsInclusion = {{GROUND, BOUND, ANY}, // GROUND event
        {BOUND, ANY}, // BOUND event
        {ANY}}; // ANY event

    /**
     * It helps to specify what events should be executed if a given event occurs.
     *
     * @param pruningEvent the pruning event for which we want to know what events it encompasses.
     * @return an array specifying what events should be included given this event.
     */
    public int[] getEventsInclusion(int pruningEvent) {
        return eventsInclusion[pruningEvent];
    }

    /**
     * Unique identifier for an interval domain type.
     */

    public static final int IntervalDomainID = 0;

    /**
     * It specifies an empty integer domain.
     */
    public static final FloatDomain emptyFloatDomain = new FloatIntervalDomain(0);

    /**
     * It adds interval of values to the domain.
     *
     * @param i Interval which needs to be added to the domain.
     */

    public void unionAdapt(FloatInterval i) {
        unionAdapt(i.min, i.max);
    }

    /**
     * It adds values as specified by the parameter to the domain.
     *
     * @param domain Domain which needs to be added to the domain.
     */

    public void addDom(FloatDomain domain) {

        // if (!domain.isSparseRepresentation()) {
        FloatIntervalEnumeration enumer = domain.floatIntervalEnumeration();
        while (enumer.hasMoreElements())
            unionAdapt(enumer.nextElement());
      /*
  }
        else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements())
                unionAdapt(enumer.nextElement());
        }
            */

    }

    /**
     * It adds all values between min and max to the domain.
     *
     * @param min the left bound of the interval being added.
     * @param max the right bound of the interval being added.
     */

    public abstract void unionAdapt(double min, double max);

    /**
     * It adds a values to the domain.
     *
     * @param value value being added to the domain.
     */

    public void unionAdapt(double value) {
        unionAdapt(value, value);
    }

    /**
     * Checks if two domains intersect.
     *
     * @param domain the domain for which intersection is checked.
     * @return true if domains are intersecting.
     */

    public boolean isIntersecting(FloatDomain domain) {

        // if (!domain.isSparseRepresentation()) {
        FloatIntervalEnumeration enumer = domain.floatIntervalEnumeration();
        while (enumer.hasMoreElements()) {
            FloatInterval next = enumer.nextElement();
            if (isIntersecting(next.min, next.max))
                return true;
        }
        /*
        }
        else {

            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements())
                if (contains(enumer.nextElement()))
                    return true;
        }
        */
        return false;
    }

    /**
     * It checks if interval min..max intersects with current domain.
     *
     * @param min the left bound of the interval.
     * @param max the right bound of the interval.
     * @return true if domain intersects with the specified interval.
     */

    public abstract boolean isIntersecting(double min, double max);

    /**
     * It specifies if the current domain contains the domain given as a
     * parameter.
     *
     * @param domain for which we check if it is contained in the current domain.
     * @return true if the supplied domain is cover by this domain.
     */

    public boolean contains(FloatDomain domain) {

        // if (!domain.isSparseRepresentation()) {

        FloatIntervalEnumeration enumer = domain.floatIntervalEnumeration();
        while (enumer.hasMoreElements()) {
            FloatInterval next = enumer.nextElement();
            if (!contains(next.min, next.max))
                return false;
        }
            /*
        }
        else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements())
                if (!contains(enumer.nextElement()))
                    return false;
        }
            */
        return true;

    }

    /**
     * It checks if an interval min..max belongs to the domain.
     *
     * @param min the minimum value of the interval being checked
     * @param max the maximum value of the interval being checked
     * @return true if value belongs to the domain.
     */

    public abstract boolean contains(double min, double max);

    /**
     * It creates a complement of a domain.
     *
     * @return it returns the complement of this domain.
     */

    public abstract FloatDomain complement();

    /**
     * It checks if value belongs to the domain.
     *
     * @param value which is checked if it exists in the domain.
     * @return true if value belongs to the domain.
     */

    public boolean contains(int value) {
        return contains(value, value);
    }


    public abstract boolean contains(double value);

    /**
     * It gives next value in the domain from the given one (lexigraphical
     * ordering). If no value can be found then returns the same value.
     * @param value it specifies the value after which a next value has to be found.
     * @return next value after the specified one which belong to this domain.
     */

    // public abstract int nextValue(int value);

    /**
     * It gives previous value in the domain from the given one (lexigraphical
     * ordering). If no value can be found then returns the same value.
     * @param value before which a value is seeked for.
     * @return it returns the value before the one specified as a parameter.
     */

    // public abstract int previousValue(int value);


    /**
     * It returns value enumeration of the domain values.
     *
     * @return valueEnumeration which can be used to enumerate one by one value from this domain.
     */

    /* TODO, Value enumeration for floats */
    public abstract ValueEnumeration valueEnumeration();

    /**
     * It returns interval enumeration of the domain values.
     *
     * @return intervalEnumeration which can be used to enumerate intervals in this domain.
     */

    public abstract IntervalEnumeration intervalEnumeration();

    /**
     * It returns interval enumeration of the domain values.
     *
     * @return intervalEnumeration which can be used to enumerate intervals in this domain.
     */

    public abstract FloatIntervalEnumeration floatIntervalEnumeration();

    /**
     * It returns the size of the domain.
     *
     * @return number of elements in this domain.
     */

    public abstract int getSize();

    /**
     * It intersects current domain with the one given as a parameter.
     *
     * @param dom domain with which the intersection needs to be computed.
     * @return the intersection between supplied domain and this domain.
     */

    public abstract FloatDomain intersect(FloatDomain dom);

    /**
     * In intersects current domain with the interval min..max.
     *
     * @param min the left bound of the interval (inclusive)
     * @param max the right bound of the interval (inclusive)
     * @return the intersection between the specified interval and this domain.
     */

    public abstract FloatDomain intersect(double min, double max);

    /**
     * It intersects with the domain which is a complement of value.
     *
     * @param value the value for which the complement is computed
     * @return the domain which does not contain specified value.
     */

    public FloatDomain subtract(double value) {
        return subtract(value, value);
    }

    /**
     * It removes value from the domain. It adapts current (this) domain.
     *
     * @param value the value for which the complement is computed
     */

    public abstract void subtractAdapt(double value);

    /**
     * It removes all values between min and max to the domain.
     *
     * @param min the left bound of the interval being removed.
     * @param max the right bound of the interval being removed.
     */

    public abstract void subtractAdapt(double min, double max);



    /**
     * It returns the maximum value in a domain.
     *
     * @return the largest value present in the domain.
     */

    public abstract double max();

    /**
     * It returns the minimum value in a domain.
     *
     * @return the smallest value present in the domain.
     */
    public abstract double min();

    /**
     * It sets the domain to the specified domain.
     *
     * @param domain the domain from which this domain takes all elements.
     */

    public abstract void setDomain(FloatDomain domain);

    /**
     * It sets this domain to contain exactly all values between min and max.
     *
     * @param min the left bound of the interval (inclusive).
     * @param max the right bound of the interval (inclusive).
     */

    public abstract void setDomain(double min, double max);

    /**
     * It returns true if given domain has only one element equal c.
     *
     * @param c the value to which the only element should be equal to.
     * @return true if the domain contains only one element c.
     */

    public boolean singleton(double c) {
        return min() == c && getSize() == 1;
    }


    /**
     * It subtracts domain from current domain and returns the result.
     *
     * @param domain the domain which is subtracted from this domain.
     * @return the result of the subtraction.
     */

    public FloatDomain subtract(FloatDomain domain) {

        if (domain.isEmpty())
            return this.cloneLight();

        // if (!domain.isSparseRepresentation()) {
        FloatIntervalEnumeration enumer = domain.floatIntervalEnumeration();
        FloatInterval first = enumer.nextElement();
        FloatDomain result = this.subtract(first.min, first.max);
        while (enumer.hasMoreElements()) {
            FloatInterval next = enumer.nextElement();
            result.subtractAdapt(next.min, next.max);

        }
        return result;
            /*
        }
        else {
            ValueEnumeration enumer = domain.valueEnumeration();
            int first = enumer.nextElement();
            FloatDomain result = this.subtract(first);
            while (enumer.hasMoreElements()) {
                int next = enumer.nextElement();
                if (result.contains(next))
                    result.subtractAdapt(next);
            }
            return result;
        }
            */
    }

    /**
     * It subtracts interval min..max.
     *
     * @param min the left bound of the interval (inclusive).
     * @param max the right bound of the interval (inclusive).
     * @return the result of the subtraction.
     */

    public abstract FloatDomain subtract(double min, double max);

    /**
     * It computes union of the supplied domain with this domain.
     *
     * @param domain the domain for which the union is computed.
     * @return the union of this domain with the supplied one.
     */

    public FloatDomain union(FloatDomain domain) {

        if (this.isEmpty())
            return domain.cloneLight();

        FloatDomain result = this.cloneLight();

        if (domain.isEmpty())
            return result;

        // if (!domain.isSparseRepresentation()) {
        FloatIntervalEnumeration enumer = domain.floatIntervalEnumeration();
        while (enumer.hasMoreElements()) {
            FloatInterval next = enumer.nextElement();
            result.unionAdapt(next.min, next.max);
        }
        return result;
            /*
        }
        else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements()) {
                int next = enumer.nextElement();
                result.unionAdapt(next);
            }
            return result;
        }
            */
    }

    /**
     * It computes union of this domain and the interval.
     *
     * @param min the left bound of the interval (inclusive).
     * @param max the right bound of the interval (inclusive).
     * @return the union of this domain and the interval.
     */

    public FloatDomain union(double min, double max) {

        FloatDomain result = this.cloneLight();
        result.unionAdapt(min, max);
        return result;

    }

    /**
     * It computes union of this domain and value.
     *
     * @param value it specifies the value which is being added.
     * @return domain which is a union of this one and the value.
     */

    public FloatDomain union(double value) {
        return union(value, value);
    }

    /**
     * It updates the domain according to the minimum value and stamp value. It
     * informs the variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param min        the minimum value to which the domain is updated.
     */

    public void inMin(int storeLevel, Var var, double min) {

        in(storeLevel, var, min, max());

    }

    /**
     * It updates the domain according to the maximum value and stamp value. It
     * informs the variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param max        the maximum value to which the domain is updated.
     */

    public void inMax(int storeLevel, Var var, double max) {

        in(storeLevel, var, min(), max);

    }

    /**
     * It updates the domain to have values only within the interval min..max.
     * The type of update is decided by the value of stamp. It informs the
     * variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param min        the minimum value to which the domain is updated.
     * @param max        the maximum value to which the domain is updated.
     */

    public abstract void in(int storeLevel, Var var, double min, double max);

    /**
     * It reduces domain to a single value.
     *
     * @param level level of the store at which the update occurs.
     * @param var   variable for which this domain is used.
     * @param value the value according to which the domain is updated.
     */
    public void inValue(int level, Var var, double value) {
        in(level, var, value, value);
    }

    /**
     * It updates the domain to have values only within the domain. The type of
     * update is decided by the value of stamp. It informs the variable of a
     * change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param domain     the domain according to which the domain is updated.
     */

    public void in(int storeLevel, Var var, FloatDomain domain) {

        inShift(storeLevel, var, domain, 0);

    }

    /**
     * It updates the domain to not contain the value complement. It informs the
     * variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param complement value which is removed from the domain if it belonged to the domain.
     */

    public void inComplement(int storeLevel, Var var, double complement) {

        inComplement(storeLevel, var, complement, complement);

    }

    /**
     * It updates the domain so it does not contain the supplied interval. It informs
     * the variable of a change if it occurred.
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param min        the left bound of the interval (inclusive).
     * @param max        the right bound of the interval (inclusive).
     */

    public abstract void inComplement(int storeLevel, Var var, double min, double max);

    /**
     * It returns number of intervals required to represent this domain.
     *
     * @return the number of intervals in the domain.
     */
    public abstract int noIntervals();

    /**
     * It returns required interval.
     *
     * @param position the position of the interval.
     * @return the interval, or null if the required interval does not exist.
     */
    public abstract FloatInterval getInterval(int position);

    /**
     * It updates the domain to contain the elements as specifed by the domain,
     * which is shifted. E.g. {1..4} + 3 = 4..7
     *
     * @param storeLevel level of the store at which the update occurs.
     * @param var        variable for which this domain is used.
     * @param domain     the domain according to which the domain is updated.
     * @param shift      the shift which is used to shift the domain supplied as argument.
     */

    public abstract void inShift(int storeLevel, Var var, FloatDomain domain, double shift);

    /**
     * It returns the left most element of the given interval.
     *
     * @param intervalNo the interval number.
     * @return the left bound of the specified interval.
     */

    public double leftElement(int intervalNo) {
        return getInterval(intervalNo).min;
    }

    /**
     * It returns the right most element of the given interval.
     *
     * @param intervalNo the interval number.
     * @return the right bound of the specified interval.
     */

    public double rightElement(int intervalNo) {
        return getInterval(intervalNo).max;
    }

    /**
     * It returns the values which have been removed at current store level.
     *
     * @param currentStoreLevel the current store level.
     * @return emptyDomain if domain did not change at current level, or the set of values which have been removed at current level.
     */

    public abstract FloatDomain recentDomainPruning(int currentStoreLevel);

    /**
     * It returns domain at earlier level at which the change has occurred.
     *
     * @return previous domain
     */
    public abstract FloatDomain previousDomain();

    /**
     * It specifies if the other int domain is equal to this one.
     *
     * @param domain the domain which is compared to this domain.
     * @return true if both domains contain the same elements, false otherwise.
     */
    public boolean eq(FloatDomain domain) {

        if (this.getSize() != domain.getSize())
            return false;

        // the same size.
        // if (!domain.isSparseRepresentation()) {
        FloatIntervalEnumeration enumer = domain.floatIntervalEnumeration();
        while (enumer.hasMoreElements()) {
            FloatInterval next = enumer.nextElement();
            if (!contains(next.min, next.max))
                return false;
        }
        return true;
            /*
        }
        else {
            ValueEnumeration enumer = domain.valueEnumeration();
            while (enumer.hasMoreElements()) {
                int next = enumer.nextElement();
                if (!contains(next))
                    return false;
            }
            return true;
        }
            */
    }

    @Override public void in(int level, Var var, Domain domain) {
        in(level, (FloatVar) var, (FloatDomain) domain);
    }

    @Override public boolean singleton(Domain value) {

        if (getSize() > 1)
            return false;

        if (isEmpty())
            return false;

        if (value.getSize() != 1)
            throw new IllegalArgumentException("An argument should be a singleton domain");

        assert (value instanceof FloatDomain) : "Can not compare int domains with other types of domains.";

        FloatDomain domain = (FloatDomain) value;

        if (eq(domain))
            return true;
        else
            return false;

    }

    /*
     * It returns the number of constraints
     *
     * @return the number of constraints attached to this domain.
     */

    public int noConstraints() {
        return searchConstraintsToEvaluate + modelConstraintsToEvaluate[GROUND] + modelConstraintsToEvaluate[BOUND]
            + modelConstraintsToEvaluate[ANY];
    }

    public abstract FloatDomain clone();

    public abstract FloatDomain cloneLight();

    /*
     * Returns the lexical ordering between the sets
     *
     * @param domain the set that should be lexically compared to this set
     * @return -1 if s is greater than this set, 0 if s is equal to this set and else it returns 1.
     */
    public int lex(FloatDomain domain) {

        ValueEnumeration thisEnumer = this.valueEnumeration();
        ValueEnumeration paramEnumer = domain.valueEnumeration();

        int i;
        int j;

        while (thisEnumer.hasMoreElements()) {

            i = thisEnumer.nextElement();

            if (paramEnumer.hasMoreElements()) {

                j = paramEnumer.nextElement();

                if (i < j)
                    return -1;
                else if (j < i)
                    return 1;
            } else
                return 1;
        }

        if (paramEnumer.hasMoreElements())
            return -1;

        return 0;

    }

    /**
     * It returns the number of elements smaller than el.
     * @param el the element from which counted elements must be smaller than.
     * @return the number of elements which are smaller than the provided element el.
     */
    // public int elementsSmallerThan(int el){

    //  int counter = -1;

    //  int value = el - 1;

    //  while(value != el){
    //      value = el;
    //      el = previousValue(el);
    //      counter++;
    //  }

    //  return counter;         
    // }


    /**
     * It computes an intersection with a given domain and stores it in this domain.
     *
     * @param intersect domain with which the intersection is being computed.
     * @return type of event which has occurred due to the operation.
     */
    public abstract int intersectAdapt(FloatDomain intersect);

    /**
     * It computes a union between this domain and the domain provided as a parameter. This
     * domain is changed to reflect the result.
     *
     * @param union the domain with is used for the union operation with this domain.
     * @return it returns information about the pruning event which has occurred due to this operation.
     */
    public int unionAdapt(FloatDomain union) {

        FloatDomain result = union(union);

        if (result.getSize() == getSize())
            return Domain.NONE;
        else {
            setDomain(result);
            // FIXME, how to setup events for domain extending events?
            return FloatDomain.ANY;
        }
    }

    /**
     * It computes an intersection of this domain with an interval [min..max].
     * It adapts this domain to the result of the intersection.
     *
     * @param min the minimum value of the interval used in the intersection computation.
     * @param max the maximum value of the interval used in the intersection computation.
     * @return it returns information about the pruning event which has occurred due to this operation.
     */
    public abstract int intersectAdapt(int min, int max);


    /**
     * It computes the size of the intersection between this domain and the domain
     * supplied as a parameter.
     *
     * @param domain the domain with which the intersection is computed.
     * @return the size of the intersection.
     */
    public int sizeOfIntersection(FloatDomain domain) {
        return intersect(domain).getSize();
    }

    /**
     * It access the element at the specified position. 
     * @param index the position of the element, indexing starts from 0. 
     * @return the value at a given position in the domain. 
     *
     */
    // public abstract int getElementAt(int index);


    /**
     * It adds a constraint to a domain, it should only be called by
     * putConstraint function of Variable object. putConstraint function from
     * Variable must make a copy of a vector of constraints if vector was not
     * cloned.
     */

    @Override public void putModelConstraint(int storeLevel, Var var, Constraint C, int pruningEvent) {

        if (stamp < storeLevel) {

            FloatDomain result = this.cloneLight();

            result.modelConstraints = modelConstraints;
            result.searchConstraints = searchConstraints;
            result.stamp = storeLevel;
            result.previousDomain = this;
            result.modelConstraintsToEvaluate = modelConstraintsToEvaluate;
            result.searchConstraintsToEvaluate = searchConstraintsToEvaluate;
            ((FloatVar) var).domain = result;

            result.putModelConstraint(storeLevel, var, C, pruningEvent);
            return;
        }

        Constraint[] pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean alreadyImposed = false;

            if (modelConstraintsToEvaluate[pruningEvent] > 0)
                for (int i = pruningEventConstraints.length - 1; i >= 0; i--)
                    if (pruningEventConstraints[i] == C)
                        alreadyImposed = true;

            int pruningConstraintsToEvaluate = modelConstraintsToEvaluate[pruningEvent];

            if (!alreadyImposed) {
                Constraint[] newPruningEventConstraints = new Constraint[pruningConstraintsToEvaluate + 1];

                System.arraycopy(pruningEventConstraints, 0, newPruningEventConstraints, 0, pruningConstraintsToEvaluate);
                newPruningEventConstraints[pruningConstraintsToEvaluate] = C;

                Constraint[][] newModelConstraints = new Constraint[3][];

                newModelConstraints[0] = modelConstraints[0];
                newModelConstraints[1] = modelConstraints[1];
                newModelConstraints[2] = modelConstraints[2];

                newModelConstraints[pruningEvent] = newPruningEventConstraints;

                modelConstraints = newModelConstraints;

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]++;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

            }

        } else {

            Constraint[] newPruningEventConstraints = new Constraint[1];

            newPruningEventConstraints[0] = C;

            Constraint[][] newModelConstraints = new Constraint[3][];

            newModelConstraints[0] = modelConstraints[0];
            newModelConstraints[1] = modelConstraints[1];
            newModelConstraints[2] = modelConstraints[2];

            newModelConstraints[pruningEvent] = newPruningEventConstraints;

            modelConstraints = newModelConstraints;

            int[] newModelConstraintsToEvaluate = new int[3];

            newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
            newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
            newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

            newModelConstraintsToEvaluate[pruningEvent] = 1;

            modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

        }

    }

    @Override public void removeModelConstraint(int storeLevel, Var var, Constraint C) {

        if (stamp < storeLevel) {

            FloatDomain result = this.cloneLight();

            result.modelConstraints = modelConstraints;
            result.searchConstraints = searchConstraints;
            result.stamp = storeLevel;
            result.previousDomain = this;
            result.modelConstraintsToEvaluate = modelConstraintsToEvaluate;
            result.searchConstraintsToEvaluate = searchConstraintsToEvaluate;
            ((FloatVar) var).domain = result;

            result.removeModelConstraint(storeLevel, var, C);
            return;
        }

        int pruningEvent = FloatDomain.GROUND;

        Constraint[] pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean isImposed = false;

            int i;

            for (i = modelConstraintsToEvaluate[pruningEvent] - 1; i >= 0; i--)
                if (pruningEventConstraints[i] == C) {
                    isImposed = true;
                    break;
                }

            if (isImposed) {

                if (i != modelConstraintsToEvaluate[pruningEvent] - 1) {

                    modelConstraints[pruningEvent][i] = modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1];

                    modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1] = C;
                }

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]--;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

                return;

            }

        }

        pruningEvent = FloatDomain.BOUND;

        pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean isImposed = false;

            int i;

            for (i = modelConstraintsToEvaluate[pruningEvent] - 1; i >= 0; i--)
                if (pruningEventConstraints[i] == C) {
                    isImposed = true;
                    break;
                }

            if (isImposed) {

                if (i != modelConstraintsToEvaluate[pruningEvent] - 1) {

                    modelConstraints[pruningEvent][i] = modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1];

                    modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1] = C;
                }

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]--;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

                return;

            }

        }

        pruningEvent = FloatDomain.ANY;

        pruningEventConstraints = modelConstraints[pruningEvent];

        if (pruningEventConstraints != null) {

            boolean isImposed = false;

            int i;

            for (i = modelConstraintsToEvaluate[pruningEvent] - 1; i >= 0; i--)
                if (pruningEventConstraints[i] == C) {
                    isImposed = true;
                    break;
                }

            // int pruningConstraintsToEvaluate =
            // modelConstraintsToEvaluate[pruningEvent];

            if (isImposed) {

                if (i != modelConstraintsToEvaluate[pruningEvent] - 1) {

                    modelConstraints[pruningEvent][i] = modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1];

                    modelConstraints[pruningEvent][modelConstraintsToEvaluate[pruningEvent] - 1] = C;
                }

                int[] newModelConstraintsToEvaluate = new int[3];

                newModelConstraintsToEvaluate[0] = modelConstraintsToEvaluate[0];
                newModelConstraintsToEvaluate[1] = modelConstraintsToEvaluate[1];
                newModelConstraintsToEvaluate[2] = modelConstraintsToEvaluate[2];

                newModelConstraintsToEvaluate[pruningEvent]--;

                modelConstraintsToEvaluate = newModelConstraintsToEvaluate;

            }

        }

    }

    /**
     * It constructs and int array containing all elements in the domain.
     * The array will have size equal to the number of elements in the domain.
     *
     * @return the int array containing all elements in a domain.
     */
    public int[] toIntArray() {

        int[] result = new int[getSize()];

        ValueEnumeration enumer = this.valueEnumeration();
        int i = 0;

        while (enumer.hasMoreElements())
            result[i++] = enumer.nextElement();

        return result;
    }

    /**
     * It returns the value to which this domain is grounded. It assumes
     * that a domain is a singleton domain.
     *
     * @return the only value remaining in the domain.
     */
    public double value() {

        assert (singleton()) : "function value() called when domain is not a singleton domain.";

        return min();

    }

    /* 
     * Finds result interval for addition of {a..b} - {c..d}
     */
    public static final FloatIntervalDomain addBounds(double a, double b, double c, double d) {

        // Changing constants to smallest encapsulating intervals to
        // limit rounding effects problem
        // if (c == d) {
        //     c = down(c);
        //     d = up(d);
        // }
        double min = down(a + c);
        double max = up(b + d);

        if (d == 0.0)
            max = b;
        if (c == 0.0)
            min = a;

        if (a == 0.0)
            min = c;
        if (b == 0.0)
            max = d;

        return new FloatIntervalDomain(min, max);
    }


    /* 
     * Finds result interval for subtraction of {a..b} - {c..d}
     */
    public static final FloatIntervalDomain subBounds(double a, double b, double c, double d) {

        // Changing constants to smallest encapsulating intervals to
        // limit rounding effects problem
        // if (c == d) {
        //     c = down(c);
        //     d = up(d);
        // }
        double min = down(a - d);
        double max = up(b - c);

        if (d == 0.0)
            min = a;
        if (c == 0.0)
            max = b;

        if (a == 0.0)
            min = -d;
        if (b == 0.0)
            max = -c;

        return new FloatIntervalDomain(min, max);
    }

    /* 
     * Finds result interval for multiplication of {a..b} * {c..d}
     */
    public static final FloatIntervalDomain mulBounds(double a, double b, double c, double d) {

        // System.out.println ("[" + a +".." +b +"] * [" + c + ".." + d + "]");

        if (c == 1.0 && d == 1.0)
            return new FloatIntervalDomain(a, b);
        else if (c == -1.0 && d == -1.0)
            return new FloatIntervalDomain(-b, -a);

        boolean M_1 = (a < 0 && b > 0);        // contains zero
        // boolean Z_1 = (a == 0 && b == 0);     // zero
        boolean P0_1 = (a == 0 && b > 0);       // positive with zero
        boolean P1_1 = (a > 0 && b > 0);        // strictly positive
        boolean N0_1 = (a < 0 && b == 0);       // negative with zero
        boolean N1_1 = (a < 0 && b < 0);        // strictly negative

        boolean M_2 = (c < 0 && d > 0);
        // boolean Z_2 = (c == 0 && d == 0);
        boolean P0_2 = (c == 0 && d > 0);
        boolean P1_2 = (c > 0 && d > 0);
        boolean N0_2 = (c < 0 && d == 0);
        boolean N1_2 = (c < 0 && d < 0);

        double min = 0;
        double max = 0;

        if (P1_1)
            if (P1_2) { // P1 /\ P1
                min = down(a * c);
                max = up(b * d);
                return new FloatIntervalDomain(min, max);
            } else if (P0_2) { // P1 /\ P0
                min = 0.0; //down(a*c);
                max = up(b * d);
                return new FloatIntervalDomain(min, max);
            } else if (M_2) { // P1 /\ M
                min = down(b * c);
                max = up(b * d);
                return new FloatIntervalDomain(min, max);
            } else if (N1_2) { // P1 /\ N1
                min = down(b * c);
                max = up(a * d);
                return new FloatIntervalDomain(min, max);
            } else if (N0_2) { // P1 /\ N0
                min = down(b * c);
                max = 0.0; // up(a*d);
                return new FloatIntervalDomain(min, max);
            } else { // P1 /\ Z
                return new FloatIntervalDomain(0.0, 0.0);
            }

        else if (P0_1)
            if (P1_2 || P0_2) { // P0 /\ { P1 \/ P0}
                min = 0.0;
                max = up(b * d);
                return new FloatIntervalDomain(min, max);
            } else if (N1_2 || N0_2) { //P0 /\ { N0 \/ N1 }
                min = down(b * c);
                max = 0.0; //up(a*d);
                return new FloatIntervalDomain(min, max);
            } else if (M_2) { // P0 /\ M
                min = down(b * c);
                max = up(b * d);
                return new FloatIntervalDomain(min, max);
            } else { //if (Z_2) // P0 /\ Z
                return new FloatIntervalDomain(0.0, 0.0);
            }

        else if (M_1)
            if (P0_2 || P1_2) { // M /\ { P0 \/ P1}
                min = down(a * d);
                max = up(b * d);
                return new FloatIntervalDomain(min, max);
            } else if (N0_2 || N1_2) { // M /\ { N0 \/ N1}
                min = down(b * c);
                max = up(a * c);
                return new FloatIntervalDomain(min, max);
            } else if (M_2) { // M /\ M
                min = down(Math.min(a * d, b * c));
                max = up(Math.max(a * c, b * d));
                return new FloatIntervalDomain(min, max);
            } else { // if (Z_2) M /\ Z
                return new FloatIntervalDomain(0.0, 0.0);
            }

        else if (N1_1)
            if (P1_2) { // N1 /\ P1
                min = down(a * d);
                max = up(b * c);
                return new FloatIntervalDomain(min, max);
            } else if (P0_2) { // N1 /\ P0
                min = down(a * d);
                max = 0.0; //up(b*c);
                return new FloatIntervalDomain(min, max);
            } else if (M_2) {  // N1 /\ M
                min = down(a * d);
                max = up(a * c);
                return new FloatIntervalDomain(min, max);
            } else if (N1_2) { // N1 /\ N1
                min = down(b * d);
                max = up(a * c);
                return new FloatIntervalDomain(min, max);
            } else if (N0_2) { // N1 /\ N0
                min = 0.0; //down(b*d);
                max = up(a * c);
                return new FloatIntervalDomain(min, max);
            } else { // N1 /\ Z
                return new FloatIntervalDomain(0.0, 0.0);
            }

        else if (N0_1)
            if (P0_2 || P1_2) { // N0 /\ { P0 \/ P1}
                min = down(a * d);
                max = 0.0; //up(b*c);
                return new FloatIntervalDomain(min, max);
            } else if (N0_2 || N1_2) { // N0 /\ { N0 \/ N1}
                min = 0.0; //down(b*d);
                max = up(a * c);
                return new FloatIntervalDomain(min, max);
            } else if (M_2) { // N0 /\ M
                min = down(a * d);
                max = up(a * c);
                return new FloatIntervalDomain(min, max);
            } else { // N0 /\ Z
                return new FloatIntervalDomain(0.0, 0.0);
            }
        else { //  Z /\ {ALL}
            return new FloatIntervalDomain(0.0, 0.0);
        }

    }

    /* 
     * Finds result interval for division of {a..b} / {c..d} for div and mod constraints
     */
    public final static FloatIntervalDomain divBounds(double a, double b, double c, double d) {

        // System.out.println ("[" + a +".." +b +"] / [" + c + ".." + d + "]");

        if (c == 1.0 && d == 1.0)
            return new FloatIntervalDomain(a, b);
        else if (c == -1.0 && d == -1.0)
            return new FloatIntervalDomain(-b, -a);

        boolean M_1 = (a < 0 && b > 0);        // contains zero
        boolean Z_1 = (a == 0 && b == 0);     // zero
        boolean P0_1 = (a == 0 && b > 0);       // positive with zero
        boolean P1_1 = (a > 0 && b > 0);        // strictly positive
        boolean N0_1 = (a < 0 && b == 0);       // negative with zero
        boolean N1_1 = (a < 0 && b < 0);        // strictly negative

        boolean M_2 = (c < 0 && d > 0);
        // boolean Z_2 = (c == 0 && d == 0);
        boolean P0_2 = (c == 0 && d > 0);
        boolean P1_2 = (c > 0 && d > 0);
        boolean N0_2 = (c < 0 && d == 0);
        boolean N1_2 = (c < 0 && d < 0);

        double min = 0;
        double max = 0;

        // FloatIntervalDomain result = null;

        if (P1_1)
            if (P1_2) { // P1 /\ P1
                min = down(a / d);
                max = up(b / c);
                return new FloatIntervalDomain(min, max); //.subtract(0.0);
            } else if (P0_2) { // P1 /\ P0
                min = down(a / d);
                return new FloatIntervalDomain(min, FloatDomain.MaxFloat); //.subtract(0.0);
            } else if (M_2) { // P1 /\ M
                min = down(a / d);
                max = up(a / c);
                return (FloatIntervalDomain) new FloatIntervalDomain(FloatDomain.MinFloat, max)
                    .union(new FloatIntervalDomain(min, FloatDomain.MaxFloat)); //.subtract(0.0)
            } else if (N1_2) { // P1 /\ N1
                min = down(b / d);
                max = up(a / c);
                return new FloatIntervalDomain(min, max); //.subtract(0.0);
            } else if (N0_2) { // P1 /\ N0
                max = up(a / c);
                return new FloatIntervalDomain(FloatDomain.MinFloat, max); //.subtract(0.0);
            } else // P1 /\ Z
                throw Store.failException;

        else if (P0_1)
            if (P1_2) { // P0 /\ P1
                min = 0.0;
                max = up(b / c);
                return new FloatIntervalDomain(min, max);
            } else if (N1_2) { //P0 /\ N1
                min = down(b / d);
                max = 0.0;
                return new FloatIntervalDomain(min, max);
            } else  // P0 /\ {M \/ Z \/ P0 \/ N0}}
                return new FloatIntervalDomain(FloatDomain.MinFloat, FloatDomain.MaxFloat);

        else if (M_1)
            if (P1_2) { // M /\ P
                min = down(a / c);
                max = up(b / c);
                return new FloatIntervalDomain(min, max);
            } else if (N1_2) { // M /\ N1
                min = down(b / d);
                max = up(a / d);
                return new FloatIntervalDomain(min, max);
            } else
                return new FloatIntervalDomain(FloatDomain.MinFloat, FloatDomain.MaxFloat);

        else if (N1_1)
            if (P1_2) { // N1 /\ P1
                min = down(a / c);
                max = up(b / d);
                return new FloatIntervalDomain(min, max); //.subtract(0.0);
            } else if (P0_2) { // N1 /\ P0
                max = up(b / d);
                return new FloatIntervalDomain(FloatDomain.MinFloat, max); //.subtract(0.0);
            } else if (M_2) {
                min = down(b / c);
                max = up(b / d);

                return (FloatIntervalDomain) new FloatIntervalDomain(FloatDomain.MinFloat, max)
                    .union(new FloatIntervalDomain(min, FloatDomain.MaxFloat)); //.subtract(0.0)
            } else if (N1_2) { // N1 /\ N1
                min = down(b / c);
                max = up(a / d);
                return new FloatIntervalDomain(min, max); //.subtract(0.0);
            } else if (N0_2) { // N1 /\ N0
                min = down(b / c);
                return new FloatIntervalDomain(min, FloatDomain.MaxFloat); //.subtract(0.0);
            } else // N1 /\ Z
                throw Store.failException;

        else if (N0_1)
            if (P1_2) { // N0 /\ P1
                min = down(a / c);
                max = 0.0;
                return new FloatIntervalDomain(min, max);
            } else if (N1_2) { // N0 /\ N1
                min = 0.0;
                max = up(a / d);
                return new FloatIntervalDomain(min, max);
            } else // N0 /\ {M \/ Z \/ P0 \/ N0}}
                return new FloatIntervalDomain(FloatDomain.MinFloat, FloatDomain.MaxFloat);

        else if (Z_1)
            if (P1_2 || N1_2) {
                min = 0.0;
                max = 0.0;
                return new FloatIntervalDomain(min, max);
            } else
                return new FloatIntervalDomain(FloatDomain.MinFloat, FloatDomain.MaxFloat);

        return null;
    }

}
