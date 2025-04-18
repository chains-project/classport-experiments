/*
 * DisjointConditional.java
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


package org.jacop.constraints;

import org.jacop.core.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * DisjointConditional constraint assures that any two rectangles from a vector
 * of rectangles does not overlap in at least one direction. The execption from
 * this rule is specified on the list of tuple [recti, rectj, C], where recti
 * and rectj are integers representing given rectangles positions on the list of
 * rectangles (starting from 1) and C is FDV 0..1. When C=1 then rectnagles must
 * not overlap otherwise the overlaping is not checked.
 *
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.10
 */

public class DisjointConditional extends Diff {

    static AtomicInteger idNumber = new AtomicInteger(0);

    static final boolean trace = false, traceNarr = false;

    List<? extends IntVar>[] condVariables;

    DisjointCondVar evalRects[];

    /**
     * It specifies what rectangles can conditionally overlap.
     */
    public ExclusiveList exclusionList = new ExclusiveList();

    /**
     * It specifies a diff constraint.
     *
     * @param rectangles    list of rectangles which can not overlap in at least one dimension.
     * @param exclusionList it is a list of exclusive items. Each item consists of two ints and a variable.
     * @param doProfile     should the constraint compute and use the profile functionality.
     */
    public DisjointConditional(Rectangle[] rectangles, ExclusiveList exclusionList, boolean doProfile) {

        checkInputForNullness(new String[] {"rectangles", "exclusionList"}, new Object[][] {rectangles, {exclusionList}});
        checkInput(rectangles, i -> i.dim == 2, "rectangle needs to have exactly two dimensions");

        this.queueIndex = 2;

        this.rectangles = Arrays.copyOf(rectangles, rectangles.length);

        this.doProfile = doProfile;
        this.exclusionList = new ExclusiveList();
        this.exclusionList.addAll(exclusionList);

        this.numberId = idNumber.incrementAndGet();

        setScope(Stream.concat(Rectangle.getStream(this.rectangles), exclusionList.stream().map(i -> i.cond)));
    }

    /**
     * It creates Disjoint conditional constraint.
     *
     * @param rectangles         the rectangles within a constraint.
     * @param exceptionIndices   a list of pairs of conditionally overlaping rectangles.
     * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
     */
    public DisjointConditional(List<List<? extends IntVar>> rectangles, List<List<Integer>> exceptionIndices,
        List<? extends IntVar> exceptionCondition) {

        queueIndex = 2;

        int size = (rectangles.get(0)).size();
        this.rectangles = new Rectangle[rectangles.size()];

        int i = 0;

        for (List<? extends IntVar> R : rectangles)
            if (R.size() == size) {
                Rectangle rect = new Rectangle(R);
                this.rectangles[i] = rect;
                i++;
            } else {
                String s = "\nNot equal sizes of rectangle vectors in Diff";
                throw new IllegalArgumentException(s);
            }
        // }
        if (size / 2 != 2) {
            String s = "\nRectangles of size > 2 not currently supported by Diff";
            throw new IllegalArgumentException(s);
        }

        for (i = 0; i < exceptionIndices.size(); i++) {
            int item1 = exceptionIndices.get(i).get(0);
            int item2 = exceptionIndices.get(i).get(1);
            IntVar condition = exceptionCondition.get(i);
            exclusionList.add(new ExclusiveItem(item1, item2, condition));
        }

        numberId = idNumber.incrementAndGet();
        setScope(Stream.concat(Rectangle.getStream(this.rectangles), exceptionCondition.stream()));
    }

    /**
     * It creates Disjoint conditional constraint.
     *
     * @param rectangles         the rectangles within a constraint.
     * @param exceptionIndices   it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap.
     * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
     * @param profile            it specifies if the profiles are used and computed within the constraint.
     */
    public DisjointConditional(List<List<? extends IntVar>> rectangles, List<List<Integer>> exceptionIndices,
        List<? extends IntVar> exceptionCondition, boolean profile) {

        this(rectangles, exceptionIndices, exceptionCondition);
        doProfile = profile;
    }

