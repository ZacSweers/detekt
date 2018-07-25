package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Unnecessary `let` TODO
 *
 * <noncompliant>
 * TODO add example
 * </noncompliant>
 *
 * @author mishkun
 */
class UnnecessaryLet(config: Config) : Rule(config) {

	override val issue = Issue(javaClass.simpleName, Severity.Style,
			"The `let` usage is unnecessary", Debt.FIVE_MINS)

	// catches the `let { it.foo() }` and `let { it.baz }` cases
	private val letItRegex = """let\s*\{\s*it\??\.\w+(?:\(.*\))?\s*}""".toRegex()
	// catches the `let { a -> a.foo() }` and `let { a -> a.baz }` cases
	private val letParamRegex = """let\s*\{\s*(\w*)\s*->\s*\1\??\.\w*(?:\(.*\))?\s*}""".toRegex()

	override fun visitCallExpression(expression: KtCallExpression) {
		super.visitCallExpression(expression)
		val isLetIt = expression.text matches letItRegex
		val isLetParam = expression.text matches letParamRegex
		if	(isLetIt || isLetParam) {
			report(CodeSmell(
					issue, Entity.from(expression),
					"let expression can be omitted"
			))
		}
	}
}