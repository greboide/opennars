# inference on goals and actions

# Introduction

Every intelligent system needs a mechanism to achieve various goals. In NARS, this function is mainly carried out as *procedural inference*, which is inference on beliefs, events, goals, and actions.

In NARS, the system's beliefs are represented as *Judgments*, each of which is a Narsese sentence, containing a Term as content, and a TruthValue indicating the evidential support to the content. An *event* is a special type of term whose truth-value is defined only during a certain period of time.

A *Goal* is similar to a *Judgment*, except that its content is an event and the "TruthValue" in it is not really the truth-value of the content, but its "degree of desire", or "desire-value", which is defined as the truth-value for the event to imply a virtual "desired situation" (see **1**, page 142).

An *Action* is what the system can do to cause changes in the outside (or inside) environment. Logically, it is an event that can be "realized", i.e., can be made to happen, by the system.

As a general-purpose system, NARS can have any belief or goal, as far as its content can be expressed in Narsese, and they change from time to time as the system runs and interacts with its environment. On the contrary, in any concrete implementation of NARS, all of its actions are composed recursively from operations, with a constant set of built-in *Operators*.

# Actions

Each operation consists of an operator and an argument list, like {{{Op(A1, ..., An)}}}. From a logical point of view, it is a statement {{{<(*, Self, A1, ..., An) {-- Op>}}}, where the subject is a product indicating a temporal relation between the system itself and the arguments, the predicate is the type of the relation, and the relation is *Instance*, since operations are countable.

In the current implementation, adding an operator into the system means
- to add one line in nars.operation.Operator to register it to the system,
- to define a class in nars.operation that extends Operator, with an "execute" method to invoke external "sensorimotor" mechanism that carries out the operation on the given arguments.
In the current code, there are 3 dummy classes in nars.operation for testing purpose.

In this way, in the future NARS can serves as the mind of a robot, or a software agent that uses various software and hardware as tools. Ideally, only the nars.operation package needs to be changed when NARS is equipped with different sensorimotor mechanism. Please note that in NARS, sensation/perception will be carried out by certain actions, so the nars.operation package handles both input and output through sensorimotor.

To the system, the meaning of (the content of) beliefs, goals, and actions are revealed by their relations with other terms, except that for actions it also include their relations with sensorimotor, which can not be fully expressed in Narsese. Especially, the meaning each truth-bearing term is mainly revealed by its sufficient conditions and necessary conditions, some of them have temporal attribute. For each action, its preconditions and consequences are represented mainly by implication and equivalence statements about the action (see **1**, page 140).

To make compound actions from component actions, the two major term operators are "sequential conjunction" (",") and "parallel conjunction" (";") (see **1**, page 136). When applied to actions, they mean "sequential execution" and "parallel execution", respectively. Combined with sufficient conditions and necessary conditions, they can form all kinds of "programs" from the "instructions" provided by the operators.

# Goals

At any moment, the system normally has multiple goals to be achieved. They are processed in parallel in a time-sharing manner. There are two types of goal: *original* goals are whose directly imposed on the system by its environment; *derived* goals are produced by the system itself from goals and beliefs via backward inference (see **1**, page 143). The two types of goals are treated by the system as the same for almost all purposes.

When a new (original or derived) goal arrives, the system does not immediately start to find ways to achieve it. Instead, it is preprocessed in the corresponding concept, where the following factors are considered:
1. The desire-value of the goal is adjusted according to its relation with other goals. When the same goal get several different desire-values from different sources, the revision rule is used to get an overall desire-value, which indicates whether the system really desires it, when all available evidence is taken into consideration.
1. The truth value of the content if the goal is checked. If the goal is already achieved, the system does not need to do anything. The *expectation* value of the content is used as the "degree of satisfaction" of the goal.
1. The plausibility of the goal is estimated, that is, whether the system knows an approach to achieve the goal. In this evaluation, the details of the approach is omitted.

This preprocessing of goal is a decision making process, by which the system reaches the decision on whether to actively look for a way to achieve the goal. As a special case, if the goal is an operation, the process will decide whether to actually execute it. If a goal passed this stage, it will get a budget value, and be worked upon in the following time, based on its budget-value.

In this way, the system doesn't treat each goal by itself, but attempts to reach an overall optimal solution for all of its tasks, by compromising among their requirements.

Beside directly achieving goal G, the system can also process the following questions:
- "G ==> ?x", whose answers reveal the consequences of G, and contribute to the desire-value of G;
- "?x ==> G", whose answers reveal plans for achieving G, and contribute to the plausibility of G.

# Inference Steps

The inference rules work on goals and operations in the same ways as they works on events in beliefs.

## Reference

**1** Pei Wang, Rigid Flexibility: The Logic of Intelligence, Springer, 2006. http://www.springer.com/west/home/computer/artificial?SGWID=4-147-22-173659733-0 

**2** Pei Wang, Non-Axiomatic Logic: A Model of Intelligent Reasoning, 2013.
http://www.worldscientific.com/worldscibooks/10.1142/8665