    /**
     * It constructs a disjoint conditional constraint.
     *
     * @param o1                 variables specifying the origin in the first dimension.
     * @param o2                 variables specifying the origin in the second dimension.
     * @param l1                 variables specifying the length in the first dimension.
     * @param l2                 variables specifying the length in the second dimension.
     * @param exceptionIndices   it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap.
     * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
     */
    public DisjointConditional(List<? extends IntVar> o1, List<? extends IntVar> o2, List<? extends IntVar> l1, List<? extends IntVar> l2,
        List<List<Integer>> exceptionIndices, List<? extends IntVar> exceptionCondition) {

        this(o1.toArray(new IntVar[o1.size()]), o2.toArray(new IntVar[o2.size()]), l1.toArray(new IntVar[l1.size()]),
            l2.toArray(new IntVar[l2.size()]), exceptionIndices, exceptionCondition);

    }

    /**
     * It constructs a disjoint conditional constraint.
     *
     * @param o1                 variables specifying the origin in the first dimension.
     * @param o2                 variables specifying the origin in the second dimension.
     * @param l1                 variables specifying the length in the first dimension.
     * @param l2                 variables specifying the length in the second dimension.
     * @param exceptionIndices   it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap.
     * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
     * @param profile            it specifies if the profiles are being computed and used within a constraint.
     */
    public DisjointConditional(List<? extends IntVar> o1, List<? extends IntVar> o2, List<? extends IntVar> l1, List<? extends IntVar> l2,
        List<List<Integer>> exceptionIndices, List<? extends IntVar> exceptionCondition, boolean profile) {

        this(o1, o2, l1, l2, exceptionIndices, exceptionCondition);
        doProfile = profile;

    }

    /**
     * It constructs a disjoint conditional constraint.
     *
     * @param origin1            variables specifying the origin in the first dimension.
     * @param origin2            variables specifying the origin in the second dimension.
     * @param length1            variables specifying the length in the first dimension.
     * @param length2            variables specifying the length in the second dimension.
     * @param exceptionIndices   it specifies a list of pairs, where each pair specifies two rectangles which conditionally overlap.
     * @param exceptionCondition a variable specifying if a corresponding pair is nonoverlapping.
     */
    public DisjointConditional(IntVar[] origin1, IntVar[] origin2, IntVar[] length1, IntVar[] length2, List<List<Integer>> exceptionIndices,
        List<? extends IntVar> exceptionCondition) {

        checkInputForNullness(new String[] {"origin1", "origin2", "length1", "length2", "exceptionIndices", "exceptionCondition"},
            new Object[][] {origin1, origin2, length1, length2, {exceptionIndices}, {exceptionCondition}});

        this.queueIndex = 2;

        int size = origin1.length;
        if (size == origin1.length && size == origin2.length && size == length1.length && size == length2.length) {

            this.rectangles = new Rectangle[size];
            for (int i = 0; i < size; i++) {
                this.rectangles[i] = new Rectangle(new IntVar[] {origin1[i], origin2[i], length1[i], length2[i]});
            }

        } else {
            throw new IllegalArgumentException("DisjointConditional does not have equal sizes of length and origin vectors.");
        }


        for (int i = 0; i < exceptionIndices.size(); i++) {
            int item1 = exceptionIndices.get(i).get(0);
            int item2 = exceptionIndices.get(i).get(1);
            IntVar condition = exceptionCondition.get(i);
            exclusionList.add(new ExclusiveItem(item1, item2, condition));
        }

        this.numberId = idNumber.incrementAndGet();

        setScope(Stream.concat(Rectangle.getStream(this.rectangles), exceptionCondition.stream()));


    }

    /**
     * It constructs a disjoint conditional constraint.
     *
     * @param o1                 variables specifying the origin in the first dimension.
     * @param o2                 variables specifying the origin in the second dimension.
     * @param l1                 variables specifying the length in the first dimension.
     * @param l2                 variables specifying the length in the second dimension.
     * @param exceptionIndices   list of rectangles that may not be considered
     * @param exceptionCondition conditions for rectangles that may not be considered
     * @param profile            it specifies if the profiles are being used and computed within that constraint.
     */
    public DisjointConditional(IntVar[] o1, IntVar[] o2, IntVar[] l1, IntVar[] l2, List<List<Integer>> exceptionIndices,
        List<? extends IntVar> exceptionCondition, boolean profile) {
        this(o1, o2, l1, l2, exceptionIndices, exceptionCondition);
        doProfile = profile;
    }

