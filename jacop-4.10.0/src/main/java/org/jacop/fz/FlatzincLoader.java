/*
 * FlatzincLoader.java
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

import org.jacop.core.FailException;
import org.jacop.core.Store;
import org.jacop.core.Var;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.SelectChoicePoint;

/**
 * An executable to parse the flatzinc file and create the JaCoP model.
 *
 * @author Krzysztof Kuchcinki
 * @version 4.10
 */

public class FlatzincLoader {

    Options opt;

    Parser parser;

    /**
     * It parses the provided file and parsing parameters and creates the JaCoP model..
     *
     * @param args parameters describing the flatzinc file containing the problem to be solved as well as options for problem solving.
     *             <p>
     *             TODO what are the conditions for different exceptions being thrown? Write little info below.
     */


    public FlatzincLoader(String[] args) {

        opt = new Options(args);
        opt.doNotRunSearch();

    }

    public void load() {

        if (opt.getVerbose())
            System.out.println("%% Flatzinc2JaCoP: compiling and executing " + opt.getFileName());

        parser = new Parser(opt.getFile());
        parser.setOptions(opt);

        try {

            parser.model();

        } catch (FailException e) {
            System.out.println("=====UNSATISFIABLE====="); // "*** Evaluation of model resulted in fail.");
            // } catch (ArithmeticException e) {
            //     System.err.println("%% Evaluation of model resulted in an overflow.");
        } catch (ParseException e) {
            System.out.println("%% Parser exception " + e);
        } catch (TokenMgrError e) {
            System.out.println("%% Parser exception " + e);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("%% JaCoP internal error. Array out of bound exception " + e);
            if (e.getStackTrace().length > 0)
                System.out.println("%%\t" + e.getStackTrace()[0]);
        } catch (OutOfMemoryError e) {
            System.out.println("%% Out of memory error; consider option -Xmx... for JVM");
        } catch (StackOverflowError e) {
            System.out.println("%% Stack overflow exception error; consider option -Xss... for JVM");
        }

    }

    public Store getStore() {
        return parser.store;
    }

    @SuppressWarnings("unchecked")
    public DepthFirstSearch<Var> getDFS() {
        return parser.solver.flatzincDFS;
    }

    @SuppressWarnings("unchecked")
    public SelectChoicePoint<Var> getSelectChoicePoint() {
        return parser.solver.flatzincVariableSelection;
    }

    public Var getCost() {
        return parser.solver.flatzincCost;
    }

    @SuppressWarnings("unchecked")
    public Solve<Var> getSolve() {
        return parser.solver;
    }

    public Tables getTables() {
        return parser.dict;
    }

    public SearchItem<Var> getSearch() {
        return getSolve().getSearch();
    }

    public Options getOptions() {
        return opt;
    }
}
