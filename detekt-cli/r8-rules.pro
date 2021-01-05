-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-keep class io.gitlab.arturbosch.detekt.cli.Main {
  public static final void main(java.lang.String[]);
}

-keep interface io.gitlab.arturbosch.detekt.api.RuleSetProvider
-keep class * implements io.gitlab.arturbosch.detekt.api.RuleSetProvider {
  java.lang.String getRuleSetId();
  io.gitlab.arturbosch.detekt.api.RuleSet instance(io.gitlab.arturbosch.detekt.api.Config);
}

-keep interface io.github.detekt.tooling.api.DetektCli
-keep class * implements io.github.detekt.tooling.api.DetektCli {
  io.github.detekt.tooling.api.AnalysisResult run(...);
}