    /**
     * It creates Disjoint conditional constraint.
     *
     * @param rectangles         the rectangles within a constraint.
     * @param exceptionIndices   list of rectangles that may not be considered
     * @param exceptionCondition conditions for rectangles that may not be considered
     */
    public DisjointConditional(IntVar[][] rectangles, List<List<Integer>> exceptionIndices, List<? extends IntVar> exceptionCondition) {

        assert (rectangles != null) : "Rectangles list is null";

        queueIndex = 2;
        IntVar[] R;

        int size = rectangles[0].length;
        this.rectangles = new Rectangle[rectangles.length];

        for (int i = 0; i < rectangles.length; i++) {
            assert (rectangles[i] != null) : i + "-th list within rectangles list is null";
            R = rectangles[i];
            if (R.length == size) {
                Rectangle rect = new Rectangle(R);
                this.rectangles[i] = rect;
            } else {
                String s = "\nNot equal sizes of rectangle vectors in Diff";
                throw new IllegalArgumentException(s);
            }
        }
        if (size / 2 != 2) {
            String s = "\nRectangles of size > 2 not currently supported by Diff";
            throw new IllegalArgumentException(s);

        }

        for (int i = 0; i < exceptionIndices.size(); i++) {
            int item1 = exceptionIndices.get(i).get(0);
            int item2 = exceptionIndices.get(i).get(1);
            IntVar condition = exceptionCondition.get(i);
            exclusionList.add(new ExclusiveItem(item1, item2, condition));
        }

        numberId = idNumber.incrementAndGet();

        setScope(Stream.concat(Rectangle.getStream(this.rectangles), exceptionCondition.stream()));
    }

    /**
     * It creates Disjoint conditional constraint.
     *
     * @param rectangles         the rectangles within a constraint.
     * @param exceptionIndices   list of rectangles that may not be considered
     * @param exceptionCondition conditions for rectangles that may not be considered
     * @param profile            it specifies if the profiles are being computed and used within that constraint.
     */
    public DisjointConditional(IntVar[][] rectangles, List<List<Integer>> exceptionIndices, List<? extends IntVar> exceptionCondition,
        boolean profile) {

        this(rectangles, exceptionIndices, exceptionCondition);
        doProfile = profile;
    }



    boolean checkRect(RectangleWithCondition r) {
        return (r.condition() == null) ? true : ((r.condition().min() == 1) ? true : false);
    }

    boolean conditionChanged(Set<IntVar> fdvQueue, int j) {
        boolean changed = false;
        List<? extends IntVar> el = condVariables[j];
        int i = 0;
        while (!changed && i < el.size()) {
            // System.out.println("checking "+ j + (Variable)el.get(i));
            changed = fdvQueue.contains(el.get(i));
            i++;
        }
        return changed;
    }

    boolean doesNotFit(int j, Rectangle r, Profile barrier) {
        boolean excludedState = true;

        IntDomain rOriginJdom = r.origin[j].dom();
        IntDomain rLengthJdom = r.length[j].dom();
        int minJ = rOriginJdom.min();
        int maxJ = rOriginJdom.max() + rLengthJdom.min();
        int durJ = rLengthJdom.min();
        int currentJposition = minJ;
        int k = 0, barrierSize = barrier.size();
        while (k < barrierSize && excludedState) {
            ProfileItem p = barrier.get(k);
            int hinderStart = p.min;
            int hinderStop = p.max;
            // System.out.println(hinder);
            if (hinderStart - currentJposition >= durJ)
                excludedState = false;
            currentJposition = hinderStop;
            k++;
        }
        if (excludedState && maxJ - currentJposition >= durJ)
            excludedState = false;
        return excludedState;
    }

    int findMaxLength(int i, int length, Rectangle r) {

        int maxLength = length;
        IntDomain origin = r.origin[i].dom();
        int dur = r.length[i].min();

        for (int m = 0; m < origin.noIntervals(); m++) {
            int intervalLength = origin.rightElement(m) - origin.leftElement(m) + dur;
            if (maxLength < intervalLength)
                maxLength = intervalLength;
        }
        return maxLength;
    }

