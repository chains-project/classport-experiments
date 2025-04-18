/*
 * ProAndCon.java
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
 * It solves a simple logic puzzle about voting city council.
 *
 * @author Romam Gawelek, Marcin Kazmierczak, Radoslaw Szymanek
 * @version 4.10
 *          <p>
 *          Logic Puzzle - Pro and Con
 *          Author: Monica Tenniel
 *          Publication: Dell Logic Puzzles
 *          Issue: April, 1998
 *          Page: 12
 *          Stars: 2
 *          At the last meeting of the local city council, each member
 *          (Mr. Akerman, Ms. Baird, Mr. Chatham, Ms. Duval, and Mr. Etting)
 *          had to vote on five motions, number 1 to 5 in the clues below.
 *          Can you discover how each one voted on each motion?
 *          <p>
 *          Note: a motion may have received zero or one yes vote, even though
 *          in real life it's unlikely that both the maker and seconder of
 *          the motion would change their minds before the motion came up for a vote.
 *          Each member voted either yes or no on each motion; no one abstained
 *          from voting on any motion.
 *          <p>
 *          Voting Chart
 *          (view with non-proportional fonts)
 *          <p>
 *          1     2     3     4     5
 *          Mr. Akerman
 *          Ms. Baird
 *          Mr. Chatham
 *          Ms. Duval
 *          Mr. Etting
 *          <p>
 *          1. Each motion got a different number of yes votes.
 *          2. In all, the five motions got three more yes votes than no votes.
 *          3. No two council members voted the same way on all five motions.
 *          4. The two women disagreed in their voting more often than they agreed.
 *          5. Mr. Chatham never made two yes votes on consecutive motions.
 *          6. Mr. Akerman and Ms. Baird both voted in favor of motion 4.
 *          7. Motion 1 received two more yes votes than motion 2 did.
 *          8. Motion 3 received twice as many yes votes as motion 4 did.
 *          <p>
 *          Determine: fill in the chart (Yes/No) for each motion
 */

public class ProAndCon extends ExampleFD {

