/*
 * Believe.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.operate.mental;

import com.google.common.collect.Lists;
import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.io.Symbols;
import nars.nal.BudgetFunctions;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.TruthValue;
import nars.nal.stamp.Stamp;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.ArrayList;

/**
 * Operator that creates a judgment with a given statement
 * Causes the system to belief things it has no evidence for
 */
public class Believe extends Operator implements Mental {

    public Believe() {
        super("^believe");
    }

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
+    * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {

        Compound content = Sentence.termOrException(args[0]);

        TruthValue truth = new TruthValue(1, Global.DEFAULT_JUDGMENT_CONFIDENCE);
        Sentence sentence = new Sentence(content, Symbols.JUDGMENT, truth, new Stamp(operation, memory, Tense.Present));
        float quality = BudgetFunctions.truthToQuality(truth);
        Budget budget = new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, quality);

        return Lists.newArrayList( operation.newSubTask(sentence, budget) );

    }

    @Override
    public boolean isExecutable(Memory mem) {
        return false;
    }
}