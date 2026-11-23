package com.example.domain

import com.example.data.model.AnswerInputMode
import com.example.data.model.MathOperation
import com.example.data.model.QuizGameMode
import kotlin.random.Random

data class QuizConfig(
    val operation: MathOperation = MathOperation.MULTIPLICATION,
    val gameMode: QuizGameMode = QuizGameMode.PRACTICE,
    val focusTable: Int = 0, // 0 = mixed (1..12)
    val questionCount: Int = 10,
    val inputMode: AnswerInputMode = AnswerInputMode.MULTIPLE_CHOICE
)

data class MathQuestion(
    val index: Int,
    val leftOperand: Int,
    val rightOperand: Int,
    val operation: MathOperation, // Always MULTIPLICATION or DIVISION
    val correctAnswer: Int,
    val focusTableNumber: Int, // 1..12 table this question trains
    val choices: List<Int>,
    val hintTextId: String
) {
    val expressionText: String
        get() = "$leftOperand ${operation.symbol} $rightOperand = ?"
}

object QuizGenerator {

    fun generateQuestions(config: QuizConfig, random: Random = Random.Default): List<MathQuestion> {
        val count = config.questionCount.coerceIn(5, 20)
        val questions = mutableListOf<MathQuestion>()
        val usedSignatures = mutableSetOf<String>()

        var attempts = 0
        while (questions.size < count && attempts < count * 12) {
            attempts++
            val op = when (config.operation) {
                MathOperation.MULTIPLICATION -> MathOperation.MULTIPLICATION
                MathOperation.DIVISION -> MathOperation.DIVISION
                MathOperation.MIXED -> if (questions.size % 2 == 0) MathOperation.MULTIPLICATION else MathOperation.DIVISION
            }

            val table = if (config.focusTable in 1..12) {
                config.focusTable
            } else {
                random.nextInt(1, 13)
            }
            val factor = random.nextInt(1, 13)

            val question = if (op == MathOperation.MULTIPLICATION) {
                val swap = config.focusTable == 0 && random.nextBoolean()
                val left = if (swap) factor else table
                val right = if (swap) table else factor
                val answer = left * right
                val sig = "M_${left}_${right}"
                if (sig in usedSignatures && usedSignatures.size < 12) continue
                usedSignatures.add(sig)

                val hint = buildMultiplicationHint(left, right)
                MathQuestion(
                    index = questions.size,
                    leftOperand = left,
                    rightOperand = right,
                    operation = MathOperation.MULTIPLICATION,
                    correctAnswer = answer,
                    focusTableNumber = table,
                    choices = generateChoices(answer, left, right, MathOperation.MULTIPLICATION, random),
                    hintTextId = hint
                )
            } else {
                // Exact integer division: (table * factor) ÷ table = factor
                val dividend = table * factor
                val divisor = table
                val quotient = factor
                val sig = "D_${dividend}_${divisor}"
                if (sig in usedSignatures && usedSignatures.size < 12) continue
                usedSignatures.add(sig)

                val hint = buildDivisionHint(dividend, divisor)
                MathQuestion(
                    index = questions.size,
                    leftOperand = dividend,
                    rightOperand = divisor,
                    operation = MathOperation.DIVISION,
                    correctAnswer = quotient,
                    focusTableNumber = divisor,
                    choices = generateChoices(quotient, dividend, divisor, MathOperation.DIVISION, random),
                    hintTextId = hint
                )
            }
            questions.add(question)
        }
        return questions
    }

    private fun buildMultiplicationHint(left: Int, right: Int): String {
        val repeatCount = minOf(left, right)
        val unitValue = maxOf(left, right)
        return if (repeatCount <= 6) {
            val addition = List(repeatCount) { unitValue.toString() }.joinToString(" + ")
            "$left × $right sama dengan menjumlahkan $unitValue sebanyak $repeatCount kali:\n$addition"
        } else {
            val half = repeatCount / 2
            val remainder = repeatCount - half
            "Pecah jadi mudah: ($half × $unitValue) + ($remainder × $unitValue) = ${half * unitValue} + ${remainder * unitValue}"
        }
    }

    private fun buildDivisionHint(dividend: Int, divisor: Int): String {
        return "Bayangkan ada $dividend bintang yang dibagi sama rata ke $divisor kotak. Atau pikirkan: $divisor × ? = $dividend"
    }

    private fun generateChoices(
        correct: Int,
        left: Int,
        right: Int,
        operation: MathOperation,
        random: Random
    ): List<Int> {
        val candidates = mutableSetOf(correct)

        // Pedagogically plausible distractors for elementary students
        val plausible = if (operation == MathOperation.MULTIPLICATION) {
            listOf(
                correct + left,
                correct - left,
                correct + right,
                correct - right,
                correct + 1,
                correct - 1,
                correct + 2,
                correct - 2,
                correct + 10,
                left + right
            )
        } else {
            listOf(
                correct + 1,
                correct - 1,
                correct + 2,
                correct - 2,
                correct + 3,
                right,
                correct + 4
            )
        }.filter { it > 0 && it != correct }.shuffled(random)

        for (value in plausible) {
            if (candidates.size >= 4) break
            candidates.add(value)
        }

        var offset = 1
        while (candidates.size < 4) {
            val candidate = correct + offset
            if (candidate > 0) candidates.add(candidate)
            offset++
        }

        return candidates.toList().shuffled(random)
    }
}
