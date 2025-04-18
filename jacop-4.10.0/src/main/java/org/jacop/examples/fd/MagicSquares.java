/*
 * MagicSquares.java
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

package org.jacop.examples.fd;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * It solves a Magic squares problem.
 *
 * @author Radoslaw Szymanek
 * @version 4.10
 *          <p>
 *          MagicSquare problem consists of filling the square of size n with
 *          numbers from 1 to n^2 in such a way that all rows, all columns, and
 *          main diagonals are equal to the same number K. K can be computed to
 *          be equal to (n * (n^2 + 1)) / 2.
 */

public class MagicSquares extends ExampleFD {

    /**
     * It specifies the number
     */
    public int number = 4;

    /**
     * It specifies the list of constraints which can be used for guiding shaving.
     */
    public List<Constraint> guidingShaving;


    @Override public void model() {

        // Creating constraint store
        store = new Store();
        vars = new ArrayList<IntVar>();

        IntVar squares[] = new IntVar[number * number];

        IntVar k = new IntVar(store, "K", (number * (number * number + 1)) / 2, (number * (number * number + 1)) / 2);

        for (int i = 0; i < number; i++)
            for (int j = 0; j < number; j++)
                squares[i * number + j] = new IntVar(store, "S" + (i + 1) + "," + (j + 1), 1, number * number);

        for (int i = 0; i < number; i++)
            vars.add(squares[(i) * number + i]);
        for (int i = number; i > 0; i--)
            vars.add(squares[(i - 1) * number + (number - i)]);
        for (IntVar v : squares)
            vars.add(v);

        // Imposing inequalities constraints between squares
        store.impose(new Alldiff(squares));

        IntVar row[] = new IntVar[number];

        for (int i = 0; i < number; i++) {
            for (int j = 0; j < number; j++)
                row[j] = squares[i * number + j];
            store.impose(new SumInt(row, "==", k));
        }

        IntVar column[] = new IntVar[number];

        for (int j = 0; j < number; j++) {
            for (int i = 0; i < number; i++)
                column[i] = squares[i * number + j];
            store.impose(new SumInt(column, "==", k));
        }

        IntVar diagonal[] = new IntVar[number];

        for (int i = 0; i < number; i++)
            diagonal[i] = squares[(i) * number + i];

        store.impose(new SumInt(diagonal, "==", k));

        for (int i = number; i > 0; i--)
            diagonal[i - 1] = squares[(i - 1) * number + (number - i)];
        store.impose(new SumInt(diagonal, "==", k));

        // symmetry breaking
        store.impose(new XltY(squares[0], squares[number - 1]));
        store.impose(new XltY(squares[0], squares[number * number - 1]));
        store.impose(new XltY(squares[0], squares[number * number - number]));

    }

    /**
     * It creates the model with specification of what constraint can
     * help in guiding shaving.
     */
    public void model4Shaving() {

        guidingShaving = new ArrayList<Constraint>();

        // Creating constraint store
        store = new Store();
        vars = new ArrayList<IntVar>();

        IntVar squares[] = new IntVar[number * number];

        IntVar k = new IntVar(store, "K", (number * (number * number + 1)) / 2, (number * (number * number + 1)) / 2);

        for (int i = 0; i < number; i++)
            for (int j = 0; j < number; j++)
                squares[i * number + j] = new IntVar(store, "S" + (i + 1) + "," + (j + 1), 1, number * number);

        for (int i = 0; i < number; i++)
            vars.add(squares[(i) * number + i]);
        for (int i = number; i > 0; i--)
            vars.add(squares[(i - 1) * number + (number - i)]);
        for (IntVar v : squares)
            vars.add(v);

        // Imposing inequalities constraints between squares
        store.impose(new Alldiff(squares));

        IntVar row[] = new IntVar[number];

        for (int i = 0; i < number; i++) {
            for (int j = 0; j < number; j++)
                row[j] = squares[i * number + j];
            Constraint cx = new SumInt(row, "==", k);
            store.impose(cx);
            guidingShaving.add(cx);
        }

        IntVar column[] = new IntVar[number];

        for (int j = 0; j < number; j++) {
            for (int i = 0; i < number; i++)
                column[i] = squares[i * number + j];

            Constraint cx = new SumInt(column, "==", k);
            store.impose(cx);
            guidingShaving.add(cx);
        }

        IntVar diagonal[] = new IntVar[number];

        for (int i = 0; i < number; i++)
            diagonal[i] = squares[(i) * number + i];

        Constraint cx = new SumInt(diagonal, "==", k);
        store.impose(cx);
        guidingShaving.add(cx);

        for (int i = number; i > 0; i--)
            diagonal[i - 1] = squares[(i - 1) * number + (number - i)];
        store.impose(new SumInt(diagonal, "==", k));

        // symmetry breaking
        store.impose(new XltY(squares[0], squares[number - 1]));
        store.impose(new XltY(squares[0], squares[number * number - 1]));
        store.impose(new XltY(squares[0], squares[number * number - number]));

        // store.print();

    }