    boolean findRectangles(Rectangle r, int index, List<IntRectangle> UsedRect, List<RectangleWithCondition> ProfileCandidates,
        List<RectangleWithCondition> OverlappingRects, Set<IntVar> fdvQueue) {
        // Variable condition;
        boolean contains = false, checkArea = false;

        long area = 0;
        int totalNumberOfRectangles = 0;
        int dim = r.dim();
        int startMin[] = new int[dim];
        int stopMax[] = new int[dim];
        int minLength[] = new int[dim];
        int r_min[] = new int[dim], r_max[] = new int[dim];
        for (int i = 0; i < startMin.length; i++) {
            IntDomain rLengthDom = r.length[i].dom();
            startMin[i] = IntDomain.MaxInt;
            stopMax[i] = 0;
            minLength[i] = rLengthDom.min();

            IntDomain rOriginDom = r.origin[i].dom();
            r_min[i] = rOriginDom.min();
            r_max[i] = rOriginDom.max() + rLengthDom.max();
        }

        for (RectangleWithCondition s : ((DisjointCondVarValue) evalRects[index].value()).Rects) {
            boolean overlap = true;

            boolean sChanged = containsChangedVariable(s, fdvQueue) || conditionChanged(fdvQueue, s.index);

            IntRectangle Use = new IntRectangle(dim);
            long sArea = 1;

            boolean use = true, minLength0 = false;
            int s_min, s_max, start, stop;
            int m = 0, j = 0;
            int sOriginMin[] = new int[dim], sOriginMax[] = new int[dim], sLengthMin[] = new int[dim];

            while (overlap && m < dim) {
                // check if domains of r and s overlap
                IntDomain sOriginIdom = s.origin[m].dom();
                IntDomain sLengthIdom = s.length[m].dom();
                int sLengthIMin = sLengthIdom.min();
                int sOriginIMax = sOriginIdom.max();
                s_min = sOriginIdom.min();
                s_max = sOriginIMax + sLengthIdom.max();

                overlap = overlap && intervalOverlap(r_min[m], r_max[m], s_min, s_max);

                // min start, max stop and min length
                sOriginMin[m] = s_min;
                sOriginMax[m] = sOriginIMax + sLengthIMin;
                sLengthMin[m] = sLengthIMin;

                // check if s occupies some space
                start = sOriginIMax;
                stop = s_min + sLengthIMin;
                if (start <= stop) { // we allow length=0 for rectangles
                    // to occupy o length space !!!
                    Use.add(start, stop - start);
                    j++;
                } else
                    use = false;

                // min length == 0
                minLength0 = minLength0 || (sLengthMin[m] <= 0);

                m++;
            }

            if (overlap) {
                if (s.condition() == null)
                    OverlappingRects.add(s);
                else if (s.condition().max() != 0)
                    OverlappingRects.add(s);

                if (checkRect(s)) {
                    if (use) {
                        UsedRect.add(Use);
                        contains = contains || sChanged;
                    }

                    if (!minLength0) { // profile candiates
                        if (j > 0) {
                            ProfileCandidates.add(s);
                            contains = contains || sChanged;
                        }

                        if (!exclusionList.onList(s.index)) {
                            // simplification - considers only rectangles
                            // which cannot be exclusive !!!
                            checkArea = true;
                            totalNumberOfRectangles++;
                            for (int i = 0; i < dim; i++) {
                                if (sOriginMin[i] < startMin[i])
                                    startMin[i] = sOriginMin[i];
                                if (sOriginMax[i] > stopMax[i])
                                    stopMax[i] = sOriginMax[i];
                                if (minLength[i] > sLengthMin[i])
                                    minLength[i] = sLengthMin[i];

                                sArea = sArea * sLengthMin[i];
                            }
                            area += sArea;
                        }
                    }
                }
            }
        }

        if (checkArea) { // check whether there is
            // enough room for all rectangles
            area += r.minArea();
            long availArea = 1;
            long rectNumber = 1;
            for (int i = 0; i < startMin.length; i++) {
                IntDomain rOriginIdom = r.origin[i].dom();
                IntDomain rLengthIdom = r.length[i].dom();
                int rOriginIMin = rOriginIdom.min(), rOriginIMax = rOriginIdom.max(), rLengthIMin = rLengthIdom.min();
                if (rOriginIMin < startMin[i])
                    startMin[i] = rOriginIMin;
                if (rOriginIMax + rLengthIMin > stopMax[i])
                    stopMax[i] = rOriginIMax + rLengthIMin;
            }
            boolean minEqZero = false;
            for (int i = 0; i < startMin.length; i++) {
                availArea = availArea * (stopMax[i] - startMin[i]);
                if (minLength[i] == 0) {
                    minEqZero = true;
                } else
                    rectNumber = rectNumber * ((stopMax[i] - startMin[i]) / minLength[i]);
            }
            if (minEqZero)
                rectNumber = Long.MAX_VALUE;

            if (availArea < area) {
                // System.out.println("Fail area: "+ availArea+" < "+area+" at
                // level "+currentStore.level);
                // System.out.println(r);
                // for (int ind=0; ind<OverlappingRects.size(); ind++)
                // if (((Rectangle)OverlappingRects.get(ind)).length[0].min() >
                // 0 &&
                // ((Rectangle)OverlappingRects.get(ind)).length[1].min() > 0)
                // if (!
                // onExList(((RectangleWithCondition)OverlappingRects.get(ind)).index)
                // )
                // System.out.println(OverlappingRects.get(ind));
                throw Store.failException;
            } else
                // check whether there is enough room for
                // all minimal rectangles
                if (rectNumber < (totalNumberOfRectangles + 1)) {
                    // System.out.println("Fail number at level
                    // "+currentStore.level);
                    throw Store.failException;
                }
        }

        return contains;
    }

