/*
 * MinizincBasedTestAbove1Hours.java
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

package org.jacop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

/**
 * Test class for running problems above 1hour. It is not really used except to
 * see if some problems are now being executed faster.
 *
 * @author Mariusz Świerkot and Radoslaw Szymanek
 * @version 4.10
 */
@RunWith(Parameterized.class) public class MinizincBasedTestAbove1Hours extends MinizincBasedTestsHelper {
    protected static final String timeCategory = "above1hour/";

    public MinizincBasedTestAbove1Hours(String testFilename) {
        super(timeCategory);
        this.testFilename = testFilename;

    }

    @Parameterized.Parameters public static Collection<String> parametricTest() throws IOException {

        return fileReader(timeCategory);
    }

    @Test() public void testMinizinc() throws IOException {

        testExecution(timeCategory);
    }



}