    /**
     * IT creates a dual model.
     */
    public void modelDual() {

        // Creating constraint store
        store = new Store();
        vars = new ArrayList<IntVar>();

        IntVar squares[] = new IntVar[number * number];

        IntVar k = new IntVar(store, "K", (number * (number * number + 1)) / 2, (number * (number * number + 1)) / 2);

        for (int i = 0; i < number; i++)
            for (int j = 0; j < number; j++)
                squares[i * number + j] = new IntVar(store, "S" + (i + 1) + "," + (j + 1), 1, number * number);

        for (int i = 0; i < number; i++)
            vars.add(squares[(i) * number + i]);
        for (int i = number; i > 0; i--)
            vars.add(squares[(i - 1) * number + (number - i)]);
        for (IntVar v : squares)
            vars.add(v);

        IntVar row[] = new IntVar[number];

        for (int i = 0; i < number; i++) {
            for (int j = 0; j < number; j++)
                row[j] = squares[i * number + j];
            store.impose(new SumInt(row, "==", k));
        }

        IntVar column[] = new IntVar[number];

        for (int j = 0; j < number; j++) {
            for (int i = 0; i < number; i++)
                column[i] = squares[i * number + j];
            store.impose(new SumInt(column, "==", k));
        }

        IntVar diagonal[] = new IntVar[number];

        for (int i = 0; i < number; i++)
            diagonal[i] = squares[(i) * number + i];

        store.impose(new SumInt(diagonal, "==", k));

        for (int i = number; i > 0; i--)
            diagonal[i - 1] = squares[(i - 1) * number + (number - i)];
        store.impose(new SumInt(diagonal, "==", k));

        // // symmetry breaking
        // store.impose(new XltY(squares[0], squares[number-1]));
        // store.impose(new XltY(squares[0], squares[number*number - 1]));
        // store.impose(new XltY(squares[0], squares[number*number - number]));

        IntVar[] d = new IntVar[number * number];

        for (int i = 0; i < number * number; i++) {
            d[i] = new IntVar(store, "d" + i, 1, number * number);
            vars.add(d[i]);
        }

        store.impose(new Assignment(squares, d, 1));

        // Imposing inequalities constraints between squares
        Constraint cx = new Alldistinct(squares);
        store.impose(cx);

    }

    /**
     * It executes the program which solves the MagicSquare problem using many different
     * model and searches.
     *
     * @param args the first argument allows to specify the size of magic square.
     */
    public static void test(String args[]) {

        MagicSquares example = new MagicSquares();

        if (args.length != 0)
            example.number = Integer.parseInt(args[0]);

        example.model();

        if (example.searchMiddle())
            System.out.println("Solution(s) found");

        MagicSquares exampleDual = new MagicSquares();

        if (args.length != 0)
            exampleDual.number = Integer.parseInt(args[0]);

        exampleDual.modelDual();

        if (exampleDual.creditSearch(64, 5000, 10))
            System.out.println("Solution(s) found");

        MagicSquares exampleShave = new MagicSquares();

        if (args.length != 0)
            exampleShave.number = Integer.parseInt(args[0]);

        exampleShave.model4Shaving();

        if (exampleShave.shavingSearch(exampleShave.guidingShaving, true))
            System.out.println("Solution(s) found");

    }



    /**
     * It executes the program which solves the MagicSquare problem.
     *
     * @param args the first argument allows to specify the size of magic square.
     */
    public static void main(String args[]) {

        MagicSquares example = new MagicSquares();

        if (args.length != 0)
            example.number = Integer.parseInt(args[0]);

        example.model();

        if (example.searchMiddle())
            System.out.println("Solution(s) found");

        MagicSquares exampleDual = new MagicSquares();

        if (args.length != 0)
            exampleDual.number = Integer.parseInt(args[0]);

        exampleDual.modelDual();

        if (exampleDual.creditSearch(64, 5000, 10))
            System.out.println("Solution(s) found");

    }


}