    @Override public void model() {

        store = new Store();
        vars = new ArrayList<IntVar>();

        String surname[] = {"Mr._Akerman", "Ms._Baird", "Mr._Chatham", "Ms._Duval", "Mr._Etting"};

        int iAkerman = 0, iBaird = 1, iChatham = 2, iDuval = 3, iEtting = 4;
        int iMotion1 = 0, iMotion2 = 1, iMotion3 = 2, iMotion4 = 3, iMotion5 = 4;

        // Votes are encoded as two dimensional array - one index for person,
        // one index for group
        IntVar vote[][] = new IntVar[5][5];
        // Sums votes for each group
        IntVar sum4Group[] = new IntVar[5];

        for (int i = 0; i < 5; i++) {
            // Each motion (group of votes) has no of yes votes associated.
            sum4Group[i] = new IntVar(store, "Sum4Group[" + (i + 1) + "]", 0, 5);
            vars.add(sum4Group[i]);

            // 0 - vote no , 1 - vote yes
            for (int j = 0; j < 5; j++) {
                vote[i][j] = new IntVar(store, surname[i] + "_Group[" + (j + 1) + "]", 0, 1);
                vars.add(vote[i][j]);
            }

        }

        // Sum constraint for each group
        List<IntVar> votesMotion1 = new ArrayList<IntVar>();
        for (int i = 0; i < 5; i++)
            votesMotion1.add(vote[i][iMotion1]);
        store.impose(new SumInt(votesMotion1, "==", sum4Group[iMotion1]));

        List<IntVar> votesMotion2 = new ArrayList<IntVar>();
        for (int i = 0; i < 5; i++)
            votesMotion2.add(vote[i][iMotion2]);
        store.impose(new SumInt(votesMotion2, "==", sum4Group[iMotion2]));

        List<IntVar> votesMotion3 = new ArrayList<IntVar>();
        for (int i = 0; i < 5; i++)
            votesMotion3.add(vote[i][iMotion3]);
        store.impose(new SumInt(votesMotion3, "==", sum4Group[iMotion3]));

        List<IntVar> votesMotion4 = new ArrayList<IntVar>();
        for (int i = 0; i < 5; i++)
            votesMotion4.add(vote[i][iMotion4]);
        store.impose(new SumInt(votesMotion4, "==", sum4Group[iMotion4]));

        List<IntVar> votesMotion5 = new ArrayList<IntVar>();
        for (int i = 0; i < 5; i++)
            votesMotion5.add(vote[i][iMotion5]);
        store.impose(new SumInt(votesMotion5, "==", sum4Group[iMotion5]));

        // Clues enconding

        // 1. Each motion got a different number of yes votes.

        store.impose(new Alldifferent(sum4Group));

        // 2. In all, the five motions got three more yes votes than no votes.

        // We sum all yes votes and no votes.

        IntVar noYesVotes = new IntVar(store, "noYesVotes", 1, 25);
        IntVar noNoVotes = new IntVar(store, "noNoVotes", 1, 25);
        IntVar noVotes = new IntVar(store, "25", 25, 25);

        vars.add(noYesVotes);
        vars.add(noNoVotes);
        vars.add(noVotes);

        // We constraint number of yes votes.
        store.impose(new SumInt(sum4Group, "==", noYesVotes));
        // To connect no of yes votes with no of no votes.
        store.impose(new XplusYeqZ(noYesVotes, noNoVotes, noVotes));

        store.impose(new XplusCeqZ(noNoVotes, 3, noYesVotes));

        // 3. No two council members voted the same way on all five motions.

        // To represent the uniqueness of binary vector, we will use weighted
        // sum constraint.
        // We transform unique binary vectors into unique numbers on which we
        // can impose all different.

        int weights[] = {1, 2, 4, 8, 16};

        IntVar weightedVotes[] = new IntVar[5];
        for (int i = 0; i < 5; i++)
            weightedVotes[i] = new IntVar(store, "weightedVotes4" + surname[i], 1, 32);

        store.impose(new LinearInt(vote[iAkerman], weights, "==", weightedVotes[iAkerman]));
        // store.impose(new SumWeight(vote[iAkerman], weights,
        // 		weightedVotes[iAkerman]));
        store.impose(new LinearInt(vote[iBaird], weights, "==", weightedVotes[iBaird]));
        // store.impose(new SumWeight(vote[iBaird], weights,
        // 				weightedVotes[iBaird]));
        store.impose(new LinearInt(vote[iChatham], weights, "==", weightedVotes[iChatham]));
        // store.impose(new SumWeight(vote[iChatham], weights,
        // 		weightedVotes[iChatham]));
        store.impose(new LinearInt(vote[iDuval], weights, "==", weightedVotes[iDuval]));
        // store.impose(new SumWeight(vote[iDuval], weights,
        // 				weightedVotes[iDuval]));
        store.impose(new LinearInt(vote[iEtting], weights, "==", weightedVotes[iEtting]));
        // store.impose(new SumWeight(vote[iEtting], weights,
        // 		weightedVotes[iEtting]));

        // All weightes votes must be different.
        store.impose(new Alldifferent(weightedVotes));

        // 4. The two women disagreed in their voting more often than they
        // agreed.
        // Women are people no 2 and 4 (Baird and Duval).

        // We will use Reified constraints to encode this clue.

        IntVar reified[] = new IntVar[5];
        for (int i = 0; i < 5; i++) {
            reified[i] = new IntVar(store, "agreeOnVote" + i, 0, 1);
            store.impose(new Reified(new XeqY(vote[iBaird][i], vote[iDuval][i]), reified[i]));
        }

        // There are 5 votes for each person, this means that no of yes votes is
        // at most 2.
        IntVar sumOfReified = new IntVar(store, "noAgreeBairdAndDuval", 1, 5);
        store.impose(new SumInt(reified, "==", sumOfReified));
        store.impose(new XlteqC(sumOfReified, 2));

        // 5. Mr. Chatham never made two yes votes on consecutive motions.

        // implied constraint, the sum must be smaller than 4.
        IntVar sumChatham = new IntVar(store, "sumChatham", 0, 3);
        store.impose(new SumInt(vote[iChatham], "==", sumChatham));

        // We take each pair and make sure they are not two yes votes
        IntVar two = new IntVar(store, "2", 2, 2);
        for (int i = 0; i < 4; i++)
            store.impose(new XplusYlteqZ(vote[iChatham][i], vote[iChatham][i + 1], two));

        // 6. Mr. Akerman and Ms. Baird both voted in favor of motion 4.
        store.impose(new XeqC(vote[iAkerman][3], 1));
        store.impose(new XeqC(vote[iBaird][3], 1));

        // 7. Motion 1 received two more yes votes than motion 2 did.
        store.impose(new XplusCeqZ(sum4Group[iMotion2], 2, sum4Group[iMotion1]));

        // 8. Motion 3 received twice as many yes votes as motion 4 did.
        store.impose(new XmulCeqZ(sum4Group[iMotion4], 2, sum4Group[iMotion3]));

    }

    /**
     * It executes the program which solves this logic puzzle.
     *
     * @param args command arguments (none here)
     */
    public static void main(String args[]) {

        ProAndCon example = new ProAndCon();

        example.model();

        if (example.search())
            System.out.println("Solution(s) found");

    }


}