    @SuppressWarnings("unchecked") @Override public void impose(Store store) {

        super.impose(store);

        condVariables = new ArrayList[rectangles.length + 1];

        evalRects = new DisjointCondVar[rectangles.length];
        for (int i = 0; i < rectangles.length; i++) {
            condVariables[i + 1] = exclusionList.fdvs(i + 1);

            List<RectangleWithCondition> rectsC = new ArrayList<RectangleWithCondition>();
            for (int j = 0; j < rectangles.length; j++) {
                if (i != j) {
                    IntVar c = exclusionList.condition(i, j);
                    rectsC.add(new RectangleWithCondition(j + 1, rectangles[j], c));
                }
            }
            evalRects[i] = new DisjointCondVar(store, rectsC);
        }

        for (ExclusiveItem ei : exclusionList) {
            IntVar v = ei.cond;
            if (!v.singleton()) {
                queueVariable(store.level, v);
            }
        }
    }

    Interval minForbiddenInterval(int start, int i, Rectangle r, List<IntRectangle> ConsideredRect, int minI) {

        if (notFit(i, r, ConsideredRect))
            return new Interval(start, minI);
        else
            return new Interval(-1, -1);
    }

    void narrowIthCondition(int i, Rectangle r, List<IntRectangle> UsedRect, List<RectangleWithCondition> ProfileCandidates) {
        Interval exclude;
        int s;
        int j = (i == 0) ? 1 : 0;
        int rSize = r.origin[j].max() - r.origin[j].min();
        int rLengthJMin = r.length[j].min(), rLengthIMin = r.length[i].min();
        int barierSize = 0;

        if (ProfileCandidates.size() != 0 && doProfile)
            profileNarrowingCondition(i, r, ProfileCandidates);

        if (UsedRect.size() != 0) {

            IntRectangle[] UsedRectArray = new IntRectangle[UsedRect.size()];

            UsedRectArray = UsedRect.toArray(UsedRectArray);

            TreeSet<IntRectangle> starts = new TreeSet<IntRectangle>(dimIthMinComparator.apply(i));
            Collections.addAll(starts, UsedRectArray);

            IntRectangle strtR = new IntRectangle(r.dim);
            IntRectangle maxRect = new IntRectangle(r.dim);
            for (int k = 0; k < r.dim; k++) {
                IntDomain rOrigin = r.origin[k].dom(), rLength = r.length[k].dom();
                strtR.add(rOrigin.min(), rLength.min());
                if (k == i)
                    maxRect.add(rOrigin.max(), rLength.max());
                else
                    maxRect.add(rOrigin.min(), rLength.min());
            }
            starts.add(strtR);

            List<IntRectangle> ConsideredRect = new ArrayList<IntRectangle>();
            for (IntRectangle ir : starts) {
                s = ir.origin[i];

                ConsideredRect.clear();
                int minI = IntDomain.MaxInt;
                long rectSize = 0;
                for (IntRectangle t : UsedRectArray) {
                    int tempMin = t.origin[i] + t.length[i];

                    if (t.origin[i] - s < rLengthIMin && s < tempMin) {
                        ConsideredRect.add(t);
                        rectSize += t.length[j];
                        // Determine minimum length in direction i
                        // (possibly new start time)
                        if (tempMin < minI)
                            minI = tempMin;
                    }
                }

                if (ConsideredRect.size() != 0 && rSize < (rectSize + (rLengthJMin - 1) * ConsideredRect.size())) {

                    IntDomain rOriginDom = r.origin[i].dom();
                    int m = 0;
                    for (; m < rOriginDom.noIntervals(); m++) {
                        // for (Interval rI : r.origin[i].dom()) {
                        if (s >= rOriginDom.leftElement(m) && s <= rOriginDom.rightElement(m)) {
                            // System.out.println("Checking rectangles in
                            // dimension "+i+
                            // " starting at time interval "+ s + ".."
                            // +(int)(s+r.length(i).min()-1)+
                            // "\nCosideredRect =" + ConsideredRect);

                            exclude = minForbiddenInterval(s, i, r, ConsideredRect, minI);

                            if (exclude.max != -1) {
                                // Domain Update = Domainset.complement(new
                                // FD(exclude.Min-
                                // r.length[i].min()+1,
                                // exclude.Max-1));
                                int min = exclude.min - r.length[i].min();
                                if (min + 1 < exclude.max) {
                                    IntervalDomain Update = new IntervalDomain(IntDomain.MinInt, min);
                                    Update.unionAdapt(exclude.max, IntDomain.MaxInt);

                                    if (traceNarr)
                                        System.out.print(
                                            "7. Obligatory rectangles Narrow " + ConsideredRect + "\n" + r + "\n" + r.origin[i] + " in "
                                                + Update + "length=" + r.length[i].min() + "\n");

                                    // currentStore.in(r.origin[i], Update);
                                    r.origin[i].domain.in(currentStore.level, r.origin[i], Update);

                                    if (traceNarr)
                                        System.out.println(" -->" + r.origin[i]);
                                }
                            }
                        }
                    }
                }
            }

            // Update rectangles length in direction i
            // sort rectangles on increasing origin i
            List<IntRectangle> ConsideredRectDur = new ArrayList<IntRectangle>();
            for (IntRectangle t : UsedRectArray) {
                if (t.overlap(maxRect)) {
                    ConsideredRectDur.add(t);
                    barierSize += t.length[j];
                }
            }

            if (ConsideredRectDur.size() != 0 && rSize < (barierSize + (rLengthJMin - 1) * ConsideredRectDur.size())) {

                IntRectangle[] rects = new IntRectangle[ConsideredRectDur.size()];
                rects = ConsideredRectDur.toArray(rects);
                Arrays.sort(rects, dimIthMinComparator.apply(i));

                // Object[] rects = ConsideredRectDur.toArray();
                // Comparator<Object> c = new DimIMinComparator<Object>(i);
                // Arrays.sort(rects, c);

                // System.out.println("Considered rectangles : " +
                // ConsideredRectDur);

                Profile barrier = new Profile();
                boolean lengthOK = true;
                int newMaxLength = 0;
                int n = 0;
                while (n < rects.length && lengthOK) {
                    IntRectangle hinder = rects[n];
                    barrier.addToProfile(hinder.origin[j], hinder.origin[j] + hinder.length[j], 1);
                    if (doesNotFit(j, r, barrier)) {
                        lengthOK = false;
                        newMaxLength = hinder.origin[i] -
                            // ((Interval)r.origin[i].dom().lastElement()).Min;
                            r.origin[i].min();
                    }
                    n++;
                }
                // System.out.println(barrier+ "-->> "+lengthOK
                // + ", new max length = " + newMaxLength);
                if (!lengthOK) {
                    // update length in dimension j
                    int maxLength = findMaxLength(i, newMaxLength, r);

                    if (maxLength < r.length[i].max()) {
                        if (traceNarr)
                            System.out
                                .println("9. Obligatory rectangles Narrow " + r.length[i] + " in " + IntDomain.MinInt + ".." + maxLength);
                        r.length[i].domain.inMax(currentStore.level, r.length[i], maxLength);
                        // currentStore.in(r.length[i], MinInt, maxLength);
                    }
                }
            }
        }
    }

