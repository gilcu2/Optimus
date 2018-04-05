/*
 *    /\\\\\
 *   /\\\///\\\
 *  /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 *  /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 *  \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *   \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *     \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *        \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *           \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package optimus.optimization

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra.AlgebraOps._
import optimus.optimization.SolverLib.SolverLib

/**
  * Sudoku is a logic-based combinatorial number-placement puzzle. The objective
  * is to fill a 9×9 grid with digits so that each column, each row, and each of
  * the nine 3×3 sub-grids that compose the grid (also called blocks) contains all
  * of the digits from 1 to 9. The puzzle setter provides a partially completed grid,
  * which for a well-posed puzzle has a unique optimal solution.
  */
trait Sudoku extends FunSpec with Matchers {

  def solver: SolverLib

  describe("9 x 9 Sudoku Problem") {

    implicit val sdkProblem: MIProblem = MIProblem(solver)

    val n = 9
    val N = 0 until n
    val x = Array.tabulate(n, n, n)(
      (l, c, n) => MPIntVar(s"x($l, $c, $n)", 0 to 1)
    )

    maximize(0)

    // each cell must be assigned exactly one integer
    for (l <- N; c <- N)
      add(sum(N)((n) => x(l)(c)(n)) := 1)

    // cells in the same row must be assigned distinct numbers
    for (l <- N; n <- N)
      add(sum(N)((c) => x(l)(c)(n)) := 1)

    // cells in the same column must be assigned distinct numbers
    for (c <- N; n <- N)
      add(sum(N)((l) => x(l)(c)(n)) := 1)

    // cells in the same region must be assigned distinct numbers
    for (l1 <- 0 until 3; c1 <- 0 until 3; n <- N)
      add(sum(0 until 3, 0 until 3)((l, c) => x(l + 3 * l1)(c + 3 * c1)(n)) := 1)

    subjectTo(
      List(
        x(0)(0)(4) := 1.0,
        x(0)(1)(2) := 1.0,
        x(0)(4)(6) := 1.0,
        x(1)(0)(5) := 1.0,
        x(1)(3)(0) := 1.0,
        x(1)(4)(8) := 1.0,
        x(1)(5)(4) := 1.0,
        x(2)(1)(8) := 1.0,
        x(2)(2)(7) := 1.0,
        x(2)(7)(5) := 1.0,
        x(3)(0)(7) := 1.0,
        x(3)(4)(5) := 1.0,
        x(3)(8)(2) := 1.0,
        x(4)(0)(3) := 1.0,
        x(4)(3)(7) := 1.0,
        x(4)(5)(2) := 1.0,
        x(4)(8)(0) := 1.0,
        x(5)(0)(6) := 1.0,
        x(5)(4)(1) := 1.0,
        x(5)(8)(5) := 1.0,
        x(6)(1)(5) := 1.0,
        x(6)(6)(1) := 1.0,
        x(6)(7)(7) := 1.0,
        x(7)(3)(3) := 1.0,
        x(7)(4)(0) := 1.0,
        x(7)(5)(8) := 1.0,
        x(7)(8)(4) := 1.0,
        x(8)(4)(7) := 1.0,
        x(8)(7)(6) := 1.0,
        x(8)(8)(8) := 1.0
      ): _*
    )

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it(s"$solver objective value should be 0") {
      objectiveValue shouldBe 0
    }

    it("all variables should have a value") {
      for (l <- N; c <- N; n <- N)
        x(l)(c)(n).value.isDefined shouldBe true
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution
    for (l <- N) {
      for (c <- N; n <- N; if x(l)(c)(n).value.get == 1) {
        if ((c + 1) % 3 == 0) print(n + 1 + " | ")
        else print(n + 1 + " ")
      }
      println
    }

    release()
  }
}