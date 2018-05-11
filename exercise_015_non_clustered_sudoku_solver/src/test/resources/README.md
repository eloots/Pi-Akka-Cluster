non_clustered_sudoku_solver

# Non-clustered Sudoku Solver

In this step, we add a standalone Sudoku solver. The solver is implemented in using Actors.

One of the goals of this implementation was to demonstrate that a solver can be implemented
without keeping any mutable state.

Have a look at the following files in the `images` folder in the root of the source repo:

- `sudokuActors.png`
- `sudokuDecomposition.png`
- `SudokuDetail.pdf`

A number of sample Sudoku puzzlers can be found in the `sudokus` folder in the root of the
source repo.

To solve on or more of these puzzles, execute the `run` command in sbt and pass one or
or more of the puzzle files. As an example:

```
➜  Pi-Akka-Cluster git:(add-non-clustered-sudoku-solver) ✗ sbt
[info] Loading settings from lightbend.sbt,plugins.sbt ...
[info] Loading project definition from /Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/project
[info] Loading settings from build.sbt ...
[info] Set current project to pi_cluster_master (in build file:/Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/)
[info] Set current project to exercise_000_initial_state (in build file:/Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/)
[info] sbt server started at local:///Users/ericloots/.sbt/1.0/server/a48ba8e55a238e4bf42a/sock
man [e] > mini-cluster > initial-state > project exercise_015_non_clustered_sudoku_solver
[info] Set current project to exercise_015_non_clustered_sudoku_solver (in build file:/Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/)
man [e] > mini-cluster > non_clustered_sudoku_solver > run sudokus/dm171125-3.sudoku sudokus/dm171203-03.sudoku
[info] Packaging /Users/ericloots/Trainingen/LightbendTraining/Pi-Akka-Cluster/exercise_015_non_clustered_sudoku_solver/target/scala-2.12/exercise_015_non_clustered_sudoku_solver_2.12-1.3.0.jar ...
[info] Done packaging.
[info] Running org.globalminimum.sudoku.SudokuMain sudokus/dm171125-3.sudoku sudokus/dm171203-03.sudoku
Running solver for sudokus/dm171125-3.sudoku
Running solver for sudokus/dm171203-03.sudoku
Result ~~>
   Vector(Set(6), Set(5), Set(7), Set(3), Set(8), Set(2), Set(1), Set(9), Set(4))
   Vector(Set(4), Set(8), Set(9), Set(5), Set(1), Set(6), Set(3), Set(7), Set(2))
   Vector(Set(1), Set(3), Set(2), Set(7), Set(9), Set(4), Set(5), Set(8), Set(6))
   Vector(Set(7), Set(6), Set(4), Set(9), Set(3), Set(5), Set(2), Set(1), Set(8))
   Vector(Set(9), Set(2), Set(8), Set(6), Set(4), Set(1), Set(7), Set(5), Set(3))
   Vector(Set(3), Set(1), Set(5), Set(8), Set(2), Set(7), Set(4), Set(6), Set(9))
   Vector(Set(2), Set(4), Set(6), Set(1), Set(5), Set(8), Set(9), Set(3), Set(7))
   Vector(Set(8), Set(9), Set(1), Set(2), Set(7), Set(3), Set(6), Set(4), Set(5))
   Vector(Set(5), Set(7), Set(3), Set(4), Set(6), Set(9), Set(8), Set(2), Set(1))
Done !
Result ~~>
   Vector(Set(8), Set(6), Set(4), Set(5), Set(2), Set(1), Set(9), Set(3), Set(7))
   Vector(Set(2), Set(7), Set(5), Set(3), Set(6), Set(9), Set(1), Set(8), Set(4))
   Vector(Set(9), Set(3), Set(1), Set(7), Set(4), Set(8), Set(6), Set(2), Set(5))
   Vector(Set(3), Set(9), Set(8), Set(1), Set(7), Set(5), Set(2), Set(4), Set(6))
   Vector(Set(5), Set(2), Set(7), Set(6), Set(9), Set(4), Set(3), Set(1), Set(8))
   Vector(Set(4), Set(1), Set(6), Set(2), Set(8), Set(3), Set(7), Set(5), Set(9))
   Vector(Set(7), Set(4), Set(2), Set(8), Set(3), Set(6), Set(5), Set(9), Set(1))
   Vector(Set(1), Set(8), Set(3), Set(9), Set(5), Set(7), Set(4), Set(6), Set(2))
   Vector(Set(6), Set(5), Set(9), Set(4), Set(1), Set(2), Set(8), Set(7), Set(3))
Done !
[success] Total time: 3 s, completed May 11, 2018 8:25:01 PM
man [e] > mini-cluster > non_clustered_sudoku_solver >
```