    void narrowRectangleCondition(Rectangle r, List<IntRectangle> UsedRect, List<RectangleWithCondition> ProfileCandidates) {

        if (trace) {
            System.out.println("Narrowing " + r);
            System.out.println(ProfileCandidates);
        }

        for (int i = 0; i < r.dim; i++) {
            // narrow in i-th dimension
            narrowIthCondition(i, r, UsedRect, ProfileCandidates);
        }
    }

    @Override void narrowRectangles(Set<IntVar> fdvQueue) {
        Rectangle r;
        boolean needToNarrow = false;

        for (int l = 0; l < rectangles.length; l++) {
            r = rectangles[l];

            boolean minLengthLt0 = false; // settled=true
            // int maxLevel=0;
            for (int i = 0; i < r.dim(); i++) {
                minLengthLt0 = minLengthLt0 || (r.length[i].min() < 0); // (
                // rLength.min()
                // < 0
                // );
            }

            if (!minLengthLt0 // Check for rectangle r which has
                // all lengths > 0
                ) {
                // and are not fixed already

                needToNarrow = needToNarrow || containsChangedVariable(r, fdvQueue);

                List<IntRectangle> UsedRect = new ArrayList<IntRectangle>();
                List<RectangleWithCondition> ProfileCandidates = new ArrayList<RectangleWithCondition>();
                List<RectangleWithCondition> OverlappingRects = new ArrayList<RectangleWithCondition>();
                boolean ntN = findRectangles(r, l, UsedRect, ProfileCandidates, OverlappingRects, fdvQueue);

                needToNarrow = needToNarrow || ntN || conditionChanged(fdvQueue, l + 1);

                if (needToNarrow) {

                    if (OverlappingRects.size() != ((DisjointCondVarValue) evalRects[l].value()).Rects.length) {
                        DisjointCondVarValue newRects = new DisjointCondVarValue();
                        newRects.setValue(OverlappingRects);
                        evalRects[l].update(newRects);
                    }

                    // Checking r against all s with minUse in the domain of r
                    narrowRectangleCondition(r, UsedRect, ProfileCandidates);

                }
            }
        }
    }

    boolean notFit(int i, Rectangle r, List<IntRectangle> ConsideredRect) {
        boolean excludedState = true;
        Profile barrier = new Profile();

        int j = 0;
        while (excludedState && j < r.dim) {
            if (i != j) {
                // System.out.println(r.toStringFull()+"\n"+ConsideredRect );
                IntDomain rOriginJdom = r.origin[j].dom();
                IntDomain rLengthJdom = r.length[j].dom();
                int minJ = rOriginJdom.min();
                int maxJ = rOriginJdom.max() + rLengthJdom.min();
                int durJ = rLengthJdom.min();

                barrier.clear();
                for (IntRectangle hinder : ConsideredRect) {
                    // System.out.println(hinder.origin[j] + ".."
                    // + (int)(hinder.origin[j]
                    // + hinder.length[j]));
                    int hinderJ = hinder.origin[j];
                    barrier.addToProfile(hinderJ, hinderJ + hinder.length[j], 1);
                }
                // System.out.println("Barrier : " + barrier);

                int currentJposition = minJ;
                // System.out.println(maxJ+", "+durJ+", "+currentJposition);
                int k = 0, barrierSize = barrier.size();
                while (k < barrierSize && excludedState) {
                    ProfileItem p = barrier.get(k);
                    int hinderStart = p.min;
                    int hinderStop = p.max;
                    // System.out.println("Hinder =
                    // "+hinderStart+".."+hinderStop);
                    if (hinderStart - currentJposition >= durJ)
                        excludedState = false;
                    currentJposition = hinderStop;
                    k++;
                }
                if (excludedState && maxJ - currentJposition >= durJ)
                    excludedState = false;
            }
            j++;
        }
        // System.out.println("2. "+excludedState );
        return excludedState;
    }

    void profileCheckInterval(Store store, DisjointConditionalProfile Profile, int limit, IntVar Start, IntVar Duration, int _min, int _max,
        IntVar Resources) {

        int dur = Duration.min();
        for (ProfileItem p : Profile) {
            if (trace)
                System.out.println("Comparing [" + _min + " " + _max + "] with profile item " + p);
            if (intervalOverlap(_min, _max + dur, p.min, p.max)) {
                if (limit - p.value < Resources.min()) {
                    // Check for possible narrowing of Start or fail
                    IntDomain StartDom = Start.dom();
                    int updateMin = p.min - dur + 1, updateMax = p.max - 1;
                    if (!(updateMin > StartDom.max() || updateMax < StartDom.min())) {
                        IntervalDomain Update = new IntervalDomain(IntDomain.MinInt, p.min - dur);
                        Update.unionAdapt(p.max, IntDomain.MaxInt);

                        if (traceNarr)
                            System.out.print(
                                "6. Profile Narrowed " + Start + " \\ " + Update + "; duration=" + Duration + "; resources=" + Resources
                                    + ", limit=" + limit + "\n" + Profile + "\n");

                        Start.domain.in(store.level, Start, Update);

                        if (traceNarr)
                            System.out.println(" => " + Start);
                    }
                } else {
                    IntDomain StartDom = Start.dom();
                    int start = StartDom.max(), stop = StartDom.min() + dur;
                    if (start < stop && intervalOverlap(start, stop, p.min, p.max)) {
                        int updateMax = limit - p.value;
                        IntervalDomain Update = new IntervalDomain(0, updateMax);
                        if (updateMax < Resources.max()) {
                            if (traceNarr)
                                System.out.println("8. Profile Narrowed " + Resources + " in " + Update);

                            Resources.domain.in(store.level, Resources, Update);

                            if (traceNarr)
                                System.out.println(" => " + Resources);
                        }
                    }
                }
            }
        }
    }

    void profileCheckRectangle(DisjointConditionalProfile Profile, Rectangle r, int i, int j) {

        IntVar s = r.origin[i];
        IntVar dur = r.length[i];
        IntVar resUse = r.length[j];
        IntDomain rOriginJdom = r.origin[j].dom();
        int limit = rOriginJdom.max() + resUse.max() - rOriginJdom.min();

        if (trace)
            System.out.println("Start time = " + s + ", resource use = " + resUse);

        IntDomain d = s.dom();
        for (int m = 0; m < d.noIntervals(); m++)
            profileCheckInterval(currentStore, Profile, limit, s, dur, d.leftElement(m), d.rightElement(m), resUse);
    }

    void profileNarrowingCondition(int i, Rectangle r, List<RectangleWithCondition> ProfileCandidates) {
        // check profile first

        IntDomain rOriginIdom = r.origin[i].dom();
        int rOriginIdomMin = rOriginIdom.min();
        int rOriginIdomMax = rOriginIdom.max();
        DisjointConditionalProfile Profile = new DisjointConditionalProfile();

        for (int j = 0; j < r.dim; j++) {
            if (j != i && r.length[i].min() != 0) {

                Profile.make(j, i, r, rOriginIdomMin, rOriginIdomMax + r.length[i].min(), ProfileCandidates, exclusionList);

                if (Profile.size() != 0) {
                    if (trace) {
                        System.out.println(" *** " + r + "\n" + ProfileCandidates);
                        System.out.println("Profile in dimension " + i + " and " + j + "\n" + Profile);
                    }

                    profileCheckRectangle(Profile, r, i, j);

                }
            }
        }
    }

    @Override public boolean satisfied() {
        boolean sat = true;

        Rectangle recti, rectj;
        int i = 0;
        while (sat && i < rectangles.length) {
            recti = rectangles[i];
            int j = 0;
            Rectangle toEvaluate[] = ((DisjointCondVarValue) evalRects[i].value()).Rects;
            while (sat && j < toEvaluate.length) {
                rectj = toEvaluate[j];
                sat = sat && !recti.domOverlap(rectj);
                j++;
            }
            i++;
        }
        return sat;
    }

    @Override public String toString() {

        StringBuffer result = new StringBuffer(id());

        result.append(" : disjointConditional( ");

        int i = 0;
        for (Rectangle rectangle : rectangles) {
            result.append(rectangle);
            if (i < rectangles.length - 1)
                result.append(", ");
            i++;
        }

        result.append(", ").append(exclusionList).append(")");

        return result.toString();

    }

}
